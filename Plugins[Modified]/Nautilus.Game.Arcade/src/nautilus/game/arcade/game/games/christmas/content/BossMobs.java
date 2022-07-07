package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.games.christmas.parts.Part5;
import net.minecraft.server.v1_8_R3.EntityCreature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

public class BossMobs 
{
	private Part5 Host;

	private boolean _active = false;
	private int _difficulty = 0;
	
	private ArrayList<Location> _spawns;
	private long _lastSpawn;
	
	private HashMap<Creature, Player> _ents = new HashMap<Creature, Player>();
	
	public BossMobs(Part5 host, ArrayList<Location> spawns)
	{
		Host = host;
		
		_spawns = spawns;
	}
	
	public void SetActive(boolean active, int difficulty)
	{
		_active = active;
		_difficulty = difficulty;
	}
	
	public void Update()
	{
		MoveDieHit();

		if (!_active)
			return;

		//Timer
		if (!UtilTime.elapsed(_lastSpawn, 1500 - 250 * _difficulty))
			return;
		_lastSpawn = System.currentTimeMillis();
		
		//Spawn
		Host.Host.CreatureAllowOverride = true;
		Creature ent = UtilAlg.Random(_spawns).getWorld().spawn(UtilAlg.Random(_spawns), Skeleton.class);
		Host.Host.CreatureAllowOverride = false;
		
		//Weapon
		double r = Math.random();
		if (r > 0.66)		ent.getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
		else if (r > 0.33)	ent.getEquipment().setItemInHand(new ItemStack(Material.IRON_AXE));
		else 				ent.getEquipment().setItemInHand(new ItemStack(Material.BOW));
		
		ent.setHealth(5);
		
		//Add
		_ents.put(ent, null);
	}

	private void MoveDieHit() 
	{
		Iterator<Creature> entIterator = _ents.keySet().iterator();

		//Move & Die
		while (entIterator.hasNext())
		{
			Creature ent = entIterator.next();

			//Get Target
			Player target = _ents.get(ent);
			if (target == null || !target.isValid() || !Host.Host.IsAlive(target))
			{
				if (Host.Host.GetPlayers(true).size() > 0)
				{
					target = UtilAlg.Random(Host.Host.GetPlayers(true));
					_ents.put(ent, target);
				}
				else
				{
					continue;
				}
			}
				

			//Move
			EntityCreature ec = ((CraftCreature)ent).getHandle();
			ec.getControllerMove().a(target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ(), 1.2 + 0.3 * _difficulty);

			//Remove
			if (!ent.isValid() )
			{
				ent.remove();
				entIterator.remove();
			}
		}
	}
	
	public void Clean()
	{
		for (Creature ent : _ents.keySet())
			ent.remove();
		
		_ents.clear();
	}
}
