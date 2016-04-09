package info.cameronlund.bettergrades;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import info.cameronlund.gradescrape.api.MarkingPeriod;
import info.cameronlund.gradescrape.parentaccess.Grade;
import info.cameronlund.gradescrape.parentaccess.ParentAccessScrape;
import info.cameronlund.gradescrape.user.Credentials;
import info.cameronlund.gradescrape.user.Student;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class HttpPortal
{
	private String ip = "192.168.1.164";
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
		System.out.println("Creating context: " + server.createContext("/api", new ParentAccessHandler()).getPath());
		server.setExecutor(null);
		server.start();
		System.out.println(" - Success!");
	}

	public class ParentAccessHandler implements HttpHandler
	{
		public void handle(HttpExchange exchange) throws IOException
		{
			long start = System.currentTimeMillis();
			try
			{
				// Console output
				System.out.println("Got call to " + exchange.getHttpContext().getPath());
				System.out.println("Context " + exchange.getHttpContext().getPath() + " responding to " + exchange.getRemoteAddress());
				JsonObject response = new JsonObject();
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
					String[] studentName = args.get("student").split("\\+");
					String username = args.get("username");
					String password = args.get("password");
					Student student = new Student(studentName[0], studentName[1]);
					student.giveCredentials("parent_access", new Credentials(username, password));
					try
					{
						getData:
						{
							System.out.println(" - Getting their grades");
							startNewTask("Create");
							ParentAccessScrape site = new ParentAccessScrape(student);
							printTaskTime("Create");
							startNewTask("Authenticate");
							if (!site.checkAuth()) break getData;
							printTaskTime("Authenticate");

							MarkingPeriod[] markingPeriods = new MarkingPeriod[]{
									MarkingPeriod.FIRST, MarkingPeriod.SECOND, MarkingPeriod.THIRD, MarkingPeriod.FOURTH
							};
							// In the future we'll use grade scales (And progressbook provides them :D
							for (MarkingPeriod period : markingPeriods)
							{
								startNewTask("Get marking period " + period.getLowName());
								JsonObject grades = new JsonObject();
								System.out.println("    - " + period.getLowName());
								for (Map.Entry<String, Grade> grade : site.getClassGrades(period).entrySet())
								{
									if (grade.getValue() != null)
										grades.addProperty(grade.getKey(), grade.getValue().getNumericalGrade() + " ("
												+ grade.getValue().getLetterGrade() + ")");
									else
										grades.addProperty(grade.getKey(), "N/A");
									System.out.println("       - " + grade.getKey() + ": " +
											(grade.getValue() != null ? grade.getValue().getNumericalGrade() : "N/A"));
								}
								gradeJson.add(period.getLowName(), grades);
								printTaskTime("Get marking period " + period.getLowName());
							}
							startNewTask("Unauth");
							site.unauth();
							printTaskTime("Unauth");
							success = true;
						}
					} catch (Exception e)
					{
						success = false;
						e.printStackTrace();
					}
				}

				// Send JSON
				startNewTask("Send response");
				response.addProperty("success", success ? "1" : "0");
				response.addProperty("time", (System.currentTimeMillis() - start) / 1000D + "s");
				if (success)
					response.add("grades", gradeJson);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.getResponseHeaders().add("Content-Length", response.toString().getBytes().length + "");
				exchange.sendResponseHeaders(200, response.toString().getBytes().length);
				OutputStream os = exchange.getResponseBody();
				os.write(response.toString().getBytes());
				os.close();
				printTaskTime("Send response");
				System.out.println(success ? " - Success!" : " - Failed!");
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public Map<String, String> queryToMap(String query)
	{
		System.out.println("Parsing query: " + query);
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

	private static long lastTimeChecked = System.currentTimeMillis();

	public static void startNewTask(String task)
	{
		System.out.println("-----" + task + " started-----");
		lastTimeChecked = System.currentTimeMillis();
	}

	public static void printTaskTime(String task)
	{
		System.out.println("-----" + task + " completed-----");
		System.out.println("Task took " + getTimePassed() + " seconds");
		System.out.println("----------------------------");
	}

	public static double getTimePassed()
	{
		return ((double) (System.currentTimeMillis() - lastTimeChecked)) / 1000D;
	}
}
