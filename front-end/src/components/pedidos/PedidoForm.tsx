import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createPedido,
  getClientes,
  getProdutos,
  getEnderecosCliente,
  criarEndereco,
  consultarCep,
  atualizarEndereco,
} from "@/lib/api";
import type {
  FormaPagamento,
  TipoPedido,
  ItemPedidoDTO,
  EnderecoRequestDTO,
  EnderecoResponseDTO,
  CepResponseDTO,
} from "@/types/api";
import { formaPagamentoLabels, tipoPedidoLabels } from "@/types/api";
import { toast } from "sonner";

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Plus, Trash2, MapPin, Edit3 } from "lucide-react";

const schema = z
  .object({
    idCliente: z.coerce.number().optional(),
    formaPagamento: z.string().min(1, "Selecione forma de pagamento"),
    tipoPedido: z.string().min(1, "Selecione o tipo"),
    observacao: z.string().optional(),
    enderecoEntregaId: z.number().optional(),
  })
  .superRefine((data, ctx) => {
    if (data.tipoPedido !== "local" && !data.idCliente) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["idCliente"],
        message: "Selecione um cliente",
      });
    }

    if (data.tipoPedido === "delivery" && !data.enderecoEntregaId) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["enderecoEntregaId"],
        message: "Selecione um endereço de entrega",
      });
    }
  });

