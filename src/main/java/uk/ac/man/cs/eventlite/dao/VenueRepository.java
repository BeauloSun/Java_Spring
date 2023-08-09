package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Pair;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends CrudRepository<Venue, Long> {

	@Query(value = "SELECT v, COUNT(e) FROM Event e JOIN e.venue v GROUP By v.id ORDER BY COUNT(e) DESC")
	public ArrayList<ArrayList<Object>> findMostEvents();

	public Iterable<Venue> findAllByNameIgnoreCaseContainingOrderByNameAsc(String searchTerm);

	@Query(value = "SELECT COUNT(e) as eventcount FROM Event e JOIN e.venue v GROUP By v.id ORDER BY eventcount DESC")
	public ArrayList<Integer> findMostEventsCounts();

	@Query(value = "SELECT e from Event e where e.venue.id = ?1 and e.date > NOW() ORDER BY e.date, e.time")
	public ArrayList<Event> findEventsByVenue(long id);

	@Query(value = "SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END from Event e where e.venue.id = ?1")
	public boolean hasEvent(long id);
}
