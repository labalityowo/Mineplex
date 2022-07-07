package mineplex.core.gadget.gadgets.particle.freedom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.particleeffects.FreedomFireworkEffect;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleFreedom extends ParticleGadget
{

	private Map<UUID, FreedomFireworkEffect> _effects = new HashMap<>();

	public ParticleFreedom(GadgetManager manager)
	{
		super(manager, "Freedom Aura", UtilText.splitLineToArray(UtilText.colorWords("Do you hear that? It's the sound of Freedom swirling around you.",
				ChatColor.RED, ChatColor.WHITE, ChatColor.BLUE), LineFormat.LORE), -8,
				Material.WOOL, (byte) 11);
		setDisplayItem(CountryFlag.USA.getBanner());
	}

	@Override
	public void playParticle(Player player, UpdateEvent updateEvent)
	{

	}

	@Override
	public void startEffect(Player player)
	{
		_effects.put(player.getUniqueId(), new FreedomFireworkEffect(player, true));
		_effects.get(player.getUniqueId()).start();
	}

	@Override
	public void stopEffect(Player player)
	{
		if (_effects.containsKey(player.getUniqueId()))
			_effects.remove(player.getUniqueId()).stop();
	}

}
