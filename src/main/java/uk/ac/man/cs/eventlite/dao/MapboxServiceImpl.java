package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Service
public class MapboxServiceImpl implements MapboxService{
	
	private String ACCESS_TOKEN = "pk.eyJ1IjoiZmFoaW02MjAiLCJhIjoiY2xmNnNnZndvMGs2bzQ0cGNxa2dmOHI5cCJ9.paIRhUlENneZhrlglP3M0g";
	
	private Double longitude;
	
	private Double latitude;
	
	@Override
	public Double[] geocode(String query) throws InterruptedException {
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken(ACCESS_TOKEN)
				.query(query)
				.build();
			
			mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
				@Override
				public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
			 
					List<CarmenFeature> results = response.body().features();
			 
					if (results.size() > 0) {
			 
					  Point firstResultPoint = results.get(0).center();
					  setLongitude(firstResultPoint.longitude());
					  setLatitude(firstResultPoint.latitude());
					  
					}
				}
			 
				@Override
				public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
					throwable.printStackTrace();
				}
			});
			
			Thread.sleep(1000L);
			
			return new Double[]{getLongitude(), getLatitude()};
	}
	
	@Override
	public Double getLongitude() {
		return longitude;
	}
	
	@Override
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
	@Override
	public Double getLatitude() {
		return latitude;
	}
	
	@Override
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	
	@Override
	public String getAccessToken() {
		return ACCESS_TOKEN;
	}

}
