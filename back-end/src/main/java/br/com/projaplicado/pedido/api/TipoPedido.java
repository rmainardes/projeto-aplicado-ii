package br.com.projaplicado.pedido.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoPedido {
    DELIVERY("delivery"),
    RETIRADA("retirada"),
    LOCAL("local");

    private final String value;

    TipoPedido(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TipoPedido fromValue(String value) {
        for (TipoPedido item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("TipoPedido invalido: " + value);
    }
}

