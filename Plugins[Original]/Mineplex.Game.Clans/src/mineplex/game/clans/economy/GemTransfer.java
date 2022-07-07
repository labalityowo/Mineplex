package mineplex.game.clans.economy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import mineplex.serverdata.data.Data;

public class GemTransfer implements Data
{
	private static DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
	
	private String _playerName;
	private Date _date;
	
	public GemTransfer(String playerName)
	{
		_playerName = playerName;
		_date = new Date();
	}
	
	public boolean transferWasToday()
	{
		return currentDate().before(_date);
	}
	
	@Override
	public String getDataId()
	{
		return _playerName;
	}
	
	/**
	 * @return the current date, with hours/minutes/seconds defaulted to 00:00, 12:00am of the
	 * current day.
	 */
	public static Date currentDate()
	{
		Date current = new Date();
		
		try
		{
			return dateFormatter.parse(dateFormatter.format(current));
		}
		catch (Exception exception) { }
		
		return null;
	}
}