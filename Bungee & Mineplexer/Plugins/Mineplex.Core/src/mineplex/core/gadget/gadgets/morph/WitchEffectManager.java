package mineplex.core.gadget.gadgets.morph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mineplex.core.common.util.UtilServer;
import mineplex.core.particleeffects.WitchParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;

public class WitchEffectManager
{

	private static Map<Player, WitchEffectManager> _managers = new HashMap<>();

	private Player _player;
	private Bat _bat;
	private Location _location;
	private long _started = -1;
	private WitchParticleEffect _witchParticleEffect;

	public WitchEffectManager(Player player, Location location)
	{
		_player = player;
		_location = location;
		_managers.put(player, this);
	}

	public void start()
	{
		_started = System.currentTimeMillis();
		_location.getBlock().setType(Material.CAULDRON);
		_location.getBlock().setData((byte) 3);

		_location.getWorld().strikeLightningEffect(_location);
		Bat bat = _location.getWorld().spawn(_location.clone().add(0, 1, 0), Bat.class);
		bat.setCustomName(_player.getName() + "'s Magical Bat");
		bat.setCustomNameVisible(true);
		_bat = bat;

		_witchParticleEffect = new WitchParticleEffect(_location);
		_witchParticleEffect.start();
	}

	public void stop()
	{
		_bat.remove();
		_location.getBlock().setType(Material.AIR);
		_managers.remove(_player);
		_witchParticleEffect.stop();
	}

	public boolean hasStarted()
	{
		return _started != -1;
	}

	public long getStarted()
	{
		return _started;
	}

	public static Set<Player> getPlayers()
	{
		return _managers.keySet();
	}

	public static WitchEffectManager getManager(Player player)
	{
		if (_managers.containsKey(player))
		{
			return _managers.get(player);
		}
		return null;
	}

}
