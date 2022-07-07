package mineplex.game.clans.clans.siege.weapon.util;

import java.util.function.Predicate;

import org.bukkit.entity.Player;

public class AccessRule
{
	private Predicate<Player> _access;
	private AccessType _type;
	
	public AccessRule(AccessType type, Predicate<Player> access)
	{
		_type = type;
		_access = access;
	}
	
	public boolean allow(AccessType type, Player player)
	{
		return type.equals(_type) && _access.test(player);
	}

}
