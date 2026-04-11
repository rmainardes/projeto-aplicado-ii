package br.com.projaplicado.endereco.domain;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EnderecoRepository implements PanacheRepository<Endereco> {

    public List<Endereco> listarPorCliente(Long idCliente) {
        return list("cliente.idCliente = ?1 and ativo = true order by principal desc, descricao asc", idCliente);
    }

    public Optional<Endereco> buscarPorIdECliente(Long idEndereco, Long idCliente) {
        return find("idEndereco = ?1 and cliente.idCliente = ?2 and ativo = true", idEndereco, idCliente)
                .firstResultOptional();
    }

    public Optional<Endereco> buscarPrincipal(Long idCliente) {
        return find("cliente.idCliente = ?1 and principal = true and ativo = true", idCliente)
                .firstResultOptional();
    }

    public List<Endereco> buscarPorDescricao(Long idCliente, String descricao) {
        return list("cliente.idCliente = ?1 and upper(descricao) = upper(?2) and ativo = true", idCliente, descricao);
    }

    public List<Endereco> listarAtivosPorCliente(Long idCliente) {
        return list("cliente.idCliente = ?1 and ativo = true", idCliente);
    }

    public Optional<Endereco> buscarPrimeiroAtivoExceto(Long idCliente, Long idEnderecoIgnorado) {
        return find("cliente.idCliente = ?1 and ativo = true and idEndereco <> ?2 order by descricao asc",
                idCliente, idEnderecoIgnorado)
                .firstResultOptional();
    }

    public void flushChanges() {
        getEntityManager().flush();
    }
}