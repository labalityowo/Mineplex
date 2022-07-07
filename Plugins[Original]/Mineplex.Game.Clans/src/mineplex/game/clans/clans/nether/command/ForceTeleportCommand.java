package mineplex.game.clans.clans.nether.command;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.game.clans.clans.ClanTips.TipType;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.nether.NetherManager;
import mineplex.game.clans.spawn.Spawn;

/**
 * Command to artificially portal 
 */
public class ForceTeleportCommand extends CommandBase<NetherManager>
{
	public ForceTeleportCommand(NetherManager plugin)
	{
		super(plugin, NetherManager.Perm.PORTAL_FORCE_COMMAND, "forcePortal");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		boolean natural = false;
		
		if (args.length > 0 && (args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false")))
		{
			natural = Boolean.parseBoolean(args[0]);
		}
		
		if (Plugin.isInNether(caller))
		{
			if (natural)
			{
				Plugin.InNether.remove(caller);
				caller.teleport(Plugin.getReturnLocation(caller));
				Plugin.OverworldOrigins.remove(caller);
				caller.removePotionEffect(PotionEffectType.NIGHT_VISION);
				UtilPlayer.message(caller, F.main(ClansManager.getInstance().getNetherManager().getName(), "You have escaped " + F.clansNether("The Nether") + "!"));
				ClansManager.getInstance().runSyncLater(() ->
				{
					ClansManager.getInstance().getCombatManager().getLog(caller).SetLastCombatEngaged(System.currentTimeMillis());
				}, 20);
			}
			else
			{
				caller.teleport(Spawn.getNorthSpawn());
			}
		}
		else
		{
			if (natural)
			{
				Plugin.InNether.put(caller, System.currentTimeMillis() + UtilTime.convert(10, TimeUnit.MINUTES, TimeUnit.MILLISECONDS));
				caller.teleport(Plugin.getNetherWorld().getSpawnLocation());
				ClansManager.getInstance().ClanTips.displayTip(TipType.ENTER_NETHER, caller);
			}
			else
			{
				caller.teleport(Plugin.getNetherWorld().getSpawnLocation());
			}
		}
	}
}