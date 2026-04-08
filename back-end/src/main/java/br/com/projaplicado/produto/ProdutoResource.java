package br.com.projaplicado.produto;

import br.com.projaplicado.produto.api.ProdutoDTO;
import br.com.projaplicado.produto.domain.Produto;
import br.com.projaplicado.produto.service.ProdutoService;
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
    ProdutoService produtoService;

    @GET
    public List<ProdutoDTO> listar() {
        return produtoService.listarTodos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id_produto}")
    public ProdutoDTO buscar(@PathParam("id_produto") Long idProduto) {
        Produto produto = produtoService.buscarPorId(idProduto);
        if (produto == null) {
            throw new NotFoundException("Produto não encontrado: " + idProduto);
        }
        return toDTO(produto);
    }

    @POST
    @Transactional
    public Response criar(@Valid ProdutoDTO dto) {
        Produto produto = produtoService.criar(dto);

        return Response.created(URI.create("/produtos/" + produto.idProduto))
                .entity(toDTO(produto))
                .build();
    }

    @PUT
    @Path("/{id_produto}")
    @Transactional
    public ProdutoDTO atualizar(@PathParam("id_produto") Long idProduto, ProdutoDTO dto) {
        Produto produto = produtoService.atualizar(idProduto, dto);
        return toDTO(produto);
    }

    @DELETE
    @Path("/{id_produto}")
    @Transactional
    public void deletar(@PathParam("id_produto") Long idProduto) {
        boolean deletado = produtoService.deletar(idProduto);
        if (!deletado) {
            throw new NotFoundException("Produto não encontrado: " + idProduto);
        }
    }

    private ProdutoDTO toDTO(Produto produto) {
        ProdutoDTO dto = new ProdutoDTO();
        dto.idProduto = produto.idProduto;
        dto.nome = produto.nome;
        dto.descricao = produto.descricao;
        dto.preco = produto.preco != null ? produto.preco : null;
        dto.quantidadeEstoque = produto.quantidadeEstoque;
        return dto;
    }
}
