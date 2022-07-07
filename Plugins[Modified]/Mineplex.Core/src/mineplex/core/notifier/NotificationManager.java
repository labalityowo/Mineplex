package mineplex.core.notifier;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.event.UpdateEvent;

public class NotificationManager extends MiniPlugin
{
	private boolean _enabled = true;
	
	private CoreClientManager _clientManager;

	private String _summerLine = 
			C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + 
					C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + 
					C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + 
					C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + 
					C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + 
					C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█" + C.cBlack + "█" + C.cYellow + "█";

	public NotificationManager(JavaPlugin plugin, CoreClientManager client)
	{
		super("Notification Manager", plugin);

		_clientManager = client;
	}

	@EventHandler
	public void notify(UpdateEvent event)
	{
		if (!_enabled)
			return;

//		if (event.getType() == UpdateType.MIN_08)
//			hugeSale();

//		if (event.getType() == UpdateType.MIN_16)
//			sale();
		
//		if (event.getType() == UpdateType.MIN_08)
//			christmasSale();
	}

	private void christmasSale() 
	{
		for (Player player : UtilServer.getPlayers())
		{
			if (_clientManager.Get(player).getPrimaryGroup() == PermissionGroup.PLAYER)
			{
				player.sendMessage(C.cWhite + " ");
				player.sendMessage(C.cRedB + "                    MASSIVE WINTER SALE");
				player.sendMessage(C.cWhiteB + "                     50% OFF ALL RANKS");
				player.sendMessage("                          " + C.cGreen + C.Line + "www.mineplex.com/shop");
				player.sendMessage(C.cWhite + " ");
			}
		}
	}
	
	private void sale() 
	{
		for (Player player : UtilServer.getPlayers())
		{
			PermissionGroup group = _clientManager.Get(player).getPrimaryGroup();
			
			if (group.inheritsFrom(PermissionGroup.LEGEND))
			{
				continue;
			}

			if (group == PermissionGroup.PLAYER)
			{
				UtilPlayer.message(player, C.cWhite + " 50% Off Sale! " + " Purchase " + C.cAqua + C.Bold + "Ultra Rank" + C.cWhite + " for $15");
			}
			else if (group == PermissionGroup.ULTRA)
			{
				UtilPlayer.message(player, C.cWhite + " 50% Off Sale! " + " Upgrade to " + C.cPurple + C.Bold + "Hero Rank" + C.cWhite + " for $15!");
			} 
			else if (group == PermissionGroup.HERO)
			{
				UtilPlayer.message(player, C.cWhite + " 50% Off Sale! " + "Upgrade to " + C.cGreen + C.Bold + "Legend Rank" + C.cWhite + " for $15!");
			} 

			UtilPlayer.message(player, C.cWhite + " Visit " + F.link("www.mineplex.com/shop") + C.cWhite + " for 50% Off Ranks!");
		}
	}

	private void hugeSale() 
	{
		for (Player player : UtilServer.getPlayers())
		{
			PermissionGroup group = _clientManager.Get(player).getPrimaryGroup();
			
			if (group.inheritsFrom(PermissionGroup.LEGEND))
			{
				continue;
			}

			UtilPlayer.message(player, _summerLine);
			UtilPlayer.message(player, " ");
			UtilPlayer.message(player, "          " + 
					C.cGreen + C.Bold + "75% OFF" + 
					C.cYellow + C.Bold + "  SUMMER SUPER SALE  " + 
					C.cGreen + C.Bold + "75% OFF");
			UtilPlayer.message(player, " ");

			if (group == PermissionGroup.PLAYER)
			{
				UtilPlayer.message(player, C.cWhite + " " + player.getName() + ", you can get 75% Off " + C.cAqua + C.Bold + "All Lifetime Ranks" + C.cWhite + "!");
				UtilPlayer.message(player, C.cWhite + " This is our biggest sale ever, " + C.cRed + C.Line + "ends Sunday 16th" +  C.cWhite + "!");
			}
			else if (group == PermissionGroup.ULTRA)
			{
				UtilPlayer.message(player, C.cWhite + " Hello " + player.getName() + ", upgrade to " + C.cPurple + C.Bold + "HERO RANK" + C.cWhite + " for only $7.50!");
				UtilPlayer.message(player, C.cWhite + " This is our biggest sale ever, " + C.cRed + C.Line + "ends Sunday 16th" +  C.cWhite + "!");
			} 
			else if (group == PermissionGroup.HERO)
			{
				UtilPlayer.message(player, C.cWhite + " Hello " + player.getName() + ", upgrade to " + C.cGreen + C.Bold + "LEGEND RANK" + C.cWhite + " for only $7.50!");
				UtilPlayer.message(player, C.cWhite + " This is our biggest sale ever, " + C.cRed + C.Line + "ends Sunday 16th" +  C.cWhite + "!");
			} 

			UtilPlayer.message(player, " ");
			UtilPlayer.message(player, "                         " + C.cGreen + "www.mineplex.com/shop");
			UtilPlayer.message(player, " ");
			//UtilPlayer.message(player, C.cRed + C.Bold + "                  This Weekend Only!");
			UtilPlayer.message(player, _summerLine);
		}
	}
}