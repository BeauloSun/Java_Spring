package uk.ac.man.cs.eventlite.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.MapboxService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotDeletedException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Autowired
	private MapboxService mapboxService;

	@ExceptionHandler(VenueNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String venueNotFoundHandler(VenueNotFoundException ex, Model model) {
		model.addAttribute("error_title", "Venue not found");
		model.addAttribute("venue_id", ex.getId());
		model.addAttribute("error_msg", "was not found");

		return "venues/error_page";
	}

	@ExceptionHandler(VenueNotDeletedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String venueNotDeletedHandler(VenueNotDeletedException ex, Model model) {
		model.addAttribute("error_title", "Cannot delete vennue");
		model.addAttribute("venue_id", ex.getId());
		model.addAttribute("error_msg", "has events and cannot be deleted");

		return "venues/error_page";
	}

	@GetMapping
	public String getAllVenues(Model model, @Param("keyword") String keyword) {
		model.addAttribute("venues", venueService.findAll(keyword));
		return "venues/index";
	}

	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id,
			@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {

		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));

		model.addAttribute("events_for_venue", venueService.findEventsByVenue(id));

		model.addAttribute("venue", venue);

		model.addAttribute("accessToken", mapboxService.getAccessToken());

		return "venues/show";
	}

	@GetMapping("/update/{id}")
	public String updateVenue(@PathVariable("id") long id, Model model) {
		if (!model.containsAttribute("venue")) {
			Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
			model.addAttribute("venue", venue);
		}

		return "venues/update";
	}

	@PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String executeUpdate(@PathVariable("id") long id, @Valid @ModelAttribute("venue") Venue updatedVenue,
			BindingResult result, Model model, RedirectAttributes redirectAttrs) throws InterruptedException {
		if (result.hasErrors()) {
			return updateVenue(id, model);
		}

		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		venue.setName(updatedVenue.getName());
		venue.setCapacity(updatedVenue.getCapacity());

		if (!venue.getAddress().equals(updatedVenue.getAddress())) {
			venue.setAddress(updatedVenue.getAddress());
			venue.setCoords(mapboxService.geocode(updatedVenue.getAddress()));
		}

		redirectAttrs.addFlashAttribute("yellow", "Venue updated");
		venueService.save(venue);
		return "redirect:/venues";
	}

	@DeleteMapping("/{id}")
	public String deleteVenue(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		if (!venueService.existsById(id)) {
			throw new VenueNotFoundException(id);
		}

		if (venueService.hasEvent(id)) {
			throw new VenueNotDeletedException(id);
		}

		redirectAttrs.addFlashAttribute("red", "Venue deleted");
		venueService.deleteById(id);
		return "redirect:/venues";
	}

	@GetMapping("/add")
	public String viewAddVenue(Model model) {
		if (!model.containsAttribute("venue")) {
			model.addAttribute("venue", new Venue());
		}

		return "venues/add";
	}

	@PostMapping(path = "/addVenue", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String addVenue(@Valid @ModelAttribute("venue") Venue venue,
			BindingResult result, Model model, RedirectAttributes redirectAttrs)
			throws InterruptedException {
		if (result.hasErrors()) {
			return viewAddVenue(model);
		}

		venue.setCoords(mapboxService.geocode(venue.getAddress()));
		redirectAttrs.addFlashAttribute("green", "Venue added");
		venueService.save(venue);

		return "redirect:/venues";
	}
}
