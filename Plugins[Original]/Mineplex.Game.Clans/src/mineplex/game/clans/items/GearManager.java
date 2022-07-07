package mineplex.game.clans.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.weight.Weight;
import mineplex.core.common.weight.WeightSet;
import mineplex.core.donation.DonationManager;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.game.clans.items.attributes.AttributeContainer;
import mineplex.game.clans.items.attributes.AttributeType;
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
import mineplex.game.clans.items.commands.GearCommand;
import mineplex.game.clans.items.commands.RuneCommand;
import mineplex.game.clans.items.economy.GoldToken;
import mineplex.game.clans.items.legendaries.AlligatorsTooth;
import mineplex.game.clans.items.legendaries.DemonicScythe;
import mineplex.game.clans.items.legendaries.GiantsBroadsword;
import mineplex.game.clans.items.legendaries.HyperAxe;
import mineplex.game.clans.items.legendaries.KnightLance;
import mineplex.game.clans.items.legendaries.LegendaryItem;
import mineplex.game.clans.items.legendaries.MagneticMaul;
import mineplex.game.clans.items.legendaries.MeridianScepter;
import mineplex.game.clans.items.legendaries.WindBlade;
import mineplex.game.clans.items.rares.RareItem;
import mineplex.game.clans.items.rares.RunedPickaxe;
import mineplex.game.clans.items.runes.RuneManager;
import mineplex.game.clans.items.smelting.SmeltingListener;
import mineplex.game.clans.items.ui.GearShop;
import mineplex.serverdata.serialization.RuntimeTypeAdapterFactory;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import net.minecraft.server.v1_8_R3.PacketPlayOutWindowItems;
/**
 * Handles converting legendary itemstacks to their respective CustomItem objects
 */
public class GearManager extends MiniPlugin implements IPacketHandler, Runnable
{
	public enum Perm implements Permission
	{
		RUNE_COMMAND,
		GEAR_COMMAND,
	}

	private static final String ITEM_SERIALIZATION_TAG = "-JSON-";
	private static final Gson GSON;

	// Weightings for randomly selecting number of attributes (1, 2, 3)
	private static final WeightSet<Integer> ATTRIBUTE_WEIGHTS = new WeightSet<Integer>(
			new Weight<>(3, 3),
			new Weight<>(20, 2),
			new Weight<>(77, 1)
	);

	// Weightings for randomly selecting item type (legendary/weapon/armor/bow)
	private static final WeightSet<ItemType> TYPE_WEIGHTS = new WeightSet<ItemType>(
			new Weight<>(9, ItemType.LEGENDARY),
			new Weight<>(9, ItemType.RARE),
			new Weight<>(34, ItemType.ARMOR),
			new Weight<>(25, ItemType.WEAPON),
			new Weight<>(23, ItemType.BOW)
	);

	private static final WeightSet<Class<? extends LegendaryItem>> LEGENDARY_WEIGHTS = new WeightSet<>(
			MeridianScepter.class,
			AlligatorsTooth.class,
			WindBlade.class,
			GiantsBroadsword.class,
			HyperAxe.class,
			MagneticMaul.class,
			KnightLance.class
	);

	private static final WeightSet<Class<? extends RareItem>> RARE_WEIGHTS = new WeightSet<>(
			RunedPickaxe.class
	);

	private static final WeightSet<Material> WEAPON_TYPES = new WeightSet<>(
			Material.DIAMOND_SWORD,
			Material.GOLD_SWORD,
			Material.IRON_SWORD,
			Material.DIAMOND_AXE,
			Material.GOLD_AXE,
			Material.IRON_AXE
	);

	private static final WeightSet<Material> ARMOR_TYPES = new WeightSet<>(
			Material.DIAMOND_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.DIAMOND_BOOTS,
			Material.IRON_HELMET,
			Material.IRON_CHESTPLATE,
			Material.IRON_LEGGINGS,
			Material.IRON_BOOTS,
			Material.GOLD_HELMET,
			Material.GOLD_CHESTPLATE,
			Material.GOLD_LEGGINGS,
			Material.GOLD_BOOTS
	);

