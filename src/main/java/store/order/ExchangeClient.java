package store.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import feign.Response;
import feign.codec.ErrorDecoder;

// translates errors from the exchange contract's Feign client (store.exchange.ExchangeController)
// registered in application.yaml: spring.cloud.openfeign.client.config.exchange.error-decoder
public class ExchangeClient implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400, 404, 422 -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Currency not supported");
            default -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Exchange service unavailable");
        };
    }

}
