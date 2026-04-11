package br.com.projaplicado.endereco.service;

import br.com.projaplicado.endereco.api.CepResponseDTO;
import br.com.projaplicado.endereco.api.ViaCepResponseDTO;
import br.com.projaplicado.endereco.client.ViaCepClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ViaCepService {

    private final ViaCepClient viaCepClient;

    public ViaCepService(@RestClient ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    public CepResponseDTO consultarCep(String cep) {
        String cepNormalizado = normalizarCep(cep);

        try {
            ViaCepResponseDTO response = viaCepClient.consultarCep(cepNormalizado);

            if (response == null) {
                throw new BadRequestException("Nao foi possivel consultar o CEP informado");
            }

            if (Boolean.TRUE.equals(response.erro)) {
                throw new NotFoundException("CEP nao encontrado");
            }

            CepResponseDTO dto = new CepResponseDTO();
            dto.cep = response.cep != null ? response.cep.replaceAll("\\D", "") : cepNormalizado;
            dto.logradouro = response.logradouro;
            dto.bairro = response.bairro;
            dto.cidade = response.localidade;
            dto.uf = response.uf;
            dto.encontrado = true;
            return dto;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Erro ao consultar CEP");
        }
    }

    private String normalizarCep(String cep) {
        if (cep == null) {
            throw new BadRequestException("CEP e obrigatorio");
        }

        String cepNormalizado = cep.replaceAll("\\D", "");

        if (!cepNormalizado.matches("\\d{8}")) {
            throw new BadRequestException("CEP deve conter 8 digitos");
        }

        return cepNormalizado;
    }
}