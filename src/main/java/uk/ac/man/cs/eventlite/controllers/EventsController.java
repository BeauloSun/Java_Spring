package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.entity.Status;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.MapboxService;
import uk.ac.man.cs.eventlite.dao.MastodonService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Autowired
	private MapboxService mapboxService;

	@Autowired
	private MastodonService mastodonService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("error_title", "Event not found");
		model.addAttribute("event_id", ex.getId());
		model.addAttribute("error_msg", "was not found");

		return "events/error_page";
	}

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {

		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));

		model.addAttribute("event", event);

		model.addAttribute("accessToken", mapboxService.getAccessToken());

		model.addAttribute("message", new String());

		return "events/show";
	}

	private class Feed {
		private String date;
		private String time;
		private String content;
		private String url;

		public Feed(String date, String time, String content, String url) {
			this.date = date;
			this.time = time;
			this.content = content;
			this.url = url;
		}

		public String getDate() {
			return this.date;
		}

		public String getTime() {
			return this.time;
		}

		public String getContent() {
			return this.content;
		}

		public String getUrl() {
			return this.url;
		}
	}

	@GetMapping
	public String getAllEvents(Model model, @Param("keyword") String keyword) throws Mastodon4jRequestException {

		List<Status> allFeed = mastodonService.read();
		ArrayList<Feed> parsedFeed = new ArrayList<Feed>();
		for (Status status : allFeed) {
			String eachDateTime = status.getCreatedAt();
			eachDateTime = eachDateTime.substring(0, eachDateTime.length() - 1);
			String eachContent = status.getContent()
					.replaceAll("<a(?: [^>]*)?>", "").replaceAll("</a>", "");

			// LocalDateTime parsedEachDate = LocalDateTime.parse(eachDate);
			// LocalDateTime parsedEachTime = LocalDateTime.parse(eachTime);
			// System.out.println(parsedEachDate);
			LocalDateTime dateTimeTmp = LocalDateTime.parse(eachDateTime);

			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			String parsedDate = dateTimeTmp.format(dateFormatter);
			String parsedTime = dateTimeTmp.format(timeFormatter);

			Feed eachParsedFeed = new Feed(parsedDate, parsedTime, eachContent, status.getUrl());
			parsedFeed.add(eachParsedFeed);
		}

		model.addAttribute("keyword", keyword);
		model.addAttribute("events_upcoming", eventService.findAll("upcoming", keyword));
		model.addAttribute("events_previous", eventService.findAll("previous", keyword));
		model.addAttribute("events_social", parsedFeed);
		model.addAttribute("events", eventService.findAll(null, keyword));

		model.addAttribute("accessToken", mapboxService.getAccessToken());

		return "events/index";
	}

	@GetMapping("/update/{id}")
	public String updateEvent(@PathVariable("id") long id, Model model) {
		if (!model.containsAttribute("event")) {
			Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
			model.addAttribute("event", event);
		}

		model.addAttribute("venues", venueService.findAll());

		return "events/update";
	}

	@PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String executeUpdate(@PathVariable("id") long id, @Valid @ModelAttribute("event") Event updatedEvent,
			BindingResult result, Model model, RedirectAttributes redirectAttrs) {
		if (result.hasErrors()) {
			return updateEvent(id, model);
		}
		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		event.setName(updatedEvent.getName());
		event.setDate(updatedEvent.getDate());
		event.setTime(updatedEvent.getTime());
		event.setDescription(updatedEvent.getDescription());
		event.setVenue(updatedEvent.getVenue());

		redirectAttrs.addFlashAttribute("yellow", "Event updated");
		eventService.save(event);

		return "redirect:/events";
	}

	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}

		redirectAttrs.addFlashAttribute("red", "Event deleted");
		eventService.deleteById(id);

		return "redirect:/events";
	}

	@GetMapping("/add")
	public String viewAddEvents(Model model) {
		if (!model.containsAttribute("event")) {
			Event event = new Event();
			event.setDate(LocalDate.now());
			model.addAttribute("event", event);
		}
		model.addAttribute("venues", venueService.findAll());

		return "events/add";
	}

	@PostMapping(path = "/addEvents", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String addEvents(@Valid @ModelAttribute("event") Event event,
			BindingResult result, Model model, RedirectAttributes redirectAttrs) {
		if (result.hasErrors()) {
			return viewAddEvents(model);
		}

		redirectAttrs.addFlashAttribute("green", "Event added");
		eventService.save(event);

		return "redirect:/events";
	}

	@PostMapping(path = "/shareEvent", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String shareEvent(@RequestParam("message") String message, @RequestParam("id") Integer id,
			RedirectAttributes redirectAttrs, Model model) throws Mastodon4jRequestException {

		mastodonService.publish(message);
		redirectAttrs.addFlashAttribute("message_posted", message);

		return "redirect:/events/" + id;
	}

}
