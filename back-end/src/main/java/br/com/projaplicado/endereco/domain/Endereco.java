package br.com.projaplicado.endereco.domain;

import br.com.projaplicado.endereco.api.UF;
import br.com.projaplicado.cliente.domain.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "endereco")
public class Endereco extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco")
    public Long idEndereco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    public Cliente cliente;

    @Column(nullable = false, length = 15)
    public String descricao;

    @Column(nullable = false, length = 8)
    public String cep;

    @Column(nullable = false, length = 60)
    public String logradouro;

    @Column(nullable = false, length = 10)
    public String numero;

    @Column(length = 30)
    public String complemento;

    @Column(nullable = false, length = 40)
    public String bairro;

    @Column(nullable = false, length = 40)
    public String cidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    public UF uf;

    @Column(length = 60)
    public String referencia;

    @Column(nullable = false)
    public boolean principal = false;

    @Column(nullable = false)
    public boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}