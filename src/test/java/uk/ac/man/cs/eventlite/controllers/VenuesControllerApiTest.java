package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class, EventModelAssembler.class })
@ActiveProfiles({"venues", "events"})
public class VenuesControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(
					get("/api/venues")
					.accept(MediaType.APPLICATION_JSON)				
				)
				.andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues"))
				.andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

		verify(venueService).findAll();
	}
	
	@Test
	public void getIndexWithVenues() throws Exception {
		
		Venue v = new Venue();
		v.setName("An example venue");
		v.setCapacity(100);
		v.setLongitude(50.0);
		v.setLatitude(35.0);
		v.setAddress("93 NORTH 9TH STREET, BROOKLYN NY 11211");
		v.setCoords(new Double[]{50.0, 35.0});
		
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(v));

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues"))
				.andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));
		verify(venueService).findAll();
	}
	
	@Test
	public void getVenueNotFound() throws Exception {
		
		mvc.perform(get("/api/venues/99")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("venue 99")))
				.andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getVenue"));
		
	}
	
	@Test
	public void getVenueFound() throws Exception {		
		Venue v = new Venue();
		v.setName("An example venue");
		v.setCapacity(100);
		v.setLongitude(50.0);
		v.setLatitude(35.0);
		v.setAddress("93 NORTH 9TH STREET, BROOKLYN NY 11211");
		v.setCoords(new Double[]{50.0, 35.0});
		
		when(venueService.findById(1)).thenReturn(Optional.of(v));
		
		mvc.perform(get("/api/venues/1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", equalTo("An example venue")))
				.andExpect(jsonPath("$.capacity", equalTo(100)))
				.andExpect(jsonPath("$.longitude", equalTo(50.0)))
				.andExpect(jsonPath("$.latitude", equalTo(35.0)))
				.andExpect(jsonPath("$.address", equalTo("93 NORTH 9TH STREET, BROOKLYN NY 11211")))
				.andExpect(handler().methodName("getVenue"));
		
		verify(venueService).findById(1);
	}
		
	@Test
	public void getEventsAtVenueNotFound() throws Exception{
		mvc.perform(
				get("/api/venues/1/events")
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				
		)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("venue 1")))
				.andExpect(jsonPath("$.id", equalTo(1)))
				.andExpect(handler().methodName("getEventsAtVenue"));
		
		verify(venueService).findById(1);
	}
	
	@Test
	public void getEventsAtVenue() throws Exception{
		Venue v = new Venue();
		v.setName("An example venue");
		v.setCapacity(100);
		v.setLongitude(50.0);
		v.setLatitude(35.0);
		v.setAddress("93 NORTH 9TH STREET, BROOKLYN NY 11211");
		v.setCoords(new Double[]{50.0, 35.0});
		
		Event e = new Event();
		e.setName("An example event");
		e.setDescription("Enjoyable event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v);
		
		
		when(venueService.findById(1)).thenReturn(Optional.of(v));
		
		mvc.perform(
				get("/api/venues/1/events")
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				
		)
				.andExpect(status().isOk())
				.andExpect(handler().methodName("getEventsAtVenue"));
		
		verify(venueService).findById(1);
	}
	
	@Test
	public void getNext3EventsAtVenueNotFound() throws Exception{
		mvc.perform(
				get("/api/venues/1/next3events")
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				
		)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("venue 1")))
				.andExpect(jsonPath("$.id", equalTo(1)))
				.andExpect(handler().methodName("getNext3EventsAtVenue"));
		
		verify(venueService).findById(1);
	}
	
	@Test
	public void getNextThreeEvents() throws Exception {
		Venue v = new Venue();
		v.setName("An example venue");
		v.setCapacity(100);
		v.setLongitude(50.0);
		v.setLatitude(35.0);
		v.setAddress("93 NORTH 9TH STREET, BROOKLYN NY 11211");
		v.setCoords(new Double[]{50.0, 35.0});
		
		Event e1 = new Event();
		e1.setName("An example event");
		e1.setDescription("Enjoyable event");
		e1.setDate(LocalDate.now());
		e1.setTime(LocalTime.now());
		e1.setVenue(v);
		
		Event e2 = new Event();
		e2.setName("An example event");
		e2.setDescription("Enjoyable event");
		e2.setDate(LocalDate.now());
		e2.setTime(LocalTime.now());
		e2.setVenue(v);
		
		Event e3 = new Event();
		e3.setName("An example event");
		e3.setDescription("Enjoyable event");
		e3.setDate(LocalDate.now());
		e3.setTime(LocalTime.now());
		e3.setVenue(v);
		
		when(venueService.findById(1)).thenReturn(Optional.of(v));
		when(venueService.findEventsByVenue(1)).thenReturn(new ArrayList<>(Arrays.asList(e1, e2, e3)));
		
		mvc.perform(
				get("/api/venues/1/next3events")
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				
		)
				.andExpect(status().isOk())
				.andExpect(handler().methodName("getNext3EventsAtVenue"));
		
		verify(venueService).findById(1);
		verify(venueService).findEventsByVenue(1);
	}
	
	@Test
	public void getNext3EventsAtVenueMoreThan3() throws Exception{
		Venue v = new Venue();
		v.setName("An example venue");
		v.setCapacity(100);
		v.setLongitude(50.0);
		v.setLatitude(35.0);
		v.setAddress("93 NORTH 9TH STREET, BROOKLYN NY 11211");
		v.setCoords(new Double[]{50.0, 35.0});
		
		Event e1 = new Event();
		e1.setName("An example event");
		e1.setDescription("Enjoyable event");
		e1.setDate(LocalDate.now());
		e1.setTime(LocalTime.now());
		e1.setVenue(v);
		
		Event e2 = new Event();
		e2.setName("An example event");
		e2.setDescription("Enjoyable event");
		e2.setDate(LocalDate.now());
		e2.setTime(LocalTime.now());
		e2.setVenue(v);
		
		Event e3 = new Event();
		e3.setName("An example event");
		e3.setDescription("Enjoyable event");
		e3.setDate(LocalDate.now());
		e3.setTime(LocalTime.now());
		e3.setVenue(v);
		
		Event e4 = new Event();
		e4.setName("An example event");
		e4.setDescription("Enjoyable event");
		e4.setDate(LocalDate.now());
		e4.setTime(LocalTime.now());
		e4.setVenue(v);
		
		when(venueService.findById(1)).thenReturn(Optional.of(v));
		when(venueService.findEventsByVenue(1)).thenReturn(new ArrayList<>(Arrays.asList(e1, e2, e3, e4)));
		
		mvc.perform(
				get("/api/venues/1/next3events")
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				
		)
				.andExpect(status().isOk())
				.andExpect(handler().methodName("getNext3EventsAtVenue"));
		
		verify(venueService).findById(1);
		verify(venueService).findEventsByVenue(1);
	}
	
}

