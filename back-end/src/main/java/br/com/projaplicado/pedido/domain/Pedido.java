package br.com.projaplicado.pedido.domain;

import br.com.projaplicado.itempedido.domain.ItemPedido;
import br.com.projaplicado.pedido.api.FormaPagamento;
import br.com.projaplicado.pedido.api.StatusPedido;
import br.com.projaplicado.pedido.api.TipoPedido;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    public Long idPedido;

    @Column(name = "id_cliente", nullable = false)
    public Long idCliente;

    @Column(name = "data", nullable = false)
    public LocalDateTime data;

    @Column(name = "forma_pagamento", nullable = false)
    @Convert(converter = FormaPagamentoConverter.class)
    public FormaPagamento formaPagamento;

    @Column(name = "status", nullable = false)
    @Convert(converter = StatusPedidoConverter.class)
    public StatusPedido status;

    @Column(name = "valor", nullable = false)
    public java.math.BigDecimal valor = java.math.BigDecimal.ZERO;

    @Column(name = "observacao")
    public String observacao;

    @Column(name = "tipo_pedido", nullable = false)
    @Convert(converter = TipoPedidoConverter.class)
    public TipoPedido tipoPedido;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_pedido")
    public List<ItemPedido> itens = new ArrayList<>();

}
