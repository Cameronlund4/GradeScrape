import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import info.cameronlund.gradescrape.parentaccess.ParentAccessPage;
import info.cameronlund.gradescrape.parentaccess.ParentAccessSite;
import info.cameronlund.gradescrape.user.Credentials;

// The subclass GradeScrapeProgressBook is obsolete... Check classes under api and parentaccess packages
import java.io.IOException;

public class GradeScrape {
	private static GradeScrapeProgressbook instance;

	public static void main(String args[])
	{
		Student cameron = new Student("Cameron", "Lund", "<redacted>", "<redacted>");
		//instance = new GradeScrapeProgressbook(cameron);
		new ParentAccessSite(new info.cameronlund.gradescrape.user.Student("Cameron","Lund"),
				new Credentials("footballfan12","Thesock#12"));
	}

	public static GradeScrapeProgressbook getProgressbookScrape()
	{
		return instance;
	}

	private static class GradeScrapeProgressbook {
		final WebClient webClient;
		final Student student;

		GradeScrapeProgressbook(Student student)
		{
			webClient = new WebClient();
			this.student = student;
			outputMarkingPeriodGrades();
		}

		private HtmlPage logIntoProgressbook(Student student) throws IOException
		{
			final HtmlPage page = webClient.getPage(ParentAccessPage.BASE);

			// If we're at the home page
			if (page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.LOGIN_SCREEN) ||
					page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.LOGIN_SCREEN_EXTENDED))
			{
				// Get login text fields
				final HtmlTextInput userName = (HtmlTextInput) page.getElementById("UserName");
				final HtmlPasswordInput password = (HtmlPasswordInput) page.getElementById("Password");

				// Fill text files with user/password
				userName.setValueAttribute(student.getUsername());
				password.setValueAttribute(student.getPassword());

				// Submit and check result
				final HtmlButton button = page.getFirstByXPath("//button[@type='submit']");
				final HtmlPage result = button.click();
				if (result.getUrl().toString().equalsIgnoreCase(ParentAccessPage.PLANNER))
					return result;
			}

			// If we're at the planner
			if (page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.PLANNER))
				return page;

			return null;
		}


		private HtmlPage goToPage(WebClient client, String page, String expectedResult) throws IOException
		{
			final HtmlPage htmlPage = goToPage(client, page);
			if (htmlPage.getUrl().toString().equalsIgnoreCase(expectedResult)) {
				return htmlPage;
			}
			return null;
		}

		private HtmlPage goToPage(WebClient client, String page) throws IOException
		{
			return client.getPage(page);
		}

		public void outputMarkingPeriodGrades()
		{
			webClient.getCookieManager().setCookiesEnabled(true);
			String nextPage = ParentAccessPage.BASE;
			while (true)
			{
				final HtmlPage page;
				try
				{
					page = webClient.getPage(nextPage);

					// If we're at the home page
					if (page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.LOGIN_SCREEN) ||
							page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.LOGIN_SCREEN_EXTENDED))
					{
						final HtmlTextInput userName = (HtmlTextInput) page.getElementById("UserName");
						final HtmlPasswordInput password = (HtmlPasswordInput) page.getElementById("Password");

						userName.setValueAttribute(student.getUsername());
						password.setValueAttribute(student.getPassword());

						final HtmlButton button = page.getFirstByXPath("//button[@type='submit']");
						final HtmlPage result = button.click();
						if (result.getUrl().toString().equalsIgnoreCase(ParentAccessPage.PLANNER))
						{
							System.out.println("Login success");
							nextPage = ParentAccessPage.GRADES;
							continue;
						}
						else
						{
							System.out.println("Login failure, returned page: "+page.getUrl());
							return;
						}
					}

					// If we're at the planner
					if (page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.PLANNER))
					{
						System.out.println("Planner success");
						return;
					}

					// If we're at the grades
					if (page.getUrl().toString().equalsIgnoreCase(ParentAccessPage.GRADES))
					{
						System.out.println("Grades success");

						// For loops to the extreme, sorry ;)
						for (Object capture : page.getByXPath("//table[@class='table table-striped table-condensed']"))
						{ // Loops tables
							HtmlTable gradeTable = (HtmlTable) capture;
							HtmlTableBody gradeBody = null;
							for (DomElement tbody : gradeTable.getChildElements()) {
								if (tbody instanceof HtmlTableBody) {
									gradeBody = (HtmlTableBody) tbody;
									break;
								}
							}
							System.out.println("    Grades:");

							for (final DomElement row : gradeBody.getChildElements())
							{ // Loop
								for (final DomElement element : row.getChildElements())
								{
									if (!(element instanceof HtmlTableDataCell)) continue;
									if (!element.getAttribute("class").equalsIgnoreCase("fixed-column important"))
										continue;

									DomElement gradeElement = element.getFirstElementChild();

									String gradeRaw = gradeElement.getTextContent().replaceAll("[\\s]", "");
									String gradeNumber = gradeRaw.replaceAll("[^0-9?!\\.]", "");
									String gradeLetter = gradeRaw.replace(gradeNumber, "");
									System.out.println("        "+"class"+": "+gradeNumber+" ("+gradeLetter+")");
								}
							/*
							String gradeRaw = ((HtmlElement)((HtmlElement)
									row.getFirstByXPath("//td[@class='fixed-column important']"))
									.getFirstByXPath("//span[@class='expandable-row']")).getTextContent().replaceAll("[\\s]","");
							String gradeNumber = gradeRaw.replaceAll("[^0-9?!\\.]","");
							String gradeLetter = gradeRaw.replace(gradeNumber,"");
							System.out.println("        "+"class"+": "+gradeNumber+" ("+gradeLetter+")");
							*/
							}
						}
						return;
					}

					System.out.println("Hmmm, not recognized: "+page.getUrl());
					return;
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
