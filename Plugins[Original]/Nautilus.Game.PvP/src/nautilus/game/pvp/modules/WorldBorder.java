package nautilus.game.pvp.modules;

import java.util.HashSet;

import mineplex.core.Rank;
import me.chiss.Core.Module.AModule;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilServer;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldBorder extends AModule
{
	private int _edgeWorld = 600;
	private HashSet<Material> _ignore;

	public WorldBorder(JavaPlugin plugin) 
	{
		super("World Border", plugin);
	}

	@Override
	public void enable() 
	{
		_ignore = new HashSet<Material>();
		_ignore.add(Material.LOG);
	}

	@Override
	public void disable() 
	{

	}

	@Override
	public void config()
	{

	}

	@Override
	public void commands() 
	{
		AddCommand("edgemark");
	}

	@Override
	public void command(Player caller, String cmd, String[] args)
	{
		if (!Clients().Get(caller).Rank().Has(Rank.ADMIN, true))
			return;

		if (cmd.equals("edgemark"))
		{
			caller.sendMessage("Marking Edges...");

			for (int x=-_edgeWorld ; x<=_edgeWorld ; x++)
				for (int z=-_edgeWorld ; z<=_edgeWorld ; z++)
				{
					if (Math.abs(x) != _edgeWorld && Math.abs(z) != _edgeWorld)
						continue;

					Block block = UtilBlock.getHighest(caller.getWorld(), x, z, _ignore);
					int yMax = block.getY();

					for (int y=yMax ; y>=0 ; y--)
						block.setType(Material.BEDROCK);
				}
			
			caller.sendMessage("Marked Edges.");
		}
	}
	
	@EventHandler
	public void UpdateEvent(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		for (Player cur : UtilServer.getPlayers())
		{
			if (cur.getWorld().getEnvironment() != Environment.NORMAL)
				continue;
			
			double offset = 0;
			
			//X+
			if (cur.getLocation().getX() > _edgeWorld)
				if (Math.abs(cur.getLocation().getX() - _edgeWorld) > offset)
					offset = Math.abs(cur.getLocation().getX() - _edgeWorld);

			//X-
			if (cur.getLocation().getX() < -_edgeWorld)
				if (Math.abs(cur.getLocation().getX() + _edgeWorld) > offset)
					offset = Math.abs(cur.getLocation().getX() + _edgeWorld);
			
			//Z+
			if (cur.getLocation().getZ() > _edgeWorld)
				if (Math.abs(cur.getLocation().getZ() - _edgeWorld) > offset)
					offset = Math.abs(cur.getLocation().getZ() - _edgeWorld);

			//Z-
			if (cur.getLocation().getZ() < -_edgeWorld)
				if (Math.abs(cur.getLocation().getZ() + _edgeWorld) > offset)
					offset = Math.abs(cur.getLocation().getZ() + _edgeWorld);
			
			if (offset == 0)
				continue;
			
			Damage().NewDamageEvent(cur, null, null, DamageCause.VOID, 1 + offset/3 , false, true, false, "Worlds Edge", null);
			cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, 7);
		}		
	}
}
