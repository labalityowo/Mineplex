package mineplex.core.gadget.types;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;

public abstract class OutfitGadget extends Gadget
{
	public enum ArmorSlot
	{
		HELMET("Helmet"),
		CHEST("Chestplate"),
		LEGS("Leggings"),
		BOOTS("Boots");

		private String _databaseKey;

		ArmorSlot(String databaseKey)
		{
			_databaseKey = databaseKey;
		}

		public String getDatabaseKey()
		{
			return _databaseKey;
		}
	}
	
	protected final ArmorSlot _slot;
	protected Color _color = null;
	
	public OutfitGadget(GadgetManager manager, String name, String[] desc, int cost, ArmorSlot slot, Material mat, byte data, String... altNames)
	{
		super(manager, GadgetType.COSTUME, name, desc, cost, mat, data, 1, altNames);
		
		_slot = slot;
	}
	
	public ArmorSlot getSlot()
	{
		return _slot;
	}

	public void setColor(Color color)
	{
		_color = color;
	}

	public Color getColor()
	{
		return _color;
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player, message);
	}

	public void applyArmor(Player player, boolean message)
	{
		Manager.removeGadgetType(player, GadgetType.MORPH, this);

		if (_slot == ArmorSlot.HELMET)
		{
			Manager.removeGadgetType(player, GadgetType.FLAG, this);
		}

		Manager.removeOutfit(player, _slot);
		
		_active.add(player);

		if (message)
			UtilPlayer.message(player, F.main(Manager.getName(), "You put on " + F.elem(getName()) + "."));

		ItemStack itemStack;

		if (getDisplayItem() == null)
		{
			itemStack = new ItemStack(getDisplayMaterial(), 1, getDisplayData());
		}
		else
		{
			itemStack = getDisplayItem();
		}

		if (getColor() != null)
		{
			if (itemStack.getItemMeta() instanceof LeatherArmorMeta)
			{
				LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
				leatherArmorMeta.setColor(getColor());
				leatherArmorMeta.setDisplayName(getName());
				itemStack.setItemMeta(leatherArmorMeta);
			}
		}

		switch (_slot)
		{
			case HELMET:
				player.getInventory().setHelmet(itemStack);
				break;
			case CHEST:
				player.getInventory().setChestplate(itemStack);
				break;
			case LEGS:
				player.getInventory().setLeggings(itemStack);
				break;
			case BOOTS:
				player.getInventory().setBoots(itemStack);
				break;
		}
	}
	
	public void removeArmor(Player player, boolean message)
	{
		if (!_active.remove(player))
			return;

		if (message)
			UtilPlayer.message(player, F.main("Gadget", "You took off " + F.elem(getName()) + "."));

		switch (_slot)
		{
			case HELMET:
				player.getInventory().setHelmet(null);
				break;
			case CHEST:
				player.getInventory().setChestplate(null);
				break;
			case LEGS:
				player.getInventory().setLeggings(null);
				break;
			case BOOTS:
				player.getInventory().setBoots(null);
				break;
		}
	}
}
