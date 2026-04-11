package br.com.projaplicado.endereco.service;

import br.com.projaplicado.cliente.domain.Cliente;
import br.com.projaplicado.endereco.api.EnderecoMapper;
import br.com.projaplicado.endereco.api.EnderecoRequestDTO;
import br.com.projaplicado.endereco.api.EnderecoResponseDTO;
import br.com.projaplicado.endereco.api.UF;
import br.com.projaplicado.endereco.domain.Endereco;
import br.com.projaplicado.endereco.domain.EnderecoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final EnderecoMapper enderecoMapper;

    public EnderecoService(EnderecoRepository enderecoRepository,
                           EnderecoMapper enderecoMapper) {
        this.enderecoRepository = enderecoRepository;
        this.enderecoMapper = enderecoMapper;
    }

    public List<EnderecoResponseDTO> listarPorCliente(Long idCliente) {
        validarClienteExiste(idCliente);

        return enderecoRepository.listarPorCliente(idCliente)
                .stream()
                .map(enderecoMapper::toResponseDTO)
                .toList();
    }

    public EnderecoResponseDTO buscarPrincipal(Long idCliente) {
        validarClienteExiste(idCliente);

        Endereco endereco = enderecoRepository.buscarPrincipal(idCliente)
                .orElseThrow(() -> new NotFoundException("Endereco principal nao encontrado"));

        return enderecoMapper.toResponseDTO(endereco);
    }

    public List<EnderecoResponseDTO> buscarPorDescricao(Long idCliente, String descricao) {
        validarClienteExiste(idCliente);

        String descricaoNormalizada = descricao == null ? null : descricao.trim();
        return enderecoRepository.buscarPorDescricao(idCliente, descricaoNormalizada)
                .stream()
                .map(enderecoMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public EnderecoResponseDTO criar(Long idCliente, EnderecoRequestDTO dto) {
        Cliente cliente = buscarClienteOuFalhar(idCliente);

        Endereco endereco = enderecoMapper.toEntity(dto);
        endereco.cliente = cliente;
        endereco.ativo = true;

        validarAreaEntrega(endereco);

        boolean clienteNaoTemPrincipal = enderecoRepository.buscarPrincipal(idCliente).isEmpty();
        boolean marcarComoPrincipal = Boolean.TRUE.equals(dto.principal) || clienteNaoTemPrincipal;

        if (marcarComoPrincipal) {
            desmarcarPrincipais(idCliente);
            endereco.principal = true;
        } else {
            endereco.principal = false;
        }

        enderecoRepository.persist(endereco);
        return enderecoMapper.toResponseDTO(endereco);
    }

    @Transactional
    public EnderecoResponseDTO atualizar(Long idCliente, Long idEndereco, EnderecoRequestDTO dto) {
        validarClienteExiste(idCliente);

        Endereco endereco = enderecoRepository.buscarPorIdECliente(idEndereco, idCliente)
                .orElseThrow(() -> new NotFoundException("Endereco nao encontrado"));

        boolean eraPrincipal = Boolean.TRUE.equals(endereco.principal);

        enderecoMapper.updateEntity(dto, endereco);
        validarAreaEntrega(endereco);

        if (Boolean.TRUE.equals(dto.principal)) {
            desmarcarPrincipais(idCliente);
            endereco.principal = true;
        } else if (eraPrincipal) {
            endereco.principal = true;
        }

        return enderecoMapper.toResponseDTO(endereco);
    }

    @Transactional
    public void excluir(Long idCliente, Long idEndereco) {
        validarClienteExiste(idCliente);

        Endereco endereco = enderecoRepository.buscarPorIdECliente(idEndereco, idCliente)
                .orElseThrow(() -> new NotFoundException("Endereco nao encontrado"));

        boolean eraPrincipal = Boolean.TRUE.equals(endereco.principal);

        endereco.ativo = false;
        endereco.principal = false;

        if (eraPrincipal) {
            enderecoRepository.buscarPrimeiroAtivoExceto(idCliente, idEndereco)
                    .ifPresent(novoPrincipal -> novoPrincipal.principal = true);
        }
    }

    @Transactional
    public void definirPrincipal(Long idCliente, Long idEndereco) {
        validarClienteExiste(idCliente);

        Endereco endereco = enderecoRepository.buscarPorIdECliente(idEndereco, idCliente)
                .orElseThrow(() -> new NotFoundException("Endereco nao encontrado"));

        desmarcarPrincipais(idCliente);
        endereco.principal = true;
    }

    private Cliente buscarClienteOuFalhar(Long idCliente) {
        Cliente cliente = Cliente.findById(idCliente);
        if (cliente == null) {
            throw new NotFoundException("Cliente nao encontrado");
        }
        return cliente;
    }

    private void validarClienteExiste(Long idCliente) {
        if (Cliente.findById(idCliente) == null) {
            throw new NotFoundException("Cliente nao encontrado");
        }
    }

    private void validarAreaEntrega(Endereco endereco) {
        if (!"Blumenau".equalsIgnoreCase(endereco.cidade)) {
            throw new BadRequestException("Delivery disponível apenas para Blumenau");
        }

        if (!endereco.uf.equals(UF.SC)) {
            throw new BadRequestException("Delivery disponível apenas para Santa Catarina");
        }
    }

    private void desmarcarPrincipais(Long idCliente) {
        enderecoRepository.listarAtivosPorCliente(idCliente)
                .forEach(endereco -> endereco.principal = false);
    }
}