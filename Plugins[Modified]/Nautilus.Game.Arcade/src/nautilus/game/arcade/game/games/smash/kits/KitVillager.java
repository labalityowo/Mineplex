package nautilus.game.arcade.game.games.smash.kits;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseVillager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.game.kit.KitAvailability;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.SuperSmash;
import nautilus.game.arcade.game.games.smash.events.SmashActivateEvent;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.villager.PerkArts;
import nautilus.game.arcade.game.games.smash.perks.villager.PerkSonicBoom;
import nautilus.game.arcade.game.games.smash.perks.villager.PerkVillagerShot;
import nautilus.game.arcade.game.games.smash.perks.villager.SmashVillager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitVillager extends SmashKit
{

	private static final Perk[] PERKS = {
			new PerkSmashStats(),
			new PerkDoubleJump("Double Jump"),
			new PerkSonicBoom(),
			new PerkVillagerShot(),
			new PerkArts(),
			new SmashVillager()
	};

	private static final ItemStack IN_HAND = new ItemStack(Material.WHEAT);

	private static final ItemStack[] PLAYER_ITEMS = {
			ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
					C.cYellowB + "Right-Click" + C.cWhiteB + " - " + C.cGreenB + "Sonic Hurr",
					new String[]{
							C.Reset + "Screech at the top of your lungs piercing players ears",
							C.Reset + "dealing damage and knockback in front of you.",
					}),
			ItemStackFactory.Instance.CreateStack(Material.IRON_HOE, (byte) 0, 1,
					C.cYellowB + "Right-Click" + C.cWhiteB + " - " + C.cGreenB + "Trade Scatter",
					new String[]{
							C.Reset + "After a hard days work of trading with the players,",
							C.Reset + "you unload your goods upon your enemies,",
							C.Reset + "propelling you back or forth depending on your trade skills",
							C.Reset + "and throwing your favorite items in the opposite direction."
					}),
			ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
					C.cYellowB + "Right-Click" + C.cWhiteB + " - " + C.cGreenB + "Cycle Arts",
					new String[]{
							C.Reset + "Use your schooling from villager academy to hone in on one of",
							C.Reset + "three arts you specialized in and that give you different stats.",
							C.Reset + "Press right click to switch between arts and drop to activate.",
					}),
			ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
					C.cYellowB + "Smash Crystal" + C.cWhiteB + " - " + C.cGreenB + "Perfection",
					new String[]{
							C.Reset + "Master all of the three arts and reaching perfection!",
							C.Reset + "You gain all of the positive effects from all three arts."
					}),
	};

	public static final int ART_ACTIVE_SLOT = 2;
	public static final int ART_VISUAL_SLOT = 7;

	private static final ItemStack[] PLAYER_ARMOR_NORMAL = {
			ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
			ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
			ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
			null,
	};
	private static final ItemStack[] PLAYER_ARMOR_ATTACK = {
			ItemStackFactory.Instance.CreateStack(Material.GOLD_BOOTS),
			ItemStackFactory.Instance.CreateStack(Material.GOLD_LEGGINGS),
			ItemStackFactory.Instance.CreateStack(Material.GOLD_CHESTPLATE),
			null,
	};

	private static final ItemStack[] PLAYER_ARMOR_DEFENSE = {
			ItemStackFactory.Instance.CreateStack(Material.DIAMOND_BOOTS),
			ItemStackFactory.Instance.CreateStack(Material.DIAMOND_LEGGINGS),
			ItemStackFactory.Instance.CreateStack(Material.IRON_CHESTPLATE),
			null,
	};

	private static final ItemStack[] PLAYER_ARMOR_SPEED = {
			null,
			null,
			ItemStackFactory.Instance.CreateStack(Material.DIAMOND_CHESTPLATE),
			null,
	};


	private final Map<Player, VillagerType> _types = new HashMap<>();

	public KitVillager(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_VILLAGER, PERKS, DisguiseVillager.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		_types.putIfAbsent(player, VillagerType.ATTACK);
		VillagerType type = get(player);

		disguise(player);

		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);
		}
		else
		{
			player.getInventory().setItem(ART_ACTIVE_SLOT, getArtItem(type, false));
			player.getInventory().setItem(ART_VISUAL_SLOT, getArtVisualItem(player, type));
		}

		giveArmour(player, false);
	}

	public void giveArmour(Player player, boolean active)
	{
		if (!active)
		{
			player.getInventory().setArmorContents(PLAYER_ARMOR_NORMAL);
			return;
		}

		VillagerType type = get(player);
		player.getInventory().setArmorContents(type.getArmour());
	}

	public void updateDisguise(Player player, Profession profession)
	{
		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);

		if (disguise == null || !(disguise instanceof DisguiseVillager))
		{
			return;
		}

		((DisguiseVillager) disguise).setProfession(profession);
		Manager.GetDisguise().updateDisguise(disguise);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_types.remove(event.getPlayer());
	}

	@EventHandler
	public void smashActivate(SmashActivateEvent event)
	{
		Player player = event.getPlayer();

		if (HasKit(player))
		{
			player.getInventory().setArmorContents(PLAYER_ARMOR_DEFENSE);
		}
	}

	public VillagerType get(Player player)
	{
		return _types.get(player);
	}

	public void set(Player player, VillagerType type)
	{
		_types.put(player, type);
	}

	public ItemStack getArtItem(Player player, boolean active)
	{
		return getArtItem(get(player), active);
	}

	public ItemStack getArtItem(VillagerType type, boolean active)
	{
		return new ItemBuilder(Material.IRON_SPADE)
				.setTitle(C.cYellowB + "Right-Click/Drop" + C.cWhiteB + " - " + (active ? ChatColor.GRAY : type.getChatColour()) + C.Bold + type.getName())
				.setUnbreakable(true)
				.build();
	}

	public ItemStack getArtVisualItem(Player player, VillagerType type)
	{
		return getArtVisualItem(type, !Recharge.Instance.usable(player, type.getName()));
	}

	public ItemStack getArtVisualItem(VillagerType type, boolean active)
	{
		if (!active)
		{
			return new ItemBuilder(Material.INK_SACK, type.getDyeData())
					.setTitle(type.getChatColour() + C.Bold + type.getName())
					.build();
		}
		else
		{
			return new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.getByDyeData(type.getDyeData()).getWoolData())
					.setTitle(C.cGrayB + type.getName())
					.build();
		}
	}

	public VillagerType getActiveArt(Player player)
	{
		for (Perk perk : GetPerks())
		{
			if (perk instanceof PerkArts)
			{
				return ((PerkArts) perk).getActiveArt(player);
			}
		}

		return null;
	}

	public enum VillagerType
	{

		ATTACK("Butcher", ChatColor.RED, Color.RED, 1, Profession.BUTCHER, PLAYER_ARMOR_ATTACK),
		DEFENSE("Blacksmith", ChatColor.GOLD, Color.ORANGE, 14, Profession.BLACKSMITH, PLAYER_ARMOR_DEFENSE),
		SPEED("Speedster", ChatColor.GREEN, Color.LIME, 10, Profession.LIBRARIAN, PLAYER_ARMOR_SPEED);

		private final String _name;
		private final ChatColor _chatColour;
		private final Color _colour;
		private final byte _dyeData;
		private final Profession _profession;
		private final ItemStack[] _armour;

		VillagerType(String name, ChatColor chatColour, Color colour, int dyeData, Profession profession, ItemStack[] armour)
		{
			_name = name;
			_chatColour = chatColour;
			_colour = colour;
			_dyeData = (byte) dyeData;
			_profession = profession;
			_armour = armour;
		}

		public String getName()
		{
			return _name;
		}

		public ChatColor getChatColour()
		{
			return _chatColour;
		}

		public Color getColour()
		{
			return _colour;
		}

		public byte getDyeData()
		{
			return _dyeData;
		}

		public Profession getProfession()
		{
			return _profession;
		}

		public ItemStack[] getArmour()
		{
			return _armour;
		}

		public VillagerType getNext()
		{
			return ordinal() == values().length - 1 ? values()[0] : values()[ordinal() + 1];
		}
	}
}
