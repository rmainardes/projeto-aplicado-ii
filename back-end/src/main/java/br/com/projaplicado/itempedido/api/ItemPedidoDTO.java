package br.com.projaplicado.itempedido.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ItemPedidoDTO {
    public Long idItem;
    public Long idPedido;

    @NotNull(message = "id_produto e obrigatorio")
    public Long idProduto;

    @NotNull(message = "quantidade e obrigatoria")
    @Positive(message = "quantidade deve ser maior que zero")
    public Integer quantidade;

    public BigDecimal precoUnitario;
}
