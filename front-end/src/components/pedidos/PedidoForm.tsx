import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createPedido, getClientes, getProdutos } from "@/lib/api";
import type { FormaPagamento, TipoPedido, ItemPedidoDTO } from "@/types/api";
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
import { Plus, Trash2 } from "lucide-react";

const schema = z.object({
  idCliente: z.coerce.number().min(1, "Selecione um cliente"),
  formaPagamento: z.string().min(1, "Selecione forma de pagamento"),
  tipoPedido: z.string().min(1, "Selecione o tipo"),
  observacao: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function PedidoForm({ open, onOpenChange }: Props) {
  const qc = useQueryClient();
  const { data: clientes = [] } = useQuery({ queryKey: ["clientes"], queryFn: getClientes });
  const { data: produtos = [] } = useQuery({ queryKey: ["produtos"], queryFn: getProdutos });
  const produtosAtivos = produtos.filter((p) => p.ativo !== false);

  const [itens, setItens] = useState<{ idProduto: number; quantidade: number }[]>([]);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { idCliente: 0, formaPagamento: "", tipoPedido: "", observacao: "" },
  });

  const addItem = () => setItens([...itens, { idProduto: 0, quantidade: 1 }]);
  const removeItem = (i: number) => setItens(itens.filter((_, idx) => idx !== i));
  const updateItem = (i: number, field: string, value: number) => {
    const copy = [...itens];
    (copy[i] as any)[field] = value;
    setItens(copy);
  };

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const validItens = itens.filter((i) => i.idProduto > 0 && i.quantidade > 0);
      if (validItens.length === 0) throw new Error("Adicione pelo menos 1 item");
      return createPedido({
        idCliente: values.idCliente,
        formaPagamento: values.formaPagamento as FormaPagamento,
        tipoPedido: values.tipoPedido as TipoPedido,
        observacao: values.observacao || undefined,
        itens: validItens as ItemPedidoDTO[],
      });
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["pedidos"] });
      toast.success("Pedido criado!");
      onOpenChange(false);
      form.reset();
      setItens([]);
    },
    onError: (e: any) => toast.error(e?.message || "Erro ao criar pedido"),
  });

  const total = itens.reduce((acc, item) => {
    const prod = produtosAtivos.find((p) => p.idProduto === item.idProduto);
    return acc + (prod?.preco ?? 0) * item.quantidade;
  }, 0);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Novo Pedido</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit((v) => mutation.mutate(v))} className="space-y-4">
            <FormField
              control={form.control}
              name="idCliente"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Cliente</FormLabel>
                  <Select onValueChange={field.onChange} value={String(field.value || "")}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Selecionar cliente" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {clientes.map((c) => (
                        <SelectItem key={c.idCliente} value={String(c.idCliente)}>
                          {c.nome}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
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
                          <SelectItem key={k} value={k}>{v}</SelectItem>
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
                          <SelectItem key={k} value={k}>{v}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <FormField
              control={form.control}
              name="observacao"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Observação</FormLabel>
                  <FormControl>
                    <Textarea placeholder="Opcional" {...field} />
                  </FormControl>
                </FormItem>
              )}
            />

            {/* Itens */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Itens do Pedido</span>
                <Button type="button" size="sm" variant="outline" onClick={addItem}>
                  <Plus className="h-3 w-3 mr-1" /> Adicionar
                </Button>
              </div>
              {itens.length === 0 && (
                <p className="text-sm text-muted-foreground">Nenhum item adicionado</p>
              )}
              {itens.map((item, i) => (
                <div key={i} className="flex gap-2 items-end">
                  <div className="flex-1">
                    <Select
                      value={String(item.idProduto || "")}
                      onValueChange={(v) => updateItem(i, "idProduto", Number(v))}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Produto" />
                      </SelectTrigger>
                      <SelectContent>
                        {produtosAtivos.map((p) => (
                          <SelectItem key={p.idProduto} value={String(p.idProduto)}>
                            {p.nome} — R$ {p.preco?.toFixed(2)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <Input
                    type="number"
                    min={1}
                    className="w-20"
                    value={item.quantidade}
                    onChange={(e) => updateItem(i, "quantidade", Number(e.target.value))}
                  />
                  <Button type="button" size="icon" variant="ghost" onClick={() => removeItem(i)}>
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
              {itens.length > 0 && (
                <p className="text-sm font-semibold text-right">
                  Total estimado: R$ {total.toFixed(2)}
                </p>
              )}
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={mutation.isPending}>
                {mutation.isPending ? "Criando..." : "Criar Pedido"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
