package mineplex.core.gadget.gadgets.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemStackFactory;

public class ItemDuelingSword extends ItemGadget
{
	public ItemDuelingSword(GadgetManager manager) 
	{
		super(manager, "PvP Sword", new String[] 
				{
				C.cGreen + "Activated in King of the Hill",
				},
				CostConstants.NO_LORE,
				Material.GOLD_SWORD, (byte)0,
				1000, new Ammo("Dueling Sword", "10 Swords", Material.WOOD_SWORD, (byte)0, new String[] { C.cWhite + "10 Swords to duel with" }, 1000, 10));

		setHidden(true);
		setPreserveStaticItem();
	}
	
	@Override
	public void ApplyItem(Player player, boolean inform)
	{
		Manager.removeGadgetType(player, GadgetType.ITEM);

		_active.add(player);

		player.getInventory().setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(Material.GOLD_SWORD, (byte)0, 1, "PvP Sword"));
	}
	
	@Override
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{

	}

	@Override
	public void ActivateCustom(Player player)
	{		
		
	}
}
