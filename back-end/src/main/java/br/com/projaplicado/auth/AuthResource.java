package br.com.projaplicado.auth;

import br.com.projaplicado.auth.api.UsuarioDTO;
import br.com.projaplicado.auth.domain.Usuario;
import br.com.projaplicado.auth.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest req) {
        LoginResponse resp = authService.login(req);

        return Response.ok(resp.usuario)
                .cookie(tokenCookie(resp.token, AuthService.TOKEN_TTL_SECONDS))
                .build();
    }

    @POST
    @Path("/logout")
    @PermitAll
    public Response logout() {
        return Response.noContent()
                .cookie(tokenCookie("", 0))
                .build();
    }

    // Devolve o usuário logado a partir do cookie de sessão, usado pelo
    // front-end para restaurar o estado de autenticação ao carregar a página
    // (o token é HttpOnly, então o JS não tem como lê-lo diretamente).
    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "FUNCIONARIO"})
    public Response me() {
        Usuario usuario = Usuario.findById(Long.valueOf(jwt.getSubject()));
        if (usuario == null || !usuario.ativo) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(new UsuarioDTO(usuario.id, usuario.nome, usuario.email, usuario.role)).build();
    }

    // Apenas ADMIN cria usuários
    @POST @Path("/usuarios")
    @RolesAllowed("ADMIN")
    public Response criarUsuario(@QueryParam("nome") String nome,
                                 @QueryParam("email") String email,
                                 @QueryParam("senha") String senha,
                                 @QueryParam("role") String role) {
        Usuario.Role r = role != null ? Usuario.Role.valueOf(role.toUpperCase()) : Usuario.Role.FUNCIONARIO;
        return Response.status(201).entity(authService.criarUsuario(nome, email, senha, r)).build();
    }

    private static NewCookie tokenCookie(String value, int maxAgeSeconds) {
        return new NewCookie.Builder(ACCESS_TOKEN_COOKIE)
                .value(value)
                .path("/")
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.NONE)
                .build();
    }
}
