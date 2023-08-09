package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
import org.springframework.web.context.WebApplicationContext;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerTestFaking extends AbstractTransactionalJUnit4SpringContextTests{

	private MockMvc mvc;
	
	@Autowired
	private WebApplicationContext context;
		
	@BeforeEach
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}
	
	@Test
	public void getVenuesWithSearchExactMatch() throws Exception {
		mvc.perform(get("/venues/?keyword=Kilburn Building").accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("venues/index"))
			.andExpect(handler().methodName("getAllVenues"))
			.andExpect(model().attribute("venues", hasSize(1)))
			.andExpect(model().attribute("venues", everyItem(instanceOf(Venue.class))))
		    .andExpect(model().attribute("venues", contains(hasProperty("name", is("Kilburn Building")))));
	}
	
	@Test
	public void getVenuesWithSearchCaseInsensitive() throws Exception {
		mvc.perform(get("/venues/?keyword=kilburn building").accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("venues/index"))
			.andExpect(handler().methodName("getAllVenues"))
			.andExpect(model().attribute("venues", hasSize(1)))
			.andExpect(model().attribute("venues", everyItem(instanceOf(Venue.class))))
		    .andExpect(model().attribute("venues", contains(hasProperty("name", is("Kilburn Building")))));
	}
	
	@Test
	public void getVenuesWithSearchPartial() throws Exception {
		mvc.perform(get("/venues/?keyword=Kil").accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("venues/index"))
			.andExpect(handler().methodName("getAllVenues"))
			.andExpect(model().attribute("venues", hasSize(1)))
			.andExpect(model().attribute("venues", everyItem(instanceOf(Venue.class))))
		    .andExpect(model().attribute("venues", contains(hasProperty("name", is("Kilburn Building")))));
	}
	
}

