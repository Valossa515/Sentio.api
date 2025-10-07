package br.com.aftersunrise.sentio.application.abstractions.data;

import lombok.Data;

@Data
public class Message {
    private String code;
    private String text;

    public Message(String code, String text) {
        this.code = code;
        this.text = text;
    }
}