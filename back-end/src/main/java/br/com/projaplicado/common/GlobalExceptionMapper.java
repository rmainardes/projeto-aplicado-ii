package br.com.projaplicado.common;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException wae) {
            return Response.fromResponse(wae.getResponse())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }

        // Erro inesperado: não vaza detalhe pro cliente, mas registra com um
        // id de correlação pra dar pra achar o stack trace real no log.
        String errorId = UUID.randomUUID().toString();
        LOG.error("Erro não tratado [" + errorId + "]", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", "Erro interno do servidor", "errorId", errorId))
                .build();
    }
}