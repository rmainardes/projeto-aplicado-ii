package br.com.projaplicado.support;

import io.smallrye.jwt.build.Jwt;

import java.util.Set;

/**
 * Emite JWTs no mesmo formato que AuthService.login gera, sem depender de um
 * usuário real no banco — usado pra autenticar chamadas HTTP em teste.
 */
public final class TestTokens {

    private TestTokens() {
    }

    public static String admin() {
        return token("1", "admin@teste.com", "ADMIN");
    }

    public static String funcionario() {
        return token("2", "funcionario@teste.com", "FUNCIONARIO");
    }

    private static String token(String subject, String email, String role) {
        return Jwt.issuer("foodtruck-app")
                .subject(subject)
                .claim("email", email)
                .claim("role", role)
                .groups(Set.of(role))
                .expiresIn(3600)
                .sign();
    }
}
