package br.com.projaplicado.itempedido.domain;

import br.com.projaplicado.pedido.domain.Pedido;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
public class ItemPedido extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    public Long idItem;

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    public Pedido pedido;

    @Column(name = "id_produto", nullable = false)
    public Long idProduto;

    @Column(name = "quantidade", nullable = false)
    public Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    public BigDecimal precoUnitario;
}
