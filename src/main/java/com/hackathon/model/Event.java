package com.hackathon.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

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
    private String data;

    @Override
    public String toString() {
        return String.format("%s -> %s : %s", source, target, data);
    }
}
