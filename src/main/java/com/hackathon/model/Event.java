package com.hackathon.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@KeySpace("event")
public class Event {
    Map<String, List<Event>> eventMap = new HashMap<>();
    @Id
    private String traceId;
    private String source;
    private String target;
    private String type;
    private String payload;

    @Override
    public String toString() {
       String encodedPayload =Base64.getEncoder().encodeToString(payload.getBytes());
        return String.format("%s -> %s : %s [[http://localhost:8080/data?payload=%s&trace-id=%s{%s} data]]", source, target, type,encodedPayload,traceId,payload);
    }
}
