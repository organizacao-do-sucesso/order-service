package store.order;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

// mirrors the response of exchange-service: GET /exchanges/{from}/{to}
public record ExchangeOut(
    BigDecimal sell,
    BigDecimal buy,
    String date,
    @JsonProperty("id-account") String idAccount
) {}
