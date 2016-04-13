package info.cameronlund.gradescrape.api.v1.scrapables;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import info.cameronlund.gradescrape.api.v1.user.Student;
import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.logging.Level;

public class Scrapable {
	private final Student student;
	private HashMap<String, HtmlPage> cache = new HashMap<String, HtmlPage>();
	private HashMap<String, Long> cachedAge = new HashMap<String, Long>();

	public Scrapable(Student student)
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
			if (i > expected.length-1) return false; // This causes false if original has more sections than expected
			if (expected[i].equalsIgnoreCase("*")) continue;
			if (!isUrlPartSame(original[i], expected[i]))
			{
				return false;
			}
		}
		return true;
	}

	// Override and super call this if you want to add custom conditions before we result to equals checks
	public boolean isUrlPartSame(String result, String expected)
	{
		return expected.equalsIgnoreCase(result);
	}

	public void printPage(HtmlPage page, boolean removeNewline)
	{
		System.out.println(removeNewline ? page.getWebResponse().getContentAsString().replace("\n", "").replace("\r", "")
				: page.getWebResponse().getContentAsString());
	}

	// This is the best method to use if you don't need extremely up to date info
	public HtmlPage getPage(String page) throws IOException
	{
		if (cache.containsKey(page))
			return cache.get(page);
		else
			return getNewPage(page);
	}

	// This is the best method to use if you are using data that needs to be up to date
	public HtmlPage getPage(String page, long maxAge) throws IOException
	{
		long pageAge = getAge(page);
		if (pageAge > -1 || pageAge <= maxAge)
		{ // If we already have this page and it's young enough
			return getPage(page);
		}
		return getNewPage(page);
	}

	// Should really use getPage(String, long) unless you absolutely need a new page
	public HtmlPage getNewPage(String page) throws IOException
	{
		long start = System.currentTimeMillis();
		System.out.println("-- Getting new page: "+page);
		HtmlPage htmlPage = getClient().getPage(page);
		System.out.println("-- Took: "+(System.currentTimeMillis()-start) / 1000D+"s");
		cache.put(htmlPage.getUrl().toString(), htmlPage);
		cachedAge.put(htmlPage.getUrl().toString(), System.currentTimeMillis());
		return htmlPage;
	}

	public long getAge(String page)
	{
		if (cachedAge.containsKey(page))
		{
			return cachedAge.get(page);
		}
		else return -1;
	}

	public void clearCache()
	{
		cache = new HashMap<String, HtmlPage>();
	}

	public Student getStudent()
	{
		return student;
	}

	public WebClient getClient()
	{
		return getStudent().getClient();
	}

	// Gets rid of highly annoying (and typically useless) console spam from HtmlUnit
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
