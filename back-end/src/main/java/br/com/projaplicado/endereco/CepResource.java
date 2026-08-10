package br.com.projaplicado.endereco;

import br.com.projaplicado.endereco.api.CepResponseDTO;
import br.com.projaplicado.endereco.service.ViaCepService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/enderecos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "FUNCIONARIO"})
public class CepResource {

    @Inject
    ViaCepService viaCepService;

    @GET
    @Path("/cep/{cep}")
    public CepResponseDTO consultarCep(@PathParam("cep") String cep) {
        return viaCepService.consultarCep(cep);
    }
}