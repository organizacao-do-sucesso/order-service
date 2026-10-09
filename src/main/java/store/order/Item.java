package store.order;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder @Accessors(chain = true, fluent = true)
public class Item {

    private String id;
    private String idProduct;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;

}
