package mineplex.game.clans.clans.tntgenerator;

import java.util.UUID;

import org.bukkit.block.Block;

public class TntGenerator
{
	private UUID _creator;
	private int _ticks;
	private int _stock;

	public TntGenerator(String data)
	{
		_creator = UUID.fromString(data);
	}

	public int getTicks()
	{
		return _ticks;
	}

	public void setTicks(int ticks)
	{
		_ticks = ticks;
	}

	public void incrementTicks()
	{
		_ticks++;
	}

	public int getStock()
	{
		return _stock;
	}

	public void setStock(int stock)
	{
		_stock = stock;
	}

	public UUID getBuyer()
	{
		return _creator;
	}
}
