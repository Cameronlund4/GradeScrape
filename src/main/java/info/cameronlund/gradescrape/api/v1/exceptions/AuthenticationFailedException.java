package info.cameronlund.gradescrape.api.v1.exceptions;

public class AuthenticationFailedException extends RuntimeException {
	public AuthenticationFailedException(Class c)
	{
		super(c.getName()+" failed to authenticate!");
	}
}
