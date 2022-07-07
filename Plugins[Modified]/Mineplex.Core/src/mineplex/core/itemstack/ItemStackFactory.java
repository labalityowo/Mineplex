package mineplex.core.itemstack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;

public class ItemStackFactory extends MiniPlugin
{
	public static ItemStackFactory Instance;

	private HashMap<Integer, HashMap<Byte, Entry<String, Boolean>>> _names;
	private HashMap<Integer, HashMap<Byte, String[]>> _lores;

	private String _nameFormat = "§r" + C.mItem;

	private HashSet<Listener> _statListeners = new HashSet<Listener>();

	private boolean _customNames = false;

	protected ItemStackFactory(JavaPlugin plugin, boolean customNames)
	{
		super("ItemStack Factory", plugin);

		AddDefault();

		if (customNames)
			SetCustom();
	}

	public static void Initialize(JavaPlugin plugin, boolean customNames)
	{
		Instance = new ItemStackFactory(plugin, customNames);
	}

	public void AddStatListener(Listener listener)
	{
		_statListeners.add(listener);
		registerEvents(listener);
	}

	private void SetCustom() 
	{
		Add(0, (byte)0, "Unarmed", false);

		Add(Material.DIAMOND_SWORD, (byte)0, ChatColor.GOLD + "Diamond Sword", new String[] 
				{
			C.cGray + "Damage: " + C.cYellow + "6",
			""
				}, true);

		Add(Material.IRON_SWORD, (byte)0, "Iron Sword", new String[] 
				{
			C.cGray + "Damage: " + C.cYellow + "6",
			""
				}, true);

		Add(Material.GOLD_SWORD, (byte)0, ChatColor.GOLD + "Power Sword", new String[] 
				{
			C.cGray + "Damage: " + C.cYellow + "7",
			""
				}, true);

		Add(Material.DIAMOND_AXE, (byte)0, ChatColor.GOLD + "Diamond Axe", new String[] 
				{
			C.cGray + "Damage: " + C.cYellow + "6",
			""
				}, true);

		Add(Material.IRON_AXE, (byte)0, "Iron Axe", new String[] 
				{
			C.cGray + "Damage: " + C.cYellow + "6",
			""
				}, true);

		Add(Material.GOLD_AXE, (byte)0, ChatColor.GOLD + "Power Axe", new String[] 
				{
			C.cGray + "Damage: " + C.cYellow + "7",
			""
				}, true);

		Add(Material.RECORD_5, (byte)0, "50,000 Coin Token", true);

		Add(Material.IRON_HELMET, (byte)0, "Knights Helm", true);
		Add(Material.IRON_CHESTPLATE, (byte)0, "Knights Chestplate", true);
		Add(Material.IRON_LEGGINGS, (byte)0, "Knights Leggings", true);
		Add(Material.IRON_BOOTS, (byte)0, "Knights Boots", true);

		Add(Material.CHAINMAIL_HELMET, (byte)0, "Rangers Cap", true);
		Add(Material.CHAINMAIL_CHESTPLATE, (byte)0, "Rangers Vest", true);
		Add(Material.CHAINMAIL_LEGGINGS, (byte)0, "Rangers Leggings", true);
		Add(Material.CHAINMAIL_BOOTS, (byte)0, "Rangers Boots", true);

		Add(Material.LEATHER_HELMET, (byte)0, "Assassins Cap", true);
		Add(Material.LEATHER_CHESTPLATE, (byte)0, "Assassins Vest", true);
		Add(Material.LEATHER_LEGGINGS, (byte)0, "Assassins Chaps", true);
		Add(Material.LEATHER_BOOTS, (byte)0, "Assassins Boots", true);

		Add(Material.DIAMOND_HELMET, (byte)0, "Brutes Helm", true);
		Add(Material.DIAMOND_CHESTPLATE, (byte)0, "Brutes Chestplate", true);
		Add(Material.DIAMOND_LEGGINGS, (byte)0, "Brutes Leggings", true);
		Add(Material.DIAMOND_BOOTS, (byte)0, "Brutes Boots", true);

		Add(Material.GOLD_HELMET, (byte)0, "Mages Helm", true);
		Add(Material.GOLD_CHESTPLATE, (byte)0, "Mages Chestplate", true);
		Add(Material.GOLD_LEGGINGS, (byte)0, "Mages Leggings", true);
		Add(Material.GOLD_BOOTS, (byte)0, "Mages Boots", true);

		Add(Material.ENDER_CHEST, (byte)0, "Class Unlock Shop", true);
		Add(Material.ENCHANTMENT_TABLE, (byte)0, "Class Setup Table", true);
		Add(Material.BREWING_STAND, (byte)0, "TNT Generator", true);
		Add(Material.BEACON, (byte)0, "Clan Outpost", true);

		Add(Material.GOLD_NUGGET, (byte)0, ChatColor.YELLOW + "Power Charge", true);


		//CONSUMABLE ITEMS
		Add(Material.MUSHROOM_SOUP, (byte)0, ChatColor.YELLOW + "Mushroom Soup", new String[] 
				{
			C.cGray + "Right-Click: " + C.cYellow + "Consume",
			C.cGray + "  " + "Regeneration I for 4 Seconds",
			C.cGray + "  " + "4 Food",
			""
				}, true);

		//THROWABLES
		Add(Material.POTION, (byte)0, ChatColor.YELLOW + "Water Bottle", new String[] 
				{
			C.cGray + "Left-Click: " + C.cYellow + "Throw",
			C.cGray + "  " + "Douses Players",
			C.cGray + "  " + "Douses Fires",
			"",
			C.cGray + "Right-Click: " + C.cYellow + "Drink",
			C.cGray + "  " + "Douse Self",
			C.cGray + "  " + "Fire Resistance I for 4 Seconds"
				}, true);

		Add(Material.SLIME_BALL, (byte)0, ChatColor.YELLOW + "Poison Ball", new String[] 
				{
			C.cGray + "Left-Click: " + C.cYellow + "Throw",
			C.cGray + "  " + "Poison I for 6 Seconds",
			C.cGray + "  " + "Returns to Thrower"
				}, true);

		Add(Material.ENDER_PEARL, (byte)0, ChatColor.YELLOW + "Ender Pearl", new String[] 
				{
			C.cGray + "Left-Click: " + C.cYellow + "Throw",
			C.cGray + "  " + "Ride Ender Pearl",
			"",
			C.cGray + "Right-Click: " + C.cYellow + "Consume",
			C.cGray + "  " + "Removes Negative Effects",
			C.cGray + "  " + "4 Food"
				}, true);

		Add(Material.NOTE_BLOCK, (byte)0, ChatColor.YELLOW + "Proximity Incendiary", new String[] 
				{
			C.cGray + "Left-Click: " + C.cYellow + "Throw",
			C.cGray + "  " + "Activates after 4 Seconds",
			C.cGray + "  " + "Detonates on player proximity;",
			C.cGray + "    " + "30 Fires spew out",
			C.cGray + "    " + "Fires ignite for 3 Seconds",
			C.cGray + "    " + "Fires remains for 15 Seconds"
				}, true);

		Add(Material.REDSTONE_LAMP_ON, (byte)0, ChatColor.YELLOW + "Proximity Zapper", new String[] 
				{
			C.cGray + "Left-Click: " + C.cYellow + "Throw",
			C.cGray + "  " + "Activates after 4 Seconds",
			C.cGray + "  " + "Detonates on player proximity;",
			C.cGray + "    " + "Lightning strikes the Zapper",
			C.cGray + "    " + "Silence for 6 seconds",
			C.cGray + "    " + "Shock for 6 seconds",
			C.cGray + "    " + "Slow IV for 6 seconds"
				}, true);

		Add(Material.COMMAND, (byte)0, ChatColor.YELLOW + "Proximity Explosive", new String[] 
				{
			C.cGray + "Left-Click: " + C.cYellow + "Throw",
			C.cGray + "  " + "Activates after 4 Seconds",
			C.cGray + "  " + "Detonates on player proximity;",
			C.cGray + "    " + "8 Range",
			C.cGray + "    " + "Strong Knockback"
				}, true);

		//TOOLS
		Add(Material.SHEARS, (byte)0, ChatColor.YELLOW + "Scanner VR-9000", new String[] 
				{
			C.cGray + "Right-Click: " + C.cYellow + "Scan Player",
			C.cGray + "  " + "100 Blocks Range",
			C.cGray + "  " + "Shows Targets Skills",
			""
				}, true);
	}

