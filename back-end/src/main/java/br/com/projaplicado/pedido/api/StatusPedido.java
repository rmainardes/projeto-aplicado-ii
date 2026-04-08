package br.com.projaplicado.pedido.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusPedido {
    PENDENTE("pendente"),
    PREPARANDO("preparando"),
    PRONTO("pronto"),
    ENTREGUE("entregue"),
    CANCELADO("cancelado");

    private final String value;

    StatusPedido(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StatusPedido fromValue(String value) {
        for (StatusPedido item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("StatusPedido invalido: " + value);
    }
}

