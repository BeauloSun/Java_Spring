package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

public interface MastodonService {
	
	void publish(String message) throws Mastodon4jRequestException;
	
	List<Status> read() throws Mastodon4jRequestException;
}
