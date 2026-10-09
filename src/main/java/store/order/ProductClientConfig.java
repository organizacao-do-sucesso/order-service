package store.order;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import store.product.ProductController;

// in profile dev the ProductControllerStub is used instead of the Feign client
@Configuration
@Profile("!dev")
@EnableFeignClients(clients = { ProductController.class })
public class ProductClientConfig {

}
