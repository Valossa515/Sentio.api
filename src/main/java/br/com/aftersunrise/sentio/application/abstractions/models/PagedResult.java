package br.com.aftersunrise.sentio.application.abstractions.models;

import java.util.List;

public record PagedResult<T>(
        List<T> data,
        long totalRecords,
        int page,
        int size) {
}