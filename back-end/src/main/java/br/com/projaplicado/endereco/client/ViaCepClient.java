package br.com.projaplicado.endereco.client;

import br.com.projaplicado.endereco.api.ViaCepResponseDTO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/ws")
@RegisterRestClient(configKey = "viacep-api")
@Produces(MediaType.APPLICATION_JSON)
public interface ViaCepClient {

    @GET
    @Path("/{cep}/json/")
    ViaCepResponseDTO consultarCep(@PathParam("cep") String cep);
}