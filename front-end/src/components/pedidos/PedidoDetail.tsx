import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  getPedido,
  updatePedido,
  addItemPedido,
  updateItemPedido,
  removeItemPedido,
  getProdutos,
  getClientes,
} from "@/lib/api";
import type { PedidoDTO, StatusPedido, ItemPedidoDTO } from "@/types/api";
import {
  statusPedidoLabels,
  formaPagamentoLabels,
  tipoPedidoLabels,
} from "@/types/api";
import { toast } from "sonner";
import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Plus, Trash2, ChevronUp, ChevronDown } from "lucide-react";

const statusColors: Record<StatusPedido, string> = {
  pendente: "bg-warning text-warning-foreground",
  preparando: "bg-primary text-primary-foreground",
  pronto: "bg-success text-success-foreground",
  entregue: "bg-muted text-muted-foreground",
  cancelado: "bg-destructive text-destructive-foreground",
};

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  pedidoId: number | null;
}

export default function PedidoDetail({ open, onOpenChange, pedidoId }: Props) {
  const qc = useQueryClient();
  const { data: pedido, isLoading } = useQuery({
    queryKey: ["pedido", pedidoId],
    queryFn: () => getPedido(pedidoId!),
    enabled: !!pedidoId && open,
  });
  const { data: produtos = [] } = useQuery({
    queryKey: ["produtos"],
    queryFn: getProdutos,
  });
  const { data: clientes = [] } = useQuery({
    queryKey: ["clientes"],
    queryFn: getClientes,
  });

  const [newItem, setNewItem] = useState({ idProduto: 0, quantidade: 1 });

  const cliente = clientes.find((c) => c.idCliente === pedido?.idCliente);

  const statusMut = useMutation({
    mutationFn: (status: StatusPedido) => {
      if (!pedido) throw new Error("Pedido não encontrado");
      return updatePedido(pedido.idPedido!, { ...pedido, status });
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["pedido", pedidoId] });
      qc.invalidateQueries({ queryKey: ["pedidos"] });
      toast.success("Status atualizado!");
    },
    onError: () => toast.error("Erro ao atualizar status"),
  });

  const addMut = useMutation({
    mutationFn: () => {
      // Check if product already exists in the order
      const existingItem = pedido?.itens?.find(
        (i) => i.idProduto === newItem.idProduto,
      );

      if (existingItem) {
        // Update quantity of existing item
        return updateItemPedido(pedidoId!, existingItem.idItem!, {
          idProduto: existingItem.idProduto,
          quantidade: existingItem.quantidade + newItem.quantidade,
          precoUnitario: existingItem.precoUnitario,
        });
      } else {
        // Add new item
        return addItemPedido(pedidoId!, newItem as ItemPedidoDTO);
      }
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["pedido", pedidoId] });
      qc.invalidateQueries({ queryKey: ["pedidos"] });
      setNewItem({ idProduto: 0, quantidade: 1 });
      toast.success("Item adicionado!");
    },
    onError: () => toast.error("Erro ao adicionar item"),
  });

  const removeMut = useMutation({
    mutationFn: (idItem: number) => removeItemPedido(pedidoId!, idItem),
    onSuccess: (_, idItem) => {
      qc.setQueryData(["pedido", pedidoId], (old: PedidoDTO | undefined) => {
        if (!old) return old;
        return {
          ...old,
          itens: old.itens?.filter((i) => i.idItem !== idItem),
        };
      });
      qc.invalidateQueries({ queryKey: ["pedido", pedidoId] });
      qc.invalidateQueries({ queryKey: ["pedidos"] });
      toast.success("Item removido!");
    },
    onError: () => toast.error("Erro ao remover item"),
  });

  const updateQuantityMut = useMutation({
    mutationFn: ({
      idItem,
      quantidade,
    }: {
      idItem: number;
      quantidade: number;
    }) => {
      const item = pedido?.itens?.find((i) => i.idItem === idItem);
      if (!item) throw new Error("Item não encontrado");
      return updateItemPedido(pedidoId!, idItem, {
        idProduto: item.idProduto,
        quantidade,
        precoUnitario: item.precoUnitario,
      });
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["pedido", pedidoId] });
      qc.invalidateQueries({ queryKey: ["pedidos"] });
      toast.success("Quantidade atualizada!");
    },
    onError: () => toast.error("Erro ao atualizar quantidade"),
  });

  const produtosAtivos = produtos.filter((p) => p.ativo !== false);

  if (!pedidoId) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Pedido #{pedidoId}</DialogTitle>
        </DialogHeader>

        {isLoading && <p className="text-muted-foreground">Carregando...</p>}

        {pedido && (
          <div className="space-y-4">
            {/* Info */}
            <div className="grid grid-cols-2 gap-2 text-sm">
              <div>
                <span className="text-muted-foreground">Cliente:</span>{" "}
                <span className="font-medium">
                  {cliente?.nome ?? `#${pedido.idCliente}`}
                </span>
              </div>
              <div>
                <span className="text-muted-foreground">Valor:</span>{" "}
                <span className="font-semibold">
                  R$ {pedido.valor?.toFixed(2)}
                </span>
              </div>
              <div>
                <span className="text-muted-foreground">Tipo:</span>{" "}
                {tipoPedidoLabels[pedido.tipoPedido]}
              </div>
              <div>
                <span className="text-muted-foreground">Pagamento:</span>{" "}
                {formaPagamentoLabels[pedido.formaPagamento]}
              </div>
              <div>
                <span className="text-muted-foreground">Data:</span>{" "}
                {new Date(pedido.data).toLocaleString("pt-BR")}
              </div>
              {pedido.observacao && (
                <div className="col-span-2">
                  <span className="text-muted-foreground">Obs:</span>{" "}
                  {pedido.observacao}
                </div>
              )}
            </div>

            {/* Status */}
            <div className="flex items-center gap-3">
              <Badge className={statusColors[pedido.status]}>
                {statusPedidoLabels[pedido.status]}
              </Badge>
              <Select
                value={pedido.status}
                onValueChange={(v) => statusMut.mutate(v as StatusPedido)}
              >
                <SelectTrigger className="w-40">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(statusPedidoLabels).map(([k, v]) => (
                    <SelectItem key={k} value={k}>
                      {v}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Itens */}
            <div className="space-y-2">
              <h4 className="text-sm font-medium">Itens</h4>
              {(pedido.itens?.length ?? 0) > 0 ? (
                pedido.itens!.map((item) => {
                  const prod = produtos.find(
                    (p) => p.idProduto === item.idProduto,
                  );
                  return (
                    <div
                      key={item.idItem}
                      className="flex items-center justify-between rounded-md border p-2"
                    >
                      <div className="text-sm">
                        <span className="font-medium">
                          {prod?.nome ?? `Produto #${item.idProduto}`}
                        </span>
                        <div className="flex items-center gap-2 mt-1">
                          <Button
                            size="icon"
                            variant="outline"
                            className="h-6 w-6"
                            onClick={() =>
                              updateQuantityMut.mutate({
                                idItem: item.idItem!,
                                quantidade: Math.max(1, item.quantidade - 1),
                              })
                            }
                            disabled={
                              updateQuantityMut.isPending ||
                              item.quantidade <= 1
                            }
                          >
                            <ChevronDown className="h-3 w-3" />
                          </Button>
                          <span className="w-8 text-center">
                            {item.quantidade}
                          </span>
                          <Button
                            size="icon"
                            variant="outline"
                            className="h-6 w-6"
                            onClick={() =>
                              updateQuantityMut.mutate({
                                idItem: item.idItem!,
                                quantidade: item.quantidade + 1,
                              })
                            }
                            disabled={updateQuantityMut.isPending}
                          >
                            <ChevronUp className="h-3 w-3" />
                          </Button>
                        </div>
                        <span className="text-muted-foreground ml-2 text-xs">
                          R${" "}
                          {(
                            (item.precoUnitario ?? 0) * item.quantidade
                          ).toFixed(2)}
                        </span>
                      </div>
                      <Button
                        size="icon"
                        variant="ghost"
                        onClick={() => removeMut.mutate(item.idItem!)}
                        disabled={
                          removeMut.isPending ||
                          (pedido.itens?.length ?? 0) === 1
                        }
                        title={
                          (pedido.itens?.length ?? 0) === 1
                            ? "Não é possível remover o último item. Altere o status do pedido para 'Cancelado' se deseja cancelar o pedido."
                            : ""
                        }
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </div>
                  );
                })
              ) : (
                <p className="text-sm text-muted-foreground">Sem itens</p>
              )}

              {/* Add item */}
              <div className="flex gap-2 items-end pt-2">
                <div className="flex-1 max-w-xs">
                  <Select
                    value={String(newItem.idProduto || "")}
                    onValueChange={(v) =>
                      setNewItem({ ...newItem, idProduto: Number(v) })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Produto" />
                    </SelectTrigger>
                    <SelectContent>
                      {produtosAtivos.map((p) => (
                        <SelectItem
                          key={p.idProduto}
                          value={String(p.idProduto)}
                        >
                          {p.nome}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <Input
                  type="number"
                  min={1}
                  className="w-20"
                  value={newItem.quantidade}
                  onChange={(e) =>
                    setNewItem({
                      ...newItem,
                      quantidade: Number(e.target.value),
                    })
                  }
                />
                <Button
                  size="sm"
                  onClick={() => addMut.mutate()}
                  disabled={newItem.idProduto === 0 || addMut.isPending}
                >
                  <Plus className="h-3 w-3 mr-1" /> Incluir
                </Button>
              </div>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
