package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.classcombat.Skill.SkillActiveCharge;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Rupture extends SkillActiveCharge
{
	private HashSet<Item> _items = new HashSet<Item>();

	private WeakHashMap<Player, Location> _target = new WeakHashMap<Player, Location>();

	public Rupture(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
			int cost, int levels, 
			int energy, int energyMod, 
			long recharge, long rechargeMod, boolean rechargeInform, 
			Material[] itemArray, 
			Action[] actionArray) 
	{
		super(skills, name, classType, skillType,
				cost, levels,
				energy, energyMod, 
				recharge, rechargeMod, rechargeInform, 
				itemArray,
				actionArray,
				0.01f, 0.005f);

		SetDesc(new String[] 
				{
				"Hold Block to create a rupture",
				"at your feet. It will snake through",
				"the ground with #1#0.1 velocity, moving",
				"towards where you are looking,",
				"giving Slow 2 to nearby opponents.",
				"",
				GetChargeString(),
				"",
				"Release Block to release the rupture,",
				"causing earth and players to fly upward,",
				"dealing up to #4#1 initial damage."
				
				});
	}

	@Override
	public String GetEnergyString()
	{
		return "Energy: #20#-1 per Second";
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}

		if (!UtilEnt.isGrounded(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " while airborne."));
			return false;
		}

		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		_target.put(player, player.getLocation().subtract(0, 1, 0));
		_charge.put(player, 0f);
	}
	
	public void Clean(Player player)
	{
		_target.remove(player);
		_charge.remove(player);
	}

	@EventHandler
	public void UpdateSlow(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : _target.keySet())
		{
			Location loc = _target.get(cur);

			for (Player other : cur.getWorld().getPlayers())
				if (!other.equals(cur))
					if (UtilMath.offset(loc, other.getLocation()) < 2)
						if (Factory.Relation().canHurt(cur, other))
							Factory.Condition().Factory().Slow(GetName(), other, cur, 1.9, 1, false, true, false, true);
		}		
	}

	@EventHandler
	public void UpdateMove(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			//Not Charging
			if (!_target.containsKey(cur))
				continue;

			//Level
			int level = getLevel(cur);
			if (level == 0)			return;

			//Add Charge
			if (!UtilPlayer.isBlocking(cur))
				DoRupture(cur);

			else
			{
				//Energy
				if (!Factory.Energy().Use(cur, GetName(), 1 - (0.05 * level), true, true))
				{
					DoRupture(cur);
					continue;
				}

				Charge(cur);

				MoveRupture(cur);
			}
		}
	}

	public void MoveRupture(Player cur) 
	{
		Block targetBlock = cur.getTargetBlock((HashSet<Byte>)null, 150);
		if (targetBlock == null)	return;

		//Aiming at sky
		if (targetBlock.getY() == 0)
		{
			//Display
			DisplayRupture(cur);

			return;
		}

		Location target = targetBlock.getLocation().add(0.5, 0.5, 0.5);

		Location loc = _target.get(cur);
		if (loc == null)			return;

		int level = getLevel(cur);
		loc.add(UtilAlg.getTrajectory(loc, target).normalize().multiply(0.30 + (0.03 * level)));

		//Relocate
		RelocateRupture(cur, loc, target);

		//Display
		DisplayRupture(cur);
	}

	public void RelocateRupture(Player cur, Location loc, Location target)
	{
		if (CanTravel(loc.getBlock()))
			return;

		Location bestLoc = null;
		double bestDist = 9999;

		for (Block block : UtilBlock.getInRadius(loc, 1.5d).keySet())
		{
			if (!CanTravel(block))
				continue;

			if (UtilMath.offset(block.getLocation(), target) < bestDist)
			{
				bestLoc = block.getLocation();
				bestDist = UtilMath.offset(block.getLocation(), target);
			}
		}

		if (bestLoc == null)
		{
			UtilPlayer.message(cur, F.main("Skill", "Your " + F.skill(GetName()) + " has failed."));
			Clean(cur);
		}

		else
			_target.put(cur, bestLoc);
	}

	public boolean CanTravel(Block block) 
	{
		return !UtilBlock.liquid(block)
		//return true
		/*int id = block.getTypeId();

		return (id == 1 || 
				id == 2 || 
				id == 3 || 
				id == 12 || 
				id == 13)*/;
	}

	public void DisplayRupture(Player cur)
	{	
		if (_target.get(cur) == null)
			return;

		for (Block block : UtilBlock.getInRadius(_target.get(cur), 1d).keySet())
			if (block.getRelative(BlockFace.UP).getTypeId() == 0 ||
				block.getRelative(BlockFace.DOWN).getTypeId() == 0 ||
				block.getRelative(BlockFace.NORTH).getTypeId() == 0 ||
				block.getRelative(BlockFace.SOUTH).getTypeId() == 0 ||
				block.getRelative(BlockFace.EAST).getTypeId() == 0 ||
				block.getRelative(BlockFace.WEST).getTypeId() == 0)
			{
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
			}

	}

	public void DoRupture(Player player)
	{
		Location loc = _target.get(player);
		float charge = _charge.get(player);

		Clean(player);

		double range = 2 + 2 * charge;
		double mult = 0.5 + 0.5 * charge;

		int level = getLevel(player);

		//Fling
		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(loc, range);
		for (LivingEntity cur : targets.keySet())
		{
			//Velocity
			UtilAction.velocity(cur, 
					UtilAlg.getTrajectory2d(loc.toVector().add(new Vector(0.5,0,0.5)), cur.getLocation().toVector()), 
					0.8 + 0.8 * targets.get(cur) * mult, true, 0, 0.4 + 1.0 * targets.get(cur) * mult, 0.4 + 1.0 * mult, true);

			//Condition
			Factory.Condition().Factory().Falling(GetName(), cur, player, 10, false, true);

			//Inform
			if (cur instanceof Player)
				UtilPlayer.message((Player)cur, F.main(GetClassType().name(), F.name(player.getName()) +" hit you with " + F.skill(GetName(level)) + "."));
			
			//Damage Event
			Factory.Damage().NewDamageEvent(cur, player, null, 
					DamageCause.CUSTOM, 1 + (charge * (3 + level)), false, true, false,
					player.getName(), GetName());
		}

		//Blocks
		int attempts = 0;
		int done = 0;

		Block locBlock = loc.getBlock();

		while (done < charge * 40 && attempts < charge * 200)
		{
			attempts++;

			Vector vec = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize();
			Location side = new Location(loc.getWorld(), loc.getX() + vec.getX(), loc.getY() + vec.getY(), loc.getZ() + vec.getZ());

			if (!UtilBlock.airFoliage(side.getBlock()))
				continue;

			//Add Directional
			vec.add(UtilAlg.getTrajectory(loc.getBlock().getLocation(), side.getBlock().getLocation()));

			//Add Up
			vec.add(new Vector(0, 1.6, 0));

			vec.normalize();

			//Scale 
			vec.multiply(0.1 + 0.3 * Math.random() + 0.6 * charge);

			//Block!
			Item item = loc.getWorld().dropItem(side, ItemStackFactory.Instance.CreateStack(locBlock.getTypeId(), locBlock.getData()));
			item.setVelocity(vec);
			item.setPickupDelay(50000);
			_items.add(item);

			//Effect
			side.getWorld().playEffect(side, Effect.STEP_SOUND, locBlock.getTypeId());

			done++;
		}
	}

	@EventHandler
	public void ItemPickup(PlayerPickupItemEvent event)
	{
		if (_items.contains(event.getItem()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void HopperPickup(InventoryPickupItemEvent event)
	{
		if (_items.contains(event.getItem()))
			event.setCancelled(true);
	}

	@EventHandler
	public void ItemDestroy(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_items.isEmpty())
			return;
		
		Iterator<Item> itemIterator = _items.iterator();

		while (itemIterator.hasNext())
		{
			Item item = itemIterator.next();
			
			if (item.isDead() || !item.isValid())
			{
				item.remove();
				itemIterator.remove();
			}
			else if (UtilEnt.isGrounded(item) || item.getTicksLived() > 60)
			{
				item.getWorld().playEffect(item.getLocation(), Effect.STEP_SOUND, item.getItemStack().getTypeId());
				item.remove();
				itemIterator.remove();
			}
		}
	}

	@Override
	public void Reset(Player player) 
	{
		Clean(player);
	}
}
