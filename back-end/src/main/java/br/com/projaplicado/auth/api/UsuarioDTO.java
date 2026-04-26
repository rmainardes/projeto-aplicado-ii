package br.com.projaplicado.auth.api;

import br.com.projaplicado.auth.domain.Usuario;

public class UsuarioDTO {
    public Long id;
    public String nome;
    public String email;
    public Usuario.Role role;

    public UsuarioDTO(Long id, String nome, String email, Usuario.Role role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
    }
}
