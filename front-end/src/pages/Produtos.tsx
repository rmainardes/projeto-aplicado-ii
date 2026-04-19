import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getProdutos, deleteProduto, updateProduto } from "@/lib/api";
import type { ProdutoDTO } from "@/types/api";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Plus, Pencil, Power } from "lucide-react";
import ProdutoForm from "@/components/produtos/ProdutoForm";
import { useAuth } from "@/context/AuthContext";

export default function Produtos() {
  const qc = useQueryClient();
  const { isAdmin } = useAuth();
  const canManageProducts = isAdmin();
  const { data: produtos = [], isLoading } = useQuery({ queryKey: ["produtos"], queryFn: getProdutos });

  const [search, setSearch] = useState("");
  const [showInactive, setShowInactive] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<ProdutoDTO | null>(null);

  const toggleMut = useMutation({
    mutationFn: async (p: ProdutoDTO) => {
      if (p.ativo !== false) {
        await deleteProduto(p.idProduto!);
      } else {
        await updateProduto(p.idProduto!, { ...p, ativo: true });
      }
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["produtos"] });
      toast.success("Status do produto atualizado!");
    },
    onError: () => toast.error("Erro ao alterar status"),
  });

  const filtered = produtos
    .filter((p) => showInactive || p.ativo !== false)
    .filter(
      (p) =>
        p.nome.toLowerCase().includes(search.toLowerCase()) ||
        p.descricao.toLowerCase().includes(search.toLowerCase())
    );

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <h2 className="text-2xl font-display font-bold">Produtos</h2>
        {canManageProducts && (
          <Button onClick={() => { setEditing(null); setFormOpen(true); }}>
            <Plus className="h-4 w-4 mr-1" /> Novo Produto
          </Button>
        )}
      </div>

      <div className="flex flex-col sm:flex-row gap-3 sm:items-center">
        <div className="relative max-w-sm flex-1">
          <Input
            placeholder="Buscar produto..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-2">
          <Switch
            id="show-inactive"
            checked={showInactive}
            onCheckedChange={setShowInactive}
          />
          <Label htmlFor="show-inactive" className="text-sm">Mostrar inativos</Label>
        </div>
      </div>

      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Nome</TableHead>
              <TableHead className="hidden md:table-cell">Descrição</TableHead>
              <TableHead>Preço</TableHead>
              <TableHead>Estoque</TableHead>
              <TableHead>Status</TableHead>
              {canManageProducts && <TableHead className="w-24">Ações</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={canManageProducts ? 7 : 6} className="text-center text-muted-foreground">
                  Carregando...
                </TableCell>
              </TableRow>
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={canManageProducts ? 7 : 6} className="text-center text-muted-foreground">
                  Nenhum produto encontrado
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((p) => (
                <TableRow key={p.idProduto} className={p.ativo === false ? "opacity-60" : ""}>
                  <TableCell className="font-mono text-muted-foreground">{p.idProduto}</TableCell>
                  <TableCell className="font-medium">{p.nome}</TableCell>
                  <TableCell className="hidden md:table-cell max-w-[200px] truncate">
                    {p.descricao}
                  </TableCell>
                  <TableCell>R$ {p.preco?.toFixed(2)}</TableCell>
                  <TableCell>{p.quantidadeEstoque}</TableCell>
                  <TableCell>
                    <Badge variant={p.ativo !== false ? "default" : "secondary"}>
                      {p.ativo !== false ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  {canManageProducts && (
                    <TableCell>
                      <div className="flex gap-1">
                        <Button
                          size="icon"
                          variant="ghost"
                          onClick={() => { setEditing(p); setFormOpen(true); }}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          size="icon"
                          variant="ghost"
                          onClick={() => toggleMut.mutate(p)}
                          disabled={toggleMut.isPending}
                          title={p.ativo !== false ? "Desativar" : "Reativar"}
                        >
                          <Power className={`h-4 w-4 ${p.ativo !== false ? "text-destructive" : "text-success"}`} />
                        </Button>
                      </div>
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {canManageProducts && (
        <ProdutoForm
          key={editing?.idProduto ?? "new"}
          open={formOpen}
          onOpenChange={setFormOpen}
          produto={editing}
        />
      )}
    </div>
  );
}
