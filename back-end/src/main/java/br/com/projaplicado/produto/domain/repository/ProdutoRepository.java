package br.com.projaplicado.produto.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import br.com.projaplicado.produto.domain.Produto;
import java.util.List;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepository<Produto> {

	public List<Produto> listarTodos() {
		return listAll();
	}

	public Produto buscarPorId(Long idProduto) {
		return findById(idProduto);
	}

	public void salvar(Produto produto) {
		persistAndFlush(produto);
	}

	public boolean deletarPorId(Long idProduto) {
		return deleteById(idProduto);
	}
}