package br.com.projaplicado.cliente.api;

import br.com.projaplicado.cliente.domain.Cliente;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteMapper {

    public ClienteDTO toDTO(Cliente entity) {
        if (entity == null) {
            return null;
        }

        ClienteDTO dto = new ClienteDTO();
        dto.idCliente = entity.idCliente;
        dto.nome = entity.nome;
        dto.contato = entity.contato;

        return dto;
    }
}
