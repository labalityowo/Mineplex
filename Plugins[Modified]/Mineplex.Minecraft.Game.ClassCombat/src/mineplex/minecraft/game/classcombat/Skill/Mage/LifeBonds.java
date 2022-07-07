package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class LifeBonds extends Skill
{

	private final Set<Player> _active = new HashSet<>();
	private final Set<Item> _items = new HashSet<>();
	private final Set<LifeBondsData> _hearts = new HashSet<>();

	public LifeBonds(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Drop Axe/Sword to Toggle.",
				"",
				"Transfers life from yourself to",
				"nearby allies with less health.",
				"",
				"Transfers #0.5#0.5 health every second.",
				"Maximum range of #3#3 Blocks from user."
				});
	}

	@Override
	public String GetEnergyString()
	{
		return "Energy: #9#-1 per Second";
	}

	@EventHandler
	public void Toggle(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (getLevel(player) == 0)				
			return;

		if (!UtilGear.isWeapon(event.getItemDrop().getItemStack()))
			return;

		event.setCancelled(true);

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		UtilServer.getServer().getPluginManager().callEvent(trigger);
		if (trigger.IsCancelled())
			return;

		if (_active.contains(player))
		{
			Remove(player);	
		}
		else
		{
			if (!Factory.Energy().Use(player, "Enable " + GetName(), 10, true, true))
				return;

			Add(player);
		}
	}

	public void Add(Player player)
	{
		_active.add(player);
		UtilPlayer.message(player, F.main(GetClassType().name(), GetName() + ": " + F.oo("Enabled", true)));
	}

	public void Remove(Player player)
	{
		_active.remove(player);
		UtilPlayer.message(player, F.main(GetClassType().name(), GetName() + ": " + F.oo("Disabled", false)));
	}

	@EventHandler
	public void Energy(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{	
			if (!_active.contains(cur))
				continue;

			//Level
			int level = getLevel(cur);
			if (level == 0)
			{
				Remove(cur);	
				continue;
			}

			//Check Allowed
			SkillTriggerEvent trigger = new SkillTriggerEvent(cur, GetName(), GetClassType());
			UtilServer.getServer().getPluginManager().callEvent(trigger);
			if (trigger.IsCancelled())
			{
				Remove(cur);
				continue;
			}

			//Energy
			if (!Factory.Energy().Use(cur, GetName(), 0.45 - (level * 0.05), true, true))
			{
				_active.remove(cur);
				UtilPlayer.message(cur, F.main(GetClassType().name(), GetName() + ": " + F.oo("Disabled", false)));
			}
		}
	}

	@EventHandler
	public void Plants(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		for (Player cur : GetUsers())
		{	
			if (!_active.contains(cur))
				continue;

			for (Player other : UtilPlayer.getNearby(cur.getLocation(), 8))
			{
				if (Factory.Relation().canHurt(cur, other) && !other.equals(cur))
					continue;

				//Plants
				ItemStack stack;

				double r = Math.random();

				if (r > 0.4)		stack = ItemStackFactory.Instance.CreateStack(31, (byte)1);
				else if (r > 0.11)	stack = ItemStackFactory.Instance.CreateStack(31, (byte)2);
				else if (r > 0.05)	stack = ItemStackFactory.Instance.CreateStack(37, (byte)0);
				else				stack = ItemStackFactory.Instance.CreateStack(38, (byte)0);

				Item item = other.getWorld().dropItem(other.getLocation().add(0, 0.4, 0), stack);
				_items.add(item);

				Vector vec = new Vector(Math.random() - 0.5, Math.random()/2 + 0.2, Math.random() - 0.5).normalize();
				vec.multiply(0.1 + Math.random()/8);
				item.setVelocity(vec);
			}
		}
	}

	@EventHandler
	public void LifeTransfer(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player cur : GetUsers())
		{	
			if (!_active.contains(cur))
				continue;
			
			int level = getLevel(cur);
			
			//Bonds
			Player lowest = null;
			double lowestHp = 20;

			for (Player other : UtilPlayer.getNearby(cur.getLocation(), 6 + (2 * level)))
			{
				if (Factory.Relation().canHurt(cur, other) && !other.equals(cur))
					continue;

				if (lowest == null || other.getHealth() < lowestHp)
				{
					lowest = other;
					lowestHp = other.getHealth();
				}
			}
			
			//Nothing to Transfer
			if (cur.equals(lowest) || cur.getHealth() - lowestHp < 2)
				continue;

			double amount = 0.5 + (0.5 * level);
			
			amount = Math.min((cur.getHealth() - lowestHp) / 2d, amount);
			
			//Steal
			UtilPlayer.health(cur, -amount * .25);

			//Hearts
			_hearts.add(new LifeBondsData(cur.getLocation().add(0, 0.8, 0), lowest, amount));
		}
	}
	
	@EventHandler
	public void Hearts(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		_hearts.removeIf(LifeBondsData::update);
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

		_items.removeIf(cur ->
		{
			if (UtilEnt.isGrounded(cur) || cur.getTicksLived() > 40 || cur.isDead() || !cur.isValid())
			{
				Block block = cur.getLocation().getBlock();
				if (block.getType() == Material.AIR)
				{
					int below = block.getRelative(BlockFace.DOWN).getTypeId();
					if (below == 2 || below == 3)
					{
						byte data = 0;
						if (cur.getItemStack().getData() != null)
							data = cur.getItemStack().getData().getData();

						Factory.BlockRestore().add(block, cur.getItemStack().getTypeId(), data, 2000);
					}
				}

				cur.remove();
				return true;
			}

			return false;
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.IsCancelled() || event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damagee = event.GetDamageePlayer(), damager = event.GetDamagerPlayer(false);

		if (damagee == null || damager == null)
		{
			return;
		}

		int level = getLevel(damager);

		if (level == 0 || !_active.contains(damager))
		{
			return;
		}

		UtilPlayer.health(damager, 0.25 + 0.25 * level);
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
	}

	private class LifeBondsData
	{

		Location _loc;
		Player _target;
		double _health;

		LifeBondsData(org.bukkit.Location loc, Player target, double amount)
		{
			_loc = loc;
			_target = target;
			_health = amount;
		}

		boolean update()
		{
			if (!_target.isValid() || !_target.isOnline())
				return true;

			if (UtilMath.offsetSquared(_loc, _target.getLocation()) < 1)
			{
				UtilPlayer.health(_target, _health);
				return true;
			}

			_loc.add(UtilAlg.getTrajectory(_loc, _target.getLocation().add(0, 0.8, 0)).multiply(2.5));
			UtilParticle.PlayParticleToAll(ParticleType.HEART, _loc, 0, 0, 0, 0, 1, ViewDist.LONG);
			return false;
		}

	}
}
