package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}
	
	@Override
	public Iterable<Event> findAll(String type, String keyword) {
		if (keyword == null) {
			return getEvents(type);
		} else {
			return searchEvents(type, keyword);
		}
	}

	@Override
	public Event save(Event event) {
		return eventRepository.save(event);
	}

	@Override
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}

	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}

	@Override
	public boolean existsById(long id) {
		return eventRepository.existsById(id);
	}
	
	public Iterable<Event> getEvents(String type) {
		if (type == "upcoming") {
			return eventRepository.findAllByDateGreaterThanOrderByDateAscNameAsc(LocalDate.now());
		} else if (type == "previous") {
			return eventRepository.findAllByDateLessThanEqualOrderByDateDescNameAsc(LocalDate.now());
		} else if (type == "top3") {
			return eventRepository.findTop3ByDateGreaterThanOrderByDateAscNameAsc(LocalDate.now());
		} else {
			return eventRepository.findAllByOrderByDateAscNameAsc();
		}
	}
	
	public Iterable<Event> searchEvents(String type, String keyword) {
		if (type == "upcoming") {
			return eventRepository.findAllByDateGreaterThanAndNameIgnoreCaseContainingOrderByDateAscNameAsc(LocalDate.now(), keyword);
		} else if (type == "previous") {
			return eventRepository.findAllByDateLessThanEqualAndNameIgnoreCaseContainingOrderByDateDescNameAsc(LocalDate.now(), keyword);
		} else {
			return eventRepository.findAllByNameIgnoreCaseContainingOrderByDateAscNameAsc(keyword);
		}
	}
}