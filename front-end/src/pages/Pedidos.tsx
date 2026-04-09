import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { getPedidos, getClientes } from "@/lib/api";
import type { StatusPedido } from "@/types/api";
import { statusPedidoLabels, formaPagamentoLabels, tipoPedidoLabels } from "@/types/api";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Plus, Eye, Search } from "lucide-react";
import PedidoForm from "@/components/pedidos/PedidoForm";
import PedidoDetail from "@/components/pedidos/PedidoDetail";

const statusColors: Record<StatusPedido, string> = {
  pendente: "bg-warning text-warning-foreground",
  preparando: "bg-primary text-primary-foreground",
  pronto: "bg-success text-success-foreground",
  entregue: "bg-muted text-muted-foreground",
  cancelado: "bg-destructive text-destructive-foreground",
};

export default function Pedidos() {
  const { data: pedidos = [], isLoading } = useQuery({ queryKey: ["pedidos"], queryFn: getPedidos });
  const { data: clientes = [] } = useQuery({ queryKey: ["clientes"], queryFn: getClientes });

  const [search, setSearch] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [detailId, setDetailId] = useState<number | null>(null);

  const clienteName = (id?: number) => clientes.find((c) => c.idCliente === id)?.nome ?? `#${id}`;

  const filtered = pedidos.filter(
    (p) =>
      String(p.idPedido).includes(search) ||
      clienteName(p.idCliente).toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <h2 className="text-2xl font-display font-bold">Pedidos</h2>
        <Button onClick={() => setFormOpen(true)}>
          <Plus className="h-4 w-4 mr-1" /> Novo Pedido
        </Button>
      </div>

      <div className="relative max-w-sm">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Buscar por ID ou cliente..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-9"
        />
      </div>

      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Cliente</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Valor</TableHead>
              <TableHead>Pagamento</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="hidden md:table-cell">Data</TableHead>
              <TableHead className="w-16">Ver</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center text-muted-foreground">
                  Carregando...
                </TableCell>
              </TableRow>
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center text-muted-foreground">
                  Nenhum pedido encontrado
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((p) => (
                <TableRow key={p.idPedido}>
                  <TableCell className="font-mono text-muted-foreground">{p.idPedido}</TableCell>
                  <TableCell className="font-medium">{clienteName(p.idCliente)}</TableCell>
                  <TableCell>{tipoPedidoLabels[p.tipoPedido]}</TableCell>
                  <TableCell className="font-semibold">R$ {p.valor?.toFixed(2)}</TableCell>
                  <TableCell className="text-sm">{formaPagamentoLabels[p.formaPagamento]}</TableCell>
                  <TableCell>
                    <Badge className={statusColors[p.status]}>
                      {statusPedidoLabels[p.status]}
                    </Badge>
                  </TableCell>
                  <TableCell className="hidden md:table-cell text-sm text-muted-foreground">
                    {new Date(p.data).toLocaleString("pt-BR")}
                  </TableCell>
                  <TableCell>
                    <Button size="icon" variant="ghost" onClick={() => setDetailId(p.idPedido!)}>
                      <Eye className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <PedidoForm open={formOpen} onOpenChange={setFormOpen} />
      <PedidoDetail open={!!detailId} onOpenChange={() => setDetailId(null)} pedidoId={detailId} />
    </div>
  );
}
