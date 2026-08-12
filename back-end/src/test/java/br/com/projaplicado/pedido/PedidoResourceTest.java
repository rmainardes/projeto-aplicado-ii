package br.com.projaplicado.pedido;

import br.com.projaplicado.produto.domain.Produto;
import br.com.projaplicado.support.TestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static br.com.projaplicado.auth.AuthResource.ACCESS_TOKEN_COOKIE;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

// Cobre a camada HTTP/segurança (autenticação via cookie, autorização por
// role) chamando os endpoints de verdade. A lógica de negócio (controle de
// estoque etc.) é testada separadamente em PedidoServiceTest, direto no
// service — evita precisar de fixtures visíveis entre a transação do teste e
// a transação da própria requisição HTTP, que rodam em conexões diferentes.
@QuarkusTest
class PedidoResourceTest {

    @Test
    void health_semToken_retorna200() {
        given()
                .when().get("/health")
                .then().statusCode(200);
    }

    @Test
    void listarPedidos_semToken_retorna401() {
        given()
                .when().get("/pedidos")
                .then().statusCode(401);
    }

    @Test
    void listarPedidos_comTokenInvalido_retorna401() {
        given()
                .cookie(ACCESS_TOKEN_COOKIE, "token-invalido")
                .when().get("/pedidos")
                .then().statusCode(401);
    }

    @Test
    void listarPedidos_comTokenFuncionario_retorna200() {
        given()
                .cookie(ACCESS_TOKEN_COOKIE, TestTokens.funcionario())
                .when().get("/pedidos")
                .then().statusCode(200);
    }

    @Test
    void buscarPedidoInexistente_retorna404ComMensagem() {
        given()
                .cookie(ACCESS_TOKEN_COOKIE, TestTokens.admin())
                .when().get("/pedidos/999999999")
                .then()
                .statusCode(404)
                .body("message", containsString("Pedido não encontrado"));
    }

    @Test
    void funcionarioNaoPodeCriarProduto_retorna403() {
        given()
                .cookie(ACCESS_TOKEN_COOKIE, TestTokens.funcionario())
                .contentType("application/json")
                .body("""
                        {"nome":"x","descricao":"x","preco":1,"quantidadeEstoque":1,"ativo":true}
                        """)
                .when().post("/produtos")
                .then().statusCode(403);
    }

    @Test
    void adminPodeCriarProduto_retorna201() {
        // POST real vai pro banco de verdade (não roda em @TestTransaction,
        // já que passa pela requisição HTTP numa transação própria) — nome
        // único evita colidir com a constraint de nome em reexecuções, e o
        // registro é removido de verdade no final pra não sujar o banco.
        String nomeUnico = "Produto de teste " + UUID.randomUUID();

        long idProduto = given()
                .cookie(ACCESS_TOKEN_COOKIE, TestTokens.admin())
                .contentType("application/json")
                .body("""
                        {"nome":"%s","descricao":"desc","preco":9.90,"quantidadeEstoque":5,"ativo":true}
                        """.formatted(nomeUnico))
                .when().post("/produtos")
                .then().statusCode(201)
                .extract().jsonPath().getLong("idProduto");

        QuarkusTransaction.requiringNew().run(() -> Produto.deleteById(idProduto));
    }

    @Test
    void criarPedidoComPayloadInvalido_retorna400() {
        given()
                .cookie(ACCESS_TOKEN_COOKIE, TestTokens.admin())
                .contentType("application/json")
                .body("{}")
                .when().post("/pedidos")
                .then().statusCode(400);
    }
}
