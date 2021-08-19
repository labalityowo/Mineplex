package mineplex.core.gadget.gadgets.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetCollideEntityEvent;
import mineplex.core.gadget.event.ItemGadgetOutOfAmmoEvent;
import mineplex.core.gadget.event.ItemGadgetUseEvent;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;

public class ItemLovePotion extends ItemGadget
{
	private static final String[] DRINK_MESSAGES = {
			"YUCK! This does not taste like it was meant for humans.",
			"Gross! Why do I keep drinking this stuff... ",
			"BAAAAA BAAAAAAAA. Weird, that made me feel a bit sheepish."
	};

	/**
	 * Created by: Mysticate
	 * Timestamp: February 6, 2016
	*/

	public ItemLovePotion(GadgetManager manager)
	{
		super(
				manager, 
				"Love Potion", 
				UtilText.splitLineToArray(C.cGray + "It doesn't seem to affect humans..? What kind of love potion is this!?", LineFormat.LORE), 
				-6,
				Material.POTION, 
				(byte) 8233, 
				20000, 

				new Ammo(
						"Love Potion", 
						"Love Potion", 
						Material.POTION, 
						(byte) 8233, 
						UtilText.splitLineToArray(C.cGray + "It doesn't seem to affect humans..? What kind of love potion is this!?", LineFormat.LORE), 
						-6,
						16)
				);
	}

	@Override
	public void ApplyItem(Player player, boolean inform)
	{
		Manager.removeGadgetType(player, GadgetType.ITEM);

		_active.add(player);

		List<String> itemLore = new ArrayList<String>();
		itemLore.addAll(Arrays.asList(getDescription()));
		itemLore.add(C.cBlack);
		itemLore.add(C.cWhite + "Your Ammo : " + Manager.getInventoryManager().Get(player).getItemCount(getName()));
		
		player.getInventory().setItem(Manager.getActiveItemSlot(), new ItemBuilder(getDisplayMaterial(), getDisplayData()).setTitle(F.item(Manager.getInventoryManager().Get(player).getItemCount(getName()) + " " + getName())).setHideInfo(true).build());
		
		if (inform)
			UtilPlayer.message(player, F.main("Gadget", "You equipped " + F.elem(getName()) + "."));
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event)
	{
		Player player = event.getPlayer();
		
		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), getDisplayMaterial()))
			return;
		
		if (!isActive(player))
			return;
		
		if (!Recharge.Instance.use(player, "Interact Love Potion", 1000, false, false))
			return;

		event.setCancelled(true);
		
		//Stock
		if (!hasAmmo(player))
		{			
			UtilPlayer.message(player, F.main("Gadget", "You do not have any " + getName() + " left."));

			ItemGadgetOutOfAmmoEvent ammoEvent = new ItemGadgetOutOfAmmoEvent(event.getPlayer(), this);
			Bukkit.getServer().getPluginManager().callEvent(ammoEvent);

			return;
		}
		
		if (!Recharge.Instance.usable(player, getName()) || !Manager.selectEntity(this, event.getRightClicked()))
		{
			UtilInv.Update(player);
			return;	
		}
				
		Recharge.Instance.use(player, getName(), getName(), _recharge, _recharge > 1000, true, false, true, "Cosmetics"); // Cooldown
		
		Manager.getInventoryManager().addItemToInventory(player, getName(), -1);

		ItemGadgetUseEvent itemGadgetUseEvent = new ItemGadgetUseEvent(player, this, 1);
		UtilServer.CallEvent(itemGadgetUseEvent);
		
		player.getInventory().setItem(Manager.getActiveItemSlot(), new ItemBuilder(getDisplayMaterial(), getDisplayData()).setTitle(F.item(Manager.getInventoryManager().Get(player).getItemCount(getName()) + " " + getName())).setHideInfo(true).build());
	}
	
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), this.getDisplayMaterial()))
			return;

		Player player = event.getPlayer();

		if (!isActive(player))
			return; 
		
		if (!Recharge.Instance.use(player, "Interact Love Potion", 500, false, false))
			return;
		
		event.setCancelled(true);
		
		//Stock
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

		String message = UtilMath.randomElement(DRINK_MESSAGES);
		UtilPlayer.message(player, F.main("Potion", message));

		Manager.getInventoryManager().addItemToInventory(player, getName(), -1);

		player.getInventory().setItem(Manager.getActiveItemSlot(), new ItemBuilder(getDisplayMaterial(), getDisplayData()).setTitle(F.item(Manager.getInventoryManager().Get(player).getItemCount(getName()) + " " + getName())).setHideInfo(true).build());

		ItemGadgetUseEvent itemGadgetUseEvent = new ItemGadgetUseEvent(player, this, 1);
		UtilServer.CallEvent(itemGadgetUseEvent);

		ActivateCustom(event.getPlayer());
	}
	
	@Override
	public void ActivateCustom(Player player)
	{
		player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 40, false, false));
	}
}
