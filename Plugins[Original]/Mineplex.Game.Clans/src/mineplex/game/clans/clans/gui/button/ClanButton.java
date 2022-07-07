package mineplex.game.clans.clans.gui.button;

import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.shop.item.IButton;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;

public abstract class ClanButton implements IButton
{
	private ClanShop _shop;
	protected ClansManager _clansManager;
	private Player _player;
	private ClanInfo _clanInfo;
	private ClanRole _clanRole;

	public ClanButton(ClanShop shop, ClansManager clansManager, Player player, ClanInfo clanInfo, ClanRole clanRole)
	{
		_shop = shop;
		_clansManager = clansManager;
		_player = player;
		_clanInfo = clanInfo;
		_clanRole = clanRole;
	}

	public ClansManager getClansManager()
	{
		return _clansManager;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public ClanInfo getClanInfo()
	{
		return _clanInfo;
	}

	public ClanRole getClanRole()
	{
		return _clanRole;
	}

	public ClanShop getShop()
	{
		return _shop;
	}

	protected void displayText(String header, String message)
	{
		UtilTextMiddle.display(header, message, _player);
	}

	protected void displayClan(String header, String message, boolean displayForClicker)
	{
		for (Player player : _clanInfo.getOnlinePlayers())
		{
			if (displayForClicker || !player.equals(getPlayer()))
				UtilTextMiddle.display(header, message, player);
		}

//		_clansManager.messageClan(_clanInfo, message);
	}
}
