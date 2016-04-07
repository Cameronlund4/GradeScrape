package info.cameronlund.gradescrape.parentaccess;

import com.gargoylesoftware.htmlunit.html.*;
import info.cameronlund.gradescrape.api.AuthenticatedSite;
import info.cameronlund.gradescrape.api.AuthenticationFailedException;
import info.cameronlund.gradescrape.api.MarkingPeriod;
import info.cameronlund.gradescrape.user.Student;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TODO Rework so we don't need to re-get the same html for individual class grades
// TODO Maybe a ReadData object that caches pages to urls and get reused if we don't need to read
public class ParentAccessSite extends AuthenticatedSite {
	public ParentAccessSite(Student student) throws AuthenticationFailedException
	{
		super(student, "parent_access", 1000 * 15); //*60*10); // 10 minute idle
		shutup();
	}

	public Map<String, Grade> getClassGrades(MarkingPeriod period)
	{
		checkAuth();
		resetIdleCountdown();

		Map<String, Grade> grades = new HashMap<String, Grade>();
		final HtmlPage page;
		try
		{
			page = goToPage(ParentAccessPage.GRADES);
		} catch (IOException e)
		{
			e.printStackTrace();
			return grades;
		}

		// If we're at the grades
		if (!isPage(page, ParentAccessPage.GRADES)) return grades;

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
					if (gradeRaw.length() < 1) break inspectElement; // If no grade is set just keep'er goin
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

	public boolean auth()
	{
		resetIdleCountdown();

		try
		{
			final HtmlPage page = goToPage(ParentAccessPage.BASE);

			// If we're at the home page
			if (isPage(page, ParentAccessPage.LOGIN_SCREEN) ||
					isPage(page, ParentAccessPage.LOGIN_SCREEN_EXTENDED))
			{
				// Manipulate the form and get the results
				final HtmlTextInput userName = (HtmlTextInput) page.getElementById("UserName");
				final HtmlPasswordInput password = (HtmlPasswordInput) page.getElementById("Password");
				userName.setValueAttribute(getUsername());
				password.setValueAttribute(getPassword());
				final HtmlButton button = page.getFirstByXPath("//*[@id=\"login_form\"]/fieldset/div[3]/div/button");
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

	public boolean unauth()
	{
		try
		{
			final HtmlPage page = goToPage(ParentAccessPage.LOGOUT);

			// If we're at the logout page
			return setAuthState(!isPage(page, ParentAccessPage.LOGOUT));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return setAuthState(true);
	}

	public boolean updateAuth()
	{
		resetIdleCountdown();
		try
		{
			final HtmlPage page = goToPage(ParentAccessPage.BASE);
			// Basically, if we're at a login screen
			if (!(!isPage(page, ParentAccessPage.LOGIN_SCREEN) &&
					!isPage(page, ParentAccessPage.LOGIN_SCREEN_EXTENDED)))
			{
				return auth();
			}
			else return setAuthState(true);
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public String formatClass(String classRaw)
	{
		if (classRaw.contains(" -"))
			return classRaw.substring(0, classRaw.indexOf(" -"));
		return classRaw;
	}

	@Override
	public boolean hasAuthExpired()
	{
		return super.hasAuthExpired();
	}
}
