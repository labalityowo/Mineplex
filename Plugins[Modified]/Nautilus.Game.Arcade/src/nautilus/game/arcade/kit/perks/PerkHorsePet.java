package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.kit.Perk;

public class PerkHorsePet extends Perk
{
	private static final ItemStack SADDLE_ITEM = new ItemBuilder(Material.SADDLE).setTitle(C.cGreenB + "Saddle").build();

	private final Map<Player, Horse> _horseMap = new HashMap<>();
	private final Map<Player, Long> _deathTime = new HashMap<>();
	
	public PerkHorsePet()
	{
		super("Horse Master", new String[] 
				{
				C.cGray + "You have a loyal horse companion.",
				});
	}

	@Override
	public void Apply(Player player) 
	{
		if (Manager.GetGame().InProgress())
		{
			Manager.runSyncLater(() -> spawnHorse(player, false), 35 * 20);
		}
	}

	@Override
	public void unregisteredEvents()
	{
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			despawnHorse(player);
		}
	}

	public void spawnHorse(Player player, boolean baby)
	{
		if (!Manager.GetGame().IsAlive(player))
			return;
		
		Manager.GetGame().CreatureAllowOverride = true;

		Horse horse = UtilVariant.spawnHorse(player.getLocation(), Variant.HORSE);
		horse.setAdult();
		horse.setAgeLock(true);
		horse.setColor(Color.BROWN);
		horse.setStyle(Style.NONE);
		horse.setOwner(player);
		horse.setMaxDomestication(1);
		horse.setJumpStrength(1);
		horse.getInventory().setSaddle(SADDLE_ITEM);
		horse.setMaxHealth(40);
		horse.setHealth(40);
		
		UtilEnt.vegetate(horse);
		
		_horseMap.put(player, horse);
		
		horse.getWorld().playSound(horse.getLocation(), Sound.HORSE_ANGRY, 2f, 1f);
		Manager.GetGame().CreatureAllowOverride = false;
	}

	@EventHandler
	public void horseUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (!Manager.GetGame().IsLive())
			return;
		
		//Respawn
		Iterator<Player> respawnIterator = _deathTime.keySet().iterator();
		while (respawnIterator.hasNext())
		{
			Player player = respawnIterator.next();
			
			if (UtilTime.elapsed(_deathTime.get(player), 15000) && hasPerk(player))
			{
				respawnIterator.remove();
				spawnHorse(player, true);
			}
		}
			
		//Update
		Iterator<Player> playerIterator = _horseMap.keySet().iterator();
		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();
			Horse horse = _horseMap.get(player);

			//Dead
			if (!horse.isValid() || horse.isDead())
			{
				horse.getWorld().playSound(horse.getLocation(), Sound.HORSE_DEATH, 1f, 1f);
				_deathTime.put(player, System.currentTimeMillis());
				playerIterator.remove();
				continue;
			}	
			
			//Return to Owner
			if (UtilMath.offset(horse, player) > 3)
			{
				if (UtilMath.offset(horse, player) > 24)
				{
					horse.teleport(player);
					continue;
				}
				
				float speed = Math.min(1f, (float)(UtilMath.offset(horse, player) - 5) / 8f);
				
				UtilEnt.CreatureMove(horse, player.getLocation().add(UtilAlg.getTrajectory(player, horse).multiply(2.5)), 1f + speed);
			}
			
			//Age
			if (horse.getTicksLived() > 900 && !horse.isAdult())
			{
				horse.setAdult();
				horse.getWorld().playSound(horse.getLocation(), Sound.HORSE_ANGRY, 2f, 1f);
				
				UtilPlayer.message(player, F.main("Game", "Your horse is now an adult!"));
				player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 2f);
				
				horse.getInventory().setArmor(new ItemStack(Material.IRON_BARDING));
			}
		}
	}


	@EventHandler
	public void heal(UpdateEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (event.getType() != UpdateType.SLOW)
			return;

		for (Horse horse : _horseMap.values())
		{
			if (horse.getHealth() > 0)
				horse.setHealth(Math.min(horse.getMaxHealth(), horse.getHealth()+1));
		}
	}
	
	@EventHandler
	public void outOfGame(PlayerStateChangeEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
			
		if (event.GetState() == PlayerState.OUT)
		{
			despawnHorse(event.GetPlayer());
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		despawnHorse(event.getEntity());
	}
	
	private void despawnHorse(Player player)
	{
		Horse horse = _horseMap.remove(player);

		if (horse == null)
			return;

		horse.remove();
	}

	@EventHandler
	public void damageRider(CustomDamageEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!(event.GetDamageeEntity() instanceof Horse))
			return;
		
		Horse horse = (Horse)event.GetDamageeEntity();
		
		if (!_horseMap.values().contains(horse))
			return;
		
		if (!horse.isAdult())
			event.SetCancelled("Baby Cancel");
		
		Entity ent = event.GetDamageeEntity().getPassenger();

		if (!(ent instanceof Player))
			return;

		//Damage Event
		Manager.GetDamage().NewDamageEvent((Player)ent, event.GetDamagerEntity(true), event.GetProjectile(), 
				event.GetCause(), event.GetDamage() * 0.5, true, false, false,
				UtilEnt.getName(event.GetDamagerEntity(true)), event.GetReason());	
	}
	
	@EventHandler
	public void mountCancel(PlayerInteractEntityEvent event)
	{
		if (!(event.getRightClicked() instanceof Horse))
			return;
				
		if (!_horseMap.containsValue(event.getRightClicked()))
			return;
		
		if (!Manager.GetGame().IsLive())
		{
			event.setCancelled(true);
			return;
		}

		Player player = event.getPlayer();
		Horse horse = (Horse)event.getRightClicked();
		
		if (horse.getOwner() != null && !horse.getOwner().equals(player))
		{
			UtilPlayer.message(player, F.main("Mount", "This is not your Horse!"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onClick(InventoryClickEvent event)
	{
		Horse horse = _horseMap.get(event.getWhoClicked());
		if (horse != null)
		{
			if (event.getInventory().equals(horse.getInventory()))
			{
				if (UtilInv.shouldCancelEvent(event, item -> UtilItem.isSimilar(item, SADDLE_ITEM, UtilItem.ItemAttribute.NAME)))
				{
					event.setCancelled(true);
				}
			}
		}
	}
}