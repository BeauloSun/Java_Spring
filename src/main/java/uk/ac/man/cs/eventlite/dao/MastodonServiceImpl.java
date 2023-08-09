package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Timelines;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import okhttp3.OkHttpClient;

@Service
public class MastodonServiceImpl implements MastodonService {
	
	private final static Logger log = LoggerFactory.getLogger(MastodonServiceImpl.class);
	
	private String ACCESS_TOKEN = "W3lac6HrYz84I_Uq6wyLrU9psiFV96ijk83nD32ujkI";
	
	MastodonClient client = new MastodonClient.Builder("mstdn.social", new OkHttpClient.Builder(), new Gson())
            .accessToken(this.ACCESS_TOKEN)
            .useStreamingApi()
            .build();
	
	@Override
	public void publish(String message) throws Mastodon4jRequestException {
		Statuses s = new Statuses(this.client);
		s.postStatus(message, null, null, false, null, Status.Visibility.Unlisted).execute();
	}

	@Override
	public List<Status> read() throws Mastodon4jRequestException {
		Timelines timelines = new Timelines(this.client);
		Pageable<Status> homeTimelinePageable = timelines.getHome(new Range()).execute();
		List<Status> homeTimelineList = homeTimelinePageable.getPart();
		homeTimelineList.removeIf(status -> !status.getAccount().getUserName().equals("Eleven11"));
		return homeTimelineList;
	}
}
