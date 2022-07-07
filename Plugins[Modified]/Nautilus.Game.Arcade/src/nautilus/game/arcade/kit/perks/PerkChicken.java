package nautilus.game.arcade.kit.perks;

import java.util.Iterator;

import mineplex.core.common.util.C;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.Navigation;
import net.minecraft.server.v1_8_R3.NavigationAbstract;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PerkChicken extends Perk
{

	private NautHashMap<String, Creature> _activeKitHolders = new NautHashMap<String, Creature>();
	private NautHashMap<String, Integer> _failedAttempts = new NautHashMap<String, Integer>();
	
	private long _lastEgg = 0;

	public PerkChicken()
	{
		super("Animal Tamer", new String[]
		{
				"Get a chicken that follows you around",
				"And lays eggs every 8 Seconds!"
		});

	}

	public void spawnChicken(Player player, Location location)
	{

		if (_activeKitHolders.containsKey(player.getName()))
		{
			return;
		}

		Manager.GetGame().CreatureAllowOverride = true;

		Location loc = player.getLocation();
		Chicken c = loc.getWorld().spawn(loc.add(0, 1, 0), Chicken.class);
		c.setRemoveWhenFarAway(false);
		c.setMaxHealth(35.0D);
		c.setAdult();
		
		c.setCustomName(C.cAqua + UtilEnt.getName(player) + "'s Chicken");
		
		c.setCustomNameVisible(true);

		_activeKitHolders.put(player.getName(), c);
		_failedAttempts.put(player.getName(), 0);

		Manager.GetGame().CreatureAllowOverride = false;

	}

	@EventHandler
	public void chickenUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		int xDiff;
		int yDiff;
		int zDiff;

		Iterator<String> ownerIterator = _activeKitHolders.keySet().iterator();

		while (ownerIterator.hasNext())
		{
			String playerName = ownerIterator.next();
			Player owner = Bukkit.getPlayer(playerName);
			
			//Clean
			if (owner == null || !Manager.GetGame().IsAlive(owner))
			{
				Creature chicken = _activeKitHolders.get(playerName);
				chicken.remove();
				
				ownerIterator.remove();
				continue;
			}

			Creature chicken = _activeKitHolders.get(playerName);
			Location chickenSpot = chicken.getLocation();
			Location ownerSpot = owner.getLocation();
			xDiff = Math.abs(chickenSpot.getBlockX() - ownerSpot.getBlockX());
			yDiff = Math.abs(chickenSpot.getBlockY() - ownerSpot.getBlockY());
			zDiff = Math.abs(chickenSpot.getBlockZ() - ownerSpot.getBlockZ());

			if ((xDiff + yDiff + zDiff) > 4)
			{
				EntityCreature ec = ((CraftCreature) chicken).getHandle();
				NavigationAbstract nav = ec.getNavigation();

				int xIndex = -1;
				int zIndex = -1;
				Block targetBlock = ownerSpot.getBlock().getRelative(xIndex,
						-1, zIndex);
				while (targetBlock.isEmpty() || targetBlock.isLiquid())
				{
					if (xIndex < 2)
						xIndex++;
					else if (zIndex < 2)
					{
						xIndex = -1;
						zIndex++;
					}
					else return;

					targetBlock = ownerSpot.getBlock().getRelative(xIndex, -1,
							zIndex);
				}

				float speed = 0.9f;

				if (_failedAttempts.get(playerName) > 4)
				{
					chicken.teleport(owner.getLocation().add(0, 2, 0));
					_failedAttempts.put(playerName, 0);
				}
				else if (!nav.a(targetBlock.getX(), targetBlock.getY() + 1, targetBlock.getZ(), speed))
				{
					if (chicken.getFallDistance() == 0)
					{
						_failedAttempts.put(playerName, _failedAttempts.get(playerName) + 1);
					}
				}
				else
				{
					_failedAttempts.put(playerName, 0);
				}
			}
		}
	}
	
	@EventHandler
	public void dropEggs(UpdateEvent e)
	{
		if (e.getType() != UpdateType.FAST)
		{
			return;
		}
		
		if (!UtilTime.elapsed(_lastEgg, 8000))
			return;
		
		_lastEgg = System.currentTimeMillis();

		Iterator<String> ownerIterator = _activeKitHolders.keySet().iterator();

		while (ownerIterator.hasNext())
		{
			String playerName = ownerIterator.next();
			Player owner = Bukkit.getPlayer(playerName);

			Creature chicken = _activeKitHolders.get(playerName);
			Location chickenSpot = chicken.getLocation();

			Bukkit.getWorld(owner.getWorld().getName())
					.dropItemNaturally(chickenSpot, new ItemStack(Material.EGG))
					.setPickupDelay(30);
		}

	}

	@EventHandler
	public void onChickenDeath(EntityDeathEvent e)
	{

		if (!(e.getEntity() instanceof Chicken))
		{
			return;
		}

		Creature chicken = (Creature) e.getEntity();
		
		Iterator<String> ownerIterator = _activeKitHolders.keySet().iterator();

		while (ownerIterator.hasNext())
		{
			String playerName = ownerIterator.next();

			if (_activeKitHolders.get(playerName).equals(chicken))
			{
				ownerIterator.remove();
			}
		}
	}
}
