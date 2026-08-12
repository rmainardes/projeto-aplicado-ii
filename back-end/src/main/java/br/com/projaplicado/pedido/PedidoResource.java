package br.com.projaplicado.pedido;

import br.com.projaplicado.itempedido.api.ItemPedidoDTO;
import br.com.projaplicado.itempedido.domain.ItemPedido;
import br.com.projaplicado.pedido.api.PedidoCriacaoDTO;
import br.com.projaplicado.pedido.api.PedidoDTO;
import br.com.projaplicado.pedido.api.PedidoMapper;
import br.com.projaplicado.pedido.api.StatusPedido;
import br.com.projaplicado.pedido.domain.Pedido;
import br.com.projaplicado.pedido.domain.repository.PedidoRepository;
import br.com.projaplicado.pedido.service.PedidoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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
@RolesAllowed({"ADMIN", "FUNCIONARIO"})
public class PedidoResource {

    @Inject
    PedidoService pedidoService;

    @Inject
    PedidoRepository pedidoRepository;

    @Inject
    PedidoMapper pedidoMapper;

    @GET
    public List<PedidoDTO> listar() {
        return pedidoRepository.listAll().stream()
                .map(pedidoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id_pedido}")
    public PedidoDTO buscar(@PathParam("id_pedido") Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null) {
            throw new NotFoundException("Pedido não encontrado: " + idPedido);
        }
        return pedidoMapper.toDTO(pedido);
    }

    @POST
    @Transactional
    public Response criar(@Valid PedidoCriacaoDTO dto) {
        Pedido pedido = pedidoService.criarPedido(dto);

        return Response.created(URI.create("/pedidos/" + pedido.idPedido))
                .entity(pedidoMapper.toDTO(pedido))
                .build();
    }

    @PUT
    @Path("/{id_pedido}")
    @Transactional
    public PedidoDTO atualizar(@PathParam("id_pedido") Long idPedido, PedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(idPedido);
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
            if (dto.status == StatusPedido.cancelado && pedido.status != StatusPedido.cancelado) {
                pedidoService.estornarEstoqueDoPedido(pedido.idPedido);
            }
            pedido.status = dto.status;
        }

        return pedidoMapper.toDTO(pedido);
    }

    @POST
    @Path("/{id_pedido}/itens")
    @Transactional
    public Response adicionarItem(@PathParam("id_pedido") Long idPedido, @Valid ItemPedidoDTO dto) {
        ItemPedido itemSalvo = pedidoService.adicionarItem(idPedido, dto);

        return Response.status(Response.Status.CREATED)
                .entity(pedidoMapper.toItemDTO(itemSalvo))
                .build();
    }

    @PUT
    @Path("/{id_pedido}/itens/{id_item}")
    @Transactional
    public ItemPedidoDTO atualizarItem(@PathParam("id_pedido") Long idPedido,
                                       @PathParam("id_item") Long idItem,
                                       @Valid ItemPedidoDTO dto) {
        ItemPedido itemAtualizado = pedidoService.atualizarItem(idPedido, idItem, dto);
        return pedidoMapper.toItemDTO(itemAtualizado);
    }

    @DELETE
    @Path("/{id_pedido}/itens/{id_item}")
    @Transactional
    public Response removerItem(@PathParam("id_pedido") Long idPedido, @PathParam("id_item") Long idItem) {
        pedidoService.removerItem(idPedido, idItem);

        return Response.noContent().build();
    }

}
