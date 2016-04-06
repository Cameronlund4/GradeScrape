package info.cameronlund.gradescrape.user;

import com.gargoylesoftware.htmlunit.WebClient;

import java.util.HashMap;
import java.util.Map;

public class Student {
	private final String firstName;
	private final String lastName;
	private final WebClient client;
	private final Map<String, Credentials> auths = new HashMap<String, Credentials>();

	public Student(String firstName, String lastName)
	{
		this.firstName = firstName;
		this.lastName = lastName;
		client = new WebClient();
	}

	public String getFirstName()
	{
		return firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void giveCredentials(String provider, Credentials credentials)
	{
		auths.put(provider, credentials);
	}

	public Credentials getCredentials(String provider)
	{
		return auths.get(provider);
	}

	public boolean hasCredentials(String provider)
	{
		return auths.containsKey(provider);
	}

	public WebClient getClient()
	{
		return client;
	}
}
