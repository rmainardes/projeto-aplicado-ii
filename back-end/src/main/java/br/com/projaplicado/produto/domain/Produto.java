package br.com.projaplicado.produto.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produto")
public class Produto extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    public Long idProduto;

    @Column(nullable = false, length = 100)
    public String nome;

    @Column(nullable = false, length = 100)
    public String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal preco;

    @Column(name = "quantidade_estoque", nullable = false)
    public Integer quantidadeEstoque;
}
