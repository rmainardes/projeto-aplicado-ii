package br.com.projaplicado.produto;

import br.com.projaplicado.produto.api.ProdutoDTO;
import br.com.projaplicado.produto.domain.Produto;
import jakarta.transaction.Transactional;
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

    @GET
    public List<ProdutoDTO> listar() {
        return Produto.<Produto>listAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id_produto}")
    public ProdutoDTO buscar(@PathParam("id_produto") Long idProduto) {
        Produto produto = Produto.findById(idProduto);
        if (produto == null) {
            throw new NotFoundException("Produto não encontrado: " + idProduto);
        }
        return toDTO(produto);
    }

    @POST
    @Transactional
    public Response criar(ProdutoDTO dto) {
        if (dto.nome == null || dto.nome.trim().isEmpty()) {
            throw new BadRequestException("Nome é obrigatório");
        }
        if (dto.descricao == null || dto.descricao.trim().isEmpty()) {
            throw new BadRequestException("Descricao é obrigatória");
        }
        if (dto.preco == null) {
            throw new BadRequestException("Preco é obrigatório");
        }
        if (dto.quantidadeEstoque == null) {
            throw new BadRequestException("Quantidade em estoque é obrigatória");
        }

        Produto produto = new Produto();
        produto.nome = dto.nome.trim();
        produto.descricao = dto.descricao.trim();
        produto.preco = dto.preco;
        produto.quantidadeEstoque = dto.quantidadeEstoque;
        produto.persistAndFlush();

        return Response.created(URI.create("/produtos/" + produto.idProduto))
                .entity(toDTO(produto))
                .build();
    }

    @PUT
    @Path("/{id_produto}")
    @Transactional
    public ProdutoDTO atualizar(@PathParam("id_produto") Long idProduto, ProdutoDTO dto) {
        Produto produto = Produto.findById(idProduto);
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

        return toDTO(produto);
    }

    @DELETE
    @Path("/{id_produto}")
    @Transactional
    public void deletar(@PathParam("id_produto") Long idProduto) {
        boolean deletado = Produto.deleteById(idProduto);
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
