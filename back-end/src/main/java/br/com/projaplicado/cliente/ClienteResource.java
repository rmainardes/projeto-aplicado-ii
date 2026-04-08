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
    @Path("/{id_cliente}")
    public ClienteDTO buscar(@PathParam("id_cliente") Long idCliente) {
        Cliente cliente = Cliente.findById(idCliente);
        if (cliente == null) {
            throw new NotFoundException("Cliente não encontrado: " + idCliente);
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
        cliente.contato = normalizeContato(dto.contato);
        cliente.persistAndFlush();

        return Response.created(URI.create("/clientes/" + cliente.idCliente))
                .entity(toDTO(cliente))
                .build();
    }

    @PUT
    @Path("/{id_cliente}")
    @Transactional
    public ClienteDTO atualizar(@PathParam("id_cliente") Long idCliente, ClienteDTO dto) {
        Cliente cliente = Cliente.findById(idCliente);
        if (cliente == null) {
            throw new NotFoundException("Cliente não encontrado: " + idCliente);
        }

        if (dto.nome != null && !dto.nome.trim().isEmpty()) {
            cliente.nome = dto.nome.trim();
        }

        if (dto.contato != null) {
            cliente.contato = normalizeContato(dto.contato);
        }

        return toDTO(cliente);
    }

    @DELETE
    @Path("/{id_cliente}")
    @Transactional
    public void deletar(@PathParam("id_cliente") Long idCliente) {
        boolean deletado = Cliente.deleteById(idCliente);
        if (!deletado) {
            throw new NotFoundException("Cliente não encontrado: " + idCliente);
        }
    }

    private ClienteDTO toDTO(Cliente c) {
        ClienteDTO dto = new ClienteDTO();
        dto.idCliente = c.idCliente;
        dto.nome = c.nome;
        dto.contato = c.contato;
        return dto;
    }

    private String normalizeContato(String contato) {
        if (contato == null) {
            return null;
        }

        String normalized = contato.trim();
        if (normalized.isEmpty()) {
            return null;
        }


        return normalized;
    }
}
