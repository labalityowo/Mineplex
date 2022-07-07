package mineplex.game.clans.clans.gui.button;

import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilWorld;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;

public class ClanTerritoryButton extends ClanButton
{
	public ClanTerritoryButton(ClanShop shop, ClansManager clansManager, Player player, ClanInfo clanInfo, ClanRole clanRole)
	{
		super(shop, clansManager, player, clanInfo, clanRole);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (UtilServer.CallEvent(new ClansButtonClickEvent(player, ClansButtonClickEvent.ButtonType.Territory)).isCancelled())
			return;
		if (_clansManager.getNetherManager().isInNether(player))
		{
			UtilPlayer.message(player, F.main(_clansManager.getNetherManager().getName(), "You cannot manage your clan's territory while in " + F.clansNether("The Nether") + "!"));
			player.closeInventory();
			return;
		}
		if (_clansManager.getWorldEvent().getRaidManager().isInRaid(player.getLocation()))
		{
			UtilPlayer.message(player, F.main(_clansManager.getWorldEvent().getRaidManager().getName(), "You cannot manage your clan's territory while in a raid!"));
			player.closeInventory();
			return;
		}
		
		if (clickType == ClickType.LEFT)
		{
			player.closeInventory();

			Chunk chunk = player.getLocation().getChunk();
			String chunkName = UtilWorld.chunkToStrClean(chunk);
			if (getClansManager().getClanUtility().claim(player))
			{
				displayText(C.cGreen + "Territory", "You claimed the chunk " + chunkName);
				displayClan(C.cGreen + "Territory", C.cYellow + getPlayer().getName() + ChatColor.RESET + " claimed the chunk " + chunkName, false);
			}
		}
		else if (clickType == ClickType.SHIFT_LEFT)
		{
			player.closeInventory();

			Chunk chunk = player.getLocation().getChunk();
			String chunkName = UtilWorld.chunkToStrClean(chunk);
			if (getClansManager().getClanUtility().unclaim(player, player.getLocation().getChunk()))
			{
				displayText(C.cGreen + "Territory", "You unclaimed the chunk " + chunkName);
				displayClan(C.cGreen + "Territory", C.cYellow + getPlayer().getName() + ChatColor.RESET + " unclaimed the chunk " + chunkName, false);
			}
		}
		else if (clickType == ClickType.SHIFT_RIGHT && getClanRole() == ClanRole.LEADER)
		{
			player.closeInventory();

			if (getClansManager().getClanUtility().unclaimAll(player))
			{
				displayText(C.cGreen + "Territory", "You unclaimed all chunks");
				displayClan(C.cGreen + "Territory", C.cYellow + getPlayer().getName() + ChatColor.RESET + " unclaimed all chunks", false);
			}
		}
	}
}
