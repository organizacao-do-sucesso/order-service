package store.order;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import store.product.ProductController;
import store.product.ProductIn;
import store.product.ProductOut;

// temporary: replaces the product-service while it does not exist (profile dev)
@Component
@Profile("dev")
public class ProductControllerStub implements ProductController {

    private static final List<ProductOut> PRODUCTS = List.of(
        new ProductOut("0195abfb-7074-73a9-9d26-b4b9fbaab0a8", "Tomato", 10.12, "kg"),
        new ProductOut("0195abfe-e416-7052-be3b-27cdaf12a984", "Cheese", 0.62, "slice")
    );

    @Override
    public ResponseEntity<ProductOut> findById(String id) {
        return PRODUCTS.stream()
            .filter(p -> p.id().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<ProductOut>> findAll() {
        return ResponseEntity.ok(PRODUCTS);
    }

    @Override
    public ResponseEntity<ProductOut> create(ProductIn in) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<Void> delete(String id) {
        throw new UnsupportedOperationException();
    }

}
