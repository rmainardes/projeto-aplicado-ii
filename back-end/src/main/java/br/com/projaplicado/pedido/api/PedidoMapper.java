package br.com.projaplicado.pedido.api;

import br.com.projaplicado.itempedido.api.ItemPedidoDTO;
import br.com.projaplicado.itempedido.domain.ItemPedido;
import br.com.projaplicado.pedido.domain.Pedido;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PedidoMapper {

    public PedidoDTO toDTO(Pedido entity) {
        if (entity == null) {
            return null;
        }

        PedidoDTO dto = new PedidoDTO();
        dto.idPedido = entity.idPedido;
        dto.idCliente = entity.idCliente;
        dto.valor = entity.valor;
        dto.data = entity.data;
        dto.formaPagamento = entity.formaPagamento;
        dto.status = entity.status;
        dto.observacao = entity.observacao;
        dto.tipoPedido = entity.tipoPedido;
        dto.itens = entity.itens.stream().map(this::toItemDTO).toList();

        return dto;
    }

    public ItemPedidoDTO toItemDTO(ItemPedido entity) {
        if (entity == null) {
            return null;
        }

        ItemPedidoDTO dto = new ItemPedidoDTO();
        dto.idItem = entity.idItem;
        dto.idPedido = entity.pedido.idPedido;
        dto.idProduto = entity.idProduto;
        dto.quantidade = entity.quantidade;
        dto.precoUnitario = entity.precoUnitario;

        return dto;
    }
}
