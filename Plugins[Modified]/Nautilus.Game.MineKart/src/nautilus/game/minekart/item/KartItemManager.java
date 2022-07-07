package nautilus.game.minekart.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.minekart.gp.GPBattle;
import nautilus.game.minekart.item.KartItemActive.ActiveType;
import nautilus.game.minekart.item.control.Collision;
import nautilus.game.minekart.item.control.Movement;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartManager;
import nautilus.game.minekart.kart.crash.Crash_Explode;

public class KartItemManager extends MiniPlugin
{
	public KartManager KartManager;

	private HashMap<Integer, ArrayList<KartItemType>> _itemSelection = new HashMap<Integer, ArrayList<KartItemType>>();
	
	private HashSet<KartItemActive> _kartItems = new HashSet<KartItemActive>();
	private HashSet<KartItemEntity> _worldItems = new HashSet<KartItemEntity>();

	public KartItemManager(JavaPlugin plugin, KartManager kartManager)
	{
		super("Kart Item Manager", plugin);
		
		KartManager = kartManager;
	}

	@EventHandler
	public void UseItem(PlayerDropItemEvent event)
	{
		Kart kart = KartManager.GetKart(event.getPlayer());
		if (kart == null)	return;

		event.setCancelled(true);

		KartItemActive active = kart.GetItemActive();
		if (active != null)
		{
			if (active.Use())
			{
				//Depleted
				kart.SetItemActive(null);

				//Due to Auto-Activate
				ItemDecrement(kart);

				GetKartItems().remove(active);
				
				//Sound
				kart.GetDriver().playSound(kart.GetDriver().getLocation(), kart.GetKartType().GetSoundMain(), 2f, 1f);
			}

			return;
		}

		KartItemType type = kart.GetItemStored(); 
		if (type != null)	
		{
			if (kart.GetItemCycles() > 10)
			{
				kart.SetItemCycles(kart.GetItemCycles() - 3);
				return;
			}
			else if (kart.GetItemCycles() <= 0)
			{
				type.GetAction().Use(this, kart);

				//Sound
				kart.GetDriver().playSound(kart.GetDriver().getLocation(), kart.GetKartType().GetSoundMain(), 2f, 1f);
			}
		}
	}

	@EventHandler
	public void CancelPickup(PlayerPickupItemEvent event)
	{
		for (KartItemEntity item : GetWorldItems())
			if (item.GetEntity() != null && item.GetEntity().equals(event.getItem()))
			{
				event.setCancelled(true);
				return;
			}

		//Not Map Item
		event.setCancelled(true);
		event.getItem().remove();
	}

	@EventHandler
	public void CancelTarget(EntityTargetEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void EggHit(EntityDamageEvent event)
	{
		if (!(event instanceof EntityDamageByEntityEvent))
			return;

		EntityDamageByEntityEvent eventEE = (EntityDamageByEntityEvent)event;

		if (!(eventEE.getDamager() instanceof Egg))
			return;

		Egg egg = (Egg)eventEE.getDamager();

		if (egg.getShooter() == null)
			return;

		if (!(egg.getShooter() instanceof Player))
			return;

		if (!(event.getEntity() instanceof Player))
			return;

		Player damager = (Player)egg.getShooter();
		Player damagee = (Player)event.getEntity();

		Kart kart = KartManager.GetKart(damagee);
		if (kart == null)	return;

		new Crash_Explode(kart, 0.3f, false);

		//Inform
		if (damager.equals(damagee))
		{
			UtilPlayer.message(damagee, 	F.main("MK", "You hit yourself with " + F.item("Egg Blaster") + "."));
		}
		else
		{
			UtilPlayer.message(damagee, 	F.main("MK", F.elem(damager.getName()) + " hit you with " + F.item("Egg Blaster") + "."));
			UtilPlayer.message(damager, 	F.main("MK", "You hit " + F.elem(damagee.getName()) + " with " + F.item("Egg Blaster") + "."));
		}
	}

	@EventHandler
	public void KartItemCycle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		for (Kart kart : KartManager.GetKarts().values())	
		{
			if (kart.GetItemCycles() > 0)
			{
				//Selected
				if (kart.GetItemCycles() < 10)
				{
					//End Sound
					if (kart.GetItemCycles()%3 == 0)
						kart.GetDriver().playSound(kart.GetDriver().getLocation(), Sound.NOTE_PLING, 0.4f, 2f);
				}
				//Random
				else
				{
					//Sound
					kart.GetDriver().playSound(kart.GetDriver().getLocation(), Sound.NOTE_PLING, 0.2f, 1f + (10 - (kart.GetItemCycles()%10)) *0.05f);

					KartItemType next = GetNewItem(kart);

					//Ensure it doesnt cycle item twice in a row
					while (next.equals(kart.GetItemStored())) 
						next = GetNewItem(kart);

					kart.SetItemStored(next);
				}

				kart.SetItemCycles(kart.GetItemCycles() - 1);

				//Auto Activate 
				if (kart.GetItemCycles() == 0)
				{
					if (kart.GetItemStored() == KartItemType.Banana ||
						kart.GetItemStored() == KartItemType.BananaBunch ||
						kart.GetItemStored() == KartItemType.SingleGreenShell ||
						kart.GetItemStored() == KartItemType.DoubleGreenShell ||
						kart.GetItemStored() == KartItemType.TripleGreenShell ||
						kart.GetItemStored() == KartItemType.SingleRedShell ||
						kart.GetItemStored() == KartItemType.DoubleRedShell ||
						kart.GetItemStored() == KartItemType.TripleRedShell)
					{
						kart.GetItemStored().GetAction().Use(this, kart);
					}
				}
			}
			//Remove Stored, if active is gone
			else if (kart.GetItemActive() == null && kart.GetItemStored() != null)
			{
				ItemDecrement(kart);
			}
		}
	}

