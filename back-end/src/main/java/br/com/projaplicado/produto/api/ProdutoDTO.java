package br.com.projaplicado.produto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ProdutoDTO {
    public Long idProduto;

    @NotBlank(message = "nome e obrigatorio")
    public String nome;

    @NotBlank(message = "descricao e obrigatoria")
    public String descricao;

    @NotNull(message = "preco e obrigatorio")
    @PositiveOrZero(message = "preco nao pode ser negativo")
    public BigDecimal preco;

    @NotNull(message = "quantidade_estoque e obrigatoria")
    @PositiveOrZero(message = "quantidade_estoque nao pode ser negativa")
    public Integer quantidadeEstoque;
}
