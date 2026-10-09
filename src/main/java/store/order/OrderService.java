package store.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import store.product.ProductController;
import store.product.ProductOut;

@Service
public class OrderService {

    private static final String BASE_CURRENCY = "USD";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductController productController;

    @Autowired
    private ExchangeClient exchangeClient;

    public Order create(Order order) {
        if (order.items() == null || order.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must have at least one item");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Item item : order.items()) {
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
            }
            ProductOut product = findProduct(item.idProduct());
            BigDecimal price = BigDecimal.valueOf(product.price());
            item.price(price)
                .total(price.multiply(BigDecimal.valueOf(item.quantity())));
            total = total.add(item.total());
        }

        order.date(LocalDateTime.now()).total(total);
        return orderRepository.save(new OrderModel(order)).to();
    }

    public List<Order> findAll(String idAccount) {
        return orderRepository.findByIdAccountOrderByDateDesc(idAccount)
            .stream()
            .map(OrderModel::to)
            .toList();
    }

    public Order findById(String id, String idAccount, String currency) {
        Order order = orderRepository.findByIdAndIdAccount(id, idAccount)
            .map(OrderModel::to)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (currency == null || currency.isBlank()
                || currency.equalsIgnoreCase(BASE_CURRENCY)) {
            return order;
        }

        String target = currency.toUpperCase();
        BigDecimal rate = rate(target, idAccount);
        order.items().forEach(i -> i.total(convert(i.total(), rate)));
        return order.total(convert(order.total(), rate)).currency(target);
    }

    private ProductOut findProduct(String idProduct) {
        try {
            ProductOut product = productController.findById(idProduct).getBody();
            if (product == null) {
                throw productNotFound(idProduct);
            }
            return product;
        } catch (FeignException.NotFound e) {
            throw productNotFound(idProduct);
        }
    }

    private ResponseStatusException productNotFound(String idProduct) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + idProduct);
    }

    private BigDecimal rate(String currency, String idAccount) {
        if (!currency.matches("[A-Z]{3}")) {
            throw currencyNotSupported(currency);
        }
        try {
            ExchangeOut ex = exchangeClient.exchange(BASE_CURRENCY, currency, idAccount);
            return BigDecimal.valueOf(ex.sell());
        } catch (FeignException.NotFound | FeignException.BadRequest
                 | FeignException.UnprocessableEntity e) {
            throw currencyNotSupported(currency);
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Exchange service unavailable");
        }
    }

    private ResponseStatusException currencyNotSupported(String currency) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Currency not supported: " + currency);
    }

    private BigDecimal convert(BigDecimal value, BigDecimal rate) {
        return value.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }

}
