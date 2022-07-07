package mineplex.core.gadget.gadgets.doublejump;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class DoubleJumpEmerald extends DoubleJumpEffectGadget
{
	
	private HashMap<Player, Long> _players = new HashMap<>();

	public DoubleJumpEmerald(GadgetManager manager)
	{
		super(manager, "Green Jump", 
				UtilText.splitLineToArray(C.cGreen + "Harness the ancient emerald energy to leap through the air.", LineFormat.LORE),
				-2, Material.EMERALD, (byte)0);
	}

	@Override
	public void doEffect(Player player)
	{
		_players.put(player, System.currentTimeMillis() + 500);
		float r = 0.5f;
		UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, player.getLocation(), r, r, r, 0, 20, ViewDist.LONGER);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
		
		for (Iterator<Entry<Player, Long>> it = _players.entrySet().iterator(); it.hasNext();)
		{
			Entry<Player, Long> e = it.next();
			if(e.getValue() < System.currentTimeMillis())
			{
				it.remove();
				continue;
			}
			double diff = e.getValue()-System.currentTimeMillis();
			double r = (diff + 100)/400;
			
			Vector v = e.getKey().getVelocity();
			Vector up = UtilAlg.getUp(v);
			Vector left = UtilAlg.getLeft(v);
			
			Location loc = e.getKey().getLocation();
			
			double amount = 3;
			double ticks = 15;
			for(int i = 0; i < amount; i++)
			{
				double rad = Math.PI*2.0;
				rad += i/amount * rad;
				rad += Math.PI*2*(e.getKey().getTicksLived()%ticks)/ticks;
				double l = -Math.sin(rad);
				double u = Math.cos(rad);
				
				Vector vel = v.clone().add(up.clone().multiply(u)).add(left.clone().multiply(l));
				vel.multiply(r);

				UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, loc.clone().add(vel), vel, 1f, 0, ViewDist.LONGER);
			}
		}
	}

}
