package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalTime;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.MapboxService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	@Autowired
	private MapboxService mapboxService;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				// Build and save initial venues here.
				
				createVenue(
						"Kilburn Building", 
						"Kilburn Building, Oxford Rd, Manchester M13 9PL", 
						120, 
						true
						);
				
				createVenue(
						"Online", 
						"127.0.0.1", 
						100000, 
						false
						);

				createVenue(
						"Venue A", 
						"1600 Pennsylvania Avenue NW Washington, D.C. 20500 U.S.", 
						120, 
						true
						);
				
				createVenue(
						"Venue B", 
						"London SW1A 0AA", 
						120, 
						true
						);
				
			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
				
				createEvent(
						"COMP23412 Showcase 01", 
						"COMP23412 Showcase 01 - Description", 
						"2023-05-08", 
						"12:00", 
						1
						);
				
				createEvent(
						"COMP23412 Showcase 02", 
						"COMP23412 Showcase 02 - Description", 
						"2023-05-09", 
						"11:00", 
						1
						);
				
				createEvent(
						"COMP23412 Showcase 03", 
						"COMP23412 Showcase 03 - Description", 
						"2023-05-11", 
						"11:00", 
						1
						);
				
				createEvent(
						"Event Alpha", 
						"Event Alpha - Description", 
						"2023-07-11", 
						"12:30", 
						4
						);
				
				createEvent(
						"Event Beta", 
						"Event Beta - Description", 
						"2023-07-11", 
						"10:00", 
						3
						);
				
				createEvent(
						"Event Apple", 
						"Event Apple - Description", 
						"2023-07-12", 
						null, 
						3
						);
				
				createEvent(
						"Event Former", 
						"Event Former - Description", 
						"2023-01-11", 
						"11:00", 
						4
						);
				
				createEvent(
						"Event Previous", 
						"Event Previous - Description", 
						"2023-01-11", 
						"18:30", 
						3
						);
				
				createEvent(
						"Event Past", 
						"Event Past - Description", 
						"2023-01-10", 
						"17:00", 
						3
						);
				
			}
		};
	}

	private void createVenue(String name, String address, Integer capacity, Boolean setCoords) throws InterruptedException {
		
		Venue venue = new Venue();
		venue.setName(name);
		venue.setAddress(address);
		if (capacity != null) venue.setCapacity(capacity);
		if (setCoords) venue.setCoords(mapboxService.geocode(address));
		venueService.save(venue);
		
	}
	
	private void createEvent(String name, String description, String date, String time, Integer venue) {
		
		Event event = new Event();
		event.setName(name);
		if (description != null) event.setDescription(description);
		event.setDate(LocalDate.parse(date));
		if (time != null) event.setTime(LocalTime.parse(time));
		event.setVenue(venueService.findById(venue).get());
		eventService.save(event);
		
	}
	
}
