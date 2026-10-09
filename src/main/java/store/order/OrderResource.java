package store.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class OrderResource implements OrderController {

    @Autowired
    private OrderService orderService;

    @Override
    public ResponseEntity<OrderOut> create(String idAccount, OrderIn in) {
        Order order = orderService.create(OrderParser.to(in, idAccount));
        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(order.id())
                .toUri()
        ).body(OrderParser.to(order));
    }

    @Override
    public ResponseEntity<List<OrderSummaryOut>> findAll(String idAccount) {
        return ResponseEntity.ok(
            OrderParser.toSummary(orderService.findAll(idAccount))
        );
    }

    @Override
    public ResponseEntity<OrderOut> findById(String idAccount, String id, String currency) {
        return ResponseEntity.ok(
            OrderParser.to(orderService.findById(id, idAccount, currency))
        );
    }

}
