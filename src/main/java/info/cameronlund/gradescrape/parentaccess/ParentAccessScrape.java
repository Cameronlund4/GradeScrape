package info.cameronlund.gradescrape.parentaccess;

import com.gargoylesoftware.htmlunit.html.*;
import info.cameronlund.gradescrape.api.v1.enums.MarkingPeriod;
import info.cameronlund.gradescrape.api.v1.exceptions.AuthenticationFailedException;
import info.cameronlund.gradescrape.api.v1.scrapables.AuthenticatedScrapable;
import info.cameronlund.gradescrape.api.v1.user.Student;
import info.cameronlund.gradescrape.parentaccess.constants.ParentAccessPage;
import info.cameronlund.gradescrape.parentaccess.constants.ParentAccessXpath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParentAccessScrape extends AuthenticatedScrapable
{
	public ParentAccessScrape(Student student) throws AuthenticationFailedException
	{
		super(student, "parent_access", 1000 * 15); //*60*10); // 10 minute idle
		shutup();
	}

	/**
	 *
	 * Scraping methods
	 *
	 **/

	// Get the classes overall grade
	public Map<String, Grade> getClassGrades(MarkingPeriod period)
	{
		startTimeReadout(); // TODO Remove
		Map<String, Grade> grades = new HashMap<String, Grade>();

		printTime("step 1"); // TODO Remove
		// Get the page for grades
		HtmlPage page;
		try
		{
			page = getPage(ParentAccessPage.GRADES);
			printTime("step 2"); // TODO Remove
			if (!checkAuth(page))
			{
				printTime("step 3"); // TODO Remove
				page = auth(page);
			}
			printTime("step 4"); // TODO Remove
			if (!checkAuth(page))
			{
				printTime("step 5"); // TODO Remove
				throw new AuthenticationFailedException(this.getClass());
			}
			resetIdleCountdown();
			printTime("step 6"); // TODO Remove
			if (!isPage(page, ParentAccessPage.GRADES))
			{
				page = getPage(ParentAccessPage.GRADES);
			}
			if (!isPage(page, ParentAccessPage.GRADES))
			{
				System.out.println("Got wrong page: "+page.getUrl());
				return grades;
			}
			printTime("step 7"); // TODO Remove
		} catch (IOException e)
		{
			e.printStackTrace();
			return grades;
		}

		printTime("step 8"); // TODO Remove
		HtmlTableBody markingPeriod = null;
		if (period == MarkingPeriod.FIRST)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP1);
		if (period == MarkingPeriod.SECOND)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP2);
		if (period == MarkingPeriod.THIRD)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP3);
		if (period == MarkingPeriod.FOURTH)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP4);
		printTime("step 9"); // TODO Remove

		for (final DomElement row : markingPeriod.getChildElements())
		{ // Loop
			String klassen = "";
			Grade grade = null;
			for (final DomElement element : row.getChildElements())
				inspectElement:{
				if (!(element instanceof HtmlTableDataCell)) continue;
				if (element.getAttribute("class").equalsIgnoreCase("fixed-column important"))
				{
					DomElement gradeElement = element.getFirstElementChild();

					String gradeRaw = gradeElement.getTextContent().replaceAll("[\\s]", "");
					if (gradeRaw.length() < 1) break inspectElement; // If no grade is set just keep'er going
					String gradeNumber = gradeRaw.replaceAll("[^0-9?!\\.]", "");
					String gradeLetter = gradeRaw.replace(gradeNumber, "");
					grade = new Grade(gradeLetter, Double.parseDouble(gradeNumber));
				}
				if (element.getAttribute("class") == null || element.getAttribute("class").length() < 1)
				{
					DomElement classElement = element.getFirstElementChild().getFirstElementChild();
					if (classElement == null) continue;
					if (!classElement.getTextContent().toLowerCase().contains("see all details"))
						klassen = classElement.getTextContent();
				}
			}
			if (klassen.length() > 0)
				grades.put(klassen, grade);
		}
		printTime("step 10"); // TODO Remove
		return grades;
	}

	/**
	 *
	 * Authentication methods
	 *
	 **/

	@Override
	public HtmlPage auth(HtmlPage page)
	{
		resetIdleCountdown();

		try
		{
			// If we're at the home page
			if (!checkAuth(page))
			{
				final HtmlTextInput userName = (HtmlTextInput) page.getElementById("UserName");
				final HtmlPasswordInput password = (HtmlPasswordInput) page.getElementById("Password");
				userName.setValueAttribute(getUsername());
				password.setValueAttribute(getPassword());
				final HtmlButton button = page.getFirstByXPath(ParentAccessXpath.LOGIN_BUTTON);
				final HtmlPage result = button.click();
				return setAuthState(result, checkAuth(result));
			}

			return setAuthState(page, true);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public HtmlPage auth()
	{
		try
		{
			return auth(getPage(ParentAccessPage.BASE, getAuthExpireMillis()));
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public HtmlPage unauth()
	{
		try
		{
			return unauth(getPage(ParentAccessPage.LOGOUT, getAuthExpireMillis()));
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public HtmlPage unauth(HtmlPage page)
	{
		if (!checkAuth(page))
			return null;
		if (page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.LOGOUT))
			return setAuthState(page, checkAuth(page));
		try
		{
			// TODO Press the 'logout' button
			throw new IOException(); // To stop errors on the catch
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public boolean checkAuth(HtmlPage page)
	{
		return !(isPage(page, ParentAccessPage.LOGIN_SCREEN) ||
				page.getUrl().toString().contains("?returnUrl="))
				&& page.getFirstByXPath(ParentAccessXpath.LOGOUT_BUTTON) != null;
	}

	@Override
	public boolean hasAuthExpired()
	{
		return super.hasAuthExpired();
	}

	// TODO Remove the following debug tools
	long lastReadout;

	public void startTimeReadout()
	{
		lastReadout = System.currentTimeMillis();
	}

	public void printTime(String subject)
	{
		System.out.println("Task "+subject+" took "+(System.currentTimeMillis()-lastReadout) / 1000D+"s");
		lastReadout = System.currentTimeMillis();
	}
}
