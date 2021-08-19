package mineplex.core.creature.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.creature.Creature;
import mineplex.core.creature.event.CreatureKillEntitiesEvent;

public class KillCommand extends CommandBase<Creature>
{
	public KillCommand(Creature plugin)
	{
		super(plugin, Creature.Perm.KILL_MOB_COMMAND, "kill", "k");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Missing Entity Type Parameter."));
			return;
		}

		EntityType type = UtilEnt.searchEntity(caller, args[0], true);

		if (type == null && !args[0].equalsIgnoreCase("all"))
			return;

		int count = 0;
		List<Entity> killList = new ArrayList<Entity>();
		
		for (World world : UtilServer.getServer().getWorlds())
		{
			for (Entity ent : world.getEntities())
			{
				if (ent.getType() == EntityType.PLAYER)
					continue;
				
				if (type == null || ent.getType() == type)
				{
					killList.add(ent);
				}
			}
		}

		CreatureKillEntitiesEvent event = new CreatureKillEntitiesEvent(killList);
		Plugin.getPlugin().getServer().getPluginManager().callEvent(event);
		
		for (Entity entity : event.GetEntities())
		{
			entity.remove();
			count++;
		}
		
		String target = "ALL";
		if (type != null)
			target = UtilEnt.getName(type);

		UtilPlayer.message(caller, F.main(Plugin.getName(), "Killed " + target + ". " + count + " Removed."));
	}
}