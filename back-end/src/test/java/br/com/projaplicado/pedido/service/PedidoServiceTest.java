package br.com.projaplicado.pedido.service;

import br.com.projaplicado.cliente.domain.Cliente;
import br.com.projaplicado.cliente.domain.repository.ClienteRepository;
import br.com.projaplicado.itempedido.api.ItemPedidoDTO;
import br.com.projaplicado.itempedido.domain.ItemPedido;
import br.com.projaplicado.pedido.api.FormaPagamento;
import br.com.projaplicado.pedido.api.PedidoCriacaoDTO;
import br.com.projaplicado.pedido.api.TipoPedido;
import br.com.projaplicado.pedido.domain.Pedido;
import br.com.projaplicado.produto.domain.Produto;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Chama o PedidoService diretamente (sem passar pelo HTTP) pra poder rodar
// tudo dentro de uma única transação de teste que é revertida no final — não
// deixa nenhum resíduo no banco (compartilhado com o dev), mesmo em falha.
@QuarkusTest
class PedidoServiceTest {

    @Inject
    PedidoService pedidoService;

    @Inject
    ClienteRepository clienteRepository;

    @Test
    @TestTransaction
    void criarPedidoComEstoqueSuficienteDecrementaEstoque() {
        Produto produto = criarProduto(10);
        Cliente cliente = criarCliente();

        Pedido pedido = pedidoService.criarPedido(pedidoDTO(cliente.idCliente, produto.idProduto, 3));

        assertEquals(0, new BigDecimal("30.00").compareTo(pedido.valor));
        assertEquals(7, Produto.<Produto>findById(produto.idProduto).quantidadeEstoque);
    }

    @Test
    @TestTransaction
    void criarPedidoComEstoqueInsuficienteLancaBadRequestENaoAlteraEstoque() {
        Produto produto = criarProduto(2);
        Cliente cliente = criarCliente();

        assertThrows(BadRequestException.class,
                () -> pedidoService.criarPedido(pedidoDTO(cliente.idCliente, produto.idProduto, 5)));

        assertEquals(2, Produto.<Produto>findById(produto.idProduto).quantidadeEstoque);
    }

    @Test
    @TestTransaction
    void criarPedidoTipoLocalSemClienteUsaClientePadrao() {
        Cliente clienteLocal = clienteRepository.getClienteLocalPadrao();
        if (clienteLocal == null) {
            clienteLocal = new Cliente();
            clienteLocal.nome = "CONSUMO NO LOCAL";
            clienteLocal.contato = "local@teste.com";
            clienteLocal.persist();
        }

        Produto produto = criarProduto(10);

        PedidoCriacaoDTO dto = pedidoDTO(null, produto.idProduto, 1);
        dto.tipoPedido = TipoPedido.local;

        Pedido pedido = pedidoService.criarPedido(dto);

        assertEquals(clienteLocal.idCliente, pedido.idCliente);
    }

    @Test
    @TestTransaction
    void criarPedidoDeliverySemClienteLancaBadRequest() {
        Produto produto = criarProduto(10);

        PedidoCriacaoDTO dto = pedidoDTO(null, produto.idProduto, 1);
        dto.tipoPedido = TipoPedido.delivery;

        assertThrows(BadRequestException.class, () -> pedidoService.criarPedido(dto));
    }

    @Test
    @TestTransaction
    void estornarEstoqueDoPedidoRestauraQuantidade() {
        Produto produto = criarProduto(10);
        Cliente cliente = criarCliente();

        Pedido pedido = pedidoService.criarPedido(pedidoDTO(cliente.idCliente, produto.idProduto, 4));
        assertEquals(6, Produto.<Produto>findById(produto.idProduto).quantidadeEstoque);

        pedidoService.estornarEstoqueDoPedido(pedido.idPedido);

        assertEquals(10, Produto.<Produto>findById(produto.idProduto).quantidadeEstoque);
    }

    @Test
    @TestTransaction
    void adicionarItemAlemDoEstoqueLancaBadRequestENaoAlteraPedido() {
        Produto produto = criarProduto(5);
        Cliente cliente = criarCliente();
        Pedido pedido = pedidoService.criarPedido(pedidoDTO(cliente.idCliente, produto.idProduto, 2));

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.idProduto = produto.idProduto;
        item.quantidade = 10;

        assertThrows(BadRequestException.class, () -> pedidoService.adicionarItem(pedido.idPedido, item));
        assertEquals(3, Produto.<Produto>findById(produto.idProduto).quantidadeEstoque);
    }

    @Test
    @TestTransaction
    void removerItemRestauraEstoqueEDiminuiValor() {
        Produto produto = criarProduto(10);
        Cliente cliente = criarCliente();
        Pedido pedido = pedidoService.criarPedido(pedidoDTO(cliente.idCliente, produto.idProduto, 4));
        ItemPedido item = pedido.itens.get(0);

        pedidoService.removerItem(pedido.idPedido, item.idItem);

        assertEquals(10, Produto.<Produto>findById(produto.idProduto).quantidadeEstoque);
        assertEquals(0, BigDecimal.ZERO.compareTo(Pedido.<Pedido>findById(pedido.idPedido).valor));
    }

    @Test
    @TestTransaction
    void removerItemDeOutroPedidoLancaNotFound() {
        Produto produto = criarProduto(10);
        Cliente cliente = criarCliente();
        Pedido pedidoA = pedidoService.criarPedido(pedidoDTO(cliente.idCliente, produto.idProduto, 1));
        Pedido pedidoB = pedidoService.criarPedido(pedidoDTO(cliente.idCliente, produto.idProduto, 1));
        ItemPedido itemDoPedidoA = pedidoA.itens.get(0);

        assertThrows(NotFoundException.class,
                () -> pedidoService.removerItem(pedidoB.idPedido, itemDoPedidoA.idItem));
    }

    private Produto criarProduto(int quantidadeEstoque) {
        Produto produto = new Produto();
        produto.nome = "Produto Teste";
        produto.descricao = "Descrição de teste";
        produto.preco = BigDecimal.TEN;
        produto.quantidadeEstoque = quantidadeEstoque;
        produto.ativo = true;
        produto.persist();
        return produto;
    }

    private Cliente criarCliente() {
        Cliente cliente = new Cliente();
        cliente.nome = "Cliente Teste";
        cliente.contato = "47999999999";
        cliente.persist();
        return cliente;
    }

    private PedidoCriacaoDTO pedidoDTO(Long idCliente, Long idProduto, int quantidade) {
        ItemPedidoDTO item = new ItemPedidoDTO();
        item.idProduto = idProduto;
        item.quantidade = quantidade;

        PedidoCriacaoDTO dto = new PedidoCriacaoDTO();
        dto.idCliente = idCliente;
        dto.formaPagamento = FormaPagamento.pix;
        dto.tipoPedido = TipoPedido.retirada;
        dto.itens = List.of(item);
        return dto;
    }
}
