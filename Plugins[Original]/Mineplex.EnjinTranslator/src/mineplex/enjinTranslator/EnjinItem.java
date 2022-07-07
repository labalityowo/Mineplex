package mineplex.enjinTranslator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EnjinItem
{
	public String item_name;
	public double item_price;
	public int item_id;
	public Map<String, String> variables = new HashMap<String, String>();
	
	public void logInfoToConsole()
	{
		System.out.println("item_id : " + item_id + ", item_name : " + item_name + ", item_price : " + item_price);
		
		for (Entry<String, String> variable : variables.entrySet())
		{
			System.out.println("key : " + variable.getKey() + ", value : " + variable.getValue());
		}
	}
}
