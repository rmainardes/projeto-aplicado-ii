package br.com.projaplicado.pedido.domain;

import br.com.projaplicado.pedido.api.FormaPagamento;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FormaPagamentoConverter implements AttributeConverter<FormaPagamento, String> {

    @Override
    public String convertToDatabaseColumn(FormaPagamento attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public FormaPagamento convertToEntityAttribute(String dbData) {
        return dbData == null ? null : FormaPagamento.fromValue(dbData);
    }
}

