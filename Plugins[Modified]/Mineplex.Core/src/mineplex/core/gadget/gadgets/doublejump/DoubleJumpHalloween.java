package mineplex.core.gadget.gadgets.doublejump;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class DoubleJumpHalloween extends DoubleJumpEffectGadget
{

	private static final int PARTICLE_AMOUNT = 50;

	private HashMap<Player, Long> _playerMap = new HashMap<>();

	public DoubleJumpHalloween(GadgetManager manager)
	{
		super(manager, "Trick-Or-Leap", UtilText.splitLineToArray(C.cGray + "Unfortunately we're all out of candy, so have this instead!", LineFormat.LORE), -9,
				Material.PUMPKIN, (byte) 0);
	}

	@Override
	public void doEffect(Player player)
	{
		_playerMap.put(player, System.currentTimeMillis() + 1000);
		for(int amount = 0; amount < PARTICLE_AMOUNT; amount++)
		{
			Vector r = Vector.getRandom().subtract(Vector.getRandom()).normalize().multiply(2).setY(Math.random() * 0.4);
			Location loc = player.getLocation().add(r).add(0, UtilMath.random(.5, 2.3), 0);
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(Color.ORANGE), loc);
			coloredParticle.display(7);

			r = Vector.getRandom().subtract(Vector.getRandom()).normalize().multiply(2).setY(Math.random() * 0.4);
			loc = player.getLocation().add(r).add(0, 0.3, 0);
			coloredParticle.setLocation(loc);
			coloredParticle.setColor(new DustSpellColor(Color.BLACK));
			coloredParticle.display(5);
		}
		ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(Color.ORANGE), player.getLocation().clone().add(0, .4, 0));
		coloredParticle.display(7);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.FASTEST) return;

		for(Iterator<Map.Entry<Player, Long>> it = _playerMap.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Player, Long> e = it.next();
			if(e.getValue() >= System.currentTimeMillis())
			{
				it.remove();
				continue;
			}
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(Color.ORANGE), e.getKey().getLocation());
			coloredParticle.display(5);
		}
	}

}
