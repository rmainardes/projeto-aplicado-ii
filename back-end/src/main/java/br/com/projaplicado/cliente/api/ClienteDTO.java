package br.com.projaplicado.cliente.api;

import jakarta.validation.constraints.NotNull;

public class ClienteDTO {
    public Long idCliente;

    @NotNull(message = "nome e obrigatorio")
    public String nome;
    public String contato;
}
