package info.cameronlund.bettergrades;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import info.cameronlund.gradescrape.api.v1.enums.MarkingPeriod;
import info.cameronlund.gradescrape.api.v1.server.ScrapeProvider;
import info.cameronlund.gradescrape.api.v1.user.Credentials;
import info.cameronlund.gradescrape.api.v1.user.Student;
import info.cameronlund.gradescrape.parentaccess.Grade;
import info.cameronlund.gradescrape.parentaccess.ParentAccessScrape;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class HttpPortal {
	private String ip = "192.168.1.13";
	private int port = 8080;
	private HttpServer server;

	public void start()
	{
		System.out.println("Attempting to create address /" + ip + ":" + port);
		InetSocketAddress address;
		try
		{
			address = new InetSocketAddress(InetAddress.getByName(ip), port);
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
			return;
		}
		System.out.println(" - Success!");
		System.out.println("Attempting to bind server on " + address.getAddress() + ":" + address.getPort());
		try
		{
			server = HttpServer.create(address, 0);
		} catch (IOException e)
		{
			if (e instanceof BindException)
			{
				System.out.println(" - Failed! " + e.getMessage());
			} else
				e.printStackTrace();
			return;
		}
		System.out.println(" - Success!");
		System.out.println("Creating context: "+server.createContext("/", new WeirdCallHanlder()).getPath());
		System.out.println("Creating context: " + server.createContext("/api", new ParentAccessHandler()).getPath());
		server.setExecutor(null);
		server.start();
		System.out.println(" - Success!");
	}

	public class WeirdCallHanlder implements HttpHandler
	{
		public void handle(HttpExchange exchange) throws IOException
		{
			try
			{
				System.out.println("!-POSSIBLE SECURITY THREAT -!");
				System.out.println("    Got weird call to "+exchange.getHttpContext().getPath());
				System.out.println("    Context "+exchange.getHttpContext().getPath()+" ignoring "+exchange.getRemoteAddress());
				System.out.println("    Request attempt: "+exchange.getRequestURI().getQuery());
				System.out.println("    Request path: "+exchange.getRequestURI());
				System.out.println("     - Success!");
				System.out.println("!-POSSIBLE SECURITY THREAT -!");
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public class ParentAccessHandler extends ScrapeProvider {
		public boolean handleRequest(HttpExchange exchange, JsonObject response)
		{
			long start = System.currentTimeMillis();
			try
			{
				// Console output
				System.out.println("Got call to " + exchange.getHttpContext().getPath());
				System.out.println("Context " + exchange.getHttpContext().getPath() + " responding to " + exchange.getRemoteAddress());
				boolean success = false;
				JsonObject gradeJson = new JsonObject();

				processRequest:
				{
					// Create JSON
					if (exchange.getRequestURI().getQuery() == null)
					{
						System.out.println(" - Got null query!");
						break processRequest;
					}
					Map<String, String> args = queryToMap(exchange.getRequestURI().getQuery());
					String[] studentName = args.get("student").split("\\s");
					String username = args.get("username");
					System.out.println("Username: "+args.get("username"));
					String password = args.get("password");
					System.out.println("Password: "+args.get("password"));
					Student student = new Student(studentName[0], studentName[1]);
					CookieManager cookieManager = student.getCookieManager();
					cookieManager.setCookiesEnabled(true);
					cookieManager.addCookie(new Cookie("parentaccess.chclc.org", "SoftwareAnswers.Portal.District", "chps", "/", null, false, true));
					student.setCookieManager(cookieManager);
					student.giveCredentials("parent_access", new Credentials(username, password));
					try
					{
						getData:
						{
							System.out.println(" - Getting their grades");
							ParentAccessScrape site = new ParentAccessScrape(student);

							MarkingPeriod[] markingPeriods = new MarkingPeriod[]{
									MarkingPeriod.FIRST, MarkingPeriod.SECOND, MarkingPeriod.THIRD, MarkingPeriod.FOURTH
							};
							// In the future we'll use grade scales (And progressbook provides them :D)
							for (MarkingPeriod period : markingPeriods)
							{
								JsonObject grades = new JsonObject();
								System.out.println("    - " + period.getLowName());
								for (Map.Entry<String, Grade> grade : site.getClassGrades(period).entrySet())
								{
									if (grade.getValue() != null)
										grades.addProperty(grade.getKey(), grade.getValue().getNumericalGrade() + " ("
												+ grade.getValue().getLetterGrade() + ")");
									else
										grades.addProperty(grade.getKey(), "NA");
									System.out.println("       - " + grade.getKey() + ": " +
											(grade.getValue() != null ? grade.getValue().getNumericalGrade() : "N/A"));
								}
								gradeJson.add(period.getLowName(), grades);
							}
							success = true;
						}
					} catch (Exception e)
					{
						success = false;
						e.printStackTrace();
					}
				}

				if (success)
					response.add("grades", gradeJson);

				return success;
			} catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}

	public Map<String, String> queryToMap(String query)
	{
		Map<String, String> result = new HashMap<String, String>();
		for (String param : query.split("&"))
		{
			String pair[] = param.split("=");
			if (pair.length > 1)
			{
				result.put(pair[0], pair[1]);
			} else
			{
				result.put(pair[0], "");
			}
		}
		return result;
	}

	private HttpContext createSubContext(HttpContext superContext, String subPath, HttpHandler subContext)
	{
		return server.createContext(superContext.getPath() + subPath, subContext);
	}
}
