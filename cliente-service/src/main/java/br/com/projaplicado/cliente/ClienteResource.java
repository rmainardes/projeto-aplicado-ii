package br.com.projaplicado.cliente;

import br.com.projaplicado.cliente.api.ClienteDTO;
import br.com.projaplicado.cliente.domain.Cliente;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    @GET
    public List<ClienteDTO> listar() {
        return Cliente.<Cliente>listAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public ClienteDTO buscar(@PathParam("id") Long id) {
        Cliente cliente = Cliente.findById(id);
        if (cliente == null) {
            throw new NotFoundException("Cliente não encontrado: " + id);
        }
        return toDTO(cliente);
    }

    @POST
    @Transactional
    public Response criar(ClienteDTO dto) {
        if (dto.nome == null || dto.nome.trim().isEmpty()) {
            throw new BadRequestException("Nome é obrigatório");
        }

        Cliente cliente = new Cliente();
        cliente.nome = dto.nome.trim();
        cliente.persistAndFlush();

        return Response.created(URI.create("/clientes/" + cliente.id))
                .entity(toDTO(cliente))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public ClienteDTO atualizar(@PathParam("id") Long id, ClienteDTO dto) {
        Cliente cliente = Cliente.findById(id);
        if (cliente == null) {
            throw new NotFoundException("Cliente não encontrado: " + id);
        }

        if (dto.nome != null && !dto.nome.trim().isEmpty()) {
            cliente.nome = dto.nome.trim();
        }

        return toDTO(cliente);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void deletar(@PathParam("id") Long id) {
        boolean deletado = Cliente.deleteById(id);
        if (!deletado) {
            throw new NotFoundException("Cliente não encontrado: " + id);
        }
    }

    private ClienteDTO toDTO(Cliente c) {
        ClienteDTO dto = new ClienteDTO();
        dto.id = c.id;
        dto.nome = c.nome;
        return dto;
    }
}
