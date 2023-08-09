package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import org.springframework.web.context.WebApplicationContext;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerTestFaking extends AbstractTransactionalJUnit4SpringContextTests {

	private MockMvc mvc;

	@Autowired
	private WebApplicationContext context;

	@BeforeEach
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	public void getEventsWithSearchExactMatch() throws Exception {
		mvc.perform(get("/events?keyword=COMP23412 Showcase 01").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/index"))
				.andExpect(handler().methodName("getAllEvents"))
				.andExpect(model().attribute("events", hasSize(1)))
				.andExpect(model().attribute("events", everyItem(instanceOf(Event.class))))
				.andExpect(model().attribute("events", contains(hasProperty("name", is("COMP23412 Showcase 01")))));
	}

	@Test
	public void getEventsWithSearchCaseInsensitive() throws Exception {
		mvc.perform(get("/events?keyword=comp23412 showcase 01").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/index"))
				.andExpect(handler().methodName("getAllEvents"))
				.andExpect(model().attribute("events", hasSize(1)))
				.andExpect(model().attribute("events", everyItem(instanceOf(Event.class))))
				.andExpect(model().attribute("events", contains(hasProperty("name", is("COMP23412 Showcase 01")))));
	}

	@Test
	public void getEventsWithSearchPartial() throws Exception {
		mvc.perform(get("/events?keyword=showcase 01").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/index"))
				.andExpect(handler().methodName("getAllEvents"))
				.andExpect(model().attribute("events", hasSize(1)))
				.andExpect(model().attribute("events", everyItem(instanceOf(Event.class))))
				.andExpect(model().attribute("events", contains(hasProperty("name", is("COMP23412 Showcase 01")))));
	}

	@Test
	public void postAddEventAdmin() throws Exception {
		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "11:00")
				.param("description", "testDesc")
				.param("venue", "1")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(redirectedUrl("/events"));
	}
	
	@Test
	public void postAddEvenOrganiser() throws Exception {
		mvc.perform(post("/events/addEvents").with(user("Joe").roles(Security.ORGANISER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "11:00")
				.param("description", "testDesc")
				.param("venue", "1")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(redirectedUrl("/events"));
	}
	
	@Test
	public void postAddEventNotSignedIn() throws Exception {
		mvc.perform(post("/events/addEvents").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "11:00")
				.param("venue", "1")
				.param("description", "Test Descriptions")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("http://localhost/sign-in"));
	}

	@Test
	public void postAddEventBadRole() throws Exception {
		mvc.perform(post("/events/addEvents").with(user("Rob").roles("USER")).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "11:00")
				.param("venue", "1")
				.param("description", "Test Descriptions")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
		 		.andExpect(status().isForbidden());
	}

	@Test
	public void postAddEventNoVenue() throws Exception {
		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "12:00")
				.param("description", "Test Descriptions")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(redirectedUrl(null));
	}

	@Test
	public void postAddEventBoundaryName() throws Exception {
		String name = "a".repeat(255);

		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", name)
				.param("date", "2050-01-01")
				.param("time", "11:00")
				.param("description", "testDesc")
				.param("venue", "1")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(redirectedUrl("/events"));
	}

	@Test
	public void postAddEventLongName() throws Exception {
		String name = "a".repeat(256);

		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", name)
				.param("date", "2050-01-01")
				.param("time", "12:00")
				.param("venue", "1")
				.param("description", "Test Descriptions")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(redirectedUrl(null));
	}

	@Test
	public void postAddEventBadName() throws Exception {
		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "B")
				.param("date", "2050-01-01")
				.param("time", "12:00")
				.param("venue", "1")
				.param("description", "Test Descriptions")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(redirectedUrl(null));
	}

	@Test
	public void postAddEventBadDate() throws Exception {
		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2000-01-01")
				.param("time", "12:00")
				.param("venue", "1")
				.param("description", "Test Descriptions")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(redirectedUrl(null));
	}

	@Test
	public void postAddEventNoDate() throws Exception {
		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("time", "12:00")
				.param("venue", "1")
				.param("description", "Test Descriptions")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(redirectedUrl(null));
	}

	@Test
	public void postAddEventBoundaryDesc() throws Exception {
		String desc = "a".repeat(499);

		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "11:00")
				.param("description", desc)
				.param("venue", "1")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(redirectedUrl("/events"));
	}

	@Test
	public void postAddEventLongDesc() throws Exception {
		String desc = "a".repeat(500);

		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "12:00")
				.param("venue", "1")
				.param("description", desc)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(redirectedUrl(null));
	}

	@Test
	public void postAddEventNoDesc() throws Exception {
		mvc.perform(post("/events/addEvents").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "12:00")
				.param("venue", "1")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(redirectedUrl(null));
	}

	@Test
	public void updateEventSignedIn() throws Exception {
		mvc.perform(put("/events/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Updated testEvent")
				.param("date", "2050-01-01")
				.param("time", "11:00")
				.param("description", "testDesc")
				.param("venue", "1")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeExists("yellow"));

		// TODO verification
		// // Verify that the event was updated
		// ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
		// verify(eventService).save(captor.capture());
		// Event updatedEvent = captor.getValue();
		// assertEquals("Updated Test Event", updatedEvent.getName());

	}

	@Test
	public void updateEventNoVenue() throws Exception {
		mvc.perform(put("/events/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testEvent")
				.param("date", "2050-01-01")
				.param("time", "12:00")
				.param("description", "Test Descriptions")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("events/update"))
				.andExpect(redirectedUrl(null))
				.andExpect(model().errorCount(1))
				.andExpect(handler().methodName("executeUpdate"))
				.andExpect(flash().attributeCount(0));
	}

	@Test
	public void updateEventNotFound() throws Exception {
		mvc.perform(put("/events/update/99").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Updated testEvent")
				.param("date", "2050-01-01")
				.param("time", "11:00")
				.param("description", "testDesc")
				.param("venue", "1")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(view().name("events/error_page"))
				.andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("executeUpdate"));
	}
}
