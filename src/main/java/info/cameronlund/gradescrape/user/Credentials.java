package info.cameronlund.gradescrape.user;

public class Credentials {
	final private String username;
	final private String password;

	public Credentials(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}
}
