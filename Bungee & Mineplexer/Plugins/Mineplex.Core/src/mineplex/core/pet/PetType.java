package mineplex.core.pet;

import java.time.Month;
import java.time.YearMonth;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.skin.SkinData;
import mineplex.core.pet.sales.PetSalesPackage;

public enum PetType
{
	// These pets are named by EntityType for historic reasons -- the enum
	// name() is stored by MSSQL to determine pet ownership. Future pet names
	// can be made more accurate.
	ZOMBIE("Pumpling", EntityType.ZOMBIE, -5), // Pumpling
	PIG_ZOMBIE("Coal Apparition", EntityType.PIG_ZOMBIE, -1),
	VILLAGER("Christmas Elf", EntityType.VILLAGER, -4),
	PIG("Pig", EntityType.PIG, 5000),
	SHEEP("Sheep", EntityType.SHEEP, 3000),
	COW("Cow", EntityType.COW, 2000),
	CHICKEN("Chicken", EntityType.CHICKEN, 7000),
	WOLF("Dog", EntityType.WOLF, 8000),
	OCELOT("Cat", EntityType.OCELOT, 6000),
	MUSHROOM_COW("Mooshroom", EntityType.MUSHROOM_COW, 5000),
	WITHER("Widder", EntityType.WITHER, -12),
	SKELETON("Guardian", EntityType.SKELETON, -13),
	RABBIT("Baby Zombie", EntityType.RABBIT, -9, "They're so cute - until a pack of them chases down your family and eats them."),
	BLAZE("Grim Reaper", EntityType.BLAZE, -8, "Aww isn't he so cute with his little wings and little scythe?"),
	GINGERBREAD_MAN("Gingerbread Man", EntityType.ZOMBIE, -16, "Looks like you can catch him after all."),
	CUPID_PET("Cupid", EntityType.ZOMBIE, -17, "Sometimes you need a little extra help finding true Love. Why not have Cupid help you out?", Material.BOW, (byte) 0),
	TRUE_LOVE_PET("True Love", EntityType.ZOMBIE, -14, "Sometimes love means chasing the person of your dreams until you catch them.", Material.APPLE, YearMonth.of(2017, Month.FEBRUARY)),
	LEPRECHAUN("Leprechaun", EntityType.ZOMBIE, -18, "Apparently this little guy lost his Pot of Gold in the war.", SkinData.LEPRECHAUN.getSkull()),
	KILLER_BUNNY("Killer Bunny", EntityType.RABBIT, -19, "The Easter Bunny's less talked about brother Devin was a bit less fun to hang out with.", Material.RABBIT_FOOT, (byte) 0)
	// TODO CHECK IF LOBBY IS 1.9+
	// Not in this update
	//SHULKER("Shulker Pet", EntityType.BAT, 0, "Is it a turtle or an alien? Either way its shot can be really UPLIFTING.")
	;
	private final String _name;
	private final EntityType _entityType;
	private final int _price;
	private final Optional<String> _lore;
	private final Material _material;
	private final byte _data;
	private YearMonth _yearMonth;
	private ItemStack _displayItem;

	PetType(String name, EntityType entityType, int price)
	{
		_name = name;
		_entityType = entityType;
		_price = price;
		_lore = Optional.empty();
		_material = Material.MONSTER_EGG;
		_data = (byte) entityType.getTypeId();
	}

	PetType(String name, EntityType entityType, int price, String lore)
	{
		_name = name;
		_entityType = entityType;
		_price = price;
		_lore = Optional.of(lore);
		_material = Material.MONSTER_EGG;
		_data = (byte) entityType.getTypeId();
	}

	PetType(String name, EntityType entityType, int price, String lore, Material material, byte data)
	{
		_name = name;
		_entityType = entityType;
		_price = price;
		_lore = Optional.of(lore);
		_material = material;
		_data = data;
	}

	PetType(String name, EntityType entityType, int price, String lore, Material material, YearMonth yearMonth)
	{
		this(name, entityType, price, lore, material, (byte) 0, yearMonth);
	}

	PetType(String name, EntityType entityType, int price, String lore, Material material, byte data, YearMonth yearMonth)
	{
		this(name, entityType, price, lore, material, data);
		_yearMonth = yearMonth;
	}

	PetType(String name, EntityType entityType, int price, String lore, ItemStack displayItem)
	{
		this(name, entityType, price, lore);
		_displayItem = displayItem;
	}

	public String getName()
	{
		return _name;
	}

	public EntityType getEntityType()
	{
		return _entityType;
	}

	public int getPrice()
	{
		return _price;
	}

	public Optional<String> getLore()
	{
		return _lore;
	}

	public Material getMaterial()
	{
		return _material;
	}

	public byte getData()
	{
		return _data;
	}

	public ItemStack getDisplayItem()
	{
		if (_displayItem == null)
		{
			return new ItemStack(_material, 1, _data);
		}
		return _displayItem;
	}

	public YearMonth getYearMonth()
	{
		return _yearMonth;
	}

	public PetSalesPackage toSalesPackage(String tagName)
	{
		return new PetSalesPackage(this, tagName);
	}
}
