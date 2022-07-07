package mineplex.core.gadget.gadgets.particle.christmas;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.particleeffects.ChristmasTreeEffect;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleChristmasTree extends ParticleGadget
{

	private Map<UUID, ChristmasTreeEffect> _effects = new HashMap<>();

	public ParticleChristmasTree(GadgetManager manager)
	{
		super(manager, "Holiday Tree", UtilText.splitLinesToArray(new String[]{C.cGray +
						"There's nothing like a well decorated tree to bring in the Holiday Spirit."}, LineFormat.LORE),
				-16, Material.SAPLING, (byte) 1);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{

	}

	@Override
	public void startEffect(Player player)
	{
		if (!_effects.containsKey(player.getUniqueId()))
		{
			ChristmasTreeEffect christmasTreeEffect = new ChristmasTreeEffect(player, Manager);
			christmasTreeEffect.start();
			_effects.put(player.getUniqueId(), christmasTreeEffect);
		}
	}

	@Override
	public void stopEffect(Player player)
	{
		if (_effects.containsKey(player.getUniqueId()))
		{
			ChristmasTreeEffect christmasTreeEffect = _effects.get(player.getUniqueId());
			christmasTreeEffect.stop();
			_effects.remove(player.getUniqueId());
		}
	}

}
