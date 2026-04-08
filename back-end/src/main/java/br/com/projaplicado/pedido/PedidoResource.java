package br.com.projaplicado.pedido;

import br.com.projaplicado.itempedido.api.ItemPedidoDTO;
import br.com.projaplicado.itempedido.domain.ItemPedido;
import br.com.projaplicado.pedido.api.PedidoCriacaoDTO;
import br.com.projaplicado.pedido.api.PedidoDTO;
import br.com.projaplicado.pedido.api.StatusPedido;
import br.com.projaplicado.pedido.domain.Pedido;
import br.com.projaplicado.pedido.service.PedidoService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PedidoResource {

    @Inject
    PedidoService pedidoService;

    @Inject
    EntityManager entityManager;

    @GET
    public List<PedidoDTO> listar() {
        return entityManager.createQuery("from Pedido", Pedido.class).getResultList().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id_pedido}")
    public PedidoDTO buscar(@PathParam("id_pedido") Long idPedido) {
        Pedido pedido = entityManager.find(Pedido.class, idPedido);
        if (pedido == null) {
            throw new NotFoundException("Pedido não encontrado: " + idPedido);
        }
        return toDTO(pedido);
    }

    @POST
    @Transactional
    public Response criar(@Valid PedidoCriacaoDTO dto) {
        Pedido pedido = pedidoService.criarPedido(dto);

        return Response.created(URI.create("/pedidos/" + pedido.idPedido))
                .entity(toDTO(pedido))
                .build();
    }

    @PUT
    @Path("/{id_pedido}")
    @Transactional
    public PedidoDTO atualizar(@PathParam("id_pedido") Long idPedido, PedidoDTO dto) {
        Pedido pedido = entityManager.find(Pedido.class, idPedido);
        if (pedido == null) {
            throw new NotFoundException("Pedido não encontrado: " + idPedido);
        }

        if (dto.formaPagamento != null) {
            pedido.formaPagamento = dto.formaPagamento;
        }

        if (dto.tipoPedido != null) {
            pedido.tipoPedido = dto.tipoPedido;
        }

        if (dto.observacao != null) {
            pedido.observacao = dto.observacao.trim().isEmpty() ? null : dto.observacao;
        }

        if (dto.status != null) {
            if (dto.status == StatusPedido.CANCELADO && pedido.status != StatusPedido.CANCELADO) {
                pedidoService.estornarEstoqueDoPedido(pedido.idPedido);
            }
            pedido.status = dto.status;
        }

        return toDTO(pedido);
    }

    @DELETE
    @Path("/{id_pedido}")
    @Transactional
    public void deletar(@PathParam("id_pedido") Long idPedido) {
        Pedido pedido = entityManager.find(Pedido.class, idPedido);
        if (pedido == null) {
            throw new NotFoundException("Pedido não encontrado: " + idPedido);
        }
        entityManager.remove(pedido);
    }

    private PedidoDTO toDTO(Pedido c) {
        PedidoDTO dto = new PedidoDTO();
        dto.idPedido = c.idPedido;
        dto.idCliente = c.idCliente;
        dto.valor = c.valor;
        dto.data = c.data;
        dto.formaPagamento = c.formaPagamento;
        dto.status = c.status;
        dto.observacao = c.observacao;
        dto.tipoPedido = c.tipoPedido;
        return dto;
    }

    @POST
    @Path("/{id_pedido}/itens")
    @Transactional
    public Response adicionarItem(@PathParam("id_pedido") Long idPedido, @Valid ItemPedidoDTO dto) {
        ItemPedido itemSalvo = pedidoService.adicionarItem(idPedido, dto);

        return Response.status(Response.Status.CREATED)
                .entity(toItemDTO(itemSalvo))
                .build();
    }

    @DELETE
    @Path("/{id_pedido}/itens/{id_item}")
    @Transactional
    public Response removerItem(@PathParam("id_pedido") Long idPedido, @PathParam("id_item") Long idItem) {
        pedidoService.removerItem(idPedido, idItem);

        return Response.noContent().build();
    }

    // Helper para converter o item de volta para DTO na resposta HTTP
    private ItemPedidoDTO toItemDTO(ItemPedido c) {
        ItemPedidoDTO dto = new ItemPedidoDTO();
        dto.idItem = c.idItem;
        dto.idPedido = c.idPedido;
        dto.idProduto = c.idProduto;
        dto.quantidade = c.quantidade;
        dto.precoUnitario = c.precoUnitario;
        return dto;
    }

}
