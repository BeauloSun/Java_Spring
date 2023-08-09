package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	@Autowired
	private VenueRepository venueRepository;

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	@Override
	public Iterable<Venue> findAll(String keyword) {
		if (keyword != null) {
			return venueRepository.findAllByNameIgnoreCaseContainingOrderByNameAsc(keyword);
		}
		return venueRepository.findAll();
	}

	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}

	@Override
	public Optional<Venue> findById(long id) {
		return venueRepository.findById(id);
	}

	@Override
	public ArrayList<ArrayList<Object>> findMostEvents() {
		return venueRepository.findMostEvents();
	}

	@Override
	public ArrayList<Event> findEventsByVenue(long id) {
		return venueRepository.findEventsByVenue(id);
	}
	
	@Override
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}
	
	@Override
	public boolean existsById(long id) {
		return venueRepository.existsById(id);
	}
	
	@Override
	public boolean hasEvent(long id) {
		return venueRepository.hasEvent(id);
	}

}
