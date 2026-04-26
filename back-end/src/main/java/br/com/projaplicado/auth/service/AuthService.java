package br.com.projaplicado.auth.service;
import br.com.projaplicado.auth.api.UsuarioDTO;
import br.com.projaplicado.auth.LoginRequest;
import br.com.projaplicado.auth.LoginResponse;
import br.com.projaplicado.auth.domain.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    private static final int BCRYPT_ROUNDS = 12;

    @Transactional
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = Usuario.findByEmail(req.email)
                .orElseThrow(() -> new WebApplicationException(
                        Response.status(401).entity("{\"message\":\"Credenciais inválidas\"}").build()));

        if (!usuario.ativo)
            throw new WebApplicationException(
                    Response.status(403).entity("{\"message\":\"Usuário desativado\"}").build());

        // BCrypt verifica a senha contra o hash salvo
        if (!BCrypt.checkpw(req.senha, usuario.senhaHash))
            throw new WebApplicationException(
                    Response.status(401).entity("{\"message\":\"Credenciais inválidas\"}").build());

        usuario.ultimoLogin = LocalDateTime.now();

        // JWT com role embutida como "groups" (compatível com @RolesAllowed)
        String token = Jwt.issuer("foodtruck-app")
                .subject(String.valueOf(usuario.id))
                .claim("email", usuario.email)
                .claim("role", usuario.role.name())
                .groups(Set.of(usuario.role.name()))
                .expiresIn(28800) // 8 horas
                .sign();

        return new LoginResponse(token, new UsuarioDTO(
                usuario.id, usuario.nome, usuario.email, usuario.role));
    }

    @Transactional
    public UsuarioDTO criarUsuario(String nome, String email, String senhaPlain, Usuario.Role role) {
        if (Usuario.findByEmail(email).isPresent())
            throw new WebApplicationException(
                    Response.status(409).entity("{\"message\":\"Email já cadastrado\"}").build());

        Usuario u = new Usuario();
        u.nome = nome; u.email = email;
        u.senhaHash = BCrypt.hashpw(senhaPlain, BCrypt.gensalt(BCRYPT_ROUNDS));
        u.role = role;
        u.persist();

        return new UsuarioDTO(u.id, u.nome, u.email, u.role);
    }
}