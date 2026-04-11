package br.com.projaplicado.endereco;

import br.com.projaplicado.endereco.api.CepResponseDTO;
import br.com.projaplicado.endereco.service.ViaCepService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/enderecos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CepResource {

    @Inject
    ViaCepService viaCepService;

    @GET
    @Path("/cep/{cep}")
    public CepResponseDTO consultarCep(@PathParam("cep") String cep) {
        return viaCepService.consultarCep(cep);
    }
}