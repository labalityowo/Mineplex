package mineplex.core.rankGiveaway.redis;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.rankGiveaway.LightFlicker;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class GiveawayMessageHandler implements CommandCallback
{
	private JavaPlugin _plugin;

	public GiveawayMessageHandler(JavaPlugin plugin)
	{
		_plugin = plugin;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof TitanGiveawayMessage)
		{
			TitanGiveawayMessage message = ((TitanGiveawayMessage) command);
			String playerName = message.getPlayerName();
			int count = message.getTitanCount();
			String countString = count + UtilTime.getDayOfMonthSuffix(count);
			String chatMessage = C.cRed + playerName + C.cWhite + " found Titan in a " + C.cRed + "Christmas Present";
			UtilTextMiddle.display(C.cDRed + C.Bold + "TITAN", chatMessage, 20, 80, 20, UtilServer.getPlayers());
			World world = UtilServer.getPlayers().length > 0 ? UtilServer.getPlayers()[0].getWorld() : Bukkit.getWorlds().get(0);
			LightFlicker lightFlicker = new LightFlicker(world);
			lightFlicker.runTaskTimer(_plugin, 1, 1);

			for (Player player : UtilServer.getPlayers())
			{
//				player.sendMessage(chatMessage);
				player.playSound(player.getEyeLocation(), Sound.AMBIENCE_CAVE, 1, 1);
			}
		}
		else if (command instanceof EternalGiveawayMessage)
		{
			EternalGiveawayMessage message = ((EternalGiveawayMessage) command);
			String playerName = message.getPlayerName();
			int count = message.getEternalCount();
			String countString = count + UtilTime.getDayOfMonthSuffix(count);
			String chatMessage = C.cPurple + playerName + C.cWhite + " found Eternal Rank in a " + C.cPurple + "Flaming Pumpkin";
			UtilTextMiddle.display(C.cDPurple + C.Bold + "ETERNAL", chatMessage, 20, 80, 20, UtilServer.getPlayers());
			World world = UtilServer.getPlayers().length > 0 ? UtilServer.getPlayers()[0].getWorld() : Bukkit.getWorlds().get(0);
			LightFlicker lightFlicker = new LightFlicker(world);
			lightFlicker.runTaskTimer(_plugin, 1, 1);

			for (Player player : UtilServer.getPlayers())
			{
				player.playSound(player.getEyeLocation(), Sound.AMBIENCE_CAVE, 1, 1);
			}
		}
	}
}
