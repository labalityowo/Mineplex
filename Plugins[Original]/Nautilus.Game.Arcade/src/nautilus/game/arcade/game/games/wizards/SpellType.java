package nautilus.game.arcade.game.games.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.wizards.spells.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum SpellType // ❤
{
	AnvilDrop(SpellElement.ATTACK, // Spell element
			WandElement.EARTH, // Wand element
			"Anvil Drop", // Spell name
			new ItemStack(Material.NETHER_BRICK_ITEM), // Spell icon
			SpellAnvilDrop.class, // Spell class
			3, // Spell max level
			40, // Mana cost
			15, // Spell cooldown
			-3, // Mana cost change per level
			-4, // Cooldown change per level
			10, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "(Spell Level x 2) + 3",

			"",

			"Summons exploding anvils over everyone near you!",

			"This also includes the caster!"),

	Fireball(SpellElement.ATTACK, // Spell element
			WandElement.FIRE, // Wand element
			"Fireball", // Spell name
			new ItemStack(Material.COAL), // Spell icon
			SpellFireball.class, // Spell class
			3, // Spell max level
			30, // Mana cost
			15, // Spell cooldown
			-3, // Mana cost change per level
			-2, // Cooldown change per level
			10, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "Spell Level + 3",

			"",

			"Be an object of fear and awe!",

			"Summon a blazing fireball!"),

	Flash(SpellElement.SUPPORT, // Spell element
			WandElement.LIFE, // Wand element
			"Flash", // Spell name
			new ItemStack(Material.GOLD_NUGGET), // Spell icon
			SpellFlash.class, // Spell class
			3, // Spell max level
			20, // Mana cost
			50, // Spell cooldown
			0, // Mana cost change per level
			-5, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Range: " + C.Bold + C.cWhite + "(Spell Level x 10) + 20",

			"",

			"Teleport to the block you are looking at!"),

	FrostBarrier(SpellElement.MISC, // Spell element
			WandElement.ICE, // Wand element
			"Frost Barrier", // Spell name
			new ItemStack(Material.CLAY_BALL), // Spell icon
			SpellFrostBarrier.class, // Spell class
			3, // Spell max level
			10, // Mana cost
			20, // Spell cooldown
			0, // Mana cost change per level
			-5, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Height: " + C.Bold + C.cWhite + "Spell Level + 1",

			C.cYellow + C.Bold + "Width: " + C.Bold + C.cWhite + "(Spell Level x 2) + 4",

			"",

			"Create a wall of ice!"),

	Gust(SpellElement.MISC, // Spell element
			WandElement.AIR, // Wand element
			"Gust", // Spell name
			new ItemStack(Material.SULPHUR), // Spell icon
			SpellGust.class, // Spell class
			3, // Spell max level
			15, // Mana cost
			20, // Spell cooldown
			0, // Mana cost change per level
			0, // Cooldown change per level
			5, // Item amount in loot

			C.cYellow + C.Bold + "Gust Size: " + C.Bold + C.cWhite + "(Spell Level x 3) + 10 blocks",

			C.cYellow + C.Bold + "Gust Strength: " + C.Bold + C.cWhite + "Spell Level x 30%",

			"",

			"Cast the spell and watch your enemies fly!",
			
			"Spell strength decreases with distance"),

	Heal(SpellElement.SUPPORT, // Spell element
			WandElement.LIFE, // Wand element
			"Heal", // Spell name
			new ItemStack(Material.QUARTZ), // Spell icon
			SpellHeal.class, // Spell class
			5, // Spell max level
			50, // Mana cost
			30, // Spell cooldown
			0, // Mana cost change per level
			-1, // Cooldown change per level
			5, // Item amount in loot

			C.cYellow + C.Bold + "Heals: " + C.Bold + C.cWhite + "(Spell Level / 2) + 1.5",

			"",

			"Low on health and need to retreat?",

			"Use this! Heal yourself up!"),

	IcePrison(SpellElement.MISC, // Spell element
			WandElement.ICE, // Wand element
			"Ice Prison", // Spell name
			new ItemStack(Material.EYE_OF_ENDER), // Spell icon
			SpellIcePrison.class, // Spell class
			3, // Spell max level
			25, // Mana cost
			20, // Spell cooldown
			2, // Mana cost change per level
			0, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Size: " + C.Bold + C.cWhite + "Spell Level + 3",

			"",

			"On impact creates a mighty ice",

			"prison to capture thy enemies!"),

	IceShards(SpellElement.ATTACK, // Spell element
			WandElement.ICE, // Wand element
			"Ice Shards", // Spell name
			new ItemStack(Material.GOLDEN_CARROT), // Spell icon
			SpellIceShards.class, // Spell class
			3, // Spell max level
			30, // Mana cost
			20, // Spell cooldown
			0, // Mana cost change per level
			-2, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "2",

			C.cYellow + C.Bold + "Shards: " + C.Bold + C.cWhite + "Spell Level + 1",

			"",

			"Overwhelm your opponent with shards!",

			"Each shard is fired half a second after",

			"the last allowing you to pummel your",

			"enemies senseless!"),

	Implode(SpellElement.MISC, // Spell element
			WandElement.EARTH, // Wand element
			"Implode", // Spell name
			new ItemStack(Material.GLOWSTONE_DUST), // Spell icon
			SpellImplode.class, // Spell class
			3, // Spell max level
			50, // Mana cost
			30, // Spell cooldown
			-2, // Mana cost change per level
			-3, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Range: " + C.Bold + C.cWhite + "50",

			C.cYellow + C.Bold + "Implosion Height: " + C.Bold + C.cWhite + "Spell Level",

			C.cYellow + C.Bold + "Implosion Width: " + C.Bold + C.cWhite + "Spell Level x 2",

			"",

			"Gathers the blocks at target location",

			"and scatters them about the area"),

	LightningStrike(SpellElement.ATTACK, // Spell element
			WandElement.AIR, // Wand element
			"Lightning Strike", // Spell name
			new ItemStack(Material.INK_SACK, 1, (short) 12), // Spell icon
			SpellLightningStrike.class, // Spell class
			3, // Spell max level
			50, // Mana cost
			20, // Spell cooldown
			0, // Mana cost change per level
			0, // Cooldown change per level
			10, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "(Spell Level x 2) + 1",

			"",

			"Summon a mighty lightning strike",

			"to hit the target you point out!",

			"The lightning also contains fire!"),

	ManaBolt(SpellElement.ATTACK, // Spell element
			WandElement.AIR, // Wand element
			"Mana Bolt", // Spell name
			new ItemStack(Material.MELON_SEEDS), // Spell icon
			SpellManaBolt.class, // Spell class
			3, // Spell max level
			15, // Mana cost
			5, // Spell cooldown
			-2, // Mana cost change per level
			0, // Cooldown change per level
			15, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "Spell Level + 2",

			C.cYellow + C.Bold + "Range: " + C.Bold + C.cWhite + "(Spell Level x 10) + 20",

			"",

			"Basic spell all beginner mages are taught," + " this creates a mana missile commonly attributed towards "
					+ "the magic profession and homes in towards the closest target!"),

	Napalm(SpellElement.ATTACK, // Spell element
			WandElement.FIRE, // Wand element
			"Napalm", // Spell name
			new ItemStack(Material.CARROT_STICK), // Spell icon
			SpellNapalm.class, // Spell class
			5, // Spell max level
			60, // Mana cost
			60, // Spell cooldown
			5, // Mana cost change per level
			-10, // Cooldown change per level
			1, // Item amount in loot

			C.cYellow + C.Bold + "Length: " + C.Bold + C.cWhite + "(Spell Level x 10) + 5",

			"",

			"Creates a ball of fire that grows",

			"the longer it lives. At a large size",

			"it even burns away nearby blocks!"),

	RainbowBeam(SpellElement.ATTACK, // Spell element
			WandElement.FIRE, // Wand element
			"Rainbow Beam", // Spell name
			new ItemStack(Material.INK_SACK, 1, (short) 10), // Spell icon
			SpellRainbowBeam.class, // Spell class
			5, // Spell max level
			5, // Mana cost
			8, // Spell cooldown
			3, // Mana cost change per level
			1, // Cooldown change per level
			10, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "Spell Level + 1",

			C.cYellow + C.Bold + "Range: " + C.Bold + C.cWhite + "80",

			"",

			"Firing rainbow beams of love and hope!",

			"This spell damages the target instantly!",

			"The thing is, to make this fit in with our",

			"budget the damage will decrease after",

			"30 blocks by 0.2 damage per block!"),

	RainbowRoad(SpellElement.MISC, // Spell element
			WandElement.AIR, // Wand element
			"Rainbow Road", // Spell name
			new ItemStack(Material.SADDLE), // Spell icon
			SpellRainbowRoad.class, // Spell class
			3, // Spell max level
			10, // Mana cost
			20, // Spell cooldown
			0, // Mana cost change per level
			0, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Length: " + C.Bold + C.cWhite + "Spell Level x 10",

			"",

			"Summon into being a mighty road",

			"of rainbows for thee to walk on!"),

	Rumble(SpellElement.ATTACK, // Spell element
			WandElement.EARTH, // Wand element
			"Rumble", // Spell name
			new ItemStack(Material.PUMPKIN_SEEDS), // Spell icon
			SpellRumble.class, // Spell class
			3, // Spell max level
			30, // Mana cost
			15, // Spell cooldown
			0, // Mana cost change per level
			-1, // Cooldown change per level
			10, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "Spell Level + 2",

			C.cYellow + C.Bold + "Explosion Damage: " + C.Bold + C.cWhite + "Spell Level / 4",

			C.cYellow + C.Bold + "Range: " + C.Bold + C.cWhite + "Spell Level x 10",

			C.cYellow + C.Bold + "Slowness Level: " + C.Bold + C.cWhite + "Spell Level",

			"",

			"Creates a targeted earthquake",

			"in the direction you face!",

			"Explodes with damage at the end!",

			"Affected players lose their footing!"),

	SpectralArrow(SpellElement.ATTACK, // Spell element
			WandElement.DEATH, // Wand element
			"Spectral Arrow", // Spell name
			new ItemBuilder(Material.INK_SACK, 1, (short) 13).addEnchantment(UtilInv.getDullEnchantment(), 1).build(), // Spell
																														// icon
			SpellSpectralArrow.class, // Spell class
			3, // Spell max level
			40, // Mana cost
			15, // Spell cooldown
			-5, // Mana cost change per level
			-2, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "(Blocks / (7 - Spell Level)) + 3",

			"",

			"Shoot an arrow that penetrates!",

			"Further the distance, higher the damage!"),

	SpeedBoost(SpellElement.SUPPORT, // Spell element
			WandElement.LIFE, // Wand element
			"Speed Boost", // Spell name
			new ItemStack(Material.INK_SACK, 1, (short) 2), // Spell icon
			SpellSpeedBoost.class, // Spell class
			2, // Spell max level
			20, // Mana cost
			40, // Spell cooldown
			0, // Mana cost change per level
			0, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Length: " + C.Bold + C.cWhite + "20 seconds",

			C.cYellow + C.Bold + "Strength: " + C.Bold + C.cWhite + "Spell Level + 1",

			"",

			"Gain a speed potion effect to outrun your enemies"),

	SummonWolves(SpellElement.MISC, // Spell element
			WandElement.LIFE, // Wand element
			"Summon Wolves", // Spell name
			new ItemStack(Material.MILK_BUCKET), // Spell icon
			SpellSummonWolves.class, // Spell class
			3, // Spell max level
			80, // Mana cost
			160, // Spell cooldown
			-10, // Mana cost change per level
			0, // Cooldown change per level
			8, // Item amount in loot

			C.cYellow + C.Bold + "Wolves: " + C.Bold + C.cWhite + "Spell Level + 2",

			"",

			"Summons a pack of wolves and assigns you as the leader.",

			"They will fight for you and after 30 seconds, will disappear"),

	TrapRune(SpellElement.MISC, // Spell element
			WandElement.DEATH, // Wand element
			"Trap Rune", // Spell name
			new ItemStack(Material.SHEARS), // Spell icon
			SpellTrapRune.class, // Spell class
			3, // Spell max level
			25, // Mana cost
			30, // Spell cooldown
			0, // Mana cost change per level
			-5, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Damage: " + C.Bold + C.cWhite + "(Spell Level x 2) + 3",

			C.cYellow + C.Bold + "Range: " + C.Bold + C.cWhite + "(Spell Level x 4) + 12",

			C.cYellow + C.Bold + "Rune Size: " + C.Bold + C.cWhite + "Spell Level",

			"",

			"Draws an explosion rune on the ground!",

			"The rune takes 5 seconds to prepare and will damage even you!"),

	WebShot(SpellElement.MISC, // Spell element
			WandElement.DEATH, // Wand element
			"Web Shot", // Spell name
			new ItemStack(Material.SPIDER_EYE), // Spell icon
			SpellWebShot.class, // Spell class
			3, // Spell max level
			40, // Mana cost
			20, // Spell cooldown
			-5, // Mana cost change per level
			0, // Cooldown change per level
			3, // Item amount in loot

			C.cYellow + C.Bold + "Webs: " + C.Bold + C.cWhite + "Spell Level x 2",

			"",

			"Shoot webs just like your favorite hero!"),

	WizardsCompass(SpellElement.MISC, // Spell element
			WandElement.LIFE, // Wand element
			"Wizard's Compass", // Spell name
			new ItemStack(Material.SUGAR), // Spell icon
			SpellWizardsCompass.class, // Spell class
			1, // Spell max level
			5, // Mana cost
			4, // Spell cooldown
			0, // Mana cost change per level
			0, // Cooldown change per level
			0, // Item amount in loot

			"",

			"Displays particles pointing to the closest enemy!");

	public enum SpellElement
	{
		ATTACK(1, 0, 2, new ItemBuilder(Material.INK_SACK, 1, (short) 7).setTitle(C.cRed + "Attack Spells")
				.addLore(C.cGray + "Spells of destruction").build(), C.cRed),

		MISC(7, 6, 8, new ItemBuilder(Material.INK_SACK, 1, (short) 11).setTitle(C.cDGray + "Misc Spells").addLore(

		C.cGray + "Misc spells that don't fit in",

		"These spells generally affect the world itself").build(), C.cGray),

		SUPPORT(4, 4, 4, new ItemBuilder(Material.INK_SACK, 1, (short) 14).setTitle(C.cDGreen + "Support Spells")
				.addLore(C.cGray + "Spells of assistance").build(), C.cDGreen);

		private String _chatColor;
		private int _firstSlot;
		private ItemStack _icon;
		private int _secondSlot;
		private int _slot;

		private SpellElement(int slot, int firstSlot, int secondSlot, ItemStack icon, String color)
		{
			_slot = slot;
			_firstSlot = firstSlot;
			_secondSlot = secondSlot;
			_icon = icon;
			_chatColor = color;
		}

		public String getColor()
		{
			return _chatColor;
		}

		public int getFirstSlot()
		{
			return _firstSlot;
		}

		public ItemStack getIcon()
		{
			return _icon;
		}

		public int getSecondSlot()
		{
			return _secondSlot;
		}

		public int getSlot()
		{
			return _slot;
		}

		@Override
		public String toString()
		{
			return _chatColor;
		}
	}

	public enum WandElement
	{
		AIR(Material.IRON_HOE),

		DEATH(Material.STICK),

		EARTH(Material.STONE_HOE),

		FIRE(Material.GOLD_HOE),

		ICE(Material.DIAMOND_HOE),

		LIFE(Material.WOOD_HOE);

		private Material _material;

		private WandElement(Material material)
		{
			_material = material;
		}

		public Material getMaterial()
		{
			return _material;
		}
	}

	static
	{
		ArrayList<SpellType> spells = new ArrayList<SpellType>(Arrays.asList(SpellType.values()));

		Collections.sort(spells, new Comparator<SpellType>()
		{

			@Override
			public int compare(SpellType o1, SpellType o2)
			{
				int number = new Integer(o2.getItemAmount()).compareTo(o1.getItemAmount());

				if (number == 0)
				{
					return o1.getSpellName().compareToIgnoreCase(o2.getSpellName());
				}

				return number;
			}
		});

		for (SpellType spell : spells)
		{
			spell._slot = 18 + spell.getElement().getFirstSlot();

			for (SpellType spell2 : spells)
			{
				if (spell != spell2 && spell.getElement() == spell2.getElement() && spell._slot <= spell2._slot)
				{
					spell._slot = spell2._slot;
					int divSlot = spell._slot % 9;

					if (divSlot >= 8 || divSlot + 1 > spell.getElement().getSecondSlot())
					{

						spell._slot = (spell._slot - divSlot) + 9 + spell.getElement().getFirstSlot();

					}
					else
					{

						spell._slot = spell._slot + 1;

					}
				}
			}

			if (spell._slot > 54)
			{
				System.out.print("Assigning " + spell.name() + " to " + spell._slot);
			}
		}
	}

	public static SpellType getSpell(String spellName)
	{
		for (SpellType spell : values())
		{
			if (spell.getSpellName().equals(spellName))
			{
				return spell;
			}
		}
		return null;
	}

	private int _cooldownChangePerLevel;
	private String[] _desc;
	private ItemStack _item;
	private int _itemAmount;
	private int _manaChangePerLevel;
	private int _maxLevel;
	private int _slot;
	private Class<? extends Spell> _spellClass;
	private int _spellCooldown;
	private int _spellCost;
	private String _spellName;
	private SpellElement _type;
	private WandElement _wandElement;

	private SpellType(SpellElement type, WandElement wandElement, String spellName, ItemStack spellItem,
			Class<? extends Spell> spell, int maxLevel, int spellCost, int spellCooldown, int manaChangePerLevel,
			int cooldownChangePerLevel, int itemAmount, String... desc)
	{
		_wandElement = wandElement;
		_maxLevel = maxLevel;
		_item = spellItem;
		_desc = desc;
		_type = type;
		_spellClass = spell;
		_spellName = spellName;
		_spellCost = spellCost;
		_spellCooldown = spellCooldown;
		_cooldownChangePerLevel = cooldownChangePerLevel;
		_manaChangePerLevel = manaChangePerLevel;
		_itemAmount = itemAmount;
	}

	public int getBaseCooldown()
	{
		return _spellCooldown;
	}

	public int getBaseManaCost()
	{
		return _spellCost;
	}

	public String[] getDesc()
	{
		return _desc;
	}

	public SpellElement getElement()
	{
		return _type;
	}

	public int getItemAmount()
	{
		return _itemAmount;
	}

	public int getManaCost(Wizard wizard)
	{
		return Math.max(0,

		Math.round(

		((_manaChangePerLevel * wizard.getSpellLevel(this))

		+ _spellCost - _manaChangePerLevel)

		* wizard.getCooldownModifier()));
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public int getSlot()
	{
		return _slot;
	}

	public ItemStack getSpellBook(Wizards wizards)
	{
		return makeSpell(wizards,

		new ItemBuilder(_item)

		.addLore(C.cAqua + "Click to level up this spell")

		.build());
	}

	public Class<? extends Spell> getSpellClass()
	{
		return _spellClass;
	}

	public int getSpellCooldown(Wizard wizard)
	{
		return Math.max(0,

		Math.round(

		((_cooldownChangePerLevel * wizard.getSpellLevel(this))

		+ _spellCooldown - _cooldownChangePerLevel)

		* (this == Heal || this == RainbowRoad ? 1 : wizard.getCooldownModifier())));
	}

	public ItemStack getSpellItem()
	{
		return _item;
	}

	public String getSpellName()
	{
		return _spellName;
	}

	public WandElement getWandType()
	{
		return _wandElement;
	}

	public ItemStack makeSpell(Wizards wizards, ItemStack item)
	{
		ItemBuilder builder = new ItemBuilder(item);

		builder.setTitle(C.cDBlue + C.Bold + "Spell: " + _type._chatColor + getSpellName() + wizards.buildTime());

		return builder.build();
	}
}
