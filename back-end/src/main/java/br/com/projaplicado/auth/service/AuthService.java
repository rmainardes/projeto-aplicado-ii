package br.com.projaplicado.auth.service;
import br.com.projaplicado.auth.api.UsuarioDTO;
import br.com.projaplicado.auth.LoginRequest;
import br.com.projaplicado.auth.LoginResponse;
import br.com.projaplicado.auth.domain.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    private static final int BCRYPT_ROUNDS = 12;
    public static final int TOKEN_TTL_SECONDS = 28800; // 8 horas

    @Transactional
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = Usuario.findByEmail(req.email)
                .orElseThrow(() -> new NotAuthorizedException("Credenciais inválidas", "Bearer"));

        if (!usuario.ativo)
            throw new ForbiddenException("Usuário desativado");

        // BCrypt verifica a senha contra o hash salvo
        if (!BCrypt.checkpw(req.senha, usuario.senhaHash))
            throw new NotAuthorizedException("Credenciais inválidas", "Bearer");

        usuario.ultimoLogin = LocalDateTime.now();

        // JWT com role embutida como "groups" (compatível com @RolesAllowed)
        String token = Jwt.issuer("foodtruck-app")
                .subject(String.valueOf(usuario.id))
                .claim("email", usuario.email)
                .claim("role", usuario.role.name())
                .groups(Set.of(usuario.role.name()))
                .expiresIn(TOKEN_TTL_SECONDS)
                .sign();

        return new LoginResponse(token, new UsuarioDTO(
                usuario.id, usuario.nome, usuario.email, usuario.role));
    }

    @Transactional
    public UsuarioDTO criarUsuario(String nome, String email, String senhaPlain, Usuario.Role role) {
        if (Usuario.findByEmail(email).isPresent())
            throw new WebApplicationException("Email já cadastrado", Response.Status.CONFLICT);

        Usuario u = new Usuario();
        u.nome = nome; u.email = email;
        u.senhaHash = BCrypt.hashpw(senhaPlain, BCrypt.gensalt(BCRYPT_ROUNDS));
        u.role = role;
        u.persist();

        return new UsuarioDTO(u.id, u.nome, u.email, u.role);
    }
}