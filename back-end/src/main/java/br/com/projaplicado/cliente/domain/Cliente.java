package br.com.projaplicado.cliente.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    public Long idCliente;

    @Column(nullable = false, length = 100)
    public String nome;

    @Column(length = 100)
    public String contato;
}
