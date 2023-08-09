package uk.ac.man.cs.eventlite.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Entity
@Table(name = "venues")
public class Venue {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	@Column(name = "name")
	@Pattern(regexp = "^[ -~£]*$", message = "Name contains invalid characters!")
	@Size(min = 3, message = "Name of venues should have at least 3 characters")
	@Size(max = 255, message = "Name of venues should have less than 256 characters")
	private String name;

	@NotBlank
	@Column(name = "address")
	@Size(max = 300, message = "Address should have at most 300 characters")
	// TODO add address validation including road name and postcode
	private String address;

	@Column(name = "capacity")
	@Positive
	private int capacity;

	@Column(name = "longitude")
	private Double longitude;

	@Column(name = "latitude")
	private Double latitude;

	public Venue() {
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		name = name.replaceAll("^\\s*|\\s*$", "");
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setCoords(Double[] coords) throws InterruptedException {
		setLongitude(coords[0]);
		setLatitude(coords[1]);
	}
}
