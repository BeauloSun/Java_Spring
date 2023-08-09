package uk.ac.man.cs.eventlite.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.MapboxService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private WebApplicationContext context;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@MockBean
	private MapboxService mapboxService;

	@BeforeEach
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll(null);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");

		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll(null);
	}

	@Test
	public void getVenue() throws Exception {
		when(venueService.findById(1)).thenReturn(Optional.of(venue));

		mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/show")).andExpect(handler().methodName("getVenue"));
	}

	@Test
	public void getVenueNotFound() throws Exception {
		mvc.perform(get("/venues/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("venues/error_page")).andExpect(handler().methodName("getVenue"));
	}

	@Test
	public void getAddVenuePage() throws Exception {
		mvc.perform(get("/venues/add").accept(MediaType.TEXT_HTML)).andExpect(status().isOk());
	}

	@Test
	public void deleteVenue() throws Exception {
		when(venueService.existsById(1)).thenReturn(true);

		mvc.perform(delete("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isFound()).andExpect(view().name("redirect:/venues"))
				.andExpect(handler().methodName("deleteVenue")).andExpect(flash().attributeExists("red"));

		verify(venueService).deleteById(1);
	}

	@Test
	public void deleteVenueNotFound() throws Exception {
		when(venueService.existsById(1)).thenReturn(false);

		mvc.perform(delete("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isNotFound()).andExpect(view().name("venues/error_page"))
				.andExpect(handler().methodName("deleteVenue"));

		verify(venueService, never()).deleteById(1);
	}

	@Test
	public void deleteVenueWithEvents() throws Exception {
		when(venueService.existsById(1)).thenReturn(true);
		when(venueService.hasEvent(1)).thenReturn(true);

		mvc.perform(delete("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isBadRequest()).andExpect(view().name("venues/error_page"))
				.andExpect(handler().methodName("deleteVenue"));

		verify(venueService, never()).deleteById(1);
	}

	@Test
	public void postVenue() throws Exception {
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		mvc.perform(post("/venues/addVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Earth")
				.param("address", "Kilburn Building Manchester")
				.param("capacity", "100")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("addVenue"))
				.andExpect(flash().attributeExists("green"));

		verify(venueService).save(any(Venue.class));
	}

	@Test
	public void postEmptyVenue() throws Exception {
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		mvc.perform(post("/venues/addVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.param("address", "")
				.param("capacity", "")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().errorCount(3))
				.andExpect(handler().methodName("addVenue"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void postLongNameVenue() throws Exception {
		// name must be less than 256 chars
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		String long_name = "n".repeat(256);

		mvc.perform(post("/venues/addVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", long_name)
				.param("address", "Kilburn Building Manchester")
				.param("capacity", "100")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("addVenue"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void postShortNameVenue() throws Exception {
		// name must be less than 256 chars and at least 3 char long
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		String short_name = "n".repeat(2);

		mvc.perform(post("/venues/addVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", short_name)
				.param("address", "Kilburn Building Manchester")
				.param("capacity", "100")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("addVenue"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void postNegativeCapacityVenue() throws Exception {
		// capacity should be positive
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		mvc.perform(post("/venues/addVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Earth")
				.param("address", "Kilburn Building Manchester")
				.param("capacity", "-1")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "capacity"))
				.andExpect(handler().methodName("addVenue"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void postZeroCapacityVenue() throws Exception {
		// capacity should be positive
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		mvc.perform(post("/venues/addVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Earth")
				.param("address", "Kilburn Building Manchester")
				.param("capacity", "0")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "capacity"))
				.andExpect(handler().methodName("addVenue"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void postLongAddressVenue() throws Exception {
		// address must be less than 300 chars
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		String long_address = "a".repeat(301);

		mvc.perform(post("/venues/addVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Earth")
				.param("address", long_address)
				.param("capacity", "100")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "address"))
				.andExpect(handler().methodName("addVenue"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void postInvalidNameVenue() throws Exception {
		// name must only contain printable ASCII characters
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		mvc.perform(post("/venues/addVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Eærth")
				.param("address", "Kilburn Building Manchester")
				.param("capacity", "100")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("addVenue"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void getUpdateVenuePage() throws Exception {
		when(venueService.findById(1)).thenReturn(Optional.of(venue));

		mvc.perform(get("/venues/update/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/update")).andExpect(handler().methodName("updateVenue"));
	}

	@Test
	public void getUpdateVenuePageNotFound() throws Exception {
		mvc.perform(get("/venues/update/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("venues/error_page")).andExpect(handler().methodName("updateVenue"));
	}

	@Test
	public void updateVenue() throws Exception {
		// Create a test venue
		Venue testVenue = new Venue();
		testVenue.setName("Test Venue");
		testVenue.setAddress("123 Test St");
		testVenue.setCapacity(100);

		// Mock the venue service to return the test venue
		when(venueService.findById(1)).thenReturn(Optional.of(testVenue));
		when(mapboxService.geocode(any(String.class))).thenReturn(new Double[] { 0.0, 0.0 });

		mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Updated Test Venue")
				.param("address", "456 Updated St")
				.param("capacity", "200")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeExists("yellow"));

		// Verify that the venue was updated
		ArgumentCaptor<Venue> captor = ArgumentCaptor.forClass(Venue.class);
		verify(venueService).save(captor.capture());
		Venue updatedVenue = captor.getValue();
		assertEquals("Updated Test Venue", updatedVenue.getName());
		assertEquals("456 Updated St", updatedVenue.getAddress());
		assertEquals(200, updatedVenue.getCapacity());

	}

	@Test
	public void updateVenueNotFound() throws Exception {
		mvc.perform(put("/venues/update/99").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Updated Test Venue")
				.param("address", "456 Updated St")
				.param("capacity", "200")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(view().name("venues/error_page"))
				.andExpect(handler().methodName("executeUpdate"));
	}

	@Test
	public void updateEmptyVenue() throws Exception {
		// Create a test venue
		Venue testVenue = new Venue();
		testVenue.setName("Test Venue");
		testVenue.setAddress("123 Test St");
		testVenue.setCapacity(100);

		// Mock the venue service to return the test venue
		when(venueService.findById(1)).thenReturn(Optional.of(testVenue));
		when(mapboxService.geocode(any(String.class))).thenReturn(new Double[] { 0.0, 0.0 });

		mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.param("address", "")
				.param("capacity", "")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().errorCount(3))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void updateLongNameVenue() throws Exception {
		// Create a test venue
		Venue testVenue = new Venue();
		testVenue.setName("Test Venue");
		testVenue.setAddress("123 Test St");
		testVenue.setCapacity(100);

		String long_name = "n".repeat(300);

		// Mock the venue service to return the test venue
		when(venueService.findById(1)).thenReturn(Optional.of(testVenue));
		when(mapboxService.geocode(any(String.class))).thenReturn(new Double[] { 0.0, 0.0 });

		mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", long_name)
				.param("address", "456 Updated St")
				.param("capacity", "200")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void updateShortNameVenue() throws Exception {
		// Create a test venue
		Venue testVenue = new Venue();
		testVenue.setName("Test Venue");
		testVenue.setAddress("123 Test St");
		testVenue.setCapacity(100);

		// Mock the venue service to return the test venue
		when(venueService.findById(1)).thenReturn(Optional.of(testVenue));
		when(mapboxService.geocode(any(String.class))).thenReturn(new Double[] { 0.0, 0.0 });

		mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "nn")
				.param("address", "456 Updated St")
				.param("capacity", "200")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void updateNegativeCapacityVenue() throws Exception {
		// Create a test venue
		Venue testVenue = new Venue();
		testVenue.setName("Test Venue");
		testVenue.setAddress("123 Test St");
		testVenue.setCapacity(100);

		// Mock the venue service to return the test venue
		when(venueService.findById(1)).thenReturn(Optional.of(testVenue));
		when(mapboxService.geocode(any(String.class))).thenReturn(new Double[] { 0.0, 0.0 });

		mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Updated Test Venue")
				.param("address", "456 Updated St")
				.param("capacity", "-200")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "capacity"))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void updateZeroCapacityVenue() throws Exception {
		// Create a test venue
		Venue testVenue = new Venue();
		testVenue.setName("Test Venue");
		testVenue.setAddress("123 Test St");
		testVenue.setCapacity(100);

		// Mock the venue service to return the test venue
		when(venueService.findById(1)).thenReturn(Optional.of(testVenue));
		when(mapboxService.geocode(any(String.class))).thenReturn(new Double[] { 0.0, 0.0 });

		mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Updated Test Venue")
				.param("address", "456 Updated St")
				.param("capacity", "0")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "capacity"))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void updateLongAddressVenue() throws Exception {
		// Create a test venue
		Venue testVenue = new Venue();
		testVenue.setName("Test Venue");
		testVenue.setAddress("123 Test St");
		testVenue.setCapacity(100);

		String long_address = "a".repeat(301);

		// Mock the venue service to return the test venue
		when(venueService.findById(1)).thenReturn(Optional.of(testVenue));
		when(mapboxService.geocode(any(String.class))).thenReturn(new Double[] { 0.0, 0.0 });

		mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Updated Test Venue")
				.param("address", long_address)
				.param("capacity", "200")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "address"))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void updateInvalidNameVenue() throws Exception {
		// name must only contain printable ASCII characters
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Eærth")
				.param("address", "Kilburn Building Manchester")
				.param("capacity", "100")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}

	@Test
	public void updateVenueNotExist() throws Exception {
		Double[] coordinate = { 0.0, 0.0 };
		when(mapboxService.geocode(any(String.class))).thenReturn(coordinate);

		when(venueService.findById(99)).thenThrow(new VenueNotFoundException(99));

		mvc.perform(put("/venues/update/99").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Earth")
				.param("address", "Kilburn Building Manchester")
				.param("capacity", "100")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(view().name("venues/error_page"))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(any(Venue.class));
	}
}
