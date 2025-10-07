package br.com.aftersunrise.sentio.application.abstractions.interfaces;

import br.com.aftersunrise.sentio.application.abstractions.data.HandlerResponseWithResult;

import java.util.concurrent.CompletableFuture;

public interface IHandler<TInput, TOutput>{
    CompletableFuture<HandlerResponseWithResult<TOutput>> execute(TInput request);
}