	public void ItemDecrement(Kart kart)
	{
		if (kart.GetItemStored() == KartItemType.Banana ||
			kart.GetItemStored() == KartItemType.SingleGreenShell ||	
			kart.GetItemStored() == KartItemType.SingleRedShell)
		{
			kart.SetItemStored(null);
		}

		if (kart.GetItemStored() == KartItemType.BananaBunch)
		{
			ItemStack item = kart.GetDriver().getInventory().getItem(3);
			
			if (item == null || item.getAmount() == 1)
			{
				kart.SetItemStored(null);
			}
			else
			{
				item.setAmount(item.getAmount() - 1);
				kart.GetItemStored().GetAction().Use(this, kart);
			}
		}
		
		if (kart.GetItemStored() == KartItemType.DoubleGreenShell)
		{
			kart.SetItemStored(KartItemType.SingleGreenShell);
			kart.GetItemStored().GetAction().Use(this, kart);
		}

		if (kart.GetItemStored() == KartItemType.TripleGreenShell)
		{
			kart.SetItemStored(KartItemType.DoubleGreenShell);
			kart.GetItemStored().GetAction().Use(this, kart);
		}

		if (kart.GetItemStored() == KartItemType.DoubleRedShell)
		{
			kart.SetItemStored(KartItemType.SingleRedShell);
			kart.GetItemStored().GetAction().Use(this, kart);
		}

		if (kart.GetItemStored() == KartItemType.TripleRedShell)
		{
			kart.SetItemStored(KartItemType.DoubleRedShell);
			kart.GetItemStored().GetAction().Use(this, kart);
		}
	}

	@EventHandler
	public void KartItemUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (KartItemActive active : GetKartItems())	
		{
			if (active.GetType() == ActiveType.Behind)
				Movement.Behind(active.GetKart(), active.GetEntities());

			if (active.GetType() == ActiveType.Orbit)
				Movement.Orbit(active.GetKart(), active.GetEntities());

			if (active.GetType() == ActiveType.Trail)
				Movement.Trail(active.GetKart(), active.GetEntities());	
		}	
	}

	@EventHandler
	public void WorldItemUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		HashSet<KartItemEntity> remove = new HashSet<KartItemEntity>();

		for (KartItemEntity item : GetWorldItems())
		{		
			if (item.GetTrack() == null || item.GetTrack().GetWorld() == null)
			{
				remove.add(item);
				continue;
			}

			if (remove.contains(item))
				continue;

			if (item.TickUpdate())
				remove.add(item);

			//Collide with Items
			KartItemEntity other = Collision.CollideItem(item, GetWorldItems());
			if (other != null)
			{
				remove.add(item);
				remove.add(other);
			}

			//Collide with Players
			if (Collision.CollidePlayer(item, KartManager.GetKarts().values()))
				remove.add(item);
		}

		for (KartItemEntity item : remove)
		{
			if (item.GetHost() != null)
			{
				item.GetHost().GetEntities().remove(item);

				if (item.GetHost().GetEntities().isEmpty())
				{
					item.GetHost().GetKart().SetItemActive(null);
					GetKartItems().remove(item.GetHost());
				}			
			}

			item.Clean();

			_worldItems.remove(item);
		}
	}

	public void RegisterKartItem(KartItemActive item)
	{
		_kartItems.add(item);
	}

	public void RegisterWorldItem(KartItemEntity item)
	{
		_worldItems.add(item);
	}

	public HashSet<KartItemActive> GetKartItems()
	{
		return _kartItems;
	}

	public HashSet<KartItemEntity> GetWorldItems()
	{
		return _worldItems;
	}
	
	public KartItemType GetNewItem(Kart kart)
	{
		if (Math.random() > 1 - kart.GetKartType().GetKartItem().GetChance())
			return kart.GetKartType().GetKartItem();
		
		int pos = kart.GetLapPlace();
		
		if (kart.GetGP() instanceof GPBattle)
			pos = -1;
		
		if (!_itemSelection.containsKey(pos))
			_itemSelection.put(pos, KartItemType.GetItem(pos));
		
		ArrayList<KartItemType> itemBag = _itemSelection.get(pos);
					
		return itemBag.get(UtilMath.r(itemBag.size()));
	}
}
