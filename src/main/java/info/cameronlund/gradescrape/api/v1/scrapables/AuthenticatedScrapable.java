package info.cameronlund.gradescrape.api.v1.scrapables;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import info.cameronlund.gradescrape.api.v1.user.Credentials;
import info.cameronlund.gradescrape.api.v1.user.Student;

public abstract class AuthenticatedScrapable extends Scrapable
{
	private final String credentialKey;
	private final Credentials credentials;
	private long lastInteraction;
	private long authExpireTimeMillis;
	private boolean isAuthenticated = false;

	public AuthenticatedScrapable(Student student, String credentialKey, long authExpireTimeMillis)
	{
		super(student);
		this.credentialKey = credentialKey;
		this.credentials = student.getCredentials(credentialKey);
		this.authExpireTimeMillis = authExpireTimeMillis;
		lastInteraction = System.currentTimeMillis() - authExpireTimeMillis - 1; // Make sure our authentication requires recheck
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

	public boolean hasAuthExpired()
	{
		return System.currentTimeMillis() - lastInteraction > authExpireTimeMillis;
	}

	private boolean isAuth()
	{
		return isAuthenticated;
	}

	// Useful to set the auth state in a return call
	public boolean setAuthState(boolean authenticated)
	{
		isAuthenticated = authenticated;
		return authenticated;
	}

	// Use this any time you know we're safe for another authExpireTimeMillis millis. For example, right when we
	// make a successful interaction with a site
	public void resetIdleCountdown()
	{
		lastInteraction = System.currentTimeMillis();
	}

	// Will authenticate if needed, otherwise return current state
	// SHOULD NOT call resetIdleCountdown()
	public boolean checkAuth()
	{
		// If we need to reload our auth status
		if (hasAuthExpired() || !isAuth())
			return auth();

		return isAuth();
	}

	/// Should check the page's elements to tell if we're logged in if possible
	// SHOULD call resetIdleCountdown()
	// SHOULD be used on newish pages
	public boolean checkAuth(final HtmlPage page)
	{
		return isAuth();
	}

	// Should return whether or not authentication was successful
	// SHOULD call resetIdleCountdown()
	public abstract boolean auth();

	// Should return wether or not unauthentication was successful
	// SHOULD NOT call resetIdleCountdown()
	public abstract boolean unauth();
}