	private void Add(Material mat, byte data, String name, boolean special)
	{
		Add(mat.getId(), data, name, null, special);
	}

	private void Add(int id, byte data, String name, boolean special)
	{
		Add(id, data, name, null, special);
	}

	private void Add(Material mat, byte data, String name, String[] lore, boolean special)
	{
		Add(mat.getId(), data, name, lore, special);
	}

	private void Add(int id, byte data, String name, String[] lore, boolean special)
	{
		if (!_names.containsKey(id))
			_names.put(id, new HashMap<Byte, Entry<String, Boolean>>());			

		_names.get(id).put(data, new AbstractMap.SimpleEntry<String, Boolean>(name, special));

		if (lore == null)
			return;

		if (!_lores.containsKey(id))
			_lores.put(id, new HashMap<Byte, String[]>());			

		_lores.get(id).put(data, lore);
	}

	private void AddDefault() 
	{
		_names = new HashMap<Integer, HashMap<Byte, Entry<String, Boolean>>>();
		_lores = new HashMap<Integer, HashMap<Byte, String[]>>(); 

		for (int id=0 ; id<10000 ; id++)
		{
			Material mat = Material.getMaterial(id);

			if (mat == null)
				continue;

			//Add Item
			HashMap<Byte,Entry<String, Boolean>> variants = new HashMap<Byte,Entry<String, Boolean>>();
			_names.put(id, variants);

			for (byte data=0 ; data<50 ; data++)
			{
				try
				{	
					String name = "";
					
					//Get Name
					ItemStack stack = new ItemStack(id, 1, data);
					if (CraftItemStack.asNMSCopy(stack) != null && CraftItemStack.asNMSCopy(stack).getName() != null)
						name = CraftItemStack.asNMSCopy(stack).getName();		

					if (id == 140)
						name = "Flower Pot";

					if (name.length() == 0)
						name = Clean(mat.toString());

					//No Duplicates
					boolean duplicate = false;
					for (Entry<String, Boolean> cur : variants.values())
						if (cur.getKey().equals(name))
						{
							duplicate = true;
							break;
						}
					
					if (duplicate)
						continue;

					variants.put(data, new AbstractMap.SimpleEntry<String, Boolean>(name, (mat.getMaxStackSize() == 1)));	

					//System.out.println("Added: " + name + "    " + id + ":" + data);
				}
				catch (Exception e) 
				{
					//System.out.println("Failed: " + mat.name() + "    " + id + ":" + data);
				}	
			}		
		}
	}

