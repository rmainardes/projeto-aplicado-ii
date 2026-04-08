package br.com.projaplicado.pedido.domain;

import br.com.projaplicado.pedido.api.TipoPedido;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TipoPedidoConverter implements AttributeConverter<TipoPedido, String> {

    @Override
    public String convertToDatabaseColumn(TipoPedido attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TipoPedido convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TipoPedido.fromValue(dbData);
    }
}

