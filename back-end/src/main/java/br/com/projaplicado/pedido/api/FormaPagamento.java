package br.com.projaplicado.pedido.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FormaPagamento {
    PIX("pix"),
    VOUCHER("voucher"),
    CARTAO_CREDITO("cartao_credito"),
    CARTAO_DEBITO("cartao_debito"),
    DINHEIRO("dinheiro");

    private final String value;

    FormaPagamento(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FormaPagamento fromValue(String value) {
        for (FormaPagamento item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("FormaPagamento invalida: " + value);
    }
}

