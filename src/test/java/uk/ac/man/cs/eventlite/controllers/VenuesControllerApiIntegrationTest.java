package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles({ "test", "venues", "events" })
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus()
			.isOk()
		.expectHeader()		
			.contentType(MediaType.APPLICATION_JSON)
		.expectBody()
			.jsonPath("$._embedded.venues.length()").value(equalTo(4))	
			.jsonPath("$._links.self.href").value(endsWith("/api/venues"))
		;
	}

	@Test
	public void testGetVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus()
			.isNotFound()
		.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
		.expectBody()
			.jsonPath("$.error").value(containsString("venue 99"))
			.jsonPath("$.id").isEqualTo(99)	
		;
	}
	
	@Test
	public void testGetVenue() {
		client.get().uri("/venues/1").accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus()
			.isOk()
		.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
		.expectBody()
			.jsonPath("$.name").value(containsString("Kilburn Building"))
			.jsonPath("$._links.self.href").value(endsWith("/api/venues/1"))
		;
	}
	
	
	@Test
	public void testGetEventsAtVenueNotFound() {
		client.get().uri("/venues/99/events").accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus()
			.isNotFound()
		.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
		.expectBody()
			.jsonPath("$.error").value(containsString("venue 99"))
			.jsonPath("$.id").isEqualTo(99)	
		;
	}
	
	@Test
	public void testGetEventsAtVenue() {
		client.get().uri("/venues/1/events").accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus()
			.isOk()
		.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
		.expectBody()
			.jsonPath("$._embedded.events.length()").value(equalTo(3))	
			.jsonPath("$._embedded.events[0].name").value(containsString("COMP23412 Showcase 01"))
			.jsonPath("$._links.self.href").value(endsWith("/api/venues/1/events"))
		;
	}
	
	@Test
	public void testGetNext3EventsAtVenueNotFound() {
		client.get().uri("/venues/99/next3events").accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus()
			.isNotFound()
		.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
		.expectBody()
			.jsonPath("$.error").value(containsString("venue 99"))
			.jsonPath("$.id").isEqualTo(99)	
		;
	}
	
	@Test
	public void testGetNext3EventsAtVenueFound() {
		client.get().uri("/venues/1/next3events").accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus()
			.isOk()
		.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
		.expectBody()
			.jsonPath("$._embedded.events.length()").value(equalTo(3))	
			.jsonPath("$._embedded.events[0].name").value(containsString("COMP23412 Showcase 01"))
			.jsonPath("$._embedded.events[1].name").value(containsString("COMP23412 Showcase 02"))
			.jsonPath("$._embedded.events[2].name").value(containsString("COMP23412 Showcase 03"))
			.jsonPath("$._links.self.href").value(endsWith("/api/venues/1/next3events"))
		;
	}
	
}
