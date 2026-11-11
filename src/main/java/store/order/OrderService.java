package store.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import store.product.ProductController;
import store.product.ProductOut;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String BASE_CURRENCY = "USD";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductController productController;

    @Autowired
    private ExchangeService exchangeService;

    // business metrics, scraped by prometheus at /order/actuator/prometheus
    private final Counter ordersCreated;
    private final DistributionSummary orderAmount;
    private final DistributionSummary orderItems;

    public OrderService(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("orders.created")
            .description("Orders successfully created")
            .register(registry);
        this.orderAmount = DistributionSummary.builder("orders.amount")
            .description("Order total in USD")
            .baseUnit("usd")
            .register(registry);
        this.orderItems = DistributionSummary.builder("orders.items")
            .description("Number of items per order")
            .register(registry);
    }

    public Order create(Order order) {
        if (order.items() == null || order.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must have at least one item");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Item item : order.items()) {
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
            }
            if (item.idProduct() == null || item.idProduct().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id is required");
            }
            ProductOut product = productController.findById(item.idProduct()).getBody();
            BigDecimal price = product.price();
            item.price(price)
                .total(price.multiply(BigDecimal.valueOf(item.quantity())));
            total = total.add(item.total());
        }

        order.date(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)).total(total);
        Order saved = orderRepository.save(new OrderModel(order)).to();

        ordersCreated.increment();
        orderAmount.record(total.doubleValue());
        orderItems.record(saved.items().size());
        log.info("Order created: id={} idAccount={} items={} total={}",
            saved.id(), saved.idAccount(), saved.items().size(), total);
        return saved;
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
            .orElseThrow(() -> {
                log.warn("Order not found: id={} idAccount={}", id, idAccount);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
            });

        if (currency == null || currency.isBlank()
                || currency.equalsIgnoreCase(BASE_CURRENCY)) {
            return order;
        }

        String target = currency.toUpperCase();
        BigDecimal rate = exchangeService.exchange(BASE_CURRENCY, target, idAccount).sell();
        order.items().forEach(i -> i.total(convert(i.total(), rate)));
        return order.total(convert(order.total(), rate)).currency(target);
    }

    private BigDecimal convert(BigDecimal value, BigDecimal rate) {
        return value.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }

}
