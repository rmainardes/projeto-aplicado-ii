package br.com.projaplicado.pedido.api;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoDTO {
    public Long idPedido;
    public Long idCliente;
    public BigDecimal valor;
    public LocalDateTime data;
    public FormaPagamento formaPagamento;
    public StatusPedido status;
    public String observacao;
    public TipoPedido tipoPedido;
}
