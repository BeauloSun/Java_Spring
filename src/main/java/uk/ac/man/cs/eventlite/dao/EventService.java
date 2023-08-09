package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();
	
	public Event save(Event event);
	
	public Iterable<Event> findAll(String type, String keyword);

	public Optional<Event> findById(long id);

	public void deleteById(long id);

	public boolean existsById(long id);
}