package br.com.projaplicado.produto;

import br.com.projaplicado.produto.api.ProdutoDTO;
import br.com.projaplicado.produto.domain.Produto;
import br.com.projaplicado.produto.domain.repository.ProdutoRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;


@Path("/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    @Inject
    ProdutoRepository produtoRepository;

    @GET
    public List<ProdutoDTO> listar() {
        return produtoRepository.listAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id_produto}")
    @RolesAllowed({"ADMIN", "FUNCIONARIO"})
    public ProdutoDTO buscar(@PathParam("id_produto") Long idProduto) {
        Produto produto = produtoRepository.findById(idProduto);
        if (produto == null) {
            throw new NotFoundException("Produto não encontrado: " + idProduto);
        }
        return toDTO(produto);
    }

    @POST
    @RolesAllowed("ADMIN")
    @Transactional
    public Response criar(@Valid ProdutoDTO dto) {
        Produto produto = new Produto();
        produto.nome = dto.nome.trim();
        produto.descricao = dto.descricao.trim();
        produto.preco = dto.preco;
        produto.quantidadeEstoque = dto.quantidadeEstoque;
        produto.ativo = dto.ativo;
        produtoRepository.persistAndFlush(produto);

        return Response.created(URI.create("/produtos/" + produto.idProduto))
                .entity(toDTO(produto))
                .build();
    }

    @PUT
    @Path("/{id_produto}")
    @RolesAllowed("ADMIN")
    @Transactional
    public ProdutoDTO atualizar(@PathParam("id_produto") Long idProduto, ProdutoDTO dto) {
        Produto produto = produtoRepository.findById(idProduto);
        if (produto == null) {
            throw new NotFoundException("Produto não encontrado: " + idProduto);
        }

        if (dto.nome != null && !dto.nome.trim().isEmpty()) {
            produto.nome = dto.nome.trim();
        }

        if (dto.descricao != null && !dto.descricao.trim().isEmpty()) {
            produto.descricao = dto.descricao.trim();
        }

        if (dto.preco != null) {
            produto.preco = dto.preco;
        }

        if (dto.quantidadeEstoque != null) {
            produto.quantidadeEstoque = dto.quantidadeEstoque;
        }

        if (dto.ativo != null) {
            produto.ativo = dto.ativo;
        }

        return toDTO(produto);
    }

    @DELETE
    @Path("/{id_produto}")
    @RolesAllowed("ADMIN")
    @Transactional
    public void deletar(@PathParam("id_produto") Long idProduto) {
        Produto produto = produtoRepository.findById(idProduto);
        if (produto == null) {
            throw new NotFoundException("Produto não encontrado: " + idProduto);
        }
        produto.ativo = false;
    }

    private ProdutoDTO toDTO(Produto produto) {
        ProdutoDTO dto = new ProdutoDTO();
        dto.idProduto = produto.idProduto;
        dto.nome = produto.nome;
        dto.descricao = produto.descricao;
        dto.preco = produto.preco != null ? produto.preco : null;
        dto.quantidadeEstoque = produto.quantidadeEstoque;
        dto.ativo = produto.ativo;
        return dto;
    }
}
