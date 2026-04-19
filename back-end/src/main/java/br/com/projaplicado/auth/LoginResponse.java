package br.com.projaplicado.auth;
import br.com.projaplicado.auth.api.UsuarioDTO;

public class LoginResponse {
    public String token;
    public UsuarioDTO usuario;

    public LoginResponse(String token, UsuarioDTO usuario) {
        this.token = token;
        this.usuario = usuario;
    }
}
