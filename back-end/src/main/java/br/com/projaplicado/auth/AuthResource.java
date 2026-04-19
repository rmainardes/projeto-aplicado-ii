package br.com.projaplicado.auth;

import br.com.projaplicado.auth.domain.Usuario;
import br.com.projaplicado.auth.service.AuthService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest req) {
        return Response.ok(authService.login(req)).build();
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

    @GET
    @Path("/gerar-hash")
    public String gerarHash(@QueryParam("senha") String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(12));
    }
}
