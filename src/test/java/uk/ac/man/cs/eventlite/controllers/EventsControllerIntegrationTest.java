package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static uk.ac.man.cs.eventlite.testutil.FormUtil.getCsrfToken;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	private final static String SESSION_KEY = "JSESSIONID";
	private final static String LOGIN_PATH = "/sign-in";
	private final static String LOGIN_ERROR = LOGIN_PATH + "?error";
	private final static String LOGOUT_PATH = "/sign-out";

	@LocalServerPort
	private int port;
	
	private String cookie;
	
	private String baseUri;

	private WebTestClient client;
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@BeforeEach
	public void setup() {
		baseUri = "http://localhost:" + port;
		client = WebTestClient.bindToServer().baseUrl(baseUri).build();
		
		EntityExchangeResult<String> result = client.get().uri(LOGIN_PATH).accept(MediaType.TEXT_HTML).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());
		cookie = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("username", "Rob");
		form.add("password", "Haines");

		result = client.post().uri(LOGIN_PATH).accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).cookie(SESSION_KEY, cookie).bodyValue(form)
				.exchange().expectStatus().isFound().expectHeader().value("Location", equalTo(baseUri + "/"))
				.expectBody(String.class).returnResult();
		cookie = result.getResponseCookies().getFirst(SESSION_KEY).getValue();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("All <b>Events</b>"));
			assertThat(result.getResponseBody(), containsString("Upcoming"));
			assertThat(result.getResponseBody(), containsString("Social <b>Feed</b>"));
			assertThat(result.getResponseBody(), containsString("Previous"));
		});
	}
	
	@Test
	public void testGetEvent() {
		client.get().uri("/events/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Event <b>Details</b>"));
			assertThat(result.getResponseBody(), containsString("COMP23412 Showcase 01"));
		});
	}

	@Test
	public void testGetEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	
	@Test
	public void testGetAddEventUnauthorised() {
		client.get().uri("/events/add").accept(MediaType.TEXT_HTML).exchange().expectStatus().is3xxRedirection()
		.expectHeader().valueEquals("Location", "http://localhost:" + port + "/sign-in");
	}
	
	@Test
	public void testGetAddEventAuthorised() {
		client.get().uri("/events/add").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Add <b>Event</b>"));
		});
	}
	
	@Test
	public void testGetUpdateEventUnauthorised() {
		client.get().uri("/events/update/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().is3xxRedirection()
		.expectHeader().valueEquals("Location", "http://localhost:" + port + "/sign-in");
	}
	
	@Test
	public void testGetUpdateEventAuthorised() {
		client.get().uri("/events/update/1").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Update <b>Event</b>"));
		});
	}
	
	@Test
	public void testPostAddEventUnauthorised() {
		Event event = new Event();
		event.setName("event");
		event.setDescription("description");
		event.setDate(LocalDate.now());
		event.setTime(LocalTime.now());
		event.setVenue(venueService.findById(1).get());
		
		client.post().uri("/events/addEvents").accept(MediaType.TEXT_HTML).bodyValue(event).exchange()
		.expectStatus().is3xxRedirection().expectHeader().valueEquals("Location", "http://localhost:" + port + "/sign-in");
	}
	
	@Test
	public void testPostAddEventSensibleData() {
		EntityExchangeResult<String> result = client.get().uri("/events/add").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", csrfToken);
		event.add("name", "event");
		event.add("description", "desc");
		event.add("date", "2023-06-01");
		event.add("time", "00:00");
		event.add("venue", "1");

		client.post().uri("/events/addEvents").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie(SESSION_KEY, cookie).bodyValue(event).exchange().expectStatus().is3xxRedirection()
				.expectHeader().valueEquals("Location", baseUri + "/events");
	}
	
	@Test
	public void testPostAddEventNoData() {
		EntityExchangeResult<String> result = client.get().uri("/events/add").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", csrfToken);

		client.post().uri("/events/addEvents").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie(SESSION_KEY, cookie).bodyValue(event).exchange().expectStatus().isOk();
	}
	
	@Test
	public void testPostAddEventBadData() {
		EntityExchangeResult<String> result = client.get().uri("/events/add").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", csrfToken);
		event.add("name", "");
		event.add("description", "");
		event.add("date", "");
		event.add("time", "");
		event.add("venue", "");

		client.post().uri("/events/addEvents").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie(SESSION_KEY, cookie).bodyValue(event).exchange().expectStatus().isOk();
	}
	
	@Test
	public void testPutUpdateEventUnauthorised() {
		Event event = new Event();
		event.setName("event");
		event.setDescription("description");
		event.setDate(LocalDate.now());
		event.setTime(LocalTime.now());
		event.setVenue(venueService.findById(1).get());
		
		client.put().uri("/events/update/1").accept(MediaType.TEXT_HTML).bodyValue(event).exchange()
		.expectStatus().is3xxRedirection().expectHeader().valueEquals("Location", "http://localhost:" + port + "/sign-in");
	}
	
	@Test
	public void testPutUpdateEventSensibleData() {
		EntityExchangeResult<String> result = client.get().uri("/events/update/1").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", csrfToken);
		event.add("name", "event");
		event.add("description", "desc");
		event.add("date", "2023-06-01");
		event.add("time", "00:00");
		event.add("venue", "1");

		client.put().uri("/events/update/1").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie(SESSION_KEY, cookie).bodyValue(event).exchange().expectStatus().is3xxRedirection()
				.expectHeader().valueEquals("Location", baseUri + "/events");
	}
	
	@Test
	public void testPutUpdateEventNoData() {
		EntityExchangeResult<String> result = client.get().uri("/events/update/1").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", csrfToken);

		client.put().uri("/events/update/1").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.cookie(SESSION_KEY, cookie).bodyValue(event).exchange().expectStatus().isOk();
	}
	
	@Test
	public void testPutUpdateEventBadData() {
		EntityExchangeResult<String> result = client.get().uri("/events/update/1").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", csrfToken);
		event.add("name", "");
		event.add("description", "");
		event.add("date", "");
		event.add("time", "");
		event.add("venue", "");

		client.put().uri("/events/update/1").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.cookie(SESSION_KEY, cookie).bodyValue(event).exchange().expectStatus().isOk();
	}
	
	@Test
	public void testDeleteEventUnauthorised() {
		client.delete().uri("/events/1").accept(MediaType.TEXT_HTML).exchange()
		.expectStatus().is3xxRedirection().expectHeader().valueEquals("Location", "http://localhost:" + port + "/sign-in");
	}
	
	@Test
	public void testDeleteEventAuthorised() {
		EntityExchangeResult<String> result = client.get().uri("/events/1").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", csrfToken);

		((RequestBodySpec) client.delete().uri("/events/1").accept(MediaType.TEXT_HTML))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).cookie(SESSION_KEY, cookie).bodyValue(event).exchange()
		.expectStatus().is3xxRedirection().expectHeader().valueEquals("Location", "http://localhost:" + port + "/events");
	}
	
	
	@Test
	public void testShareEvent() {
		EntityExchangeResult<String> result = client.get().uri("/events/1").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());
		
		MultiValueMap<String, String> shareEvent = new LinkedMultiValueMap<>();
		shareEvent.add("_csrf", csrfToken);
		shareEvent.add("id", "1");
		
		Random random = new Random();
		String[] haikus = {
			    "An old silent pond\nA frog jumps into the pond—\nSplash! Silence again.",
			    "Light of a candle\nIs transferred to another candle—\nSpring twilight",
			    "Winter solitude -\nin a world of one color\nthe sound of wind",
			    "The light of a star\nIs the whispers of the past\nReaching out to the future",
			    "All the flowers of the\ncherry bloosom in the spray\nof the waterfall."
			};
        String randomElement = haikus[random.nextInt(haikus.length)];
		
		shareEvent.add("message", randomElement);
		
		client.post().uri("/events/shareEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.cookie(SESSION_KEY, cookie).bodyValue(shareEvent).exchange().expectStatus().is3xxRedirection()
		.expectHeader().valueEquals("Location", "http://localhost:" + port + "/events/1");
	}
}