	private static final WeightSet<Class<? extends ItemAttribute>> WEAPON_ATTRIBUTES = new WeightSet<>(
			FrostedAttribute.class,
			SharpAttribute.class,
			JaggedAttribute.class,
			HasteAttribute.class,
			FlamingAttribute.class,
			ConqueringAttribute.class
	);

	private static final WeightSet<Class<? extends ItemAttribute>> ARMOR_ATTRIBUTES = new WeightSet<>(
			SlantedAttribute.class,
			ReinforcedAttribute.class,
			ConqueringArmorAttribute.class,
			PaddedAttribute.class,
			LavaAttribute.class
	);

	private static final WeightSet<Class<? extends ItemAttribute>> BOW_ATTRIBUTES = new WeightSet<>(
			HeavyArrowsAttribute.class,
			HuntingAttribute.class,
			InverseAttribute.class,
			LeechingAttribute.class,
			RecursiveAttribute.class,
			ScorchingAttribute.class,
			SlayingAttribute.class
	);

	// Attribute Masks
	private static final EnumSet<Material> MASK_ATTRIBUTES = EnumSet.of(
			Material.GOLD_RECORD,
			Material.GREEN_RECORD,
			Material.RECORD_3,
			Material.RECORD_4,
			Material.RECORD_5,
			Material.RECORD_6,
			Material.RECORD_7,
			Material.RECORD_8,
			Material.RECORD_9,
			Material.RECORD_10,
			Material.RECORD_11,
			Material.RECORD_12,
			Material.RABBIT_FOOT
	);

	static
	{
		// Initialize attribute types factory for JSON handling of polymorphism.
		RuntimeTypeAdapterFactory<ItemAttribute> attributeFactory = RuntimeTypeAdapterFactory.of(ItemAttribute.class);
		ARMOR_ATTRIBUTES.elements().forEach(attributeFactory::registerSubtype);
		WEAPON_ATTRIBUTES.elements().forEach(attributeFactory::registerSubtype);
		BOW_ATTRIBUTES.elements().forEach(attributeFactory::registerSubtype);

		// Initialize legendary item type factory for JSON handling of polymorphism.
		RuntimeTypeAdapterFactory<CustomItem> customItemType = RuntimeTypeAdapterFactory.of(CustomItem.class);
		customItemType.registerSubtype(CustomItem.class);
		customItemType.registerSubtype(LegendaryItem.class);
		customItemType.registerSubtype(RareItem.class);
		customItemType.registerSubtype(GoldToken.class);
		LEGENDARY_WEIGHTS.elements().forEach(customItemType::registerSubtype);
		RARE_WEIGHTS.elements().forEach(customItemType::registerSubtype);
		customItemType.registerSubtype(DemonicScythe.class);

		// Build GSON instance off factories for future serialization of items.
		GSON = new GsonBuilder().registerTypeAdapterFactory(attributeFactory).registerTypeAdapterFactory(customItemType).create();
	}

	private static Map<UUID, CustomItem> _customItemCache = new HashMap<>();
	private static GearManager _instance; // Singleton instance

	// Mapping of player names (key) to cached gear set (value).
	private Map<Player, PlayerGear> _playerGears = new HashMap<>();
	
	private GearShop _shop;
	private RuneManager _rune;

