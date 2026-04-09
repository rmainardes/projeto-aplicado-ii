import { useQuery } from "@tanstack/react-query";
import { getPedidos, getClientes, getProdutos } from "@/lib/api";
import type { StatusPedido } from "@/types/api";
import { statusPedidoLabels } from "@/types/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Users, ShoppingBag, ClipboardList, DollarSign } from "lucide-react";

const statusColors: Record<StatusPedido, string> = {
  pendente: "bg-warning text-warning-foreground",
  preparando: "bg-primary text-primary-foreground",
  pronto: "bg-success text-success-foreground",
  entregue: "bg-muted text-muted-foreground",
  cancelado: "bg-destructive text-destructive-foreground",
};

export default function Index() {
  const { data: pedidos = [] } = useQuery({
    queryKey: ["pedidos"],
    queryFn: getPedidos,
  });
  const { data: clientes = [] } = useQuery({
    queryKey: ["clientes"],
    queryFn: getClientes,
  });
  const { data: produtos = [] } = useQuery({
    queryKey: ["produtos"],
    queryFn: getProdutos,
  });

  const today = new Date().toISOString().slice(0, 10);
  const listaPedidos = Array.isArray(pedidos) ? pedidos : [];
  const pedidosHoje = listaPedidos.filter((p) => p.data?.startsWith(today));
  const pendentes = listaPedidos.filter(
    (p) => p.status === "pendente" || p.status === "preparando",
  );
  const faturamentoHoje = pedidosHoje
    .filter((p) => p.status !== "cancelado")
    .reduce((acc, p) => acc + (p.valor ?? 0), 0);

  const cards = [
    {
      title: "Clientes",
      value: clientes.length,
      icon: Users,
      color: "text-primary",
    },
    {
      title: "Produtos Ativos",
      value: Array.isArray(produtos)
        ? produtos.filter((p) => p.ativo !== false).length
        : 0,
      icon: ShoppingBag,
      color: "text-success",
    },
    {
      title: "Pedidos Hoje",
      value: pedidosHoje.length,
      icon: ClipboardList,
      color: "text-warning",
    },
    {
      title: "Faturamento Hoje",
      value: `R$ ${faturamentoHoje.toFixed(2)}`,
      icon: DollarSign,
      color: "text-primary",
    },
  ];

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-display font-bold">Dashboard</h2>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map((c) => (
          <Card key={c.title}>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {c.title}
              </CardTitle>
              <c.icon className={`h-5 w-5 ${c.color}`} />
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{c.value}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Pedidos pendentes */}
      {pendentes.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Pedidos em Andamento</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {pendentes.slice(0, 10).map((p) => (
                <div
                  key={p.idPedido}
                  className="flex items-center justify-between rounded-md border p-3"
                >
                  <div className="flex items-center gap-3">
                    <span className="font-mono text-sm text-muted-foreground">
                      #{p.idPedido}
                    </span>
                    <span className="font-medium">
                      R$ {p.valor?.toFixed(2)}
                    </span>
                  </div>
                  <Badge className={statusColors[p.status]}>
                    {statusPedidoLabels[p.status]}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
