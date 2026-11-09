package store.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;

@Service
public class ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);

    @Autowired
    private ExchangeClient exchangeClient;

    public ExchangeOut exchange(String from, String to, String idAccount) {
        if (!to.matches("[A-Z]{3}")) {
            throw currencyNotSupported(to);
        }
        try {
            return exchangeClient.exchange(from, to, idAccount);
        } catch (FeignException.NotFound | FeignException.BadRequest
                 | FeignException.UnprocessableEntity e) {
            throw currencyNotSupported(to);
        } catch (FeignException e) {
            log.error("Exchange service call failed: from={} to={} status={}", from, to, e.status());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Exchange service unavailable");
        }
    }

    private ResponseStatusException currencyNotSupported(String currency) {
        log.warn("Currency not supported: currency={}", currency);
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Currency not supported: " + currency);
    }

}
