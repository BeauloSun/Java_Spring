package uk.ac.man.cs.eventlite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

	public static final String ADMIN_ROLE = "ADMINISTRATOR", ORGANISER_ROLE = "ORGANISER", SEEKER_ROLE = "SEEKER";

	// List the mappings/methods for which no authorisation is required.	
	private static final RequestMatcher[] NO_AUTH = {
			new AntPathRequestMatcher("/webjars/**", "GET"),
			new AntPathRequestMatcher("/h2-console/**"),
			new RegexRequestMatcher("/api/?", "GET"),

			new RegexRequestMatcher("/events/?", "GET"),
			new RegexRequestMatcher("/events\\?keyword=.*", "GET"),
			new RegexRequestMatcher("/events/\\d+/?", "GET"),
			new RegexRequestMatcher("/events/shareEvent/?", "POST"),
			new RegexRequestMatcher("/api/events/?", "GET"),
			new RegexRequestMatcher("/api/events/\\d+/?", "GET"),

			new RegexRequestMatcher("/venues/?", "GET"),
			new RegexRequestMatcher("/venues\\?keyword=.*", "GET"),
			new RegexRequestMatcher("/venues/\\d+/?", "GET"),
			new RegexRequestMatcher("/api/venues/?", "GET"),
			new RegexRequestMatcher("/api/venues/\\d+/?", "GET"),
			new RegexRequestMatcher("/api/venues/\\d+/events/?", "GET"),
			new RegexRequestMatcher("/api/venues/\\d+/next3events/?", "GET")
	};
	
	private static final RequestMatcher[] ORGANISER_AUTH = {
			// Events-related authorization
			new RegexRequestMatcher("/events/add/?", "GET"),
			new RegexRequestMatcher("/events/addEvents", "POST"),
			new RegexRequestMatcher("/events/update/\\d+", "GET"),
			new RegexRequestMatcher("/events/update/\\d+", "POST"),
			new RegexRequestMatcher("/events/\\d+", "POST"),
			// Venues-related authorization
			new RegexRequestMatcher("/venues/add/?", "GET"),
			new RegexRequestMatcher("/venues/addVenue", "POST"),
			new RegexRequestMatcher("/venues/update/\\d+", "GET"),
			new RegexRequestMatcher("/venues/update/\\d+", "POST"),
			new RegexRequestMatcher("/venues/\\d+", "POST")
	};
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// By default, all requests are authenticated except our specific list.
		
		http.authorizeRequests()
			.requestMatchers(NO_AUTH).permitAll()
			.requestMatchers(ORGANISER_AUTH).hasAnyRole(ORGANISER_ROLE, ADMIN_ROLE)
			.anyRequest().hasRole(ADMIN_ROLE);
				

		// Use form login/logout for the Web.
		http.formLogin().loginPage("/sign-in").permitAll();
		http.logout().logoutUrl("/sign-out").logoutSuccessUrl("/").permitAll();

		// Use HTTP basic for the API.
		http.requestMatcher(new AntPathRequestMatcher("/api/**")).httpBasic();

		// Only use CSRF for Web requests.
		// Disable CSRF for the API and H2 console.
		http.antMatcher("/**").csrf().ignoringAntMatchers("/api/**", "/h2-console/**");

		// Disable X-Frame-Options for the H2 console.
		http.headers().frameOptions().disable();
	}

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		
		// Admins
		UserDetails rob = User.withUsername("Rob").password(encoder.encode("Haines")).roles(ADMIN_ROLE).build();
		UserDetails caroline = User.withUsername("Caroline").password(encoder.encode("Jay")).roles(ADMIN_ROLE).build();
		UserDetails markel = User.withUsername("Markel").password(encoder.encode("Vigo")).roles(ADMIN_ROLE).build();
		UserDetails mustafa = User.withUsername("Mustafa").password(encoder.encode("Mustafa")).roles(ADMIN_ROLE)
				.build();
		UserDetails tom = User.withUsername("Tom").password(encoder.encode("Carroll")).roles(ADMIN_ROLE).build();
		
		// Organisers
		UserDetails joe = User.withUsername("Joe").password(encoder.encode("Biden")).roles(ORGANISER_ROLE).build();

		return new InMemoryUserDetailsManager(rob, caroline, markel, mustafa, tom, joe);
	}
}
