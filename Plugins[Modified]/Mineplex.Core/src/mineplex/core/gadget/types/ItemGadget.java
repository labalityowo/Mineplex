package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.ItemGadgetOutOfAmmoEvent;
import mineplex.core.gadget.event.ItemGadgetUseEvent;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;

public abstract class ItemGadget extends Gadget
{

	private final Ammo _ammo;
	protected final long _recharge;

	private boolean _preserveStaticItem;

	public ItemGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data, long recharge, Ammo ammo)
	{
		super(manager, GadgetType.ITEM, name, desc, cost, mat, data);

		_ammo = ammo;
		_recharge = recharge;
		Free = true;
	}

	public abstract void ActivateCustom(Player player);

	public boolean activatePreprocess(Player player)
	{
		return true;
	}

	protected void setPreserveStaticItem()
	{
		_preserveStaticItem = true;
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		ApplyItem(player, message);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		RemoveItem(player, message);
	}

	public void ApplyItem(Player player, boolean inform)
	{
		Manager.removeGadgetType(player, GadgetType.ITEM, this);

		_active.add(player);

		giveItem(player);

		if (inform)
		{
			UtilPlayer.message(player, F.main(Manager.getName(), "You equipped " + F.elem(getName()) + "."));
		}
	}

	@EventHandler
	public void orderThatChest(PlayerDropItemEvent event)
	{
		if (isActive(event.getPlayer()) && event.getItemDrop().getItemStack().getType() == getDisplayMaterial() && !_preserveStaticItem)
		{
			event.setCancelled(true);

			final Player player = event.getPlayer();

			Manager.runSyncLater(() ->
			{
				if (player.isOnline())
				{
					player.getInventory().remove(getDisplayMaterial());
					giveItem(player);
					UtilInv.Update(player);
				}
			}, 1);
		}
	}

	protected void giveItem(Player player)
	{
		player.getInventory().setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(getDisplayMaterial(), getDisplayData(), 1, F.item((isUsingAmmo() ? getAmmoCount(player) + " " : "") + getName())));
	}

	protected void RemoveItem(Player player, boolean message)
	{
		if (_active.remove(player))
		{
			player.getInventory().setItem(Manager.getActiveItemSlot(), null);

			if (message)
				UtilPlayer.message(player, F.main("Gadget", "You unequipped " + F.elem(getName()) + "."));
		}
	}

	public Ammo getAmmo()
	{
		return _ammo;
	}

	public boolean IsItem(Player player)
	{
		return UtilInv.IsItem(player.getItemInHand(), getDisplayMaterial(), getDisplayData());
	}

	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R) || UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!isActive(player) || !IsItem(player))
		{
			return;
		}

		event.setCancelled(true);

		if (!activatePreprocess(player))
		{
			return;
		}

		if (!Manager.selectLocation(this, player.getLocation()))
		{
			Manager.informNoUse(player);
			return;
		}

		//Stock
		if (!hasAmmo(player))
		{
			UtilPlayer.message(player, F.main(Manager.getName(), "You do not have any " + getAmmo().getName() + " left."));

			ItemGadgetOutOfAmmoEvent ammoEvent = new ItemGadgetOutOfAmmoEvent(event.getPlayer(), this);
			UtilServer.CallEvent(ammoEvent);

			return;
		}

		//Recharge
		if (!Recharge.Instance.use(player, getName(), getName(), _recharge, _recharge > 1000, true, false, true, "Cosmetics"))
		{
			UtilInv.Update(player);
			return;
		}

		ItemGadgetUseEvent itemGadgetUseEvent = new ItemGadgetUseEvent(player, this, 1);
		UtilServer.CallEvent(itemGadgetUseEvent);

		if (itemGadgetUseEvent.isCancelled())
		{
			UtilPlayer.message(player, F.main(Manager.getName(), itemGadgetUseEvent.getCancelledMessage()));
			return;
		}

		if (isUsingAmmo())
		{
			Manager.getInventoryManager().addItemToInventory(player, getName(), -1);
			giveItem(player);
		}

		ActivateCustom(event.getPlayer());
	}

	private int getAmmoCount(Player player)
	{
		return Manager.getInventoryManager().Get(player).getItemCount(getName());
	}

	protected boolean hasAmmo(Player player)
	{
		return !isUsingAmmo() || Manager.getInventoryManager().Get(player).getItemCount(getName()) > 0;
	}

	public boolean isUsingAmmo()
	{
		return _ammo != null;
	}
}