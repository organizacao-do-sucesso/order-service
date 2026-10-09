package store.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder @Accessors(chain = true, fluent = true)
public class Order {

    private String id;
    private String idAccount;
    private LocalDateTime date;
    private String currency;
    private List<Item> items;
    private BigDecimal total;

}
