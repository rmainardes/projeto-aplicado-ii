package br.com.projaplicado.pedido.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import br.com.projaplicado.pedido.domain.Pedido;

@ApplicationScoped
public class PedidoRepository implements PanacheRepository<Pedido> {
}

