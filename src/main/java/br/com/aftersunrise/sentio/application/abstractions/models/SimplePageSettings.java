package br.com.aftersunrise.sentio.application.abstractions.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplePageSettings {
    private int page = 0;
    private int size = 10;
}