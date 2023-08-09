package uk.ac.man.cs.eventlite.exceptions;

public class VenueNotDeletedException extends RuntimeException{
	
	private static final long serialVersionUID = 5016812401135889608L;

	private long id;
	
	public VenueNotDeletedException(long id) {
		super("Venue " + id + " has events ");

		this.id = id;
	}

	public long getId() {
		return id;
	}

}
