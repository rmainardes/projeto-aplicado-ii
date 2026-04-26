package br.com.projaplicado.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Optional;


@Entity
@Table(name = "usuario")
public class Usuario extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String nome;

    @Column(nullable = false, unique = true, length = 150)
    public String email;

    @Column(name = "senha_hash", nullable = false)
    public String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public Role role = Role.FUNCIONARIO;

    public Boolean ativo = true;

    @Column(name = "ultimo_login")
    public LocalDateTime ultimoLogin;

    public enum Role { ADMIN, FUNCIONARIO }

    public static Optional<Usuario> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}

