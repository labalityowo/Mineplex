package mineplex.core.game.nano;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.game.Display;

public enum NanoDisplay implements Display
{


	BAWK_BAWK(1, "Bawk Bawk's Wrath", Material.EGG),
	COPY_CAT(2, "Copy Cat", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.OCELOT)),
	CHICKEN_SHOOT(3, "Chicken Shoot", Material.BOW),
	COLOUR_CHANGE(4, "Color Swap", Material.WOOL, (byte) 4),
	DEATH_TAG(5, "Zombie Survival", Material.ROTTEN_FLESH),
	DROPPER(6, "Dropper", Material.ANVIL),
	FIND_ORES(7, "Ores Ores Ores", Material.DIAMOND_PICKAXE),
	HOT_POTATO(8, "Hot Potato", Material.POTATO_ITEM),
	JUMP_ROPE(9, "Jump Rope", Material.LEASH),
	KING_SLIME(10, "King Slime", Material.SLIME_BALL),
	MICRO_BATTLE(11, "Nano Battle", Material.IRON_SWORD),
//	MOB_FARM(12, "Mob Farm", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.PIG)),
	MUSIC_MINECARTS(13, "Musical Minecarts", Material.MINECART),
	QUICK(14, "Quick", Material.DIAMOND),
	PARKOUR(15, "Hardcore Parkour", Material.FEATHER),
	RED_GREEN_LIGHT(16, "Red Light Green Light", Material.WOOL, (byte) 5),
	REVERSE_TAG(17, "Reverse Tag", Material.EMERALD),
	SLIME_CYCLES(18, "Slime Cycles", Material.SLIME_BLOCK),
	SNOWBALL_TROUBLE(19, "Snowball Trouble", Material.SNOW_BALL),
	SPLEEF(20, "AAAAAAAA! Spleef", Material.DIAMOND_SPADE),
	SPLOOR(21, "Sploor", Material.DIAMOND_BARDING),
	TERRITORY(22, "Slime Territory", Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.SLIME)),
	MINEKART(23, "MineKart", Material.COMMAND_MINECART),
	WIZARDS(24, "Nano Wizards", Material.BLAZE_ROD),
	;

	public static NanoDisplay getFromId(int gameId)
	{
		for (NanoDisplay display : values())
		{
			if (gameId == display._gameId)
			{
				return display;
			}
		}

		return null;
	}

	private final int _gameId;
	private final String _name;
	private final Material _material;
	private final byte _materialData;

	NanoDisplay(int gameId, String name, Material material)
	{
		this(gameId, name, material, (byte) 0);
	}

	NanoDisplay(int gameId, String name, Material material, byte materialData)
	{
		_gameId = gameId;
		_name = name;
		_material = material;
		_materialData = materialData;
	}

	@Override
	public int getGameId()
	{
		return _gameId;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public Material getMaterial()
	{
		return _material;
	}

	@Override
	public byte getMaterialData()
	{
		return _materialData;
	}
}
