package com.hackathon.controller;

import com.hackathon.model.Event;
import com.hackathon.repository.EventRepository;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class LogAggregatorController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/sequence-diagram")
    public String getSequenceDiagram(@RequestParam("trace-id") String traceId) throws IOException {
        if (eventRepository.existsById(traceId)) {
            ResponseEntity<String> responseEntity = getAllEvent(traceId);
            String source = responseEntity.getBody();
            SourceStringReader reader = new SourceStringReader(source);
            try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
                return new String(os.toByteArray(), StandardCharsets.UTF_8);
            }
        }
        return "Trace id doesn't exist in database.";
    }

    @GetMapping("/data")
    public String getPayloadDiagram(@RequestParam("payload") String payload, @RequestParam("trace-id") String traceId) throws IOException {
        if (payload == null) {
            return "invalid payload.";
        }
        byte[] decodedBytes = Base64.getDecoder().decode(payload);
        String decodedPayload = new String(decodedBytes);
        System.out.println(decodedPayload);
        if (eventRepository.existsById(traceId)) {
            StringBuilder sb = new StringBuilder();
            sb.append("@startjson\n");
            sb.append(decodedPayload);
            sb.append("\n");
            sb.append("@endjson\n");
            SourceStringReader reader = new SourceStringReader(sb.toString());
            try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
                return new String(os.toByteArray(), StandardCharsets.ISO_8859_1);
            }
        }
        return "trace id doesn't exist in database.";
    }

    @PostMapping("/logs")
    public ResponseEntity<Boolean> logEvent(@RequestBody Event event, @RequestHeader("trace-id") String traceId) {
        event.setTraceId(traceId);
        Optional<Event> eventOptional = eventRepository.findById(traceId);
        if (eventOptional.isPresent()) {
            Event storedEvent = eventOptional.get();
            if (storedEvent.getEventMap().isEmpty()) {
                storedEvent.getEventMap().put(traceId, Stream.of(event).collect(Collectors.toList()));
            } else {
                List<Event> eventList = storedEvent.getEventMap().get(traceId);
                eventList.add(event);
                storedEvent.getEventMap().put(traceId, eventList);
            }
        } else {
            eventRepository.save(event);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping(path = "/logs")
    public ResponseEntity<String> getAllEvent(@RequestParam("trace-id") String traceId) {
        System.out.println("Get request has been received." + traceId);
        Optional<Event> optionalEvent = eventRepository.findById(traceId);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            StringBuilder sb = new StringBuilder();
            sb.append("@startuml\n");
            sb.append(event);
            sb.append("\n");
            if (!event.getEventMap().isEmpty()) {
                List<Event> eventList = event.getEventMap().get(traceId);
                eventList.forEach(e -> {
                    sb.append(e.toString());
                    sb.append("\n");
                });
            }
            sb.append("@enduml");
            return ResponseEntity.ok(sb.toString());
        }
        return ResponseEntity.noContent().build();
    }

}
