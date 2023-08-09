package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Column;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "events")
public class Event {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "Date is required")
	@Future(message = "Date must be in the future")
	@Column(name = "date")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "HH:mm")
	@Column(name = "time")
	private LocalTime time;

	@Size(min = 3, message = "Name of events should have at least 3 characters")
	@Size(max = 255, message = "Name of events should have less than 256 characters")
	// printable ASCII chars
	@Pattern(regexp = "^[ -~£]*$", message = "Name contains invalid characters!")
	// exclude useless characters
	@Pattern(regexp = "^[^\\*\\$;<=>\\^`\\\\]*$", message = "Name contains invalid characters!")
	@Column(name = "name")
	@NotNull(message = "Name is required")
	private String name;

	@NotNull
	@Lob
	@Size(max = 499, message = "Description should have less than 500 characters")
	@Column(name = "description")
	private String description;

	@ManyToOne
	@JoinColumn(name = "venue_id", nullable = false)
	@NotNull(message = "Venue is required")
	private Venue venue;

	public Event() {
	}

	public long getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		// strip whitespace
		name = name.replaceAll("^\\s*|\\s*$", "");
		this.name = name;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
