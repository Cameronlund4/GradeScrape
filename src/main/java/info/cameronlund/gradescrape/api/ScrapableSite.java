package info.cameronlund.gradescrape.api;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import info.cameronlund.gradescrape.user.Student;

import java.io.IOException;

public class ScrapableSite {
	private final WebClient client;
	private final Student student;

	public ScrapableSite(Student student)
	{
		client = new WebClient();
		this.student = student;
	}

	// For expectedResult, you can replace a section of a url with a * if there's a variable
	// part. For example, If you want to check www.google.com/user/StaticShadow/inventory, you can
	// use www.google.com/user/*/inventory to check across all users
	public boolean isPage(HtmlPage page, String expectedResult)
	{
		String[] original = page.getUrl().toString().replace("http://", "").replace("https://", "").split("/");
		String[] expected = expectedResult.replace("http://", "").replace("https://", "").split("/");
		for (int i = 0; i < original.length; i++)
		{
			if (expected[i].equalsIgnoreCase("*")) continue;
			if (!original[i].equalsIgnoreCase(expected[i]))
				return false;
		}
		return true;
	}

	public HtmlPage goToPage(WebClient client, String page) throws IOException
	{
		return client.getPage(page);
	}

	public Student getStudent()
	{
		return student;
	}

	public WebClient getClient()
	{
		return client;
	}
}
