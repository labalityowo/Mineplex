package mineplex.game.clans.clans.amplifiers;

import mineplex.game.clans.clans.amplifiers.AmplifierManager.AmplifierType;

import org.bukkit.entity.Player;

/**
 * Data class for active amplifiers
 */
public class Amplifier
{
	private Player _owner;
	private long _end;
	
	public Amplifier(Player owner, AmplifierType type)
	{
		_owner = owner;
		_end = System.currentTimeMillis() + type.getDuration();
	}
	
	/**
	 * Gets the owner of the amplifier
	 * @return This amplifier's owner
	 */
	public Player getOwner()
	{
		return _owner;
	}
	
	/**
	 * Gets the remaining duration of this amplifier
	 * @return How much time is left before this amplifier expires
	 */
	public long getRemainingTime()
	{
		return Math.max(0, _end - System.currentTimeMillis());
	}
	
	/**
	 * Checks whether this amplifier has run out of time
	 * @return Whether this amplifier has run out of time
	 */
	public boolean isEnded()
	{
		return System.currentTimeMillis() >= _end;
	}
}