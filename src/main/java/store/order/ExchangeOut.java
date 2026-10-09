package store.order;

public record ExchangeOut(
    Double sell,
    Double buy,
    String date
) {}
