package mineplex.game.clans.clans.data;

import org.bukkit.entity.Player;

import mineplex.game.clans.clans.ClanInfo;

public class PlayerClan
{
	private Player _player;
	private ClanInfo _clan;

	public PlayerClan(Player player, ClanInfo clan)
	{
		_player = player;
		_clan = clan;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public ClanInfo getClan()
	{
		return _clan;
	}
}
