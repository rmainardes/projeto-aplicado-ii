package br.com.projaplicado.pedido.api;


import br.com.projaplicado.itempedido.api.ItemPedidoDTO;
import jakarta.validation.constraints.NotNull;import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoDTO {
    public Long idPedido;
    public Long idCliente;

    @NotNull(message = "valor e obrigatorio")
    public BigDecimal valor;

    @NotNull(message = "data e obrigatorio")
    public LocalDateTime data;

    @NotNull(message = "forma_pagamento e obrigatorio")
    public FormaPagamento formaPagamento;

    @NotNull(message = "status e obrigatorio")
    public StatusPedido status;

    public String observacao;

    @NotNull(message = "tipo_pedido e obrigatorio")
    public TipoPedido tipoPedido;

    public List<ItemPedidoDTO> itens = new ArrayList<>();
}
