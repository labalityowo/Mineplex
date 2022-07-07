package mineplex.game.clans.clans.invsee.commands;

import java.util.UUID;

import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.WorldNBTStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import mineplex.game.clans.clans.invsee.InvseeManager;

public class InvseeCommand extends CommandBase<InvseeManager>
{
	public InvseeCommand(InvseeManager plugin)
	{
		super(plugin, InvseeManager.Perm.INVSEE_COMMAND, "invsee");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.help("/invsee <playername/playeruuid>", "View a player's inventory", ChatColor.GOLD));
			return;
		}
		UUID uuid = null;
		try
		{
			uuid = UUID.fromString(args[0]);
		}
		catch (IllegalArgumentException failed) {}

		OfflinePlayer exactPlayer = Bukkit.getServer().getPlayerExact(args[0]);
		if (exactPlayer == null)
		{
			if (uuid == null)
			{
				// We don't want to open the wrong OfflinePlayer's inventory, so if we can't fetch the UUID then abort
				GameProfile gameProfile = MinecraftServer.getServer().getUserCache().getProfile(args[0]);
				if (gameProfile == null)
				{
					UtilPlayer.message(caller, F.main("Invsee", "Player is offline and we could not find the UUID. Aborting"));
					return;
				}
				uuid = gameProfile.getId();
			}
			if (uuid == null)
			{
				UtilPlayer.message(caller, F.main("Invsee", "Something has gone very wrong. Please report the username/uuid you tried to look up"));
				return;
			}
			// We need to check if we actually have data on this player
			// fixme main thread file IO but it's what the server does...?
			NBTTagCompound compound = ((WorldNBTStorage) MinecraftServer.getServer().worlds.get(0).getDataManager()).getPlayerData(uuid.toString());
			if (compound == null)
			{
				UtilPlayer.message(caller, F.main("Invsee", "The player exists, but has never joined this server. No inventory to show"));
				return;
			}
			exactPlayer = Bukkit.getServer().getOfflinePlayer(uuid);
		}
		if (exactPlayer == null)
		{
			UtilPlayer.message(caller, F.main("Invsee", "Could not load offline player data. Does the player exist?"));
			return;
		}
		if (exactPlayer.getUniqueId().equals(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Invsee", "You cannot invsee yourself!"));
			return;
		}
		if (Plugin.isBeingInvseen(caller))
		{
			UtilPlayer.message(caller, F.main("Invsee", "You cannot use invsee right now. Someone is invseeing you!"));
			return;
		}
		if (exactPlayer.isOnline() && Plugin.isInvseeing((Player) exactPlayer))
		{
			UtilPlayer.message(caller, F.main("Invsee", "You cannot use invsee right now. That person is currently using invsee!"));
			return;
		}
		
		if (!UtilServer.isTestServer())
		{
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy",
					new SlackMessage("Clans Command Logger", "crossed_swords", caller.getName() + " has started to invsee " + exactPlayer.getName() + " on " + UtilServer.getServerName() + "."),
					true);
		}
		Plugin.doInvsee(exactPlayer, caller);
	}
}