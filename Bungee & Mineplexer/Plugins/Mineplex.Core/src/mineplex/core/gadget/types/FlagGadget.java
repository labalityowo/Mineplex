package mineplex.core.gadget.types;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.flag.FlagType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Flags which sit upon players head.
 */
public class FlagGadget extends Gadget
{
	private final FlagType _flag;

	public FlagGadget(GadgetManager manager, FlagType flag)
	{
		super(manager, GadgetType.FLAG, "Flag of " + flag.getFlag().getCountryName(),
				UtilText.splitLineToArray(C.cGray + "Fly the " + flag.getFlag().getCountryAdjective() + " flag atop your head!", LineFormat.LORE),
				flag.getCost(), Material.WOOL, (byte) 0, 1, flag.getFlag().getCountryAdjective() + " Flag");

		setDisplayItem(flag.getFlag().getBanner());
		_flag = flag;
	}

	public void applyArmor(Player player, boolean message)
	{
		Manager.removeGadgetType(player, GadgetType.MORPH, this);
		Manager.removeGadgetType(player, GadgetType.FLAG, this);
		Manager.removeGadgetType(player, GadgetType.HAT, this);
		Manager.removeOutfit(player, OutfitGadget.ArmorSlot.HELMET);

		_active.add(player);

		if (message)
		{
			UtilPlayer.message(player, F.main("Gadget", "You unfurled the " + F.elem(getName()) + "."));
		}
	}

	public void removeArmor(Player player)
	{
		if (_active.remove(player))
		{
			UtilPlayer.message(player, F.main("Gadget", "You took down the " + F.elem(getName()) + "."));
		}
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);
		ItemStack flag = _flag.getFlag().getBanner();
		ItemMeta meta = flag.getItemMeta();
		meta.setDisplayName(C.cGreenB + getDisplayName());
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		flag.setItemMeta(meta);
		player.getInventory().setHelmet(flag);
		player.updateInventory();
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		player.updateInventory();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerDeath(PlayerDeathEvent event)
	{
		disable(event.getEntity());
	}

	/**
	 * @return The specific gadget which this represents.
	 */
	public FlagType getFlagType()
	{
		return _flag;
	}
}
