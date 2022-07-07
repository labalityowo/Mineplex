package mineplex.enjinTranslator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EnjinPurchase
{
	private static SimpleDateFormat _dateFormat = new SimpleDateFormat();
	
	public EnjinUser user;
	public long purchase_date;
	public String currency;
	public String character;
	public List<EnjinItem> items;
	
	public void logInfoToConsole()
	{
		user.logInfoToConsole(); 
		System.out.println(" MC Character : " + character + ", purchase_date : " + _dateFormat.format(new Date(purchase_date)) + ", currency : " + currency);
		
		for (EnjinItem item : items)
		{
			item.logInfoToConsole();
		}
	}
}
