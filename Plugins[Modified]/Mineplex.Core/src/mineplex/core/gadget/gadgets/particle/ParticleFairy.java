package mineplex.core.gadget.gadgets.particle;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.gadget.GadgetManager;

public class ParticleFairy extends ParticleGadget
{
	private HashMap<Player, ParticleFairyData> _fairy = new HashMap<Player, ParticleFairyData>();

	public ParticleFairy(GadgetManager manager)
	{
		super(manager, "Flame Fairy", new String[]{ C.cGray + "HEY! LISTEN!", C.cGray + "HEY! LISTEN!", C.cGray + "HEY! LISTEN!", }, -2,
		        Material.BLAZE_POWDER, (byte) 0);
	}

	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		// Create
		if (!_fairy.containsKey(player)) _fairy.put(player, new ParticleFairyData(player));

		ParticleFairyData data = _fairy.get(player);

		if (!data.Fairy.getWorld().equals(player.getWorld()))
		{
			data.Fairy = null;
			data.Player = null;
			data.Target = null;
			data = new ParticleFairyData(player);
			_fairy.put(player, data);
		}

		data.Update();
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		if (_active.remove(player)) UtilPlayer.message(player, F.main("Gadget", "You unsummoned " + F.elem(getName()) + "."));

		clean(player);
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
		clean(event.getPlayer());
	}

	private void clean(Player player)
	{
		_fairy.remove(player);
	}
}
