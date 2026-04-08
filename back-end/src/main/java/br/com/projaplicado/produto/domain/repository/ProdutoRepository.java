package br.com.projaplicado.produto.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import br.com.projaplicado.produto.domain.Produto;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepository<Produto> {
}