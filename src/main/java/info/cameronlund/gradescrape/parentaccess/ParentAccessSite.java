package info.cameronlund.gradescrape.parentaccess;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import info.cameronlund.gradescrape.api.AuthenticatedSite;
import info.cameronlund.gradescrape.api.AuthenticationFailedException;
import info.cameronlund.gradescrape.api.ScrapableSite;
import info.cameronlund.gradescrape.user.Credentials;
import info.cameronlund.gradescrape.user.Student;

import java.io.IOException;

public class ParentAccessSite extends AuthenticatedSite {
	public ParentAccessSite(Student student, Credentials credentials) throws AuthenticationFailedException
	{
		super(student, credentials);
		if (!authenticate(credentials))
		{
			throw (new AuthenticationFailedException(this.getClass()));
		}
	}

	@Override
	public boolean authenticate(Credentials credentials)
	{
		try
		{
			final HtmlPage page = getClient().getPage(ParentAccessPage.BASE);

			// If we're at the home page
			if (page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.LOGIN_SCREEN) ||
					page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.LOGIN_SCREEN_EXTENDED))
			{
				// Manipulate the form and get the results
				final HtmlTextInput userName = (HtmlTextInput) page.getElementById("UserName");
				final HtmlPasswordInput password = (HtmlPasswordInput) page.getElementById("Password");
				userName.setValueAttribute(getUsername());
				password.setValueAttribute(getPassword());
				final HtmlButton button = page.getFirstByXPath("//button[@type='submit']");
				final HtmlPage result = button.click();

				// If we're at the planner, we logged in right
				return isPage(result, ParentAccessPage.PLANNER);
			}

			// If we're at the planner, we logged in right
			return isPage(page, ParentAccessPage.PLANNER);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
