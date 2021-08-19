package mineplex.core.gadget.gadgets.item;

import mineplex.core.common.util.*;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.ItemGadgetOutOfAmmoEvent;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.valentines.event.AttemptGiftEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemFlowerGift extends ItemGadget
{
	public ItemFlowerGift(GadgetManager manager)
	{
		super(manager,
				"Valentines Gift",
				UtilText.splitLineToArray(C.cGray + "Maybe if Sigils and Phinary used these they wouldn't be so alone", LineFormat.LORE),
				-7,
				Material.RED_ROSE,
				(byte) 0,
				6000,
				new Ammo("Valentines Gift", "Valentines Gift", Material.RED_ROSE, (byte) 0, UtilText.splitLineToArray(C.cGray + "Maybe if Sigils and Phinary used these they wouldn't be so alone", LineFormat.LORE), -7, 10));
	}

	@Override
	public void ActivateCustom(Player player)
	{

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractAtEntityEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilGear.isMat(player.getItemInHand(), getDisplayMaterial()))
			return;

		if (!hasAmmo(player))
		{
			UtilPlayer.message(player, F.main("Gadget", "You do not have any " + getName() + " left."));

			ItemGadgetOutOfAmmoEvent ammoEvent = new ItemGadgetOutOfAmmoEvent(event.getPlayer(), this);
			Bukkit.getServer().getPluginManager().callEvent(ammoEvent);

			return;
		}

		//Recharge
		if (!Recharge.Instance.use(player, getName(), getName(), _recharge, _recharge > 1000, true, false, true, "Cosmetics"))
		{
			UtilInv.Update(player);
			return;
		}

		if (event.getRightClicked() instanceof Player)
		{
			Player to = ((Player) event.getRightClicked());
			AttemptGiftEvent giftEvent = new AttemptGiftEvent(player, to);
			Bukkit.getPluginManager().callEvent(giftEvent);
		}
		else
		{
			UtilPlayer.message(player, F.main("Gadget", "You used " + F.elem(getName()) + " on " + F.name(event.getRightClicked().getName()) + ". It's not very effective..."));
		}

		event.setCancelled(true);
	}

	@Override
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (!isActive(event.getPlayer()))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), getDisplayMaterial()))
			return;

		//Recharge
		if (!Recharge.Instance.use(event.getPlayer(), getName(), getName(), _recharge, false, true, false, true, "Cosmetics"))
			return;

		if (!hasAmmo(event.getPlayer()))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Gadget", "You do not have any " + getName() + " left."));

			ItemGadgetOutOfAmmoEvent ammoEvent = new ItemGadgetOutOfAmmoEvent(event.getPlayer(), this);
			Bukkit.getServer().getPluginManager().callEvent(ammoEvent);

			return;
		}

		event.setCancelled(true);

		// Logic is handled in onInteract (so we can cancel interact events on npc's)
		UtilPlayer.message(event.getPlayer(), F.main("Gift", "Nothing happened... Maybe I should try giving this to someone?!"));
	}
}
