package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotDeletedException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@Profile("venues")
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private VenueService venueService;

	@Autowired
	private VenueModelAssembler venueAssembler;
	
	@Autowired
	private EventModelAssembler eventAssembler;

	@ExceptionHandler(VenueNotFoundException.class)
	public ResponseEntity<?> venueNotFoundHandler(VenueNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		return venueAssembler.toModel(venue);
	}
	
	@GetMapping("/{id}/events")
	public CollectionModel<EntityModel<Event>> getEventsAtVenue(@PathVariable("id") long id) {
		// Make sure the venue exists
		venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		
		return eventAssembler.toCollectionModel(venueService.findEventsByVenue(id))
				.add(linkTo(methodOn(VenuesControllerApi.class)
				.getEventsAtVenue(id))
				.withSelfRel())
		;
	}
	
	@GetMapping("/{id}/next3events")
	public CollectionModel<EntityModel<Event>> getNext3EventsAtVenue(@PathVariable("id") long id) {
		// Make sure the venue exists
		venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		
		// This sorted by default such that most recent upcoming event is the first element
		ArrayList<Event> eventsList = venueService.findEventsByVenue(id);
		
		// Only if there are more than 3 events do we have to selectively select the first 3
		if (eventsList.size() > 3) {
			return eventAssembler.toCollectionModel(eventsList.subList(0, 3));
		}
		
		return eventAssembler.toCollectionModel(eventsList)
				.add(linkTo(methodOn(VenuesControllerApi.class)
				.getNext3EventsAtVenue(id))
				.withSelfRel())
		;
	}
	
	@GetMapping
	public CollectionModel<EntityModel<Venue>> getAllVenues() {
		return venueAssembler.toCollectionModel(venueService.findAll())
				.add(linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel());
	}
	
}
