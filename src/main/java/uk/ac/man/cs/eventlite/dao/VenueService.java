package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.util.Pair;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public Iterable<Venue> findAll(String keyword);
	
	public Venue save(Venue venue);

	public Optional<Venue> findById(long id);

	public ArrayList<ArrayList<Object>> findMostEvents();

	public ArrayList<Event> findEventsByVenue(long id);
	
	public void deleteById(long id);
	
	public boolean existsById(long id);
	
	public boolean hasEvent(long id);
}
