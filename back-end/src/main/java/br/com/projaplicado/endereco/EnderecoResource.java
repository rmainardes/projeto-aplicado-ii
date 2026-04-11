package br.com.projaplicado.endereco;

import br.com.projaplicado.endereco.api.EnderecoRequestDTO;
import br.com.projaplicado.endereco.api.EnderecoResponseDTO;
import br.com.projaplicado.endereco.service.EnderecoService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/clientes/{id_cliente}/enderecos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnderecoResource {

    @Inject
    EnderecoService enderecoService;

    @GET
    public List<EnderecoResponseDTO> listar(
            @PathParam("id_cliente") Long idCliente,
            @QueryParam("descricao") String descricao) {

        if (descricao != null && !descricao.isBlank()) {
            return enderecoService.buscarPorDescricao(idCliente, descricao);
        }

        return enderecoService.listarPorCliente(idCliente);
    }

    @GET
    @Path("/principal")
    public EnderecoResponseDTO buscarPrincipal(@PathParam("id_cliente") Long idCliente) {
        return enderecoService.buscarPrincipal(idCliente);
    }

    @POST
    @Transactional
    public Response criar(@PathParam("id_cliente") Long idCliente,
                          @Valid EnderecoRequestDTO dto) {
        EnderecoResponseDTO response = enderecoService.criar(idCliente, dto);
        return Response.created(URI.create("/clientes/" + idCliente + "/enderecos/" + response.idEndereco))
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id_endereco}")
    @Transactional
    public EnderecoResponseDTO atualizar(@PathParam("id_cliente") Long idCliente,
                                         @PathParam("id_endereco") Long idEndereco,
                                         @Valid EnderecoRequestDTO dto) {
        return enderecoService.atualizar(idCliente, idEndereco, dto);
    }

    @DELETE
    @Path("/{id_endereco}")
    @Transactional
    public void deletar(@PathParam("id_cliente") Long idCliente,
                        @PathParam("id_endereco") Long idEndereco) {
        enderecoService.excluir(idCliente, idEndereco);
    }

    @PATCH
    @Path("/{id_endereco}/principal")
    @Transactional
    public Response definirPrincipal(@PathParam("id_cliente") Long idCliente,
                                     @PathParam("id_endereco") Long idEndereco) {
        enderecoService.definirPrincipal(idCliente, idEndereco);
        return Response.noContent().build();  // 204
    }
}