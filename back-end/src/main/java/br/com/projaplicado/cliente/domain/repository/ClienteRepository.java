package br.com.projaplicado.cliente.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import br.com.projaplicado.cliente.domain.Cliente;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    public Cliente getClienteLocalPadrao() {
        return find("nome", "CONSUMO NO LOCAL").firstResult();
    }
}
