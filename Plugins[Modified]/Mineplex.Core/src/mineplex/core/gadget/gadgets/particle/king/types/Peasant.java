package mineplex.core.gadget.gadgets.particle.king.types;

import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilTime;

public class Peasant
{

	private static final long PLAYER_COOLDOWN = 15000;

	private Player _player;
	private long _cooldown = 0;
	private King _king;

	public Peasant(Player player)
	{
		_player = player;
	}

	public Player getPeasant()
	{
		return _player;
	}

	public boolean isInCooldown()
	{
		return !UtilTime.elapsed(_cooldown, PLAYER_COOLDOWN);
	}

	public void setCooldown()
	{
		_cooldown = System.currentTimeMillis();
	}

	public King getKing()
	{
		return _king;
	}

	public void setKing(King king)
	{
		_king = king;
	}

	public void removeKing()
	{
		_king = null;
	}

}
