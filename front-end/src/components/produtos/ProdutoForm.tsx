import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createProduto, updateProduto } from "@/lib/api";
import type { ProdutoDTO } from "@/types/api";
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
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {useAuth} from "@/context/AuthContext";

const schema = z.object({
    nome: z.string().min(1, "Nome é obrigatório"),
    descricao: z.string().min(1, "Descrição é obrigatória"),
    preco: z.coerce.number().min(0, "Preço deve ser >= 0"),
    quantidadeEstoque: z.coerce.number().int().min(0, "Estoque deve ser >= 0"),
});

type FormValues = z.infer<typeof schema>;

interface Props {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    produto?: ProdutoDTO | null;
}

export default function ProdutoForm({ open, onOpenChange, produto }: Props) {
    const qc = useQueryClient();
    const { isAdmin } = useAuth();
    const canManageProducts = isAdmin();
    const isEdit = !!produto?.idProduto;


    const form = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            nome: produto?.nome ?? "",
            descricao: produto?.descricao ?? "",
            preco: produto?.preco ?? 0,
            quantidadeEstoque: produto?.quantidadeEstoque ?? 0,
        },
    });

    const mutation = useMutation({
        mutationFn: (values: FormValues) => {
            if (!canManageProducts) {
                throw new Error("Operação não autorizada");
            }

            const dto: ProdutoDTO = {
                nome: values.nome,
                descricao: values.descricao,
                preco: values.preco,
                quantidadeEstoque: values.quantidadeEstoque,
                ativo: produto?.ativo ?? true,
            };
            return isEdit ? updateProduto(produto!.idProduto!, dto) : createProduto(dto);
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["produtos"] });
            toast.success(isEdit ? "Produto atualizado!" : "Produto criado!");
            onOpenChange(false);
            form.reset();
        },
        onError: () => toast.error("Erro ao salvar produto"),
    });

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>{isEdit ? "Editar Produto" : "Novo Produto"}</DialogTitle>
                </DialogHeader>
                <Form {...form}>
                    <form
                        onSubmit={form.handleSubmit((v) => {
                            if (!canManageProducts) {
                                toast.error("Apenas administradores podem gerenciar produtos.");
                                return;
                            }
                            mutation.mutate(v);
                        })}
                        className="space-y-4"
                    >
                        <FormField
                            control={form.control}
                            name="nome"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Nome</FormLabel>
                                    <FormControl>
                                        <Input placeholder="Ex: X-Burguer" disabled={!canManageProducts} {...field} />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="descricao"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Descrição</FormLabel>
                                    <FormControl>
                                        <Textarea placeholder="Descrição do produto" disabled={!canManageProducts} {...field} />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />

                        {/* Preço e Estoque: visíveis mas bloqueados para FUNCIONARIO */}
                        <div className="grid grid-cols-2 gap-4">
                            <FormField
                                control={form.control}
                                name="preco"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Preço (R$)</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="number" step="0.01" min="0"
                                                disabled={!canManageProducts}
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="quantidadeEstoque"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Estoque</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="number" min="0"
                                                disabled={!canManageProducts}
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>

                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                                Cancelar
                            </Button>
                            <Button type="submit" disabled={mutation.isPending || !canManageProducts}>
                                {mutation.isPending ? "Salvando..." : "Salvar"}
                            </Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
