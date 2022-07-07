package mineplex.game.clans.items.runes;

import java.util.Arrays;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.items.CustomItem;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.attributes.armor.ConqueringArmorAttribute;
import mineplex.game.clans.items.attributes.armor.LavaAttribute;
import mineplex.game.clans.items.attributes.armor.PaddedAttribute;
import mineplex.game.clans.items.attributes.armor.ReinforcedAttribute;
import mineplex.game.clans.items.attributes.armor.SlantedAttribute;
import mineplex.game.clans.items.attributes.bow.HeavyArrowsAttribute;
import mineplex.game.clans.items.attributes.bow.HuntingAttribute;
import mineplex.game.clans.items.attributes.bow.InverseAttribute;
import mineplex.game.clans.items.attributes.bow.LeechingAttribute;
import mineplex.game.clans.items.attributes.bow.RecursiveAttribute;
import mineplex.game.clans.items.attributes.bow.ScorchingAttribute;
import mineplex.game.clans.items.attributes.bow.SlayingAttribute;
import mineplex.game.clans.items.attributes.weapon.ConqueringAttribute;
import mineplex.game.clans.items.attributes.weapon.FlamingAttribute;
import mineplex.game.clans.items.attributes.weapon.FrostedAttribute;
import mineplex.game.clans.items.attributes.weapon.HasteAttribute;
import mineplex.game.clans.items.attributes.weapon.JaggedAttribute;
import mineplex.game.clans.items.attributes.weapon.SharpAttribute;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manager class for runes
 */
public class RuneManager implements Listener
{
	private static final String RUNE_NAME = C.cGold + "Ancient Rune";
	private String _managerName;
	
