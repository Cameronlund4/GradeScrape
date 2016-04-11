package info.cameronlund.gradescrape.api.v1.enums;

public enum MarkingPeriod {
	FIRST("MP1"), SECOND("MP2"), THIRD("MP3"), FOURTH("MP4");

	private final String lowName;

	private MarkingPeriod(final String lowName)
	{
		this.lowName = lowName;
	}

	// Name commonly used for data, not to be shown to users
	public String getLowName()
	{
		return lowName;
	}

}
