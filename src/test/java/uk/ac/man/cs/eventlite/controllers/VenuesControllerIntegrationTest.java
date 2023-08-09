package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.Matchers.equalTo;

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
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec;

import static uk.ac.man.cs.eventlite.testutil.FormUtil.getCsrfToken;
import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")

public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private final static String SESSION_KEY = "JSESSIONID";
	private final static String LOGIN_PATH = "/sign-in";

	@LocalServerPort
	private int port;

	private String cookie;

	private String baseUri;

	private WebTestClient client;

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
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("All <b>Venues</b>"));
					assertThat(result.getResponseBody(), containsString("Venue"));
					assertThat(result.getResponseBody(), containsString("Address"));
					assertThat(result.getResponseBody(), containsString("Capacity"));
				});

	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}

	@Test
	public void testGetVenue() {
		client.get().uri("/venues/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Venue <b>Details</b>"));
					assertThat(result.getResponseBody(), containsString("Kilburn Building"));
				});
	}

	@Test
	public void testUpdateVenuePageUnauthorised() {
		client.get().uri("/venues/update/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().is3xxRedirection()
		.expectHeader().value("Location", equalTo("http://localhost:" + port + "/sign-in"));
	}
	
	@Test
	public void testUpdateVenuePageAuthorised() {
		client.get().uri("/venues/update/1").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange().expectStatus().isOk();
	}

	@Test
	public void testAddVenuePageUnauthorised() {
		client.get().uri("/venues/add").accept(MediaType.TEXT_HTML).exchange().expectStatus().is3xxRedirection()
		.expectHeader().value("Location", equalTo("http://localhost:" + port + "/sign-in"));
	}
	
	@Test
	public void testAddVenuePageAuthorised() {
		client.get().uri("/venues/add").accept(MediaType.TEXT_HTML).cookie(SESSION_KEY, cookie).exchange().expectStatus().isOk();
	}

	@Test
	public void testPostAddVenue() {
		EntityExchangeResult<String> result = client.get().uri("/venues/add").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> add_venue_form = new LinkedMultiValueMap<>();
		add_venue_form.add("_csrf", csrfToken);
		add_venue_form.add("name", "My Home");
		add_venue_form.add("address", "Mar-a-lago Club");
		add_venue_form.add("capacity", "3000");

		client.post().uri("/venues/addVenue").accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie("JSESSIONID", cookie).bodyValue(add_venue_form).exchange().expectStatus().is3xxRedirection()
				.expectHeader().value("Location", equalTo("http://localhost:" + port + "/venues"));
	}

	@Test
	public void testPostAddVenueNoData() {
		EntityExchangeResult<String> result = client.get().uri("/venues/add").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> add_venue_form = new LinkedMultiValueMap<>();
		add_venue_form.add("_csrf", csrfToken);
		add_venue_form.add("name", "");
		add_venue_form.add("address", "");
		add_venue_form.add("capacity", "");

		client.post().uri("/venues/addVenue").accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie("JSESSIONID", cookie).bodyValue(add_venue_form).exchange().expectStatus().isOk();
	}

	@Test
	public void testPostAddVenueBadData() {
		EntityExchangeResult<String> result = client.get().uri("/venues/add").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		String wierdString = "";
		for (int i = 0; i < 302; i++) {
			wierdString += "x";
		}

		MultiValueMap<String, String> add_venue_form = new LinkedMultiValueMap<>();
		add_venue_form.add("_csrf", csrfToken);
		add_venue_form.add("name", "A");
		add_venue_form.add("address", wierdString);
		add_venue_form.add("capacity", "-999");

		client.post().uri("/venues/addVenue").accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie("JSESSIONID", cookie).bodyValue(add_venue_form).exchange().expectStatus().isOk();
	}

	@Test
	public void testPostAddVenueUnauthorised() {
		EntityExchangeResult<String> result = client.get().uri("/venues/add").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> add_venue_form = new LinkedMultiValueMap<>();
		add_venue_form.add("_csrf", csrfToken);
		add_venue_form.add("name", "My Home");
		add_venue_form.add("address", "Mar-a-lago Club");
		add_venue_form.add("capacity", "3000");

		client.post().uri("/venues/addVenue").accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(add_venue_form).exchange().expectStatus().is3xxRedirection()
				.expectHeader().value("Location", equalTo("http://localhost:" + port + "/sign-in"));
	}

	@Test
	public void testDeleteVenueWithEvents() {
		client.delete().uri("/venues/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound();
	}

	@Test
	public void testDeleteVenueWithoutEvents() {
		EntityExchangeResult<String> result = client.get().uri("/venues/2").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());
		MultiValueMap<String, String> venue = new LinkedMultiValueMap<>();
		venue.add("_csrf", csrfToken);

		((RequestBodySpec) client.delete().uri("/venues/2").accept(MediaType.TEXT_HTML))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).cookie("JSESSIONID", cookie).bodyValue(venue)
				.exchange()
				.expectStatus().is3xxRedirection().expectHeader()
				.valueEquals("Location", "http://localhost:" + port + "/venues");
	}

	@Test
	public void testDeleteVenueWithoutEventsUnauthorised() {
		EntityExchangeResult<String> result = client.get().uri("/venues/2").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());
		MultiValueMap<String, String> venue = new LinkedMultiValueMap<>();
		venue.add("_csrf", csrfToken);

		((RequestBodySpec) client.delete().uri("/venues/2").accept(MediaType.TEXT_HTML))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(venue)
				.exchange()
				.expectStatus().is3xxRedirection().expectHeader()
				.valueEquals("Location", "http://localhost:" + port + "/sign-in");
	}

	@Test
	public void testExecuteUpdate() {
		EntityExchangeResult<String> result = client.get().uri("/venues/update/1").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> update_venue_form = new LinkedMultiValueMap<>();
		update_venue_form.add("_csrf", csrfToken);
		update_venue_form.add("name", "White House");
		update_venue_form.add("address", "Washington DC");
		update_venue_form.add("capacity", "100");

		client.put().uri("/venues/update/1").accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie("JSESSIONID", cookie).bodyValue(update_venue_form).exchange().expectStatus().is3xxRedirection()
				.expectHeader().value("Location", equalTo("http://localhost:" + port + "/venues"));
	}

	@Test
	public void testExecuteUpdateNoData() {
		EntityExchangeResult<String> result = client.get().uri("/venues/update/1").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> addEvent = new LinkedMultiValueMap<>();
		addEvent.add("_csrf", csrfToken);
		addEvent.add("name", null);
		addEvent.add("address", null);
		addEvent.add("capacity", null);

		client.put().uri("/venues/update/1").accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie("JSESSIONID", cookie).bodyValue(addEvent).exchange().expectStatus().is2xxSuccessful()
				.expectHeader().value("Location", equalTo(null));
	}

	@Test
	public void testExecuteUpdateBadData() {
		EntityExchangeResult<String> result = client.get().uri("/venues/update/1").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> addEvent = new LinkedMultiValueMap<>();
		addEvent.add("_csrf", csrfToken);
		addEvent.add("name", "White House");
		addEvent.add("address", "Washington DC");
		addEvent.add("capacity", "-10");

		client.put().uri("/venues/update/1").accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie("JSESSIONID", cookie).bodyValue(addEvent).exchange().expectStatus().is2xxSuccessful()
				.expectHeader().value("Location", equalTo(null));
	}

	@Test
	public void testExecuteUpdateUnauthorisedUser() {
		EntityExchangeResult<String> result = client.get().uri("/venues/update/1").accept(MediaType.TEXT_HTML)
				.cookie("JSESSIONID", cookie)
				.exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());

		MultiValueMap<String, String> addEvent = new LinkedMultiValueMap<>();
		addEvent.add("_csrf", csrfToken);
		addEvent.add("name", "White House");
		addEvent.add("address", "Washington DC");
		addEvent.add("capacity", "100");

		client.put().uri("/venues/update/1").accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(addEvent).exchange().expectStatus().is3xxRedirection()
				.expectHeader().value("Location", equalTo("http://localhost:" + port + "/sign-in"));

	}
}