	public RuneManager(String name, JavaPlugin plugin)
	{
		_managerName = name;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Checks if an item is a rune
	 * @param item The item to check
	 * @return Whether the item is a rune
	 */
	public boolean isRune(ItemStack item)
	{
		if (item == null || item.getType() == Material.AIR)
		{
			return false;
		}
		ItemMeta im = item.getItemMeta();
		return im.hasDisplayName() && im.getDisplayName().equals(RUNE_NAME) && im.hasLore() && im.getLore().size() > 0;
	}
	
	/**
	 * Converts a rune item to a rune attribute
	 * @param rune The rune to convert
	 * @return The attribute of the rune
	 */
	public RuneAttribute decodeRune(ItemStack rune)
	{
		if (!isRune(rune))
		{
			return null;
		}
		RuneAttribute attribute = RuneAttribute.getFromDisplay(rune.getItemMeta().getLore().get(0));
		
		return attribute;
	}
	
	/**
	 * Gets the item representation of a rune attribute
	 * @param attribute The rune attribute to generate the rune from
	 * @return The item representation of the rune attribute
	 */
	public ItemStack getRune(RuneAttribute attribute)
	{
		ItemStack rune = new ItemStack(Material.NETHER_STAR);
		ItemMeta im = rune.getItemMeta();
		
		im.setDisplayName(RUNE_NAME);
		im.setLore(Arrays.asList(attribute.getDisplay()));
		rune.setItemMeta(im);
		
		return rune;
	}
	
	/**
	 * Applies a rune to an item
	 * @param rune The rune to apply to the item
	 * @param to The item to apply the rune to
	 * @return The item after having the rune applied
	 */
	public ItemStack applyToItem(RuneAttribute rune, ItemStack to)
	{
		if (!rune.canApplyTo(to.getType()))
		{
			return null;
		}
		if (GearManager.isCustomItem(to))
		{
			try
			{
				ItemAttribute attribute = rune.getAttributeClass().newInstance();
				CustomItem item = GearManager.parseItem(to);
				if (!item.getAttributes().getRemainingTypes().contains(attribute.getType()))
				{
					return null;
				}
				item.getAttributes().addAttribute(attribute);
				ItemStack stack = to.clone();
				item.setMaterial(stack.getType());
				item.update(stack);
				return stack;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			try
			{
				CustomItem item = new CustomItem(to.getType());
				item.addDullEnchantment();
				ItemAttribute attribute = rune.getAttributeClass().newInstance();
				item.getAttributes().addAttribute(attribute);
				ItemStack stack = to.clone();
				item.update(stack);
				return stack;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMakeBeaconWithRune(PrepareItemCraftEvent event)
	{
		if (event.getInventory().getResult() == null || !event.getInventory().getResult().getType().equals(Material.BEACON))
		{
			return;
		}
		if (!isRune(event.getInventory().getItem(5)))
		{
			return;
		}
		event.getInventory().setResult(null);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onCraftBeaconWithRune(CraftItemEvent event)
	{
		if (event.getInventory().getResult() == null || event.getInventory().getResult().getType() != Material.BEACON)
		{
			return;
		}
		if (!isRune(event.getInventory().getItem(5)))
		{
			return;
		}
		event.setCancelled(true);
	}
	  
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlaceRuneInAnvil(InventoryClickEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
		{
			return;
		}
		if (!event.getInventory().getType().equals(InventoryType.ANVIL))
		{
			return;
		}
		if (!isRune(event.getCursor()) && !isRune(event.getCurrentItem()))
		{
			return;
		}
		event.setCancelled(true);
		if (event.getInventory().getItem(0) == null && event.getInventory().getItem(1) == null)
		{
			event.getWhoClicked().closeInventory();
		}
		UtilPlayer.message(event.getWhoClicked(), F.main(_managerName, "To use a rune, place it on an item!"));
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onUseRune(InventoryClickEvent event)
	{
		if(!(event.getWhoClicked() instanceof Player))
		{
			return;
		}
		
		Player player = (Player) event.getWhoClicked();
		
		if (!isRune(event.getCursor()))
		{
			return;
		}
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
		{
			return;
		}
		
		RuneAttribute rune = decodeRune(event.getCursor());
		if (rune == null)
		{
			return;
		}
		if (event.getCurrentItem().getAmount() > 1)
		{
			UtilPlayer.message(player, F.main(_managerName, "The rune cannot apply to all of those items!"));
			return;
		}
		if (!rune.canApplyTo(event.getCurrentItem().getType()))
		{
			UtilPlayer.message(player, F.main(_managerName, "The rune seems to reject that type of item!"));
			return;
		}
		ItemStack after = applyToItem(rune, event.getCurrentItem());
		if (after == null)
		{
			UtilPlayer.message(player, F.main(_managerName, "The rune seems to reject that item!"));
			return;
		}
		
		event.setCancelled(true);
		ItemStack cursorAfter = null;
		if (event.getCursor().getAmount() > 1)
		{
			cursorAfter = event.getCursor().clone();
			cursorAfter.setAmount(cursorAfter.getAmount() - 1);
		}
		event.setCursor(new ItemStack(Material.AIR));
		event.getClickedInventory().setItem(event.getSlot(), after);
		if (cursorAfter != null)
		{
			UtilInv.insert(player, cursorAfter);
		}
		player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.5f, 5f);
		
		player.updateInventory();
	}
	
	/**
	 * Enum of all rune attributes
	 */
	public static enum RuneAttribute
	{
		FROSTED(FrostedAttribute.class, "Frosted", true, false, false),
		SHARP(SharpAttribute.class, "Sharp", true, false, false),
		JAGGED(JaggedAttribute.class, "Jagged", true, false, false),
		HASTE(HasteAttribute.class, "Haste", true, false, false),
		FLAMING(FlamingAttribute.class, "Flaming", true, false, false),
		CONQUERING(ConqueringAttribute.class, "Conquering", true, false, false),
		SLANTED(SlantedAttribute.class, "Slanted", false, true, false),
		REINFORCED(ReinforcedAttribute.class, "Reinforced", false, true, false),
		CONQUERING_ARMOR(ConqueringArmorAttribute.class, "Conquering", false, true, false),
		PADDED(PaddedAttribute.class, "Padded", false, true, false),
		LAVA(LavaAttribute.class, "Lava Forged", false, true, false),
		HEAVY(HeavyArrowsAttribute.class, "Heavy", false, false, true),
		HUNTING(HuntingAttribute.class, "Hunting", false, false, true),
		INVERSE(InverseAttribute.class, "Inverse", false, false, true),
		LEECHING(LeechingAttribute.class, "Leeching", false, false, true),
		RECURSIVE(RecursiveAttribute.class, "Recursive", false, false, true),
		SCORCHING(ScorchingAttribute.class, "Scorching", false, false, true),
		SLAYING(SlayingAttribute.class, "Slaying", false, false, true)
		;
		
		private Class<? extends ItemAttribute> _class;
		private String _display;
		private boolean _weapon, _armor, _bow;
		
		private RuneAttribute(Class<? extends ItemAttribute> attributeClass, String display, boolean weapon, boolean armor, boolean bow)
		{
			_class = attributeClass;
			_display = display;
			_weapon = weapon;
			_armor = armor;
			_bow = bow;
			
			if (weapon)
			{
				_display = C.cRed + C.Italics + _display;
			}
			if (armor)
			{
				_display = C.cGreen + C.Italics + _display;
			}
			if (bow)
			{
				_display = C.cDAqua + C.Italics + _display;
			}
		}
		
		/**
		 * Gets the lore display of this rune type
		 * @return The lore display of this rune type
		 */
		public String getDisplay()
		{
			return _display;
		}
		
		/**
		 * Gets the class for this rune type
		 * @return The class for this rune type
		 */
		public Class<? extends ItemAttribute> getAttributeClass()
		{
			return _class;
		}
		
		/**
		 * Checks whether this rune type can be applied to an item type
		 * @param type The item type to check
		 * @return Whether this rune type can be applied to that item type
		 */
		public boolean canApplyTo(Material type)
		{
			if (_weapon)
			{
				if (UtilItem.isSword(type) || UtilItem.isAxe(type))
				{
					return true;
				}
				if (type == Material.RECORD_4 || type == Material.GOLD_RECORD || type == Material.RECORD_3 || type == Material.RECORD_5 || type == Material.GREEN_RECORD || type == Material.RECORD_12)
				{
					return true;
				}
			}
			if (_armor && UtilItem.isArmor(type))
			{
				return true;
			}
			if (_bow && type == Material.BOW)
			{
				return true;
			}
			
			return false;
		}
		
		/**
		 * Gets the rune attribute from a given lore display
		 * @param display The lore display to check
		 * @return The rune attribute associated with that lore display
		 */
		public static RuneAttribute getFromDisplay(String display)
		{
			for (RuneAttribute rune : RuneAttribute.values())
			{
				if (rune.getDisplay().equals(display))
				{
					return rune;
				}
			}
			
			return null;
		}
		
		/**
		 * Gets the rune attribute that resolves to the given string
		 * @param string The string to get the rune attribute based on
		 * @return The rune attribute that resolves to that string
		 */
		public static RuneAttribute getFromString(String string)
		{
			for (RuneAttribute rune : RuneAttribute.values())
			{
				if (rune.toString().equalsIgnoreCase(string))
				{
					return rune;
				}
			}
			
			return null;
		}
	}
}