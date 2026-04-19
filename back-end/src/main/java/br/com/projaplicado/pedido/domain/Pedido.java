package br.com.projaplicado.pedido.domain;

import br.com.projaplicado.itempedido.domain.ItemPedido;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    public FormaPagamento formaPagamento;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    public StatusPedido status;

    @Column(name = "tipo_pedido", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    public TipoPedido tipoPedido;

    @Column(name = "valor", nullable = false)
    public java.math.BigDecimal valor = java.math.BigDecimal.ZERO;

    @Column(name = "observacao")
    public String observacao;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemPedido> itens = new ArrayList<>();

}
