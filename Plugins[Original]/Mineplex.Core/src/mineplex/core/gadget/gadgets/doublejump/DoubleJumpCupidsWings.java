package mineplex.core.gadget.gadgets.doublejump;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class DoubleJumpCupidsWings extends DoubleJumpEffectGadget
{
	
	private HashMap<Player, Long> _playerMap = new HashMap<>();

	public DoubleJumpCupidsWings(GadgetManager manager)
	{
		super(manager, "Cupid's Wings",
				UtilText.splitLineToArray("Take flight on the wings of love!", LineFormat.LORE),
				-2, Material.APPLE, (byte)0, "Wings of Love");
	}

	@Override
	public void doEffect(Player player)
	{
		_playerMap.put(player, System.currentTimeMillis()+1000);
		for(int amount = 30; amount > 0; amount--)
		{
			Vector r = Vector.getRandom().subtract(Vector.getRandom()).normalize().multiply(2).setY(Math.random()*0.4);
			Location loc = player.getLocation().add(r).add(0, 0.3, 0);
			UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, loc, UtilColor.colorToVector(Color.RED), 1, 0, ViewDist.NORMAL);
			
			r = Vector.getRandom().subtract(Vector.getRandom()).normalize().multiply(2).setY(Math.random()*0.4);
			loc = player.getLocation().add(r).add(0, 0.3, 0);
			UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, loc, UtilColor.colorToVector(Color.fromRGB(16738740)), 1, 0, ViewDist.NORMAL);
		}
		UtilParticle.PlayParticleToAll(ParticleType.HEART, player.getLocation().add(0, 0.4, 0), 0.8f, 0.4f, 0.8f, 0, 10, ViewDist.NORMAL);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.FASTEST) return;
		
		for(Iterator<Entry<Player, Long>> it = _playerMap.entrySet().iterator(); it.hasNext();)
		{
			Entry<Player, Long> e = it.next();
			if(e.getValue() >= System.currentTimeMillis()) 
			{
				it.remove();
				continue;
			}
			UtilParticle.PlayParticleToAll(ParticleType.HEART, e.getKey().getLocation(), 0.3f, 0.3f, 0.3f, 0, 1, ViewDist.NORMAL);
			
		}
	}

}