	private String Clean(String string) 
	{
		String out = "";
		String[] words = string.split("_");

		for (String word : words)
		{
			if (word.length() < 1)
				return "Unknown";

			out += word.charAt(0) + word.substring(1, word.length()).toLowerCase() + " ";
		}

		return out.substring(0, out.length() - 1);
	}

	public String GetItemStackName(ItemStack stack)
	{
		return ((CraftItemStack)stack).getHandle().getName();
	}

	public String GetName(ItemStack stack, boolean formatted)
	{
		if (stack == null)
			return "Unarmed";

		if (stack.getData() != null)
			return GetName(stack.getTypeId(), stack.getData().getData(), formatted);
		else
			return GetName(stack.getTypeId(), (byte)0, formatted);
	}

	public String GetName(Block block, boolean formatted)
	{
		return GetName(block.getTypeId(), block.getData(), formatted);
	}

	public String GetName(Material mat, byte data, boolean formatted)
	{
		return GetName(mat.getId(), data, formatted);
	}

	public String GetName(int id, byte data, boolean formatted)
	{
		String out = "";
		if (formatted) 
			out = _nameFormat;

		if (!_names.containsKey(id))
			return out + "Unknown";

		if (!_names.get(id).containsKey(data))
		{
			if (_names.get(id).containsKey(0))
				return out + _names.get(id).get(0).getKey();

			for (Entry<String, Boolean> cur : _names.get(id).values())
				return cur.getKey();

			return out + "Unknown";
		}

		return out + _names.get(id).get(data).getKey();
	}

