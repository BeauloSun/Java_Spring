package uk.ac.man.cs.eventlite.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.lessThan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@ActiveProfiles("test")
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;

	@Test
	public void testCount() {
		venueService.save(new Venue());

		assertThat(5L, is(equalTo(venueService.count())));
	}

	@Test
	public void testFindAll() {
		Iterable<Venue> venues = venueService.findAll();
		venueService.save(new Venue());

		assertThat(venues, not(equalTo(venueService.findAll())));
	}

	@Test
	public void testSave() {
		long count = venueService.count();
		venueService.save(new Venue());

		assertThat(count, is(lessThan(venueService.count())));
	}

	@Test
	public void testFindById() {
		String venue = venueService.findById(1).get().getName();

		assertThat("Kilburn Building", is(equalTo(venue)));
	}
}
