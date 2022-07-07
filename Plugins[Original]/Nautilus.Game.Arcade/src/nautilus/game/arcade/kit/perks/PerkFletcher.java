package nautilus.game.arcade.kit.perks;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkFletcher extends Perk
{

	private final Set<Entity> _fletchArrows = new HashSet<>();

	private int _max = 0;
	private int _time = 0;
	private boolean _remove;
	private int _slot;
	private boolean _instant = true;
	private String _name;

	public PerkFletcher()
	{
		this(0, 0, false);
	}

	public PerkFletcher(int time, int max, boolean remove) 
	{
		this(time, max, remove, -1);
	}
	
	public PerkFletcher(int time, int max, boolean remove, boolean instant)
	{
		this(time, max, remove, -1, instant);
	}
	
	public PerkFletcher(int time, int max, boolean remove, int slot)
	{
		this(time, max, remove, slot, true);
	}
	
	public PerkFletcher(int time, int max, boolean remove, int slot, boolean instant)
	{
		this(time, max, remove, slot, instant, "Fletched Arrow");
	}
	
	public PerkFletcher(int time, int max, boolean remove, int slot, boolean instant, String name)
	{
		super("Fletcher", new String[] 
				{
				"Receive 1 Arrow every " + time + " seconds. Maximum of " + max + ".",
				});
		
		_time = time;
		_max = max;
		_remove = remove;
		_slot = slot;
		_instant = instant;
		_name = name;
	}

	@Override
	public void setupValues()
	{
		_time = getPerkInt("Time", _time);
		_max = getPerkInt("Max", _max);
		_remove = getPerkBoolean("Remove", _remove);

		setDesc("Receive 1 Arrow every " + _time + " seconds. Maximum of " + _max + ".");
	}

	public boolean isFletchedArrow(ItemStack stack)
	{
		if (!UtilGear.isMat(stack, Material.ARROW))
		{
			return false;
		}

		ItemMeta meta = stack.getItemMeta();

		return meta.hasDisplayName() && meta.getDisplayName().contains(_name);
	}

	@EventHandler
	public void FletchShootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (!hasPerk(player))
		{
			return;
		}

		for (ItemStack itemStack : player.getInventory().getContents())
		{
			if (isFletchedArrow(itemStack))
			{
				_fletchArrows.add(event.getProjectile());
				((CraftArrow) event.getProjectile()).getHandle().fromPlayer = 0;
				return;
			}
		}
	}

	@EventHandler
	public void FletchProjectileHit(ProjectileHitEvent event)
	{
		if (_remove)
			if (_fletchArrows.remove(event.getEntity()))
				event.getEntity().remove();
	}

	@EventHandler
	public void Fletch(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (Manager.GetGame() == null)
			return;
		
		if (!UtilTime.elapsed(Manager.GetGame().getGameLiveTime(), _time * 1000) && !_instant)
		{
			return;
		}
			
		for (Player cur : UtilServer.getPlayers())
		{
			if (Manager.isSpectator(cur))
				continue;
			
			if (!hasPerk(cur))
				continue;

			if (!Manager.GetGame().IsAlive(cur))
				continue;

			// Enabling this causes a bug that will sometimes prevent players from getting new arrows
			// Believe this bug is related to when a player fires an arrow and gets an arrow in their inventory at the same
			// time which causes their inventory to not be in sync with the server. Best known fix right now
			// is to remove this check or keep calling UtilInv.update()
//			if (UtilPlayer.isChargingBow(cur))
//				continue;

			if (!Recharge.Instance.use(cur, GetName(), _time * 1000, false, false))
				continue;

			if (UtilInv.contains(cur, _name, Material.ARROW, (byte)0, _max))
				continue;

			//Add
			if (_slot == -1)
			{
				cur.getInventory().addItem(getItem(1));
			}
			else
			{
				int amount = 1;
				ItemStack old = cur.getInventory().getItem(_slot);
				if(old != null && old.getType() == Material.ARROW)
				{
					amount += old.getAmount();
				}
				cur.getInventory().setItem(_slot, getItem(amount));
			}			

			cur.playSound(cur.getLocation(), Sound.ITEM_PICKUP, 2f, 1f);
		}
	}

	@EventHandler
	public void FletchDrop(PlayerDropItemEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!isFletchedArrow(event.getItemDrop().getItemStack()))
			return;
		
		if (!hasPerk(event.getPlayer()))
			return;

		//Cancel
		event.setCancelled(true);

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot drop " + F.item(_name) + "."));
	}

	@EventHandler
	public void FletchDeathRemove(PlayerDeathEvent event)
	{
		event.getDrops().removeIf(this::isFletchedArrow);
	}

	@EventHandler
	public void FletchInvClick(InventoryClickEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
		{
			return;
		}
		
		if (!hasPerk((Player) event.getWhoClicked()))
		{
			return;
		}
		UtilInv.DisallowMovementOf(event, _name, Material.ARROW, (byte)0, true);
	}

	@EventHandler
	public void FletchClean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_fletchArrows.removeIf(arrow -> !arrow.isValid());
	}

	public ItemStack getItem(int amount)
	{
		return ItemStackFactory.Instance.CreateStack(262, (byte) 0, amount, F.item(_name));
	}
}