package store.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "orders")
@Setter @Accessors(chain = true, fluent = true)
@NoArgsConstructor
public class OrderModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "id_account")
    private String idAccount;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "total")
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemModel> items = new ArrayList<>();

    public OrderModel(Order o) {
        this.id = o.id();
        this.idAccount = o.idAccount();
        this.date = o.date();
        this.total = o.total();
        o.items().forEach(i -> this.items.add(new ItemModel(i, this)));
    }

    public Order to() {
        return Order.builder()
            .id(this.id)
            .idAccount(this.idAccount)
            .date(this.date)
            .currency("USD")
            .total(this.total)
            .items(this.items.stream().map(ItemModel::to).toList())
            .build();
    }

}
