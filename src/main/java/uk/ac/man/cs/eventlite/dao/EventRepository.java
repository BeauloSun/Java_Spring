package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface EventRepository extends CrudRepository<Event, Long>{
	
	// findAll()
	public Iterable<Event> findAllByOrderByDateAscNameAsc();
	
	// findAllUpcoming(LocalDate date)
	public Iterable<Event> findAllByDateGreaterThanOrderByDateAscNameAsc(LocalDate date);
		
	// findAllPrevious(LocalDate date)
	public Iterable<Event> findAllByDateLessThanEqualOrderByDateDescNameAsc(LocalDate date);
	
	// findAllSearch(String searchTerm)
	public Iterable<Event> findAllByNameIgnoreCaseContainingOrderByDateAscNameAsc(String searchTerm);
	
	// findAllUpcomingSearch(LocalDate date, String searchTerm)
	public Iterable<Event> findAllByDateGreaterThanAndNameIgnoreCaseContainingOrderByDateAscNameAsc(LocalDate date, String searchTerm);
	
	// findAllPreviousSearch(LocalDate date, String searchTerm)
	public Iterable<Event> findAllByDateLessThanEqualAndNameIgnoreCaseContainingOrderByDateDescNameAsc(LocalDate date, String searchTerm);
	
	// findTop3Upcoming(LocalDate date)
	public Iterable<Event> findTop3ByDateGreaterThanOrderByDateAscNameAsc(LocalDate date);
}