	public boolean IsSpecial(ItemStack stack)
	{
		if (stack == null)
			return false;

		if (stack.getData() != null)
			return IsSpecial(stack.getTypeId(), stack.getData().getData());
		else
			return IsSpecial(stack.getTypeId(), (byte)0);
	}

	public boolean IsSpecial(Material mat, byte data)
	{
		return IsSpecial(mat.getId(), data);
	}

	public boolean IsSpecial(int id, byte data)
	{
		if (!_names.containsKey(id))
			return false;

		if (!_names.get(id).containsKey(data))
			if (_names.get(id).containsKey(0))
				return _names.get(id).get(0).getValue();
			else
				return false;

		return _names.get(id).get(data).getValue();
	}

	public void StatsArmorRename(ItemStack item, int damage)
	{
		if (!_customNames)
			return;

		if (item == null)
			return;

		if (item.getMaxStackSize() > 1)
			return;

		damage += GetLoreVar(item, "Damage Tanked", 0);

		SetLoreVar(item, "Damage Tanked", "" + damage);

		if (damage >= 10000)					item.addEnchantment(Enchantment.DURABILITY, 1);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void StatsBlockMined(BlockBreakEvent event)
	{
		if (!_customNames)
			return;

		if (event.isCancelled())
			return;

		ItemStack item = event.getPlayer().getItemInHand();

		if (item == null)
			return;

		if (item.getMaxStackSize() > 1)
			return;

		int blocks = 1 + GetLoreVar(item, "Blocks Mined", 0);
		SetLoreVar(item, "Blocks Mined", blocks+"");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void StatsKillMob(EntityDeathEvent event)
	{
		if (!_customNames)
			return;

		if (!(event.getEntity() instanceof Monster))
			return;

		Monster ent = (Monster)event.getEntity();

		if (ent.getKiller() == null)
			return;

		if (ent.getKiller().isBlocking())
			return;

		ItemStack item = ent.getKiller().getItemInHand();

		if (item == null)
			return;

		if (item.getMaxStackSize() > 1)
			return;

		int kills = 1 + GetLoreVar(item, "Monster Kills", 0);

		SetLoreVar(item, "Monster Kills", "" + kills);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void StatsBowShoot(EntityShootBowEvent event)
	{
		if (!_customNames)
			return;

		if (event.isCancelled())
			return;

		int shots = 1 + GetLoreVar(event.getBow(), "Arrows Shot", 0);

		SetLoreVar(event.getBow(), "Arrows Shot", "" + shots);

		int hits = GetLoreVar(event.getBow(), "Arrows Hit", 0);

		double acc = UtilMath.trim(1, ((double)hits/(double)shots)*100);

		SetLoreVar(event.getBow(), "Accuracy", acc + "%");
	}

	@EventHandler
	public void RenameSpawn(ItemSpawnEvent event)
	{	
		if (!_customNames)
			return;

		if (event.isCancelled())
			return;

		//Ignore Named Items
		String color = ChatColor.getLastColors(GetItemStackName(event.getEntity().getItemStack()));
		if (color != null && color.length() >= 2 && color.charAt(1) != 'f')
			return;

		int id = event.getEntity().getItemStack().getTypeId();
		byte data = 0;
		if (event.getEntity().getItemStack().getData() != null)
			data = event.getEntity().getItemStack().getData().getData();

		((CraftItemStack)event.getEntity().getItemStack()).getHandle().c(GetName(id, data, true));
	}

	@EventHandler
	public void RenameArrow(PlayerPickupItemEvent event)
	{
		if (!_customNames)
			return;

		if (event.isCancelled())
			return;

		ItemStack stack = event.getItem().getItemStack();

		if (stack.getType() != Material.ARROW)
			return;

		//Ignore Named Items
		String color = ChatColor.getLastColors(GetItemStackName(stack));
		if (color != null && color.length() >= 2 && color.charAt(1) != 'f')
			return;

		//Data
		byte data = 0;
		if (stack.getData() != null)
			data= stack.getData().getData();

		//Remove
		event.setCancelled(true);
		event.getItem().remove();

		//Fletched
		if (data == 1)
			return;

		event.getPlayer().getInventory().addItem(CreateStack(stack.getTypeId(), data, stack.getAmount()));
	}

	@EventHandler
	public void RenameSmelt(FurnaceSmeltEvent event)
	{
		if (!_customNames)
			return;

		ItemStack stack = event.getResult();

		byte data = 0;
		if (stack.getData() != null)
			data= stack.getData().getData();

		ItemStack result = CreateStack(stack.getTypeId(), data, stack.getAmount());

		event.setResult(result);
	}

	@EventHandler
	public void RenameCraft(PrepareItemCraftEvent event)
	{
		if (!_customNames)
			return;

		ItemStack stack = event.getInventory().getResult();

		byte data = 0;
		if (stack.getData() != null)
			data= stack.getData().getData();

		String crafter = null;
		if (event.getViewers().size() == 1 && stack.getMaxStackSize() == 1)
			crafter = event.getViewers().get(0).getName() + " Crafting";

		ItemStack result = CreateStack(stack.getTypeId(), data, stack.getAmount(), null, new String[] {}, crafter);

		event.getInventory().setResult(result);
	}

	@EventHandler
	public void RenameCraftAlg(InventoryClickEvent event)
	{
		if (!_customNames)
			return;

		if (!event.isShiftClick())
			return;

		if (event.getSlotType() != SlotType.RESULT)
			return; 

		if (!(event.getInventory() instanceof CraftingInventory))
			return;

		CraftingInventory inv = (CraftingInventory)event.getInventory();

		int make = 64;

		//Find Lowest Amount
		for (ItemStack item : inv.getMatrix())
			if (item != null && item.getType() != Material.AIR)
				if (item.getAmount() < make)
					make = item.getAmount();

		make = make-1;

		//Lower Amounts
		for (int i=0 ; i<inv.getMatrix().length ; i++)
			if (inv.getMatrix()[i] != null && inv.getMatrix()[i].getType() != Material.AIR)
			{
				if (inv.getMatrix()[i].getAmount() > make)
					inv.getMatrix()[i].setAmount(inv.getMatrix()[i].getAmount() - make);
				else
					inv.getMatrix()[i].setAmount(1);	
			}

		//Get Result Data
		int id = event.getCurrentItem().getTypeId();
		byte data = 0;
		if (event.getCurrentItem().getData() != null)
			data = event.getCurrentItem().getData().getData();
		int amount = event.getCurrentItem().getAmount();

		//Crafter
		String crafter = null;
		if (event.getViewers().size() == 1 && event.getCurrentItem().getMaxStackSize() == 1)
			crafter = event.getViewers().get(0).getName() + " Crafting";

		//Give Result
		for (int i=0 ; i<make ; i++)
		{
			ItemStack result = CreateStack(id, data, amount, null, new String[] {}, crafter);

			if (result != null)
				event.getWhoClicked().getInventory().addItem(result);
		}

		//Shedule Update
		if (event.getWhoClicked() instanceof Player)
		{
			final Player player = (Player)event.getWhoClicked();
			_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
			{
				public void run()
				{
					UtilInv.Update(player);
				}
			}, 0);
		}
	}

	public ItemStack CreateStack(Material type)
	{
		return CreateStack(type.getId(), (byte)0, 1, (short)0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(int id)
	{
		return CreateStack(id, (byte)0, 1, (short)0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, int amount)
	{
		return CreateStack(type.getId(), (byte)0, amount, (short)0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(int id, int amount)
	{
		return CreateStack(id, (byte)0, amount, (short)0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data)
	{
		return CreateStack(type.getId(), data, 1, (short)0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(int id, byte data)
	{
		return CreateStack(id, data, 1, (short)0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount)
	{
		return CreateStack(type.getId(), data, amount, (short)0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(int id, byte data, int amount)
	{
		return CreateStack(id, data, amount, (short)0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name)
	{
		return CreateStack(type.getId(), data, amount, (short)0, name, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, boolean unbreakable)
	{
		return CreateStack(type.getId(), data, amount, (short)0, name, new String[] {}, null, unbreakable);
	}

	public ItemStack CreateStack(int id, byte data, int amount, String name)
	{
		return CreateStack(id, data, amount, (short)0, name, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, List<String> lore)
	{
		return CreateStack(type.getId(), data, amount, (short)0, name, lore, null);
	}

	public ItemStack CreateStack(int id, byte data, int amount, String name, List<String> lore)
	{
		return CreateStack(id, data, amount, (short)0, name, lore, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, String[] lore)
	{
		return CreateStack(type.getId(), data, amount, (short)0, name, ArrayToList(lore), null);
	}

	public ItemStack CreateStack(int id,  byte data, int amount, String name, String[] lore)
	{
		return CreateStack(id, data, amount, (short)0, name, ArrayToList(lore), null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, short damage, String name, String[] lore)
	{
		return CreateStack(type.getId(), data, amount, damage, name, ArrayToList(lore), null);
	}

	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, String[] lore)
	{
		return CreateStack(id, data, amount, damage, name, ArrayToList(lore), null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, short damage, String name, List<String> lore)
	{
		return CreateStack(type.getId(), data, amount, damage, name, lore, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, List<String> lore, String owner)
	{
		return CreateStack(type.getId(), data, amount, (short)0, name, lore, owner);
	}

	public ItemStack CreateStack(int id, byte data, int amount, String name, List<String> lore, String owner)
	{
		return CreateStack(id, data, amount, (short)0, name, lore, owner);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, String[] lore, String owner)
	{
		return CreateStack(type.getId(), data, amount, (short)0, name, ArrayToList(lore), owner);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, String[] lore, String owner, boolean unbreakable)
	{
		return CreateStack(type.getId(), data, amount, (short)0, name, ArrayToList(lore), owner, unbreakable, 0, null);
	}

	public ItemStack CreateStack(int id,  byte data, int amount, String name, String[] lore, String owner)
	{
		return CreateStack(id, data, amount, (short)0, name, ArrayToList(lore), owner);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, short damage, String name, String[] lore, String owner)
	{
		return CreateStack(type.getId(), data, amount, damage, name, ArrayToList(lore), owner);
	}

	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, String[] lore, String owner)
	{
		return CreateStack(id, data, amount, damage, name, ArrayToList(lore), owner);
	}

	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, String[] lore, String owner, boolean unbreakable)
	{
		return CreateStack(id, data, amount, damage, name, ArrayToList(lore), owner, unbreakable, 0, null);
	}


	public ItemStack CreateStack(Material type, byte data, int amount, short damage, String name, List<String> lore, String owner)
	{
		return CreateStack(type.getId(), data, amount, damage, name, lore, owner);
	}

	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, List<String> lore, String owner)
	{
		return CreateStack(id, data, amount, damage, name, lore, owner, true, 0, null);
	}

	@SuppressWarnings("deprecation")
	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, List<String> lore, String owner, boolean unbreakable)
	{
		return CreateStack(id, data, amount, damage, name, lore, owner, unbreakable, 0, null);
	}
	
	public ItemStack CreateStack(Material type, Integer enchLevel, Enchantment... enchantments)
	{
		return CreateStack(type.getId(), (byte)0, 1, (short)0, null, ArrayToList(new String[] {}), null, true, enchLevel, enchantments);
	}
	
	//XXX Owner Variant End

	@SuppressWarnings("deprecation")
	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, List<String> lore, String owner, boolean unbreakable, Integer enchLevel, Enchantment... enchantments)
	{	
		ItemStack stack;
		if (data == 0) 	
			stack = new ItemStack(id, amount, damage);
		else			
			stack = new ItemStack(id, amount, damage, data);

		ItemMeta itemMeta = stack.getItemMeta();

		if (itemMeta == null)
			return null;

		boolean setMeta = false;

		//Set Name
		if (name != null)	
		{
			itemMeta.setDisplayName(name);
			setMeta = true;
		}	
		else if (_customNames)			
		{
			itemMeta.setDisplayName(GetName(stack, true));
			setMeta = true;
		}

		//Default Lore
		if (_lores != null && _lores.containsKey(id) && _lores.get(id).containsKey(data) && lore == null)
		{
			itemMeta.setLore(ArrayToList(_lores.get(id).get(data)));
			setMeta = true;
		}

		//Owner Lore
		if (owner != null)
		{
			String[] tokens = owner.split(" ");

			String[] ownerLore = new String[tokens.length + 2];

			ownerLore[0] = C.cGray + "Owner: " + C.cAqua + tokens[0];

			if (ownerLore.length >= 3)
				ownerLore[1] = C.cGray + "Source: " + C.cAqua + tokens[1];

			ownerLore[ownerLore.length - 2] = C.cGray + "Created: " + C.cAqua + UtilTime.date();

			ownerLore[ownerLore.length - 1] = "";

			if (itemMeta.getLore() != null)		itemMeta.setLore(CombineLore(itemMeta.getLore(), ArrayToList(ownerLore)));
			else								itemMeta.setLore(ArrayToList(ownerLore));

			setMeta = true;
		}

		//Set Lore
		if (lore != null)	
		{
			if (itemMeta.getLore() != null)		itemMeta.setLore(CombineLore(itemMeta.getLore(), lore));
			else								itemMeta.setLore(lore);

			setMeta = true;
		}

		if (setMeta)
			stack.setItemMeta(itemMeta);
		
		if(enchantments != null)
		{
			for(Enchantment enchantment : enchantments)
			{
				stack.addUnsafeEnchantment(enchantment, enchLevel);
			}
		}
		
		//Unbreakable
		if (unbreakable)
		{
			if (stack.getType().getMaxDurability() > 1)
			{
				ItemMeta meta = stack.getItemMeta();
				meta.spigot().setUnbreakable(true);
				stack.setItemMeta(meta);
			}
		}

		return stack;
	}

	public void addOwnerLore(ItemStack item, String owner)
	{
		if (owner != null)
		{
			ItemMeta itemMeta = item.getItemMeta();

			String[] tokens = owner.split(" ");

			String[] ownerLore = new String[tokens.length + 2];

			ownerLore[0] = C.cGray + "Owner: " + C.cAqua + tokens[0];

			if (ownerLore.length >= 3)
				ownerLore[1] = C.cGray + "Source: " + C.cAqua + tokens[1];

			ownerLore[ownerLore.length - 2] = C.cGray + "Created: " + C.cAqua + UtilTime.date();

			ownerLore[ownerLore.length - 1] = "";

			if (itemMeta.getLore() != null)		itemMeta.setLore(CombineLore(itemMeta.getLore(), ArrayToList(ownerLore)));
			else								itemMeta.setLore(ArrayToList(ownerLore));

			item.setItemMeta(itemMeta);
		}
	}

	private List<String> CombineLore(List<String> A, List<String> B) 
	{
		for (String b : B)
			A.add(b);

		return A;
	}

	public List<String> ArrayToList(String[] array)
	{
		if (array.length == 0)
			return null;

		List<String> list = new ArrayList<String>();

		for (String cur : array)
			list.add(cur);

		return list;
	}

	public String GetLoreVar(ItemStack stack, String var)
	{
		if (stack == null)
			return null;

		ItemMeta meta = stack.getItemMeta();

		if (meta == null)
			return null;

		if (meta.getLore() == null)
			return null;

		for (String cur : meta.getLore())
			if (cur.contains(var))
			{
				int index = var.split(" ").length;

				String[] tokens = cur.split(" ");

				String out = "";
				for (int i=index ; i<tokens.length ; i++)
					out += tokens[i] + " ";

				if (out.length() > 0)
					out = out.substring(0, out.length()-1);

				return out;
			}

		return null;
	}

	public int GetLoreVar(ItemStack stack, String var, int empty)
	{
		if (stack == null)
			return empty;

		ItemMeta meta = stack.getItemMeta();

		if (meta == null)
			return empty;

		if (meta.getLore() == null)
			return empty;

		for (String cur : meta.getLore())
			if (cur.contains(var))
			{
				String[] tokens = cur.split(" ");

				try
				{
					return Integer.parseInt(tokens[tokens.length - 1]);
				}
				catch (Exception e)
				{
					return empty;
				}

			}

		return empty;
	}

	public void SetLoreVar(ItemStack stack, String var, String value)
	{
		if (stack == null)
			return;

		ItemMeta meta = stack.getItemMeta();

		if (meta == null)
			return;

		ArrayList<String> newLore = new ArrayList<String>();

		boolean inserted = false;

		if (meta.getLore() != null)
			for (String lore : meta.getLore())
			{
				if (!lore.contains(var))
				{
					newLore.add(lore);
				}
				else
				{
					newLore.add(C.cGray + var + ":" + C.cGreen + " " + value);
					inserted = true;
				}
			}

		if (!inserted)
			newLore.add(C.cGray + var + ":" + C.cGreen + " " + value);

		meta.setLore(newLore);

		stack.setItemMeta(meta);
	}

	public void SetUseCustomNames(boolean var)
	{
		_customNames = var;
	}

	public void SetCustomNameFormat(String format)
	{
		_nameFormat = format;
	}

	public ItemStack createColoredLeatherArmor(int slot, Color color)
	{
		Material material = Material.LEATHER_HELMET;
		switch (slot)
		{
			case 0:
				material = Material.LEATHER_HELMET;
				break;
			case 1:
				material = Material.LEATHER_CHESTPLATE;
				break;
			case 2:
				material = Material.LEATHER_LEGGINGS;
				break;
			case 3:
				material = Material.LEATHER_BOOTS;
				break;
		}
		ItemStack stack = CreateStack(material);
		LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) stack.getItemMeta();
		leatherArmorMeta.setColor(color);
		stack.setItemMeta(leatherArmorMeta);
		return stack;
	}

	/**
	 * Creates a potion item stack
	 * @param potionType
	 * @param level
	 * @return
	 */
	public ItemStack createCustomPotion(PotionType potionType, int level)
	{
		Potion potion = new Potion(potionType, level);
		return potion.toItemStack(1);
	}

	/**
	 * Creates a potion item stack
	 * @param potionType
	 * @return
	 */
	public ItemStack createCustomPotion(PotionType potionType)
	{
		return createCustomPotion(potionType, 1);
	}


}
