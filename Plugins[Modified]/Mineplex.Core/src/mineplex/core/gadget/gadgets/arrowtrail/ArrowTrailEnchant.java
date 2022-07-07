package mineplex.core.gadget.gadgets.arrowtrail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ArrowTrailEnchant extends ArrowEffectGadget
{
	
	private Map<Arrow, Long> _arrowMap = new HashMap<>(); 

	public ArrowTrailEnchant(GadgetManager manager)
	{
		super(manager, "Smart Arrows", 
				UtilText.splitLineToArray(C.cGray + "Ancient runes speed these arrows along their path.", LineFormat.LORE),
				-2, Material.BOOK, (byte) 0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		Vector v = arrow.getVelocity();
		double l = v.lengthSquared();
		v.multiply(-1/l);
		for(int i = 0; i < l; i++) {
			UtilParticle.PlayParticleToAll(ParticleType.ENCHANTMENT_TABLE, arrow.getLocation().add(v.clone().multiply(i+1)), null, 1, 0, ViewDist.LONG);
		}
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		_arrowMap.put(arrow, System.currentTimeMillis() + 2000);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
		
		for (Iterator<Entry<Arrow, Long>> it = _arrowMap.entrySet().iterator(); it.hasNext();)
		{
			Entry<Arrow, Long> e = it.next();
			if(e.getValue() <= System.currentTimeMillis())
			{
				it.remove();
				continue;
			}
			
			Location loc = e.getKey().getLocation().add(0, 1, 0);
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
	}

}
