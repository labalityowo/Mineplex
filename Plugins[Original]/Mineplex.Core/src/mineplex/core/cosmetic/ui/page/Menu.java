package mineplex.core.cosmetic.ui.page;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.button.open.OpenCostumes;
import mineplex.core.cosmetic.ui.button.open.OpenGameModifiers;
import mineplex.core.cosmetic.ui.button.open.OpenItems;
import mineplex.core.cosmetic.ui.button.open.OpenPets;
import mineplex.core.cosmetic.ui.button.open.OpenWeaponNames;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.event.GadgetChangeEvent;
import mineplex.core.gadget.event.GadgetChangeEvent.GadgetState;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.pet.PetType;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;

public class Menu extends ShopPageBase<CosmeticManager, CosmeticShop>
{

	private static final String VISIBILITY_HUB = "Usable in Lobbies";
	private static final String VISIBILITY_EVERYWHERE = "Visible Everywhere";
	private static final String VISIBILITY_GAMES = "Visible in Games";
	private static final String VISIBILITY_GAME_HUB = "Visible in Game Lobbies";

	public Menu(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Cosmetics", player);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int deathSlot        = 19;
		int jumpSlot         = 28;
		int particleSlot     =  1;
		int winEffectSlot    = 13;
		int arrowSlot        = 10;
		int tauntSlot        = 31;
		int gameModifierSlot = 21;
		int kitSelectorSlot  = 23;
		int musicSlot        = 48;
		int mountSlot        = 50;
		int balloonsSlot     = 47;
		int petSlot          = 51;
		int gadgetSlot       = 49;
		int hatSlot          =  7;
		int flagSlot         = 34;
		int morphSlot        = 25;
		int costumeSlot      = 16;
		int weaponNameSlot   = 4;
		int powerPlaySlot 	 = 22;
		int chatColourSlot 	 = 40;

		EnumMap<GadgetType, Integer> ownedCount = new EnumMap<>(GadgetType.class);
		EnumMap<GadgetType, Integer> maxCount = new EnumMap<>(GadgetType.class);
		EnumMap<GadgetType, Gadget> enabled = new EnumMap<>(GadgetType.class);

		for(GadgetType type : GadgetType.values())
		{
			ownedCount.put(type, 0);
			maxCount.put(type, 0);

			List<Gadget> gadgets = getPlugin().getGadgetManager().getGadgets(type);
			if (gadgets != null)
			{
				for (Gadget gadget : gadgets)
				{
					if (gadget.ownsGadget(getPlayer()))
					{
						ownedCount.put(type, ownedCount.get(type) + 1);
					}

					maxCount.put(type, maxCount.get(type) + 1);
				}
			}

			final Gadget gadget = getPlugin().getGadgetManager().getActive(getPlayer(), type);
			if(gadget != null) enabled.put(type, gadget);
		}

		int petOwned = 0;
		int petMax = 0;
		for (PetType type : PetType.values())
		{
			Map<PetType, String> pets = getPlugin().getPetManager().Get(getPlayer()).getPets();
			if (pets != null && pets.containsKey(type))
			{
				petOwned++;
			}

			petMax++;
		}
		Entity petActive = getPlugin().getPetManager().getPet(getPlayer());

		GadgetType type = GadgetType.PARTICLE;
		String[] lore = getLore(ownedCount.get(type), maxCount.get(type), "Show everyone how cool you are with swirly particles that follow you when you walk!", VISIBILITY_EVERYWHERE, enabled.get(type));
		addButton(particleSlot, new ShopItem(Material.NETHER_STAR, "Particle Effects", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(particleSlot);

		type = GadgetType.ARROW_TRAIL;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Your arrows will now leave particle trails as they soar through the air.", VISIBILITY_GAMES, enabled.get(type));
		addButton(arrowSlot, new ShopItem(Material.ARROW, "Arrow Effects", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(arrowSlot);

		type = GadgetType.DOUBLE_JUMP;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Demonstrate your parkour prowess with sweet particles when you double jump.", VISIBILITY_EVERYWHERE, enabled.get(type));
		addButton(jumpSlot, new ShopItem(Material.GOLD_BOOTS, "Double Jump Effects", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(jumpSlot);

		type = GadgetType.DEATH;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Your death will now be mourned with a wonderful particle tribute.", VISIBILITY_GAMES, enabled.get(type));
		addButton(deathSlot, new ShopItem(Material.SKULL_ITEM, "Death Animations", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(deathSlot);

		type = GadgetType.ITEM;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "All sorts of zany contraptions to use on your friends and foes.", VISIBILITY_HUB, enabled.get(type));
		addButton(gadgetSlot, new ShopItem(Material.MELON_BLOCK, "Gadgets", lore, 1, false), new OpenItems(this, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(gadgetSlot);

		type = GadgetType.MORPH;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Have you ever wanted to be a tiger? Well, you can't be a tiger! That's silly! But you can be many other things!", VISIBILITY_HUB, enabled.get(type));
		addButton(morphSlot, new ShopItem(Material.LEATHER, "Morphs", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(morphSlot);

		type = GadgetType.MOUNT;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Why walk when you can ride? Summon fancy mounts to help you move in style.", VISIBILITY_HUB, enabled.get(type));
		addButton(mountSlot, new ShopItem(Material.IRON_BARDING, "Mounts", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(mountSlot);

		lore = getLore(petOwned, petMax, "Life on a server can get lonely sometimes. Summon an adorable pet to follow you around and cheer you up!", VISIBILITY_HUB, petActive == null ? null : petActive.getCustomName(), false);
		addButton(petSlot, new ShopItem(Material.BONE, "Pets", lore, 1, false), new OpenPets(this));
		if (petActive != null) addGlow(petSlot);

		type = GadgetType.HAT;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Hats are in this year. Wear them on your head to impress the others.", VISIBILITY_HUB, enabled.get(type));
		addButton(hatSlot, new ShopItem(Material.GOLD_HELMET, "Hats", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(hatSlot);

		type = GadgetType.COSTUME;
        // -4 Fixes more than the real costumes being counted (Happens because of the hub games costumes
		lore = getLore(ownedCount.get(type), maxCount.get(type) - 4, "Sometimes going out calls for special clothes! Gain bonus effects for matching outfit.", VISIBILITY_HUB, enabled.get(type));
		addButton(costumeSlot, new ShopItem(Material.DIAMOND_CHESTPLATE, "Costumes", lore, 1, false), new OpenCostumes(this, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(costumeSlot);

		type = GadgetType.MUSIC_DISC;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "I JUST WANT TO DANCE WITH YOU!", VISIBILITY_HUB, enabled.get(type));
		addButton(musicSlot, new ShopItem(Material.GREEN_RECORD, "Music", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(musicSlot);

		type = GadgetType.TAUNT;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Taunt your enemies or just show off. Use /taunt to have a good time!", VISIBILITY_GAMES, enabled.get(type));
		addButton(tauntSlot, new ShopItem(Material.NAME_TAG, "Taunts", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(tauntSlot);

		type = GadgetType.WIN_EFFECT;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Winning a game with your friends all good and dandy, but then being able to also show off awesome effects is even more fun!", VISIBILITY_GAMES, enabled.get(type));
		addButton(winEffectSlot, new ShopItem(Material.CAKE, "Win Effects", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(winEffectSlot);

		type = GadgetType.GAME_MODIFIER;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Cosmetics which are exclusive to the game you are playing!", VISIBILITY_GAMES, null);
		addButton(gameModifierSlot, new ShopItem(Material.TORCH, "Game Cosmetics", lore, 1, false), new OpenGameModifiers(this, null));
		if (enabled.containsKey(type)) addGlow(gameModifierSlot);

		type = GadgetType.BALLOON;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Balloons are collectibles that you can float above your head as you wander the lobby. You can have up to 3 balloons in your hand at one time.", VISIBILITY_HUB, enabled.get(type));
		addButton(balloonsSlot, new ShopItem(Material.LEASH, "Balloons", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(balloonsSlot);

		type = GadgetType.KIT_SELECTOR;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Click here to select different particles to indicate which kit you have selected!", VISIBILITY_GAME_HUB, enabled.get(type));
		addButton(kitSelectorSlot, new ShopItem(Material.LEVER, "Kit Selector Particles", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(kitSelectorSlot);

		type = GadgetType.FLAG;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Show off your country's flag!", VISIBILITY_HUB, enabled.get(type));
		addButton(flagSlot, new ShopItem(Material.BANNER, "Flags", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(flagSlot);

		type = GadgetType.WEAPON_NAME;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "\"Automagically\" changes your sword names to really show off how cool you are.", VISIBILITY_GAMES, null);
		addButton(weaponNameSlot, new ShopItem(Material.DIAMOND_SWORD, "Weapon Names", lore, 1, false), new OpenWeaponNames(this, null));
		if (enabled.containsKey(type)) addGlow(weaponNameSlot);

		type = GadgetType.LEVEL_PREFIX;
		lore = getLore(ownedCount.get(type), maxCount.get(type), "Changes the color of your level in chat!", VISIBILITY_HUB, enabled.get(type));
		addButton(chatColourSlot, new ShopItem(Material.PAPER, "Level Colors", lore, 1, false), generateButton(type, enabled.get(type)));
		if (enabled.containsKey(type)) addGlow(weaponNameSlot);

		// Copy over banner design
		BannerMeta banner = (BannerMeta) CountryFlag.MINEPLEX.getBanner().getItemMeta();
		BannerMeta meta = ((BannerMeta) getItem(flagSlot).getItemMeta());
		meta.setBaseColor(banner.getBaseColor());
		meta.setPatterns(banner.getPatterns());
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		getItem(flagSlot).setItemMeta(meta);

		List<Gadget> powerPlayGadgets = getPlugin().getGadgetManager().getPowerPlayGadgets();
		int ownedPowerPlay = 0;
		int maxPowerPlay = powerPlayGadgets.size();
		for (Gadget gadget : powerPlayGadgets)
		{
			if (gadget.ownsGadget(getPlayer()))
			{
				ownedPowerPlay++;
			}
		}

		lore = getLore(ownedPowerPlay, maxPowerPlay, "View all of the Power Play Club Rewards!", VISIBILITY_EVERYWHERE, null);
		addButton(powerPlaySlot, new ShopItem(Material.DIAMOND, "Power Play Club Rewards", lore, 1, false), (player, clickType) ->
		{
			if (clickType.isLeftClick())
			{
				GadgetPage page = new PowerPlayClubPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), getPlayer());
				getShop().openPageForPlayer(player, page);
			}
		});

		ItemStack pane = new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 3)
				.setTitle(C.cAqua + _donationManager.Get(getPlayer()).getBalance(GlobalCurrency.TREASURE_SHARD) + " Shards")
				.build();

		for (int i = 0; i < getSize(); i++)
		{
			if (getItem(i) == null)
			{
				setItem(i, pane);
			}
		}
	}

	private String[] getLore(int ownedCount, int maxCount, String info, String visibility, Gadget enabled)
	{
		boolean balloon = false;
		if (enabled != null)
		{
			if (enabled.getGadgetType() == GadgetType.BALLOON)
				balloon = true;
		}
		return getLore(ownedCount, maxCount, info, visibility, enabled == null ? null : enabled.getName(), balloon);
	}

	private String[] getLore(int ownedCount, int maxCount,String info, String visibility, String enabled, boolean balloons)
	{
		if (!balloons)
		{
			if (enabled != null)
			{
				return UtilText.splitLinesToArray(new String[]{
						C.blankLine,
						C.cGray + info,
						C.cDGray + visibility,
						C.blankLine,
						C.cWhite + "You own " + ownedCount + "/" + maxCount,
						C.blankLine,
						C.cWhite + "Active: " + C.cYellow + enabled,
						C.cGreen + "Right-Click to Disable",
						C.blankLine,
						C.cGreen + "Left-Click to View Category"
				}, LineFormat.LORE);
			} else
			{
				return UtilText.splitLinesToArray(new String[]{
						C.blankLine,
						C.cGray + info,
						C.cDGray + visibility,
						C.blankLine,
						C.cWhite + "You own " + ownedCount + "/" + maxCount,
						C.blankLine,
						C.cGreen + "Left-Click to View Category"
				}, LineFormat.LORE);
			}
		}
		else
		{
			return UtilText.splitLinesToArray(new String[]{
					C.blankLine,
					C.cGray + info,
					C.cDGray + visibility,
					C.blankLine,
					C.cWhite + "You own " + ownedCount + "/" + maxCount,
					C.blankLine,
					C.cGreen + "Left-Click to View Category"
			}, LineFormat.LORE);
		}
	}

	private IButton generateButton(GadgetType type, Gadget active)
	{
		return (player, clickType) ->
		{
			if (active != null && clickType.isRightClick())
			{
				playAcceptSound(player);
				active.disable(player);
				GadgetChangeEvent gadgetChangeEvent = new GadgetChangeEvent(player, active, GadgetState.DISABLED);
				UtilServer.CallEvent(gadgetChangeEvent);
				refresh();
				return;
			}

			GadgetPage page = new GadgetPage(
					getPlugin(),
					getShop(),
					getClientManager(),
					getDonationManager(),
					type.getCategoryType(),
					player,
					type
			);
			page.buildPage();

			getShop().openPageForPlayer(player, page);
		};
	}

	public void openCostumes(Player player)
	{
		getShop().openPageForPlayer(player, new CostumePage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Costumes", player));
	}

	public void openItems(Player player)
	{
		getShop().openPageForPlayer(player, new ItemGadgetPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Item Gadgets", player));
	}
}
