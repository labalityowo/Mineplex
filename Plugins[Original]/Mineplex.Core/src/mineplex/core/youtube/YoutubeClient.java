package mineplex.core.youtube;

import java.time.LocalDate;

public class YoutubeClient
{
	private LocalDate _clickDate;
	private LocalDate _specificDate;

	public YoutubeClient(LocalDate date, LocalDate specificDate)
	{
		_clickDate = date;
		_specificDate = specificDate;
	}

	public LocalDate getClickDate()
	{
		return _clickDate;
	}
	
	public LocalDate getSpecificDate()
	{
		return _specificDate;
	}

	public void setClickDate(LocalDate date)
	{
		_clickDate = date;
	}
	
	public void setSpecificDate(LocalDate date)
	{
		_specificDate = date;
	}
}