package store.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import store.exchange.ExchangeController;
import store.product.ProductController;

@SpringBootApplication
@EnableFeignClients(clients = { ProductController.class, ExchangeController.class })
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
