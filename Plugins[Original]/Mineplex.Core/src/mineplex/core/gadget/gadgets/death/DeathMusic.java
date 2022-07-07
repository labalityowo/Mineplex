package mineplex.core.gadget.gadgets.death;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class DeathMusic extends DeathEffectGadget
{
	
	private Map<Location, Long> _playerMap = new HashMap<>();

	public DeathMusic(GadgetManager manager)
	{
		super(manager, "Musical Death", 
				UtilText.splitLineToArray(C.cGray + C.Italics + "\u266B Your very own swan song. \u266B", LineFormat.LORE),
				-2, Material.GREEN_RECORD, (byte) 0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);
		_playerMap.put(player.getLocation(), System.currentTimeMillis() + 1250);
		_playerMap.put(player.getLocation().add(0, 0.9, 0), System.currentTimeMillis() + 1250);
		_playerMap.put(player.getLocation().add(0, 1.8, 0), System.currentTimeMillis() + 1250);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
		
		if(event.getTick()%2 == 1) return;
		
		for(Iterator<Entry<Location, Long>> it = _playerMap.entrySet().iterator(); it.hasNext();)
		{
			Entry<Location, Long> e = it.next();
			if(e.getValue() <= System.currentTimeMillis())
			{
				it.remove();
				continue;
			}
			
			long diff = e.getValue()-System.currentTimeMillis();
			double d = diff/1250.0;
			
			int amount = 4;
			
			double rad = Math.PI * 2 * d;


			double step = Math.PI * 2;
			step /= amount;
			
			for(int i = 0; i < amount; i++)
			{
				double rad2 = rad + step * i;

				double x = Math.sin(rad2) * d * 1.3;
				double z = Math.cos(rad2) * d * 1.3;
				
				Location loc = e.getKey().clone().add(x, 0, z);
				
				UtilParticle.PlayParticleToAll(ParticleType.NOTE, loc, new Vector(d, 0, 0), 1, 0, ViewDist.NORMAL);

			}
		}
	}

	
}
