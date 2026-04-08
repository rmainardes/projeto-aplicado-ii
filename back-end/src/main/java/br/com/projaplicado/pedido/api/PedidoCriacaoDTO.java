package br.com.projaplicado.pedido.api;

import br.com.projaplicado.itempedido.api.ItemPedidoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PedidoCriacaoDTO {
    @NotNull(message = "id_cliente e obrigatorio")
    public Long idCliente;

    @NotNull(message = "forma_pagamento e obrigatorio")
    public FormaPagamento formaPagamento;

    public String observacao;

    @NotNull(message = "tipo_pedido e obrigatorio")
    public TipoPedido tipoPedido;

    @NotEmpty(message = "itens nao pode ser vazio")
    @Valid
    public List<ItemPedidoDTO> itens;
}

