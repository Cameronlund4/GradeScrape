package info.cameronlund.gradescrape.api;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import info.cameronlund.gradescrape.user.Student;
import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;

public class ScrapableSite {
	private final Student student;

	public ScrapableSite(Student student)
	{
		this.student = student;
	}

	// For expectedResult, you can replace a section of a url with a * if there's a variable
	// part. For example, If you want to check www.google.com/user/{UserHere}/inventory, you can
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

	public void printPage(HtmlPage page, boolean removeNewline)
	{
		System.out.println(removeNewline ? page.getWebResponse().getContentAsString().replace("\n", "").replace("\r", "")
				: page.getWebResponse().getContentAsString());
	}

	public HtmlPage goToPage(String page) throws IOException
	{
		return getClient().getPage(page);
	}

	public Student getStudent()
	{
		return student;
	}

	public WebClient getClient()
	{
		return getStudent().getClient();
	}

	public void shutup()
	{
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

		getClient().setIncorrectnessListener(new IncorrectnessListener() {
			public void notify(String arg0, Object arg1)
			{
			}
		});
		getClient().setCssErrorHandler(new ErrorHandler() {
			public void warning(CSSParseException exception) throws CSSException
			{
			}

			public void fatalError(CSSParseException exception) throws CSSException
			{
			}

			public void error(CSSParseException exception) throws CSSException
			{

			}
		});
		getClient().setJavaScriptErrorListener(new JavaScriptErrorListener() {
			public void scriptException(InteractivePage interactivePage, ScriptException e)
			{
			}

			public void timeoutError(InteractivePage interactivePage, long l, long l1)
			{
			}

			public void malformedScriptURL(InteractivePage interactivePage, String s, MalformedURLException e)
			{
			}

			public void loadScriptError(InteractivePage arg0, java.net.URL arg1, Exception arg2)
			{
			}
		});
		getClient().setHTMLParserListener(new HTMLParserListener() {
			public void error(String s, java.net.URL url, String s1, int i, int i1, String s2)
			{
			}

			public void warning(String s, java.net.URL url, String s1, int i, int i1, String s2)
			{
			}
		});
	}
}
