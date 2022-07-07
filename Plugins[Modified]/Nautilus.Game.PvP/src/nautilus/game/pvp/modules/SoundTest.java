package nautilus.game.pvp.modules;

import java.util.WeakHashMap;

import me.chiss.Core.Module.AModule;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilEvent.ActionType;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SoundTest extends AModule
{
	public boolean _enabled = false;
	
	public WeakHashMap<Player, Sound> _sound = new WeakHashMap<Player, Sound>();
	public WeakHashMap<Player, Integer> _index = new WeakHashMap<Player, Integer>();
	public WeakHashMap<Player, Float> _volume = new WeakHashMap<Player, Float>();
	public WeakHashMap<Player, Float> _pitch = new WeakHashMap<Player, Float>();

	public SoundTest(JavaPlugin plugin) 
	{
		super("Sound Test", plugin);
	}

	//Module Functions
	@Override
	public void enable() 
	{

	}

	@Override
	public void disable() 
	{

	}

	@Override
	public void config() 
	{

	}

	public void populate(Player player)
	{
		if (!_sound.containsKey(player))	_sound.put(player, Sound.AMBIENCE_CAVE);
		if (!_index.containsKey(player))	_index.put(player, 0);
		if (!_volume.containsKey(player))	_volume.put(player, 1f);
		if (!_pitch.containsKey(player))	_pitch.put(player, 1f);
	}
	
	public void playSound(Player player)
	{
		if (!_enabled)
			return;
		
		populate(player);
		
		player.sendMessage("[" + F.elem(_index.get(player)+"/"+Sound.values().length)  + "] " + F.item(_sound.get(player)+"") + " [Volume " + F.count(""+_volume.get(player)) + "] [Pitch " + F.count(""+_pitch.get(player)) + "]");	
		
		player.getWorld().playSound(player.getLocation(), _sound.get(player), _volume.get(player), _pitch.get(player));
	}

	@EventHandler
	public void doSound(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand().getType() != Material.STICK)
			return;
		
		populate(event.getPlayer());
		
		if (Util().Event().isAction(event, ActionType.L))
		{
			_index.put(event.getPlayer(), _index.get(event.getPlayer()) - 1);

			if (_index.get(event.getPlayer()) < 0)
				_index.put(event.getPlayer(), Sound.values().length);
		}

		if (Util().Event().isAction(event, ActionType.R))
		{
			_index.put(event.getPlayer(), _index.get(event.getPlayer()) + 1);

			if (_index.get(event.getPlayer()) >= Sound.values().length)
				_index.put(event.getPlayer(), 0);
		}

		int count = 0;
		for (Sound cur : Sound.values())
		{
			if (count == _index.get(event.getPlayer()))
			{	
				_sound.put(event.getPlayer(), cur);
				break;
			}

			count++;
		}

		playSound(event.getPlayer());
	}

	@EventHandler
	public void doFloatA(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand().getType() != Material.FLINT)
			return;
		
		populate(event.getPlayer());

		if (Util().Event().isAction(event, ActionType.L))
			_volume.put(event.getPlayer(), _volume.get(event.getPlayer()) - 0.1f);

		if (Util().Event().isAction(event, ActionType.R))
			_volume.put(event.getPlayer(), _volume.get(event.getPlayer()) + 0.1f);
		
		if (_volume.get(event.getPlayer()) < 0)
			_volume.put(event.getPlayer(), 0f);
		
		if (_volume.get(event.getPlayer()) > 2)
			_volume.put(event.getPlayer(), 2f);

		playSound(event.getPlayer());
	}

	@EventHandler
	public void doFloatB(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand().getType() != Material.COAL)
			return;
		
		populate(event.getPlayer());

		if (Util().Event().isAction(event, ActionType.L))
			_pitch.put(event.getPlayer(), _pitch.get(event.getPlayer()) - 0.1f);

		if (Util().Event().isAction(event, ActionType.R))
			_pitch.put(event.getPlayer(), _pitch.get(event.getPlayer()) + 0.1f);
		
		if (_pitch.get(event.getPlayer()) < 0)
			_pitch.put(event.getPlayer(), 0f);
		
		if (_pitch.get(event.getPlayer()) > 2)
			_pitch.put(event.getPlayer(), 2f);

		playSound(event.getPlayer());
	}

	@Override
	public void commands() {
		AddCommand("soundtest");
	}

	@Override
	public void command(Player caller, String cmd, String[] args) {
		_enabled = !_enabled;
		UtilServer.broadcast("Sound Test: " + _enabled);
	}
}