	public GearManager(JavaPlugin plugin, PacketHandler packetHandler, CoreClientManager clientManager, DonationManager donationManager)
	{
		super("CustomGear", plugin);

		if (_instance != null)
		{
			throw new RuntimeException("GearManager is already initialized");
		}

		_instance = this;
		
		_shop = new GearShop(this, clientManager, donationManager);
		_rune = new RuneManager("Rune", plugin);

		// Register listeners
		UtilServer.getServer().getPluginManager().registerEvents(new ItemListener(getPlugin()), getPlugin());
		UtilServer.getServer().getPluginManager().registerEvents(new SmeltingListener(), getPlugin());

		packetHandler.addPacketHandler(this, PacketPlayOutSetSlot.class, PacketPlayOutWindowItems.class);

		plugin.getServer().getScheduler().runTaskTimer(plugin, this, 1L, 1L);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.RUNE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.GEAR_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GearCommand(this));
		addCommand(new RuneCommand(this));
	}
	
	public RuneManager getRuneManager()
	{
		return _rune;
	}

	/**
	 * Tick & update internal logic for {@link GearManager}. Called once per tick.
	 */
	@Override
	public void run()
	{
		Iterator<PlayerGear> iterator = _playerGears.values().iterator();
		while (iterator.hasNext())
		{
			PlayerGear gear = iterator.next();
			if (gear.cleanup())
			{
				iterator.remove();
			}
			else
			{
				gear.update();
			}
		}
	}

	/**
	 * @param player - the player whose {@link PlayerGear} set is to be fetched.
	 * @return the cached or newly instantiated {@link PlayerGear} associated
	 * with {@code player}.
	 */
	public PlayerGear getPlayerGear(Player player)
	{
		return _playerGears.computeIfAbsent(player, PlayerGear::new);
	}
	
	public Set<Class<? extends LegendaryItem>> getFindableLegendaries()
	{
		return LEGENDARY_WEIGHTS.elements();
	}
	
	public Set<Class<? extends ItemAttribute>> getArmorAttributes()
	{
		return ARMOR_ATTRIBUTES.elements();
	}
	
	public Set<Class<? extends ItemAttribute>> getBowAttributes()
	{
		return BOW_ATTRIBUTES.elements();
	}
	
	public Set<Class<? extends ItemAttribute>> getWeaponAttributes()
	{
		return WEAPON_ATTRIBUTES.elements();
	}

	public CustomItem generateItem()
	{
		int attributeCount = ATTRIBUTE_WEIGHTS.generateRandom();
		ItemType itemType = TYPE_WEIGHTS.generateRandom();

		RareItemFactory factory = RareItemFactory.begin(itemType);

		if (itemType == ItemType.RARE)
		{
			factory.setRare(RARE_WEIGHTS.generateRandom());
		}
		else if (itemType == ItemType.LEGENDARY)
		{
			factory.setLegendary(LEGENDARY_WEIGHTS.generateRandom());
		}
		else if (itemType == ItemType.ARMOR)
		{
			factory.setType(ARMOR_TYPES.generateRandom());
		}
		else if (itemType == ItemType.WEAPON)
		{
			factory.setType(WEAPON_TYPES.generateRandom());
		}
		else if (itemType == ItemType.BOW)
		{
			factory.setType(Material.BOW);
		}

		if (itemType != ItemType.LEGENDARY || (UtilMath.random.nextDouble() <= .35 && factory.getMaterial() != Material.RECORD_6)) // Melee Legendaries have a chance to spawn with attributes
		{
			AttributeContainer attributes = new AttributeContainer();
			generateAttributes(attributes, itemType, attributeCount);

			System.out.println("Generating attributes...");
			System.out.println("Remaining size: " + attributes.getRemainingTypes().size());

			if (attributes.getSuperPrefix() != null)
			{
				System.out.println("Set super prefix: " + attributes.getSuperPrefix().getClass());
				factory.setSuperPrefix(attributes.getSuperPrefix().getClass());
			}
			if (attributes.getPrefix() != null)
			{
				System.out.println("Set prefix: " + attributes.getPrefix().getClass());
				factory.setPrefix(attributes.getPrefix().getClass());
			}
			if (attributes.getSuffix() != null)
			{
				System.out.println("Set suffix: " + attributes.getSuffix().getClass());
				factory.setSuffix(attributes.getSuffix().getClass());
			}
		}

		return factory.getWrapper();
	}

	public void generateAttributes(AttributeContainer container, ItemType type, int count)
	{
		for (int i = 0; i < count; i++)
		{
			int attempts = 0;
			Set<AttributeType> remaining = container.getRemainingTypes();
			ItemAttribute attribute = null;

			while (remaining.size() > 0 && attempts < 10 && attribute == null)
			{
				ItemAttribute sampleAttribute = null;

				switch (type)
				{
					case ARMOR:
						sampleAttribute = instantiate(ARMOR_ATTRIBUTES.generateRandom());
						break;
					case WEAPON:
						sampleAttribute = instantiate(WEAPON_ATTRIBUTES.generateRandom());
						break;
					case LEGENDARY:
						sampleAttribute = instantiate(WEAPON_ATTRIBUTES.generateRandom());
						break;
					case BOW:
						sampleAttribute = instantiate(BOW_ATTRIBUTES.generateRandom());
						break;
					default:
						break;
				}

				if (sampleAttribute != null && remaining.contains(sampleAttribute.getType()))
				{
					attribute = sampleAttribute; // Select valid attribute to add
				}

				attempts++;
			}
			
			if (attribute != null)
			{
				container.addAttribute(attribute);
			}
		}
	}

	public void spawnItem(Location location)
	{
		CustomItem item = generateItem();
		if (item.getMaterial() == Material.RECORD_4 || item.getMaterial() == Material.GOLD_RECORD || item.getMaterial() == Material.RECORD_3 || item.getMaterial() == Material.RECORD_5 || item.getMaterial() == Material.RECORD_6 || item.getMaterial() == Material.GREEN_RECORD || item.getMaterial() == Material.RECORD_12)
		{
			UtilFirework.playFirework(location, Type.BALL, Color.RED, true, false);
		}
		else
		{
			UtilFirework.playFirework(location, Type.BALL, Color.AQUA, true, false);
		}
		location.getWorld().dropItem(location, item.toItemStack());
	}

	public static CustomItem parseItem(ItemStack item)
	{
		if (item == null)
		{
			return null;
		}
		Map<String, NBTBase> data = getUnhandledTags(item);
		if (data == null)
		{
			return null;
		}
		if (data.containsKey("gearmanager.uuid"))
		{
			String strUUID = ((NBTTagString) data.get("gearmanager.uuid")).a_();
			try
			{
				UUID uuid = UUID.fromString(strUUID);
				CustomItem customItem = _customItemCache.get(uuid);
				if (customItem == null)
				{
					String json = ((NBTTagString) data.get("gearmanager.json")).a_();
					customItem = deserialize(json);
					_customItemCache.put(uuid, customItem);
				}
				return customItem;
			}
			// Not an UUID?
			catch (IllegalArgumentException exception)
			{
				if (!data.containsKey("gearmanager.warnuuid"))
				{
					// Add to the lore that this item is corrupted
					List<String> lore = item.getItemMeta().getLore();
					lore.add("");
					lore.add(C.cRedB + "Corrupted item (Error 1)");
					data.put("gearmanager.warnuuid", new NBTTagByte((byte) 1));
					exception.printStackTrace();
					saveUnhandledTags(item, data);
				}
				return null;
			}
			// Failed to parse
			catch (JsonSyntaxException exception)
			{
				if (!data.containsKey("gearmanager.warnsyntax"))
				{
					// Add to the lore that this item is corrupted
					List<String> lore = item.getItemMeta().getLore();
					lore.add("");
					lore.add(C.cRedB + "Corrupted item (Error 2)");
					data.put("gearmanager.warnsyntax", new NBTTagByte((byte) 1));
					System.out.println(((NBTTagString) data.get("gearmanager.json")).a_());
					exception.printStackTrace();
					saveUnhandledTags(item, data);
				}
				return null;
			}
			// Other
			catch (Exception exception)
			{
				if (!data.containsKey("gearmanager.warnother"))
				{
					// Add to the lore that this item is corrupted
					List<String> lore = item.getItemMeta().getLore();
					lore.add("");
					lore.add(C.cRedB + "Corrupted item (Error 3)");
					data.put("gearmanager.warnother", new NBTTagByte((byte) 1));
					exception.printStackTrace();
					saveUnhandledTags(item, data);
				}
				return null;
			}
		}
		
		return null;
	}

	public static void writeNBT(CustomItem customItem, ItemStack item)
	{
		Map<String, NBTBase> data = getUnhandledTags(item);
		data.put("gearmanager.uuid", new NBTTagString(customItem._uuid));
		data.put("gearmanager.json", new NBTTagString(serialize(customItem)));
		saveUnhandledTags(item, data);
	}

	public static boolean isCustomItem(ItemStack item)
	{
		Map<String, NBTBase> data = getUnhandledTags(item);
		if (data.containsKey("gearmanager.uuid") && data.containsKey("gearmanager.json"))
		{
			return true;
		}
		
		return false;
	}

	/**
	 * @param type - the class-type of the object to be instantiated. (must have
	 *             zero-argument constructor)
	 * @return a newly instantiated instance of {@code type} class-type.
	 * Instantied with zero argument constructor.
	 */
	private static <T> T instantiate(Class<T> type)
	{
		try
		{
			return type.newInstance();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static String serialize(CustomItem customItem)
	{
		return GSON.toJson(customItem, CustomItem.class);
	}

	public static CustomItem deserialize(String serialization)
	{
		return GSON.fromJson(serialization, CustomItem.class);
	}

	/**
	 * @return singleton instance of {@link GearManager}.
	 */
	public static GearManager getInstance()
	{
		return _instance;
	}

	/**
	 * @param player - the player to see if they should have their out-going
	 *               packets masked on CustomGear items.
	 * @return true, if the player should have their gear lore masked, false
	 * otherwise.
	 */
	private boolean maskGearPacket(Player player)
	{
		return player.getGameMode() != GameMode.CREATIVE;
	}

	public void handle(PacketInfo packetInfo)
	{
		// Don't mask custom gear lore for creative players, as this will break them.
		// To be precise, the lore is added more than once because the creative client spawns in new items with the existing lore
		// fixme
		if (!maskGearPacket(packetInfo.getPlayer())) return;

		Packet<?> packet = packetInfo.getPacket();

		if (packet instanceof PacketPlayOutSetSlot)
		{
			PacketPlayOutSetSlot slotPacket = (PacketPlayOutSetSlot) packet;
			slotPacket.c = maskItem(slotPacket.c, packetInfo.getPlayer()); // Mask all out-going item packets
		}
		else if (packet instanceof PacketPlayOutWindowItems)
		{
			PacketPlayOutWindowItems itemsPacket = (PacketPlayOutWindowItems) packet;

			for (int i = 0; i < itemsPacket.b.length; i++)
			{
				itemsPacket.b[i] = maskItem(itemsPacket.b[i], packetInfo.getPlayer()); // Mask all out-going item packets
				ItemStack item = CraftItemStack.asCraftMirror(itemsPacket.b[i]);
				if (item != null && MASK_ATTRIBUTES.contains(item.getType()))
					itemsPacket.b[i] = removeAttributes(itemsPacket.b[i]);
			}
		}
	}

	private net.minecraft.server.v1_8_R3.ItemStack removeAttributes(net.minecraft.server.v1_8_R3.ItemStack item)
	{
		if (item == null) return null;

		if (item.getTag() == null)
		{
			item.setTag(new NBTTagCompound());
		}

		item.getTag().setInt("HideFlags", 62);

		return item;
	}

	private net.minecraft.server.v1_8_R3.ItemStack maskItem(net.minecraft.server.v1_8_R3.ItemStack item, Player player)
	{
		// Cannot mask a null item
		if (item == null)
		{
			return null;
		}

		CraftItemStack originalItem = CraftItemStack.asCraftMirror(item.cloneItemStack());
		ItemMeta originalMeta = originalItem.getItemMeta();

		// No need to modify item packets with no lore
		if (originalMeta == null || originalMeta.getLore() == null)
		{
			return item;
		}

		List<String> newLore = cleanseLore(originalMeta.getLore());

		CustomItem ci = parseItem(originalItem);

		if (ci != null && LEGENDARY_WEIGHTS.elements().contains(ci.getClass()))
		{
			String originalOwner = null;
			if (ci.OriginalOwner == null)
			{
				originalOwner = "You";
			}
			else
			{
				if (player.getUniqueId().toString().equals(ci.OriginalOwner))
				{
					originalOwner = "You";
				}
				else
				{
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(ci.OriginalOwner));
					originalOwner = offlinePlayer.getName();
				}
			}
			newLore.add(" ");
			newLore.add(C.cWhite + "Original Owner: " + C.cYellow + originalOwner);
		}
		if (ci != null)
		{
			newLore.add(" ");
			newLore.add(C.cWhite + "UUID: " + C.cYellow + ci._uuid);
		}

		net.minecraft.server.v1_8_R3.ItemStack newItem = CraftItemStack.asNMSCopy(originalItem);
		CraftItemStack newCopy = CraftItemStack.asCraftMirror(newItem);
		ItemMeta newMeta = newCopy.getItemMeta();
		newMeta.setLore(newLore);
		newCopy.setItemMeta(newMeta);
		return newItem;
	}

	private List<String> cleanseLore(List<String> input)
	{
		List<String> cleansed = new ArrayList<>();
		for (String s : input)
		{
			if (!s.startsWith(ITEM_SERIALIZATION_TAG))
			{
				cleansed.add(s);
			}
		}
		return cleansed;
	}

	public void openShop(Player player)
	{
		_shop.attemptShopOpen(player);
	}

	// WARNING
	// This will not be persistent if the ItemStack is a block and placed then picked up
	private static Map<String, NBTBase> getUnhandledTags(ItemStack itemStack)
	{
		net.minecraft.server.v1_8_R3.ItemStack handle = ((CraftItemStack) itemStack).getHandle();
		if (handle == null)
			return Collections.emptyMap();

		NBTTagCompound tag = handle.getTag();

		if (tag == null)
			return Collections.emptyMap();

		Map<String, NBTBase> unhandled = new HashMap<>();
		for (String name : tag.c())
		{
			unhandled.put(name, tag.get(name));
		}
		return unhandled;
	}

	private static void saveUnhandledTags(ItemStack itemStack, Map<String, NBTBase> map)
	{
		net.minecraft.server.v1_8_R3.ItemStack handle = ((CraftItemStack) itemStack).getHandle();
		NBTTagCompound tag = handle.getTag();

		if (tag != null)
		{
			for (String name : map.keySet())
			{
				tag.set(name, map.get(name));
			}
		}
	}

	public static void save(ItemStack itemStack, boolean remove)
	{
		CustomItem item = parseItem(itemStack);
		if (item != null)
		{
			Map<String, NBTBase> data = getUnhandledTags(itemStack);
			data.put("gearmanager.json", new NBTTagString(serialize(item)));
			saveUnhandledTags(itemStack, data);
			if (remove)
			{
				_customItemCache.remove(UUID.fromString(item._uuid));
			}
		}
	}

	public static void cleanup()
	{
		Iterator<CustomItem> it = _customItemCache.values().iterator();
		while (it.hasNext())
		{
			CustomItem item = it.next();
			if (item._lastUser == null || !item._lastUser.isOnline())
			{
				it.remove();
			}
		}
	}
}