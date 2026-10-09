package store.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "exchange",
    url = "http://exchange:8000"
)
public interface ExchangeClient {

    @GetMapping("/exchanges/{from}/{to}")
    public ExchangeOut exchange(
        @PathVariable("from") String from,
        @PathVariable("to") String to,
        @RequestHeader("id-account") String idAccount
    );

}
