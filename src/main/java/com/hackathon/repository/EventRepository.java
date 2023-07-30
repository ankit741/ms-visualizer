package com.hackathon.repository;

import com.hackathon.model.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, String> {
}
