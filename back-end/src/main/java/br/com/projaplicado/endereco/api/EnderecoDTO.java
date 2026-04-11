package br.com.projaplicado.endereco.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EnderecoDTO {

    public Long idEndereco;

    @NotNull(message = "idCliente e obrigatorio")
    public Long idCliente;

    @NotBlank(message = "descricao e obrigatoria")
    @Size(max = 15, message = "descricao deve ter no maximo 15 caracteres")
    public String descricao;

    @NotBlank(message = "cep e obrigatorio")
    @Pattern(regexp = "\\d{8}", message = "cep deve conter 8 digitos")
    public String cep;

    @NotBlank(message = "logradouro e obrigatorio")
    @Size(max = 60, message = "logradouro deve ter no maximo 60 caracteres")
    public String logradouro;

    @NotBlank(message = "numero e obrigatorio")
    @Size(max = 10, message = "numero deve ter no maximo 10 caracteres")
    public String numero;

    @Size(max = 30, message = "complemento deve ter no maximo 30 caracteres")
    public String complemento;

    @NotBlank(message = "bairro e obrigatorio")
    @Size(max = 40, message = "bairro deve ter no maximo 40 caracteres")
    public String bairro;

    @NotBlank(message = "cidade e obrigatoria")
    @Size(max = 40, message = "cidade deve ter no maximo 40 caracteres")
    public String cidade;

    @NotNull(message = "uf e obrigatoria")
    public UF uf;

    @Size(max = 60, message = "referencia deve ter no maximo 60 caracteres")
    public String referencia;

    public Boolean principal = false;

    public Boolean ativo = true;
}