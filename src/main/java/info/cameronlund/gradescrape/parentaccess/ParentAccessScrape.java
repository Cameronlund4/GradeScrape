package info.cameronlund.gradescrape.parentaccess;

import com.gargoylesoftware.htmlunit.html.*;
import info.cameronlund.gradescrape.api.AuthenticatedScrapable;
import info.cameronlund.gradescrape.api.AuthenticationFailedException;
import info.cameronlund.gradescrape.api.MarkingPeriod;
import info.cameronlund.gradescrape.user.Student;

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

	public Map<String, Grade> getClassGrades(MarkingPeriod period)
	{
		checkAuth();
		resetIdleCountdown();
		Map<String, Grade> grades = new HashMap<String, Grade>();

		// Get the page for grades
		final HtmlPage page;
		try
		{
			page = getPage(ParentAccessPage.GRADES);
			if (!isPage(page, ParentAccessPage.GRADES)) return grades; // Return if wrong
		} catch (IOException e)
		{
			e.printStackTrace();
			return grades;
		}

		HtmlTableBody markingPeriod = null;
		if (period == MarkingPeriod.FIRST)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP1);
		if (period == MarkingPeriod.SECOND)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP2);
		if (period == MarkingPeriod.THIRD)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP3);
		if (period == MarkingPeriod.FOURTH)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP4);
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
						klassen = formatClass(classElement.getTextContent());
				}
			}
			if (klassen.length() > 0)
				grades.put(klassen, grade);
		}
		return grades;
	}

	/**
	 *
	 * Authentication methods
	 *
	 **/

	@Override
	public boolean auth()
	{
		resetIdleCountdown();

		try
		{
			final HtmlPage page = getPage(ParentAccessPage.BASE);

			// If we're at the home page
			if (!checkAuth(page))
			{
				// Manipulate the form and get the results
				final HtmlTextInput userName = (HtmlTextInput) page.getElementById("UserName");
				final HtmlPasswordInput password = (HtmlPasswordInput) page.getElementById("Password");
				userName.setValueAttribute(getUsername());
				password.setValueAttribute(getPassword());
				final HtmlButton button = page.getFirstByXPath(ParentAccessXpath.LOGIN_BUTTON);
				final HtmlPage result = button.click();

				// If we're at the planner, we logged in right
				return setAuthState(isPage(result, ParentAccessPage.PLANNER));
			}

			// If we're at the planner, we logged in right
			return setAuthState(isPage(page, ParentAccessPage.PLANNER));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean unauth()
	{
		try
		{
			final HtmlPage page = getPage(ParentAccessPage.LOGOUT);
			return !setAuthState(checkAuth(page));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return setAuthState(false);
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

	/**
	 *
	 * Formatting methods
	 *
	 */

	private String formatClass(String classRaw)
	{
		if (classRaw.contains(" -"))
			return classRaw.substring(0, classRaw.indexOf(" -"));
		return classRaw;
	}


}
