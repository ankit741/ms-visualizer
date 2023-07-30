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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class LogAggregatorController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/sequence-diagram")
    public String getImage(@RequestParam("trace-id") String traceId) throws IOException {
        if (eventRepository.existsById(traceId)) {
            ResponseEntity<String> responseEntity = getAllEvent(traceId);
            String source = responseEntity.getBody();
            SourceStringReader reader = new SourceStringReader(source);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
            os.close();
            final String svg = new String(os.toByteArray(), StandardCharsets.UTF_8);
            return svg;
        }
        return "Trace id doesn't exist in database.";
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
            sb.append("url of esp is [[http://www.google.com]]");
            sb.append("\n");
            if(!event.getEventMap().isEmpty()) {
                List<Event> eventList = event.getEventMap().get(traceId);
                eventList.forEach(e -> {
                    sb.append(e.toString());
                    sb.append("\n");
                });
            }
            sb.append("@enduml\n");
            return ResponseEntity.ok(sb.toString());
        }
        return ResponseEntity.noContent().build();
    }

}
