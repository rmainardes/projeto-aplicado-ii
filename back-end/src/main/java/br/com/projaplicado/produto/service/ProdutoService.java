package br.com.projaplicado.produto.service;

import br.com.projaplicado.produto.api.ProdutoDTO;
import br.com.projaplicado.produto.domain.Produto;
import br.com.projaplicado.produto.domain.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class ProdutoService {

    @Inject
    ProdutoRepository produtoRepository;

    public List<Produto> listarTodos() {
        return produtoRepository.listarTodos();
    }

    public Produto buscarPorId(Long idProduto) {
        return produtoRepository.buscarPorId(idProduto);
    }

    @Transactional
    public Produto criar(ProdutoDTO dto) {
        Produto produto = new Produto();
        produto.nome = dto.nome.trim();
        produto.descricao = dto.descricao.trim();
        produto.preco = dto.preco;
        produto.quantidadeEstoque = dto.quantidadeEstoque;
        produtoRepository.salvar(produto);
        return produto;
    }

    @Transactional
    public Produto atualizar(Long idProduto, ProdutoDTO dto) {
        Produto produto = produtoRepository.buscarPorId(idProduto);
        if (produto == null) {
            throw new NotFoundException("Produto não encontrado: " + idProduto);
        }

        if (dto.nome != null && !dto.nome.trim().isEmpty()) {
            produto.nome = dto.nome.trim();
        }

        if (dto.descricao != null && !dto.descricao.trim().isEmpty()) {
            produto.descricao = dto.descricao.trim();
        }

        if (dto.preco != null) {
            produto.preco = dto.preco;
        }

        if (dto.quantidadeEstoque != null) {
            produto.quantidadeEstoque = dto.quantidadeEstoque;
        }

        return produto;
    }

    @Transactional
    public boolean deletar(Long idProduto) {
        return produtoRepository.deletarPorId(idProduto);
    }
}

