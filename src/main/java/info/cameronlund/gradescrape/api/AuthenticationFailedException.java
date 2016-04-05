package info.cameronlund.gradescrape.api;

public class AuthenticationFailedException extends RuntimeException {
	public AuthenticationFailedException(Class c)
	{
		super(c.getName()+" failed to authenticate!");
	}
}
