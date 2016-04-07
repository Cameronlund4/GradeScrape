import info.cameronlund.bettergrades.HttpPortal;

public class GradeScrape {
	private static long lastTimeChecked = System.currentTimeMillis();

	public static void main(String args[])
	{
		new HttpPortal().start();
	}

	public static void startNewTask()
	{
		System.out.println("-----Task speed test-----");
		lastTimeChecked = System.currentTimeMillis();
	}

	public static void printTaskTime(String task)
	{
		System.out.println("Completed task: "+task);
		System.out.println("Task took "+getTimePassed()+" seconds");
	}

	public static double getTimePassed()
	{
		return ((double) (System.currentTimeMillis()-lastTimeChecked)) / 1000D;
	}
}
