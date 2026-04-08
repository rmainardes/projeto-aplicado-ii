package br.com.projaplicado.pedido.domain;

import br.com.projaplicado.pedido.api.StatusPedido;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatusPedidoConverter implements AttributeConverter<StatusPedido, String> {

    @Override
    public String convertToDatabaseColumn(StatusPedido attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StatusPedido convertToEntityAttribute(String dbData) {
        return dbData == null ? null : StatusPedido.fromValue(dbData);
    }
}

