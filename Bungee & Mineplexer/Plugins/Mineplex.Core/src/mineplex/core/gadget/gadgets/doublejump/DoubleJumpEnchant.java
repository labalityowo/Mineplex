package mineplex.core.gadget.gadgets.doublejump;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class DoubleJumpEnchant extends DoubleJumpEffectGadget
{
	
	private Map<Location, Long> _locationMap = new HashMap<>();
	private Map<Player, Long> _playerMap = new HashMap<>();

	public DoubleJumpEnchant(GadgetManager manager)
	{
		super(manager, "Leap of Wisdom", 
				UtilText.splitLineToArray(C.cGray + "Distant sages use this leap to reach the highest shelves in their library.", LineFormat.LORE),
				-2, Material.BOOK, (byte) 0);
	}

	@Override
	public void doEffect(Player player)
	{
		_locationMap.put(player.getLocation(), System.currentTimeMillis() + 1500);
		_playerMap.put(player, System.currentTimeMillis() + 1100);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
		
		for (Iterator<Entry<Location, Long>> it = _locationMap.entrySet().iterator(); it.hasNext();)
		{
			Entry<Location, Long> e = it.next();
			if(e.getValue() <= System.currentTimeMillis())
			{
				it.remove();
				continue;
			}
			
			Location loc = e.getKey().clone().add(0, 1, 0);
			for(double d = 0; d < Math.PI*2; d += Math.PI/6) {
				double x = Math.sin(d);
				double z = Math.cos(d);
				Vector v = new Vector(x, -0.6, z).multiply(1.5);
//				OUT
//				UtilParticle.PlayParticleToAll(ParticleType.ENCHANTMENT_TABLE, loc.clone().add(-x*1.5, 0, -z*1.5), v, 1, 0, ViewDist.LONG);
//				IN
				UtilParticle.PlayParticleToAll(ParticleType.ENCHANTMENT_TABLE, loc.clone(), v, 1, 0, ViewDist.LONG);
			}
		}
		
		for (Iterator<Entry<Player, Long>> it = _playerMap.entrySet().iterator(); it.hasNext();)
		{
			Entry<Player, Long> e = it.next();
			if(e.getValue() <= System.currentTimeMillis())
			{
				it.remove();
				continue;
			}
			
			long diff = e.getValue()-System.currentTimeMillis();
			float r = diff/2000.0f;
			UtilParticle.PlayParticleToAll(ParticleType.ENCHANTMENT_TABLE, e.getKey().getLocation(), r, r, r, 0, 10, ViewDist.LONGER);
		}
	}

}
