package br.com.projaplicado.cliente;

import br.com.projaplicado.cliente.api.ClienteDTO;
import br.com.projaplicado.cliente.domain.Cliente;
import br.com.projaplicado.cliente.domain.repository.ClienteRepository;
import jakarta.inject.Inject;
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

    @Inject
    ClienteRepository clienteRepository;


    @GET
    public List<ClienteDTO> listar() {
        return clienteRepository.listAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public String health() {
        return "OK";
    }

    @GET
    @Path("/{id_cliente}")
    public ClienteDTO buscar(@PathParam("id_cliente") Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente);
        if (cliente == null) {
            throw new NotFoundException("Cliente não encontrado: " + idCliente);
        }
        return toDTO(cliente);
    }

    @GET
    @Path("/local-padrao")
    public Response getClienteLocalPadrao() {
        Cliente cliente = clienteRepository.getClienteLocalPadrao();
        if (cliente == null) {
            return Response.status(404).entity("Cliente padrão não encontrado").build();
        }
        return Response.ok(cliente).build();
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
        clienteRepository.persistAndFlush(cliente);

        return Response.created(URI.create("/clientes/" + cliente.idCliente))
                .entity(toDTO(cliente))
                .build();
    }

    @PUT
    @Path("/{id_cliente}")
    @Transactional
    public ClienteDTO atualizar(@PathParam("id_cliente") Long idCliente, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(idCliente);
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
        boolean deletado = clienteRepository.deleteById(idCliente);
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
