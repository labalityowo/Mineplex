package mineplex.game.clans.clans.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.spawn.Spawn;

public class KillCommand extends CommandBase<ClansManager>
{	
	public KillCommand(ClansManager plugin)
	{
		super(plugin, ClansManager.Perm.SUICIDE_COMMAND, "suicide", "kill");
	}
 
	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!UtilTime.elapsed(Plugin.getCombatManager().getLog(caller).GetLastCombatEngaged(), Spawn.COMBAT_TAG_DURATION))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot use this command whilst in combat."));
			return;
		}
		if (Plugin.getTutorial().inTutorial(caller))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot use this command whilst in the tutorial."));
			return;
		}
		if (Plugin.getClanUtility().isSafe(caller.getLocation()) || (Plugin.getClanUtility().getClaim(caller.getLocation()) != null && Plugin.getClanUtility().getClaim(caller.getLocation()).Owner.equalsIgnoreCase("Spawn")))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot use this command whilst in a safezone!"));
			return;
		}
		if (Recharge.Instance.use(caller, "Suicide", 5000, false, false))
		{
			UtilPlayer.message(caller, F.main("Clans", "Run the command again to confirm."));
			return;
		}
		
		UtilPlayer.message(caller, F.main("Clans", "You have imploded."));
		
		caller.setHealth(0D);
	}
}