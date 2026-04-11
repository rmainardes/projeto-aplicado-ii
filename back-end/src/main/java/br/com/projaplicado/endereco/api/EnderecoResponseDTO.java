package br.com.projaplicado.endereco.api;

import java.time.Instant;

public class EnderecoResponseDTO {
    public Long idEndereco;
    public Long idCliente;
    public String descricao;
    public String cep;
    public String logradouro;
    public String numero;
    public String complemento;
    public String bairro;
    public String cidade;
    public UF uf;
    public String referencia;
    public Boolean principal;
    public Boolean ativo;
    public Instant createdAt;
    public Instant updatedAt;
}