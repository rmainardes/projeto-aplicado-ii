package br.com.projaplicado.endereco.api;

import br.com.projaplicado.endereco.domain.Endereco;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EnderecoMapper {

    public EnderecoResponseDTO toResponseDTO(Endereco entity) {
        if (entity == null) {
            return null;
        }

        EnderecoResponseDTO dto = new EnderecoResponseDTO();
        dto.idEndereco = entity.idEndereco;
        dto.idCliente = entity.cliente != null ? entity.cliente.idCliente : null;
        dto.descricao = entity.descricao;
        dto.cep = entity.cep;
        dto.logradouro = entity.logradouro;
        dto.numero = entity.numero;
        dto.complemento = entity.complemento;
        dto.bairro = entity.bairro;
        dto.cidade = entity.cidade;
        dto.uf = entity.uf;
        dto.referencia = entity.referencia;
        dto.principal = entity.principal;
        dto.ativo = entity.ativo;
        dto.createdAt = entity.createdAt;
        dto.updatedAt = entity.updatedAt;

        return dto;
    }

    public Endereco toEntity(EnderecoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Endereco entity = new Endereco();
        apply(dto, entity);
        return entity;
    }

    public void updateEntity(EnderecoRequestDTO dto, Endereco entity) {
        if (dto == null || entity == null) {
            return;
        }

        apply(dto, entity);
    }

    private void apply(EnderecoRequestDTO dto, Endereco entity) {
        entity.descricao = trim(dto.descricao);
        entity.cep = normalizarCep(dto.cep);
        entity.logradouro = trim(dto.logradouro);
        entity.numero = trim(dto.numero);
        entity.complemento = trimNullable(dto.complemento);
        entity.bairro = trim(dto.bairro);
        entity.cidade = trim(dto.cidade);
        entity.uf = dto.uf;
        entity.referencia = trimNullable(dto.referencia);
        entity.principal = Boolean.TRUE.equals(dto.principal);
    }

    private String normalizarCep(String cep) {
        return cep == null ? null : cep.replaceAll("\\D", "");
    }

    private String trim(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String trimNullable(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }
}