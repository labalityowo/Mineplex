package mineplex.staffServer.repository;

import java.util.Calendar;
import java.util.Date;

public class BonusEntry
{
	private int _accountId;
	private String _itemName;
	private int _itemCount;
	private Calendar _calendar;

	public BonusEntry(int accountId, String itemName, int itemCount, Date date)
	{
		_accountId = accountId;
		_itemName = itemName;
		_itemCount = itemCount;

		_calendar = Calendar.getInstance();
		_calendar.setTimeInMillis(date.getTime());
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public String getItemName()
	{
		return _itemName;
	}

	public int getItemCount()
	{
		return _itemCount;
	}

	public Calendar getCalendar()
	{
		return _calendar;
	}
}
