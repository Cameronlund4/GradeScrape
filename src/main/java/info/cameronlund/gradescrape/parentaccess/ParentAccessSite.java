package info.cameronlund.gradescrape.parentaccess;

import com.gargoylesoftware.htmlunit.html.*;
import info.cameronlund.gradescrape.api.AuthenticatedSite;
import info.cameronlund.gradescrape.api.AuthenticationFailedException;
import info.cameronlund.gradescrape.api.MarkingPeriod;
import info.cameronlund.gradescrape.user.Student;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParentAccessSite extends AuthenticatedSite {
	private boolean isAuthenticated = false;

	public ParentAccessSite(Student student) throws AuthenticationFailedException
	{
		super(student, "parent_access");
		shutup();
	}

	public Map<String, Grade> getClassGrades(MarkingPeriod period)
	{
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
		System.out.println("Grades success");

		HtmlTableBody markingPeriod = null;
		if (period == MarkingPeriod.FIRST)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP1);
		if (period == MarkingPeriod.SECOND)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP2);
		if (period == MarkingPeriod.THIRD)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP3);
		if (period == MarkingPeriod.FOURTH)
			markingPeriod = page.getFirstByXPath(ParentAccessXpath.GRADE_MP4);
		System.out.println("    Grades:");

		for (final DomElement row : markingPeriod.getChildElements())
		{ // Loop
			String klassen = "";
			Grade grade = null;
			for (final DomElement element : row.getChildElements())
			{
				if (!(element instanceof HtmlTableDataCell)) continue;
				if (element.getAttribute("class").equalsIgnoreCase("fixed-column important"))
				{
					DomElement gradeElement = element.getFirstElementChild();

					String gradeRaw = gradeElement.getTextContent().replaceAll("[\\s]", "");
					if (gradeRaw.length() < 1) continue;
					String gradeNumber = gradeRaw.replaceAll("[^0-9?!\\.]", "");
					String gradeLetter = gradeRaw.replace(gradeNumber, "");
					grade = new Grade(gradeLetter, Double.parseDouble(gradeNumber));
				}
				if (element.getAttribute("class") == null || element.getAttribute("class").length() < 1)
				{
					DomElement classElement = element.getFirstElementChild().getFirstElementChild();
					if (classElement == null) continue;
					System.out.println(classElement.getTextContent());
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
		try
		{
			final HtmlPage page = getClient().getPage(ParentAccessPage.BASE);

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
				isAuthenticated = isPage(result, ParentAccessPage.PLANNER);
				return isAuth();
			}

			// If we're at the planner, we logged in right
			isAuthenticated = isPage(page, ParentAccessPage.PLANNER);
			return isAuth();
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
			final HtmlPage page = getClient().getPage(ParentAccessPage.LOGOUT);

			// If we're at the logout page
			isAuthenticated = !isPage(page, ParentAccessPage.LOGOUT);
			return isAuth();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		isAuthenticated = true;
		return isAuth();
	}

	public boolean isAuth()
	{
		return isAuthenticated;
	}

	public String formatClass(String classRaw)
	{
		if (classRaw.contains(" -"))
			return classRaw.substring(0, classRaw.indexOf(" -"));
		return classRaw;
	}
}
