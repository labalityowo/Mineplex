package nautilus.game.arcade.game.games.halloween2016;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;

import net.minecraft.server.v1_8_R3.MinecraftServer;

public class Crypt
{
	
	private int _maxHealth = 2000;
	private int _health = _maxHealth;
	
	private List<Schematic> _states;
	private int _stateIndex = 0;
	private Location _schematicBase;
	
	private Map<Entity, Integer> _damageCooldown = new HashMap<>();
	
	private Halloween2016 _host;

	public Crypt(Halloween2016 host, Location base, List<Schematic> states)
	{
		_host = host;
		_states = new ArrayList<>();
		_states.addAll(states);
		_schematicBase = base.clone();
	}
	
	public boolean tryDamage(Entity mob, int damage, int cooldown)
	{
		if(isDestroyed()) return false;
		Integer lastTime = _damageCooldown.get(mob);
		if(lastTime != null && lastTime > MinecraftServer.currentTick) return false;
		
		_health -= damage;
		_damageCooldown.put(mob, MinecraftServer.currentTick + cooldown);
		
		updateState(damage);
		
		return true;
	}
	
	public void setHealth(int health)
	{
		int diff = _health-health;
		_health = health;
		updateState(diff);
	}
	
	public void updateHealthDisplay()
	{
		UtilTextTop.displayProgress(C.cRed + C.Bold + "Crypt", getHealthProgress(), UtilServer.getPlayers());
	}
	
	public float getHealthProgress()
	{
		return Math.max(0, _health/ (float) _maxHealth);
	}
	
	public int getMaxHealth()
	{
		return _maxHealth;
	}
	
	public int getHealth()
	{
		return _health;
	}
	
	public int getStateIndex()
	{
		return _stateIndex;
	}
	
	public Location getSchematicBase()
	{
		return _schematicBase.clone();
	}
	
	public void updateState(int damage)
	{	
		float prevProg = (damage + _health) / (float) _maxHealth;
		float progress = getHealthProgress();
		
		int state = (int) (_states.size() * (1-progress));
		if(state != _stateIndex && state < _states.size())
		{
			_stateIndex = state;
			_states.get(state).paste(_schematicBase, false, true);
		}
		
		int iprog = (int) Math.ceil(progress*100);
		int iprevProg = (int) Math.ceil(prevProg*100);
		
		if((iprog%10 == 0 || iprog <= 5) && iprevProg > iprog)
		{
			String color = C.cGreen;
			if(iprog <= 60) color = C.cYellow;
			if(iprog <= 30) color = C.cGold;
			if(iprog <= 10) color = C.cRed;
			
			if(iprog == 0)
			{
				_host.Announce(F.main("Crypt", "The crypt has been destroyed!"), true);
			}
			else
			{
				_host.Announce(F.main("Crypt", "The crypt only has " + color + C.Bold + iprog + "%" + C.mBody + " left!"), true);
			}
			
			if(iprog == 0)
			{
				for(Location loc : _host.getInfrontOfDoorTargets())
				{
					loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 0);
					loc.getWorld().strikeLightningEffect(loc);
				}
			}
		}
	}
	
	public boolean isDestroyed()
	{
		return _health <= 0;
	}

}
