package mineplex.core.rankGiveaway.redis;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.status.ServerStatusManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class TitanChestGiveawayHandler implements CommandCallback
{
	private static final long COOLDOWN = 60000L * 120; // 120 Minutes

	private ServerStatusManager _statusManager;
	private long _lastTimer;
	private boolean _enabled;

	public TitanChestGiveawayHandler(ServerStatusManager statusManager)
	{
		_statusManager = statusManager;
		_lastTimer = 0;
		_enabled = false;
	}


	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof TitanChestGiveawayMessage && _enabled)
		{
			TitanChestGiveawayMessage chestCommand = ((TitanChestGiveawayMessage) command);
			if (chestCommand.getServer().equals(_statusManager.getCurrentServerName()) || System.currentTimeMillis() >= _lastTimer + COOLDOWN)
			{
				String chatMessage = C.cRed + chestCommand.getPlayerName() + C.cWhite + " found Titan in a " + C.cRed + "Mythical Chest";
				UtilTextMiddle.display(C.cDRed + C.Bold + "TITAN", chatMessage, 20, 80, 20, UtilServer.getPlayers());

				for (Player player : UtilServer.getPlayers())
				{
//					player.sendMessage(chatMessage);
					player.playSound(player.getEyeLocation(), Sound.AMBIENCE_CAVE, 1, 1);
				}

				_lastTimer = System.currentTimeMillis();
			}
		}
	}
}
