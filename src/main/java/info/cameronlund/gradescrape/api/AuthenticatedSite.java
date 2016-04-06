package info.cameronlund.gradescrape.api;

import info.cameronlund.gradescrape.user.Credentials;
import info.cameronlund.gradescrape.user.Student;

public abstract class AuthenticatedSite extends ScrapableSite {
	private final String credentialKey;
	private final Credentials credentials;

	public AuthenticatedSite(Student student, String credentialKey)
	{
		super(student);
		this.credentialKey = credentialKey;
		this.credentials = student.getCredentials(credentialKey);
	}

	public String getUsername()
	{
		return credentials.getUsername();
	}

	public String getPassword()
	{
		return credentials.getPassword();
	}

	public Credentials getCredentials()
	{
		return credentials;
	}

	public String getCredentialKey()
	{
		return credentialKey;
	}

	// Should return whether or not authentication was successful
	public abstract boolean auth();

	// Should return wether or not unauthentication was successful
	public abstract boolean unauth();

	// Should return whether or not the player is authenticated
	public abstract boolean isAuth();
}
