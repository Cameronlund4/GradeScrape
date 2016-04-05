package info.cameronlund.gradescrape.api;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import info.cameronlund.gradescrape.user.Credentials;
import info.cameronlund.gradescrape.user.Student;

public class AuthenticatedSite extends ScrapableSite {
	private final Credentials credentials;

	public AuthenticatedSite(Student student, Credentials credentials)
	{
		super(student);
		this.credentials = credentials;
	}

	public String getUsername() {
		return credentials.getUsername();
	}

	public String getPassword() {
		return credentials.getPassword();
	}

	public Credentials getCredentials() {
		return credentials;
	}

	// Should return whether or not authentication was successful
	public boolean authenticate(Credentials credentials) {
		return false;
	}
}
