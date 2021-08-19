package nautilus.game.arcade.game.games.halloween2016;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import nautilus.game.arcade.game.games.halloween2016.creatures.MobPumpling;

public class PumpkinPlant
{
	private static final int GROWING_TICKS = 30 * 20; // 30 seconds
	private static final int GLOW_TRANSITION_TIME = (int)Math.floor(GROWING_TICKS * .75);
	
	private Halloween2016 _game;
	private int _age;
	private Block _growing = null;
	private int _health;
	
	public PumpkinPlant(Halloween2016 game, Location loc)
	{
		_growing = loc.getBlock();
		_game = game;
	}
	
	public void startGrow()
	{
		if(isGrowing()) return;
		
		_growing.setType(Material.PUMPKIN);
		
		_health = 3;
	}
	
	public boolean isGrowing()
	{
		return _growing != null &&_growing.getType() != Material.AIR;
	}
	
	public void tick()
	{
		if(!isGrowing()) return;

		_age++;

		if (_age == GLOW_TRANSITION_TIME)
		{
			_growing.setType(Material.JACK_O_LANTERN);
		}

		if (_age == GROWING_TICKS)
		{
			spawn();
		}
	}
	
	public void spawn()
	{
		_growing.setType(Material.AIR);

		Location loc = _growing.getLocation().add(0.5, 0, 0.5);
		
		_game.AddCreature(new MobPumpling(_game, loc));
		
		loc.getWorld().playEffect(loc, Effect.LARGE_SMOKE, 4);
	}
	
	public void hit(Block block)
	{
		if(block.equals(_growing))
		{
			_health--;
			
			Location loc = _growing.getLocation().add(0.5, 0, 0.5);
			
			if(_health <= 0)
			{
				_growing.setType(Material.AIR);

				loc.getWorld().playEffect(loc, Effect.TILE_BREAK, new MaterialData(Material.PUMPKIN));
				
				_growing = null;
			}
			else
			{
				loc.getWorld().playSound(loc, Sound.DIG_WOOL, 1, 1);
			}
		}
	}

}
