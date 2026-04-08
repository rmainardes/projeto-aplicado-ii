package br.com.projaplicado.pedido.service;

import br.com.projaplicado.itempedido.api.ItemPedidoDTO;
import br.com.projaplicado.itempedido.domain.ItemPedido;
import br.com.projaplicado.pedido.api.PedidoCriacaoDTO;
import br.com.projaplicado.pedido.api.StatusPedido;
import br.com.projaplicado.pedido.domain.Pedido;
import br.com.projaplicado.produto.domain.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ApplicationScoped
public class PedidoService {

    @Inject
    EntityManager entityManager;


    @Transactional
    public Pedido criarPedido(PedidoCriacaoDTO dto) {
        Map<Long, Integer> quantidadePorProduto = new HashMap<>();
        Map<Long, Produto> produtos = new HashMap<>();

        for (ItemPedidoDTO item : dto.itens) {
            quantidadePorProduto.merge(item.idProduto, item.quantidade, Integer::sum);
            if (!produtos.containsKey(item.idProduto)) {
                Produto produto = entityManager.find(Produto.class, item.idProduto, LockModeType.PESSIMISTIC_WRITE);
                if (produto == null) {
                    throw new NotFoundException("Produto não encontrado: " + item.idProduto);
                }
                produtos.put(item.idProduto, produto);
            }
        }

        for (Map.Entry<Long, Integer> entry : quantidadePorProduto.entrySet()) {
            Produto produto = produtos.get(entry.getKey());
            if (produto.quantidadeEstoque < entry.getValue()) {
                throw new BadRequestException("Estoque insuficiente para produto " + entry.getKey());
            }
        }

        Pedido pedido = new Pedido();
        pedido.idCliente = dto.idCliente;
        pedido.data = LocalDateTime.now();
        pedido.formaPagamento = dto.formaPagamento;
        pedido.tipoPedido = dto.tipoPedido;
        pedido.status = StatusPedido.pendente;
        pedido.observacao = dto.observacao != null && dto.observacao.trim().isEmpty() ? null : dto.observacao;
        pedido.valor = BigDecimal.ZERO;

        for (ItemPedidoDTO item : dto.itens) {
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.pedido = pedido;
            itemPedido.idProduto = item.idProduto;
            itemPedido.quantidade = item.quantidade;
            itemPedido.precoUnitario = produtos.get(item.idProduto).preco;

            BigDecimal subtotal = itemPedido.precoUnitario.multiply(BigDecimal.valueOf(itemPedido.quantidade));
            pedido.valor = pedido.valor.add(subtotal);

            pedido.itens.add(itemPedido);
        }

        entityManager.persist(pedido);

        for (Map.Entry<Long, Integer> entry : quantidadePorProduto.entrySet()) {
            Produto produto = produtos.get(entry.getKey());
            produto.quantidadeEstoque = produto.quantidadeEstoque - entry.getValue();
        }

        return pedido;
    }

    @Transactional
    public void estornarEstoqueDoPedido(Long idPedido) {
        List<ItemPedido> itens = entityManager
                .createQuery("from ItemPedido i where i.pedido.idPedido = :idPedido", ItemPedido.class)
                .setParameter("idPedido", idPedido)
                .getResultList();
        for (ItemPedido item : itens) {
            Produto produto = entityManager.find(Produto.class, item.idProduto, LockModeType.PESSIMISTIC_WRITE);
            if (produto == null) {
                throw new NotFoundException("Produto não encontrado para estorno: " + item.idProduto);
            }
            produto.quantidadeEstoque = produto.quantidadeEstoque + item.quantidade;
        }
    }

    @Transactional
    public ItemPedido adicionarItem(Long idPedido, ItemPedidoDTO dto) {
        Pedido pedido = entityManager.find(Pedido.class, idPedido);
        if (pedido == null) {
            throw new NotFoundException("Pedido não encontrado: " + idPedido);
        }
        if (pedido.status != StatusPedido.pendente) {
            throw new BadRequestException("Só é possível adicionar itens a pedidos com status PENDENTE.");
        }

        Produto produto = entityManager.find(Produto.class, dto.idProduto, LockModeType.PESSIMISTIC_WRITE);
        if (produto == null) {
            throw new NotFoundException("Produto não encontrado: " + dto.idProduto);
        }

        if (produto.quantidadeEstoque < dto.quantidade) {
            throw new BadRequestException("Estoque insuficiente para o produto: " + produto.nome);
        }

        produto.quantidadeEstoque = produto.quantidadeEstoque - dto.quantidade;

        ItemPedido itemPedido = new ItemPedido();
        itemPedido.pedido = pedido;
        itemPedido.idProduto = dto.idProduto;
        itemPedido.quantidade = dto.quantidade;
        itemPedido.precoUnitario = produto.preco;

        entityManager.persist(itemPedido);

        BigDecimal subtotal = itemPedido.precoUnitario.multiply(BigDecimal.valueOf(itemPedido.quantidade));
        pedido.valor = pedido.valor.add(subtotal);

        return itemPedido;
    }

    @Transactional
    public void removerItem(Long idPedido, Long idItem) {
        Pedido pedido = entityManager.find(Pedido.class, idPedido);
        if (pedido == null) throw new NotFoundException("Pedido não encontrado: " + idPedido);
        if (pedido.status != StatusPedido.pendente) {
            throw new BadRequestException("Só é possível remover itens de pedidos com status PENDENTE.");
        }

        ItemPedido item = entityManager.find(ItemPedido.class, idItem);
        if (item == null || !item.pedido.idPedido.equals(idPedido)) {
            throw new NotFoundException("Item não pertence a este pedido ou não existe.");
        }

        Produto produto = entityManager.find(Produto.class, item.idProduto, LockModeType.PESSIMISTIC_WRITE);
        if (produto != null) {
            produto.quantidadeEstoque = produto.quantidadeEstoque + item.quantidade;
        }

        BigDecimal subtotal = item.precoUnitario.multiply(BigDecimal.valueOf(item.quantidade));
        pedido.valor = pedido.valor.subtract(subtotal);
        if (pedido.valor.compareTo(BigDecimal.ZERO) < 0) {
            pedido.valor = BigDecimal.ZERO;
        }

        entityManager.remove(item);
    }
}