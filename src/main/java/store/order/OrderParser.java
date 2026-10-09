package store.order;

import java.util.List;

public class OrderParser {

    public static Order to(OrderIn in, String idAccount) {
        return in == null ? null :
            Order.builder()
                .idAccount(idAccount)
                .items(in.items() == null ? List.of() :
                    in.items().stream()
                        .map(i -> Item.builder()
                            .idProduct(i.idProduct())
                            .quantity(i.quantity())
                            .build())
                        .toList())
                .build();
    }

    public static OrderOut to(Order o) {
        return o == null ? null :
            OrderOut.builder()
                .id(o.id())
                .date(o.date())
                .currency(o.currency())
                .total(o.total())
                .items(o.items().stream()
                    .map(i -> ItemOut.builder()
                        .id(i.id())
                        .product(new ProductRef(i.idProduct()))
                        .quantity(i.quantity())
                        .total(i.total())
                        .build())
                    .toList())
                .build();
    }

    public static List<OrderSummaryOut> toSummary(List<Order> l) {
        return l.stream()
            .map(o -> OrderSummaryOut.builder()
                .id(o.id())
                .date(o.date())
                .total(o.total())
                .build())
            .toList();
    }

}
