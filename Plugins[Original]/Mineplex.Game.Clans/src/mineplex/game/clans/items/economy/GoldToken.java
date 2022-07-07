package mineplex.game.clans.items.economy;

import org.bukkit.Material;

import mineplex.game.clans.items.CustomItem;;

public class GoldToken extends CustomItem
{
	private int _goldValue;

	public GoldToken()
	{
		this(0);
	}
	
	public GoldToken(int goldValue)
	{
		super("Gold Token", null, Material.RABBIT_FOOT);
		
		_goldValue = goldValue;
	}
	
	public int getGoldValue()
	{
		return _goldValue;
	}
	
	@Override
	public String[] getDescription()
	{
		return new String[]
		{
			String.format("A gold token worth %s gold coins.", _goldValue),
		};
	}
}