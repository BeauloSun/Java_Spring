package uk.ac.man.cs.eventlite.dao;

public interface MapboxService {

	Double[] geocode(String query) throws InterruptedException;

	Double getLongitude();

	void setLongitude(Double longitude);

	Double getLatitude();

	void setLatitude(Double latitude);

	String getAccessToken();

}