type FormValues = z.infer<typeof schema>;

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function PedidoForm({ open, onOpenChange }: Props) {
  const qc = useQueryClient();

  const clientesData = useQuery({
    queryKey: ["clientes"],
    queryFn: getClientes,
  });

  const produtosData = useQuery({
    queryKey: ["produtos"],
    queryFn: getProdutos,
  });

  const produtosAtivos =
    produtosData.data?.filter((p) => p.ativo !== false) ?? [];

  const [itens, setItens] = useState<
    { idProduto: number; quantidade: number }[]
  >([]);
  const [enderecos, setEnderecos] = useState<EnderecoResponseDTO[]>([]);
  const [enderecoSelecionadoId, setEnderecoSelecionadoId] = useState<
    number | null
  >(null);
  const [mostrarFormularioEndereco, setMostrarFormularioEndereco] =
    useState(false);
  const [loadingCep, setLoadingCep] = useState(false);
  const [enderecoForm, setEnderecoForm] = useState<Partial<EnderecoRequestDTO>>(
    {},
  );
  const [enderecoEmEdicao, setEnderecoEmEdicao] =
    useState<EnderecoResponseDTO | null>(null);

  const handleEditarEndereco = () => {
    if (!enderecoSelecionadoId) return;

    const endereco = enderecos.find(
      (e) => e.idEndereco === enderecoSelecionadoId,
    );
    if (endereco) {
      // ← CARREGA TODOS OS DADOS NO STATE
      setEnderecoForm({
        descricao: endereco.descricao || "",
        cep: endereco.cep || "",
        logradouro: endereco.logradouro || "",
        numero: endereco.numero?.toString() || "",
        complemento: endereco.complemento || "",
        bairro: endereco.bairro || "",
        cidade: endereco.cidade || "",
        uf: endereco.uf || "", // ou UF.SC se for enum
        referencia: endereco.referencia || "",
      });
      setEnderecoEmEdicao(endereco);
      setMostrarFormularioEndereco(true);
    }
  };

  const salvarEndereco = async () => {
    const idCliente = form.getValues("idCliente");
    if (!idCliente) return;

    try {
      if (enderecoEmEdicao) {
        // Modo EDIÇÃO
        await atualizarEndereco(
          idCliente,
          enderecoEmEdicao.idEndereco,
          enderecoForm as EnderecoRequestDTO,
        );
        toast.success("Endereço atualizado!");
      } else {
        // Modo CRIAÇÃO
        await salvarNovoEndereco();
        return;
      }

      setMostrarFormularioEndereco(false);
      setEnderecoForm({});
      setEnderecoEmEdicao(null);
      // Recarregar lista
      getEnderecosCliente(idCliente).then((data) => setEnderecos(data.data));
    } catch (error: unknown) {
      console.error("Erro completo:", error);

      if (error instanceof Error) {
        console.error("Mensagem:", error.message);
        toast.error(`Erro: ${error.message}`);
      } else if (
        typeof error === "object" &&
        error !== null &&
        "response" in error
      ) {
        const axiosError = error as {
          response?: { data?: { message?: string } };
        };
        console.error("Response data:", axiosError.response?.data);
        toast.error(
          `Erro: ${axiosError.response?.data?.message ?? "Erro desconhecido"}`,
        );
      } else {
        toast.error("Erro desconhecido");
      }
    }
  };
  const cancelarEdicao = () => {
    setMostrarFormularioEndereco(false);
    setEnderecoForm({});
    setEnderecoEmEdicao(null);
  };

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      idCliente: 0,
      formaPagamento: "",
      tipoPedido: "",
      observacao: "",
      enderecoEntregaId: undefined,
    },
  });

  const tipoPedido = form.watch("tipoPedido");
  const isLocal = tipoPedido === "local";
  const isDelivery = tipoPedido === "delivery";

  useEffect(() => {
    if (isLocal) {
      form.setValue("idCliente", undefined);
      form.setValue("enderecoEntregaId", undefined);
      setEnderecos([]);
      setEnderecoSelecionadoId(null);
    }
  }, [isLocal, form]);

  // Carregar endereços quando cliente e tipoPedido mudam
  useEffect(() => {
    const idCliente = form.watch("idCliente");

    if (idCliente && tipoPedido === "delivery") {
      getEnderecosCliente(idCliente).then((res) => {
        setEnderecos(res.data);
        // Pré-selecionar principal se existir
        const principal = res.data.find((e) => e.principal);
        if (principal) {
          setEnderecoSelecionadoId(principal.idEndereco);
          form.setValue("enderecoEntregaId", principal.idEndereco);
        }
      });
    } else {
      setEnderecos([]);
      setEnderecoSelecionadoId(null);
      form.setValue("enderecoEntregaId", undefined);
    }
  }, [form.watch("idCliente"), form.watch("tipoPedido")]);

  const addItem = () => {
    setItens([...itens, { idProduto: 0, quantidade: 1 }]);
  };

  const removeItem = (i: number) => {
    setItens(itens.filter((_, idx) => idx !== i));
  };

  const updateItem = (i: number, field: keyof ItemPedidoDTO, value: number) => {
    if (field === "idProduto") {
      const existingIndex = itens.findIndex(
        (item, idx) => idx !== i && item.idProduto === value,
      );

      if (existingIndex >= 0) {
        const mergedQuantity =
          itens[existingIndex].quantidade + itens[i].quantidade;
        const nextItens = itens.filter((_, idx) => idx !== i);
        nextItens[existingIndex > i ? existingIndex - 1 : existingIndex] = {
          ...nextItens[existingIndex > i ? existingIndex - 1 : existingIndex],
          quantidade: mergedQuantity,
        };
        setItens(nextItens);
        return;
      }
    }

    const copy = [...itens];
    copy[i] = { ...copy[i], [field]: value };
    setItens(copy);
  };

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const validItens = itens.filter(
        (i) => i.idProduto !== 0 && i.quantidade !== 0,
      );
      if (validItens.length === 0) {
        throw new Error("Adicione pelo menos 1 item");
      }
      return createPedido({
        idCliente: values.tipoPedido === "local" ? undefined : values.idCliente,
        formaPagamento: values.formaPagamento as FormaPagamento,
        tipoPedido: values.tipoPedido as TipoPedido,
        observacao: values.observacao ?? undefined,
        enderecoEntregaId:
          values.tipoPedido === "delivery"
            ? values.enderecoEntregaId
            : undefined,
        itens: validItens as ItemPedidoDTO[],
      });
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["pedidos"] });
      toast.success("Pedido criado!");
      onOpenChange(false);
      form.reset();
      setItens([]);
      setEnderecoSelecionadoId(null);
    },
    onError: (e: unknown) => {
      toast.error((e as Error)?.message ?? "Erro ao criar pedido");
    },
  });

  const total = itens.reduce((acc, item) => {
    const prod = produtosAtivos.find((p) => p.idProduto === item.idProduto);
    return acc + (prod?.preco ?? 0) * item.quantidade;
  }, 0);

  // Handlers de endereço
  const handleCepChange = async (cep: string) => {
    if (cep.length !== 8) return;

    setLoadingCep(true);
    try {
      const res = await consultarCep(cep);
      if (res.data.encontrado) {
        // ← SÓ sobrescreve se campos estiverem vazios (edição)
        setEnderecoForm((prev) => ({
          ...prev,
          cep: res.data.cep,
          ...(prev.logradouro ? {} : { logradouro: res.data.logradouro }),
          ...(prev.bairro ? {} : { bairro: res.data.bairro }),
          ...(prev.cidade ? {} : { cidade: res.data.cidade }),
          ...(prev.uf ? {} : { uf: res.data.uf }),
        }));
      }
    } catch {
      toast.error("CEP não encontrado");
    } finally {
      setLoadingCep(false);
    }
  };

  const salvarNovoEndereco = async () => {
    const idCliente = form.getValues("idCliente");
    if (!idCliente) return;

    try {
      const res = await criarEndereco(
        idCliente,
        enderecoForm as EnderecoRequestDTO,
      );
      toast.success("Endereço salvo!");
      setMostrarFormularioEndereco(false);
      setEnderecoForm({});
      // Recarregar lista
      getEnderecosCliente(idCliente).then((data) => setEnderecos(data.data));
      setEnderecoSelecionadoId(res.data.idEndereco);
      form.setValue("enderecoEntregaId", res.data.idEndereco);
    } catch {
      toast.error("Erro ao salvar endereço");
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Novo Pedido</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={form.handleSubmit((v) => mutation.mutate(v))}
            className="space-y-4"
          >
            {/* Cliente */}
            {!isLocal && (
              <FormField
                control={form.control}
                name="idCliente"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Cliente</FormLabel>
                    <Select
                      onValueChange={(value) => field.onChange(Number(value))}
                      value={field.value ? field.value.toString() : undefined}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Selecionar cliente" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {clientesData.data?.map((c) => (
                          <SelectItem
                            key={c.idCliente}
                            value={c.idCliente.toString()}
                          >
                            {c.nome}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}

            {/* Grid: Pagamento + Tipo */}
            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="formaPagamento"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Pagamento</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Forma" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {Object.entries(formaPagamentoLabels).map(([k, v]) => (
                          <SelectItem key={k} value={k}>
                            {v}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="tipoPedido"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tipo</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Tipo" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {Object.entries(tipoPedidoLabels).map(([k, v]) => (
                          <SelectItem key={k} value={k}>
                            {v}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              {/*         {isLocal && (
                <div className="space-y-1">
                  <p className="text-sm text-muted-foreground">
                    Pedidos para consumo no local usam o cliente padrão "CONSUMO
                    NO LOCAL".
                  </p>
                </div>
              )} */}
            </div>

            {/* Bloco de Endereço - só para DELIVERY */}
            {isDelivery && (
              <div className="space-y-2 p-4 border rounded-md">
                <FormField
                  control={form.control}
                  name="enderecoEntregaId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Endereço de entrega</FormLabel>
                      <Select
                        onValueChange={(v) => {
                          field.onChange(Number(v));
                          setEnderecoSelecionadoId(Number(v) || null);
                        }}
                        value={field.value?.toString() || ""}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Selecione endereço" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {enderecos.map((e) => (
                            <SelectItem
                              key={e.idEndereco}
                              value={e.idEndereco.toString()}
                            >
                              <div className="flex items-center space-x-2">
                                <MapPin className="h-4 w-4" />
                                {e.descricao}
                                {e.principal && (
                                  <span className="text-xs bg-primary text-primary-foreground px-2 py-0.5 rounded-full">
                                    Principal
                                  </span>
                                )}
                              </div>
                              <div className="text-xs text-muted-foreground">
                                {e.logradouro}, {e.numero} - {e.bairro}
                              </div>
                            </SelectItem>
                          ))}
                          <SelectItem value="novo">
                            <div className="flex items-center space-x-2">
                              <Plus className="h-4 w-4" />
                              Novo endereço
                            </div>
                          </SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <div className="flex gap-2 pt-2">
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    onClick={() => setMostrarFormularioEndereco(true)}
                  >
                    <Plus className="h-3 w-3 mr-1" />
                    Novo endereço
                  </Button>
                  {enderecoSelecionadoId && (
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      onClick={handleEditarEndereco} // ← AGORA FUNCIONA
                    >
                      <Edit3 className="h-3 w-3 mr-1" />
                      Editar
                    </Button>
                  )}
                </div>
              </div>
            )}

            {/* Observação */}
            <FormField
              control={form.control}
              name="observacao"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Observação</FormLabel>
                  <FormControl>
                    <Textarea placeholder="Opcional..." {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Itens */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Itens do Pedido</span>
                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  onClick={addItem}
                >
                  <Plus className="h-3 w-3 mr-1" />
                  Adicionar
                </Button>
              </div>

              {itens.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                  Nenhum item adicionado
                </p>
              ) : (
                itens.map((item, i) => (
                  <div key={i} className="flex gap-2 items-end">
                    <div className="flex-1">
                      <Select
                        value={item.idProduto.toString()}
                        onValueChange={(v) =>
                          updateItem(i, "idProduto", Number(v))
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Produto" />
                        </SelectTrigger>
                        <SelectContent>
                          {produtosAtivos.map((p) => (
                            <SelectItem
                              key={p.idProduto}
                              value={p.idProduto.toString()}
                              disabled={p.quantidadeEstoque <= 0}
                            >
                              {p.nome} - R$ {p.preco?.toFixed(2)}
                              {p.quantidadeEstoque <= 0 ? " (Sem estoque)" : ""}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div>
                      <Input
                        type="number"
                        min={1}
                        className="w-20"
                        value={item.quantidade}
                        onChange={(e) =>
                          updateItem(i, "quantidade", Number(e.target.value))
                        }
                      />
                    </div>
                    <Button
                      type="button"
                      size="icon"
                      variant="ghost"
                      onClick={() => removeItem(i)}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                ))
              )}

              {itens.length > 0 && (
                <p className="text-sm font-semibold text-right">
                  Total estimado: R$ {total.toFixed(2)}
                </p>
              )}
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={mutation.isPending}>
                {mutation.isPending ? "Criando..." : "Criar Pedido"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>

      <Dialog
        open={mostrarFormularioEndereco}
        onOpenChange={setMostrarFormularioEndereco}
      >
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>
              {enderecoEmEdicao ? "Editar Endereço" : "Novo Endereço"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium block mb-1">CEP</label>
              <Input
                placeholder="89010025"
                value={enderecoForm.cep || ""}
                onChange={(e) => {
                  setEnderecoForm({ ...enderecoForm, cep: e.target.value });
                  handleCepChange(e.target.value);
                }}
              />
              {loadingCep && (
                <p className="text-xs text-muted-foreground mt-1">
                  Consultando...
                </p>
              )}
            </div>

            <div>
              <label className="text-sm font-medium block mb-1">
                Logradouro
              </label>
              <Input
                placeholder="Rua XV de Novembro"
                value={enderecoForm.logradouro || ""}
                onChange={(e) =>
                  setEnderecoForm({
                    ...enderecoForm,
                    logradouro: e.target.value,
                  })
                }
              />
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="text-sm font-medium block mb-1">Número</label>
                <Input
                  placeholder="123"
                  value={enderecoForm.numero || ""}
                  onChange={(e) =>
                    setEnderecoForm({ ...enderecoForm, numero: e.target.value })
                  }
                />
              </div>
              <div>
                <label className="text-sm font-medium block mb-1">
                  Complemento
                </label>
                <Input
                  placeholder="Apto 201"
                  value={enderecoForm.complemento || ""}
                  onChange={(e) =>
                    setEnderecoForm({
                      ...enderecoForm,
                      complemento: e.target.value,
                    })
                  }
                />
              </div>
            </div>

            <div>
              <label className="text-sm font-medium block mb-1">Bairro</label>
              <Input
                placeholder="Centro"
                value={enderecoForm.bairro || ""}
                onChange={(e) =>
                  setEnderecoForm({ ...enderecoForm, bairro: e.target.value })
                }
              />
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="text-sm font-medium block mb-1">Cidade</label>
                <Input
                  placeholder="Blumenau"
                  value={enderecoForm.cidade || ""}
                  onChange={(e) =>
                    setEnderecoForm({ ...enderecoForm, cidade: e.target.value })
                  }
                />
              </div>
              <div>
                <label className="text-sm font-medium block mb-1">UF</label>
                <Input
                  placeholder="SC"
                  maxLength={2}
                  value={enderecoForm.uf || ""}
                  onChange={(e) =>
                    setEnderecoForm({
                      ...enderecoForm,
                      uf: e.target.value.toUpperCase(),
                    })
                  }
                />
              </div>
            </div>

            <div>
              <label className="text-sm font-medium block mb-1">
                Referência
              </label>
              <Input
                placeholder="Próximo à prefeitura"
                value={enderecoForm.referencia || ""}
                onChange={(e) =>
                  setEnderecoForm({
                    ...enderecoForm,
                    referencia: e.target.value,
                  })
                }
              />
            </div>

            <div>
              <label className="text-sm font-medium block mb-1">
                Descrição
              </label>
              <Input
                placeholder="Casa, Trabalho"
                value={enderecoForm.descricao || ""}
                onChange={(e) =>
                  setEnderecoForm({
                    ...enderecoForm,
                    descricao: e.target.value,
                  })
                }
              />
            </div>
          </div>

          <DialogFooter className="gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={cancelarEdicao} // ← CANCELA edição ou criação
            >
              Cancelar
            </Button>
            <Button onClick={salvarEndereco}>
              {" "}
              {/* ← FUNCIONA para criar E editar */}
              {enderecoEmEdicao ? "Atualizar" : "Salvar"} endereço
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Dialog>
  );
}
