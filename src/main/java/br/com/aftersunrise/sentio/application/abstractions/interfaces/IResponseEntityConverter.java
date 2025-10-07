package br.com.aftersunrise.sentio.application.abstractions.interfaces;

import br.com.aftersunrise.sentio.application.abstractions.data.HandlerResponseWithResult;
import org.springframework.http.ResponseEntity;

public interface IResponseEntityConverter {
    <T> ResponseEntity<T> convert(HandlerResponseWithResult<T> response, boolean withContentOnSuccess);
}
