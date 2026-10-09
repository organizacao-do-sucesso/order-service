package store.order;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "items")
@Setter @Accessors(chain = true, fluent = true)
@NoArgsConstructor
public class ItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_order")
    private OrderModel order;

    @Column(name = "id_product")
    private String idProduct;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "total")
    private BigDecimal total;

    public ItemModel(Item i, OrderModel order) {
        this.id = i.id();
        this.order = order;
        this.idProduct = i.idProduct();
        this.quantity = i.quantity();
        this.price = i.price();
        this.total = i.total();
    }

    public Item to() {
        return Item.builder()
            .id(this.id)
            .idProduct(this.idProduct)
            .quantity(this.quantity)
            .price(this.price)
            .total(this.total)
            .build();
    }

}
