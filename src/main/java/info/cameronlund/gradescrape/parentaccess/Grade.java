package info.cameronlund.gradescrape.parentaccess;

public class Grade {
	private final String letterGrade;
	private final double numericalGrade;

	public Grade(String letterGrade, double numericalGrade)
	{
		this.letterGrade = letterGrade;
		this.numericalGrade = numericalGrade;
	}

	public String getLetterGrade()
	{
		return letterGrade;
	}

	public double getNumericalGrade()
	{
		return numericalGrade;
	}

	public int getRoundedGrade()
	{
		return (int) Math.round(numericalGrade);
	}
}
