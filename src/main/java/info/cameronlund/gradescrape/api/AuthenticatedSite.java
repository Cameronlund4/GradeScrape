package info.cameronlund.gradescrape.api;

import info.cameronlund.gradescrape.user.Credentials;
import info.cameronlund.gradescrape.user.Student;

public abstract class AuthenticatedSite extends ScrapableSite {
	private final String credentialKey;
	private final Credentials credentials;
	private long lastInteraction;
	private long authExpireTimeMillis;
	private boolean isAuthenticated = false;

	public AuthenticatedSite(Student student, String credentialKey, long authExpireTimeMillis)
	{
		super(student);
		this.credentialKey = credentialKey;
		this.credentials = student.getCredentials(credentialKey);
		this.authExpireTimeMillis = authExpireTimeMillis;
		lastInteraction = System.currentTimeMillis()-authExpireTimeMillis-1; // Make sure our authentication requires recheck
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
		return System.currentTimeMillis()-lastInteraction > authExpireTimeMillis;
	}

	// If our boolean auth is expired, recheck our status. If not, use our current boolean
	public boolean checkAuth()
	{
		if (!isAuth()) return false; // If we locally think we're unauth, we probably are
		// If we need to recheck our auth status
		if (hasAuthExpired())
			return updateAuth();
			// If we don't need to recheck our auth
		else return isAuth();
	}

	private boolean isAuth()
	{
		return isAuthenticated;
	}

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

	// Should return whether or not authentication was successful
	// SHOULD call resetIdleCountdown()
	public abstract boolean auth();

	// Should return wether or not unauthentication was successful
	// SHOULD NOT call resetIdleCountdown()
	public abstract boolean unauth();

	// Should handle making sure auth is up-to-date.
	// SHOULD call resetIdleCountdown()
	public abstract boolean updateAuth();
}
