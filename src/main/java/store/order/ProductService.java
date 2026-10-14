package store.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import store.product.ProductController;
import store.product.ProductOut;

@Service
public class ProductService {

    @Autowired
    private ProductController productController;

    // cached in redis; not-found products throw, so they are never cached
    @Cacheable(cacheNames = CacheConfig.PRODUCTS, key = "#id")
    public ProductOut findById(String id) {
        try {
            ProductOut product = productController.findById(id).getBody();
            if (product == null) {
                throw productNotFound(id);
            }
            return product;
        } catch (FeignException.NotFound e) {
            throw productNotFound(id);
        }
    }

    private ResponseStatusException productNotFound(String id) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + id);
    }

}
