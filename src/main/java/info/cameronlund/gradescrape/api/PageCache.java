package info.cameronlund.gradescrape.api;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.HashMap;

public class PageCache
{
	private HashMap<String, HtmlPage> cache = new HashMap<String, HtmlPage>();

	private Scrapable getSite()
	{
		return site;
	}

	private Scrapable site;

	public PageCache(Scrapable site)
	{
		this.site = site;
	}

	public HtmlPage getPage(String page) throws IOException
	{
		if (!cache.containsKey(page))
			return cache.get(page);
		else
		{
			HtmlPage htmlPage = getSite().getClient().getPage(page);
			cache.put(page, htmlPage);
			return htmlPage;
		}
	}

	public HtmlPage getNewPage(String page) throws IOException
	{
		HtmlPage htmlPage = getSite().getClient().getPage(page);
		if (cache.containsKey(page)) cache.put(page, htmlPage);
		return htmlPage;
	}

	public void clearCache()
	{
		cache = new HashMap<String, HtmlPage>();
	}
}
