import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createCliente, updateCliente } from "@/lib/api";
import type { ClienteDTO } from "@/types/api";
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

const schema = z.object({
  nome: z.string().min(1, "Nome é obrigatório"),
  contato: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  cliente?: ClienteDTO | null;
}

export default function ClienteForm({ open, onOpenChange, cliente }: Props) {
  const qc = useQueryClient();
  const isEdit = !!cliente?.idCliente;

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      nome: cliente?.nome ?? "",
      contato: cliente?.contato ?? "",
    },
  });

  // Reset form when cliente changes
  const prevId = cliente?.idCliente;
  if (open) {
    const currentName = form.getValues("nome");
    const expected = cliente?.nome ?? "";
    if (currentName !== expected && (isEdit || currentName !== "")) {
      // only reset if switching between clients
    }
  }

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const dto: ClienteDTO = { nome: values.nome, contato: values.contato || undefined };
      return isEdit ? updateCliente(cliente!.idCliente!, dto) : createCliente(dto);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["clientes"] });
      toast.success(isEdit ? "Cliente atualizado!" : "Cliente criado!");
      onOpenChange(false);
      form.reset();
    },
    onError: () => toast.error("Erro ao salvar cliente"),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Editar Cliente" : "Novo Cliente"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit((v) => mutation.mutate(v))} className="space-y-4">
            <FormField
              control={form.control}
              name="nome"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Nome</FormLabel>
                  <FormControl>
                    <Input placeholder="Nome do cliente" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="contato"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Contato</FormLabel>
                  <FormControl>
                    <Input placeholder="Telefone ou e-mail" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={mutation.isPending}>
                {mutation.isPending ? "Salvando..." : "Salvar"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
