package mineplex.core.common.util;

import org.bukkit.ChatColor;

import mineplex.core.common.currency.Currency;

public class F 
{
	public static String main(String module, String body)
	{
		return C.mHead + module + "> " + C.mBody + body;
		//return C.mBody + body;
	}
	
	public static String tute(String sender, String body)
	{
		return C.cGold + sender + "> " + C.cWhite + body;
	}
	
	public static String te(String message) 
	{
		return C.cYellow + message + C.cWhite;
	}
	
	public static String game(String elem)
	{
		return C.mGame + elem + C.mBody;
	}
	
	public static String color(String msg, String color)
	{
		return color + msg + C.mBody;
	}
	
	public static String ta(String message) 
	{
		return C.cGreen + message + C.cWhite;
	}
	
	public static String ts(String message) 
	{
		return C.cGold + message + C.cWhite;
	}

	public static String sys(String head, String body)
	{
		return C.sysHead + head + "> " + C.sysBody + body;
	}

	public static String elem(Object elem)
	{
		return C.mElem + elem.toString() + ChatColor.RESET + C.mBody;
	}
	
	public static String name(String elem)
	{
		return C.mElem + elem + C.mBody;
	}
	
	public static String count(int elem)
	{
		return count(String.valueOf(elem));
	}

	public static String count(String elem)
	{
		return C.mCount + elem + C.mBody;
	}

	public static String item(String elem)
	{
		return C.mItem + elem + C.mBody;
	}

	public static String link(String elem)
	{
		return C.mLink + elem + C.mBody;
	}

	public static String skill(String elem)
	{
		return C.mSkill + elem + C.mBody;
	}
	
	public static String skill(String a, String b)
	{

		return C.cYellow + " " + C.cGreen + b + C.mBody;
	}

	public static String time(String elem)
	{
		return C.mTime + elem + C.mBody;
	}

	public static String desc(String head, String body)
	{
		return C.descHead + head + ": " + C.descBody + body;
	}

	public static String wField(String field, String data)
	{
		return C.wFrame + "[" + C.wField + field + C.wFrame + "] " + C.mBody + data + " ";
	}
	
	public static String help(String cmd, String body, ChatColor displayColor)
	{
		return displayColor + cmd + " " + C.mBody + body;
	}

	public static String value(String variable, int value)
	{
		return value(variable, "" + value);
	}

	public static String value(String variable, String value)
	{
		return value(0, variable, value);
	}

	public static String value(int a, String variable, String value)
	{
		String indent = "";
		while (indent.length() < a)
			indent += ChatColor.GRAY + ">";

		return indent + C.listTitle + variable + ": " + C.listValue + value;
	}

	public static String value(String variable, String value, boolean on)
	{
		return value(0, variable, value, on);
	}

	public static String value(int a, String variable, String value, boolean on)
	{
		String indent = "";
		while (indent.length() < a)
			indent += ChatColor.GRAY + ">";

		if (on)			return indent + C.listTitle + variable + ": " + C.listValueOn + value;
		else			return indent + C.listTitle + variable + ": " + C.listValueOff + value;
	}
	
	public static String ed(boolean var)
	{
		if (var)
			return C.listValueOn + "Enabled" + C.mBody;
		return C.listValueOff + "Disabled" + C.mBody;
	}

	public static String oo(boolean var)
	{
		if (var)
			return C.listValueOn + "On" + C.mBody;
		return C.listValueOff + "Off" + C.mBody;
	}

	public static String tf(boolean var)
	{
		if (var)
			return C.listValueOn + "True" + C.mBody;
		return C.listValueOff + "False" + C.mBody;
	}

	public static String oo(String variable, boolean value)
	{
		if (value)
			return C.listValueOn + variable + C.mBody;
		return C.listValueOff + variable + C.mBody;
	}

	public static String combine(String[] args, int start, String color, boolean comma)
	{
		if (args.length == 0)
			return "";

		String out = "";

		for (int i = start ; i<args.length ; i++)
		{
			if (color != null)
			{
				String preColor = ChatColor.getLastColors(args[i]);
				out += color + args[i] + preColor;
			}
			else
				out += args[i];

			if (comma)
				out += ", ";
			else
				out += " ";
		}

		if (out.length() > 0)
			if (comma)	out = out.substring(0, out.length() - 2);
			else		out = out.substring(0, out.length() - 1);

		return out;
	}
	
	public static String currency(Currency type, int amount)
	{
		return type.getString(amount) + ChatColor.RESET + C.mBody;
	}

	public static String vowelAN(String word)
	{
		return word.toLowerCase().startsWith("a")
			|| word.toLowerCase().startsWith("A")
			|| word.toLowerCase().startsWith("e") 
			|| word.toLowerCase().startsWith("E") 
			|| word.toLowerCase().startsWith("i") 
			|| word.toLowerCase().startsWith("I") 
			|| word.toLowerCase().startsWith("o") 
			|| word.toLowerCase().startsWith("O") 
			|| word.toLowerCase().startsWith("u") 
			|| word.toLowerCase().startsWith("U")
		? "an" : "a";
	}

	public static String clansNether(String word)
	{
		return C.cClansNether + word + C.mBody;
	}

	public static String greenElem(String text)
	{
		return C.cGreen + text + ChatColor.RESET + C.mBody;
	}

}
