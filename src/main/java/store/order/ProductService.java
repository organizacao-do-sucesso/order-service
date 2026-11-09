package store.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import store.product.ProductController;
import store.product.ProductOut;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductController productController;

    public ProductOut findById(String id) {
        if (id == null || id.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id is required");
        }
        try {
            ProductOut product = productController.findById(id).getBody();
            if (product == null) {
                throw productNotFound(id);
            }
            return product;
        } catch (FeignException.NotFound | FeignException.BadRequest e) {
            throw productNotFound(id);
        } catch (FeignException e) {
            log.error("Product service call failed: idProduct={} status={}", id, e.status());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service unavailable");
        }
    }

    private ResponseStatusException productNotFound(String id) {
        log.warn("Product not found: idProduct={}", id);
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + id);
    }

}
