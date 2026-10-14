package store.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;

@Service
public class ExchangeService {

    @Autowired
    private ExchangeClient exchangeClient;

    // the rate does not depend on the account, so it is left out of the key
    @Cacheable(cacheNames = CacheConfig.EXCHANGES, key = "#from + ':' + #to")
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
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Exchange service unavailable");
        }
    }

    private ResponseStatusException currencyNotSupported(String currency) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Currency not supported: " + currency);
    }

}
