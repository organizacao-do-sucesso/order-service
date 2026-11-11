package store.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import feign.Response;
import feign.codec.ErrorDecoder;

// translates errors from the product contract's Feign client (store.product.ProductController)
// registered in application.yaml: spring.cloud.openfeign.client.config.product.error-decoder
public class ProductClient implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400, 404 -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
            default -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service unavailable");
        };
    }

}
