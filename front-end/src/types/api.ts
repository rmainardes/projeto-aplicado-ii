// Enums
export type FormaPagamento = "pix" | "voucher" | "cartao_credito" | "cartao_debito" | "dinheiro";
export type StatusPedido = "pendente" | "preparando" | "pronto" | "entregue" | "cancelado";
export type TipoPedido = "delivery" | "retirada" | "local";

// Labels PT-BR
export const formaPagamentoLabels: Record<FormaPagamento, string> = {
  pix: "PIX",
  voucher: "Voucher",
  cartao_credito: "Cartão de Crédito",
  cartao_debito: "Cartão de Débito",
  dinheiro: "Dinheiro",
};

export const statusPedidoLabels: Record<StatusPedido, string> = {
  pendente: "Pendente",
  preparando: "Preparando",
  pronto: "Pronto",
  entregue: "Entregue",
  cancelado: "Cancelado",
};

export const tipoPedidoLabels: Record<TipoPedido, string> = {
  delivery: "Delivery",
  retirada: "Retirada",
  local: "Consumo no Local",
};

// DTOs
export interface ClienteDTO {
  idCliente?: number;
  nome: string;
  contato?: string;
}

export interface ProdutoDTO {
  idProduto?: number;
  nome: string;
  descricao: string;
  preco: number;
  quantidadeEstoque: number;
  ativo?: boolean;
}

export interface ItemPedidoDTO {
  idItem?: number;
  idPedido?: number;
  idProduto: number;
  quantidade: number;
  precoUnitario?: number;
}

export interface PedidoCriacaoDTO {
  idCliente: number;
  formaPagamento: FormaPagamento;
  observacao?: string;
  tipoPedido: TipoPedido;
  itens: ItemPedidoDTO[];
}

export interface PedidoDTO {
  idPedido?: number;
  idCliente?: number;
  valor: number;
  data: string;
  formaPagamento: FormaPagamento;
  status: StatusPedido;
  observacao?: string;
  tipoPedido: TipoPedido;
  itens?: ItemPedidoDTO[];
}
