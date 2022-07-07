package mineplex.game.clans.items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.UtilInv;
import mineplex.game.clans.items.attributes.AttributeContainer;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

/**
 * Represents a customizable wrapper for an {@link ItemStack}, enabling the
 * possession of special abilities, attributes, and triggers on item.
 */
public class CustomItem
{
	// Chat color used for item display name
	private static final ChatColor TITLE_COLOR = ChatColor.GOLD;
	// Chat color used for attribute descriptions
	private static final ChatColor ATTRIBUTE_COLOR = ChatColor.WHITE;

	protected transient String _displayName;
	protected transient String[] _description;
	protected transient Material _material;
	
	protected transient Player _lastUser = null;

	private AttributeContainer _attributes;

	protected String _uuid;

	protected boolean _dullEnchantment;

	public String OriginalOwner = null;

	public String getUUID()
	{
		return _uuid;
	}

	public CustomItem(String displayName, String[] description, Material material)
	{
		_displayName = displayName;
		_description = description;
		_material = material;
		_attributes = new AttributeContainer();
		_uuid = UUID.randomUUID().toString();
	}

	public CustomItem(Material material)
	{
		this(prettifyName(material), null, material);
	}

	/**
	 * @return the name displayed to players for the item.
	 */
	public String getDisplayName()
	{
		return ChatColor.RESET.toString() + TITLE_COLOR + _attributes.formatItemName(_displayName);
	}

	public String[] getDescription()
	{
		return _description;
	}

	public List<String> getLore()
	{
		List<String> lore = new ArrayList<String>();

		if (getDescription() != null)
		{
			for (String desc : getDescription())
			{
				lore.add(ATTRIBUTE_COLOR + desc);
			}
		}

		// Display attribute descriptions and stats in lore
		for (ItemAttribute attribute : _attributes.getAttributes())
		{
			String attributeLine = ATTRIBUTE_COLOR + "• " + attribute.getDescription();
			lore.add(attributeLine);
		}

		return lore;
	}

	public ItemStack toItemStack(int amount)
	{
		ItemStack item = CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(new ItemStack(_material, amount)));
		update(item);

		if (_dullEnchantment)
		{
			UtilInv.addDullEnchantment(item);
		}

		return item;
	}

	public ItemStack toItemStack()
	{
		return toItemStack(1);
	}

	public void addDullEnchantment()
	{
		_dullEnchantment = true;
	}

	public void removeDullEnchantment()
	{
		_dullEnchantment = false;
	}

	public void onInteract(PlayerInteractEvent event)
	{
		for (ItemAttribute attribute : _attributes.getAttributes())
		{
			attribute.onInteract(event);
		}
	}

	public void onAttack(CustomDamageEvent event)
	{
		for (ItemAttribute attribute : _attributes.getAttributes())
		{
			attribute.onAttack(event);
		}
	}

	public void onAttacked(CustomDamageEvent event)
	{
		for (ItemAttribute attribute : _attributes.getAttributes())
		{
			attribute.onAttacked(event);
		}
	}

	/**
	 * @param item - the item to check for a matching link
	 * @return true, if {@code item} matches this CustomItem via UUID, false
	 *         otherwise.
	 */
	public boolean matches(CustomItem item)
	{
		return item.getUUID().equals(_uuid);
	}

	/**
	 * Update {@code item} with the proper meta properties suited for this
	 * {@link CustomItem}.
	 *
	 * @param item - the item whose meta properties are being updated to become
	 *        a version of this updated custom item.
	 */
	public void update(ItemStack item)
	{
		ItemMeta meta = item.getItemMeta();

		String displayName = getDisplayName();
		List<String> lore = getLore();

		meta.setDisplayName(displayName);
		meta.setLore(lore);

		item.setItemMeta(meta);
		
		if (_dullEnchantment)
		{
			UtilInv.addDullEnchantment(item);
		}

		GearManager.writeNBT(this, item);
	}

	public static String prettifyName(Material material)
	{
		String name = "";
		String[] words = material.toString().split("_");

		for (String word : words)
		{
			word = word.toLowerCase();
			name += word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
		}

		return name.trim();
	}

	public Material getMaterial()
	{
		return _material;
	}

	public AttributeContainer getAttributes()
	{
		return _attributes;
	}
	
	public void setMaterial(Material material)
	{
		if (_material == null)
		{
			_displayName = prettifyName(material);
		}
		_material = material;
	}
}