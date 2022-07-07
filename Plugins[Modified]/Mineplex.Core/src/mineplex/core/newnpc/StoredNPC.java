package mineplex.core.newnpc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.serverdata.database.column.Column;
import mineplex.serverdata.database.column.ColumnByte;
import mineplex.serverdata.database.column.ColumnDouble;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class StoredNPC extends SimpleNPC
{

	public static final String LINE_DELIMITER = "//";

	private int _id;
	private final EntityType _entityType;
	protected final String _name, _colouredName;
	private final List<String> _info;
	private Location _spawn;
	private Material _inHand;
	private byte _inHandData;
	private Material _helmet, _chestplate, _leggings, _boots;

	private final Map<String, Supplier<String>> _infoVariables;

	StoredNPC(int id, EntityType entityType, String name, List<String> info, Location spawn, Material inHand, byte inHandData, Material helmet, Material chestplate, Material leggings, Material boots, String metadata)
	{
		super(metadata);

		_id = id;
		_entityType = entityType;
		_name = name;
		_colouredName = name == null ? null : ChatColor.translateAlternateColorCodes('&', name);
		_info = info;
		_spawn = spawn;
		_inHand = inHand;
		_inHandData = inHandData;
		_helmet = helmet;
		_chestplate = chestplate;
		_leggings = leggings;
		_boots = boots;
		_infoVariables = new HashMap<>(2);
	}

	@Override
	public LivingEntity spawnEntity()
	{
		_spawn.getChunk().load(false);
		LivingEntity entity = (LivingEntity) _spawn.getWorld().spawnEntity(_spawn, _entityType);
		boolean nullName = _name == null;

		entity.setCanPickupItems(false);
		entity.setRemoveWhenFarAway(false);
		entity.setCustomName(nullName ? null : _colouredName);
		entity.setCustomNameVisible(!nullName);

		EntityEquipment equipment = entity.getEquipment();

		if (_inHand != null)
		{
			equipment.setItemInHand(ItemStackFactory.Instance.CreateStack(_inHand, _inHandData));
		}
		if (_helmet != null)
		{
			equipment.setHelmet(ItemStackFactory.Instance.CreateStack(_helmet));
		}
		if (_chestplate != null)
		{
			equipment.setChestplate(ItemStackFactory.Instance.CreateStack(_chestplate));
		}
		if (_leggings != null)
		{
			equipment.setLeggings(ItemStackFactory.Instance.CreateStack(_leggings));
		}
		if (_boots != null)
		{
			equipment.setBoots(ItemStackFactory.Instance.CreateStack(_boots));
		}

		UtilEnt.vegetate(entity, true);
		UtilEnt.setFakeHead(entity, true);
		UtilEnt.CreatureLook(entity, _spawn.getPitch(), _spawn.getYaw());
		UtilEnt.ghost(entity, true, false);

		if (entity instanceof ArmorStand)
		{
			ArmorStand stand = (ArmorStand) entity;
			stand.setVisible(false);
			stand.setGravity(false);
		}

		_entity = entity;
		return entity;
	}

	void setId(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public String getColouredName()
	{
		return _colouredName;
	}

	public void addInfoVariable(String delimiter, Supplier<String> getText)
	{
		_infoVariables.put(delimiter, getText);
	}

	public void setInfo(List<String> info)
	{
		_info.clear();
		_info.addAll(info);
	}

	void updateInfo()
	{
		if (_info.isEmpty())
		{
			return;
		}

		List<String> textList = _info.stream()
				.map(text ->
				{
					for (Entry<String, Supplier<String>> entry : _infoVariables.entrySet())
					{
						text = text.replace(entry.getKey(), entry.getValue().get());
					}

					return ChatColor.translateAlternateColorCodes('&', text);
				})
				.collect(Collectors.toList());

		textList.add(0, _colouredName);
		getNameTag().setText(textList.toArray(new String[0]));
	}

	public List<String> getInfo()
	{
		return _info;
	}

	public void setSpawn(Location spawn)
	{
		_spawn = spawn;

		if (_entity != null)
		{
			_entity.teleport(spawn);
		}
	}

	public Location getSpawn()
	{
		return _spawn;
	}

	public void setEquipment(ItemStack inHand, ItemStack[] armour)
	{
		if (inHand == null)
		{
			_inHand = Material.AIR;
			_inHandData = 0;
		}
		else
		{
			_inHand = inHand.getType();
			_inHandData = (byte) Math.max(inHand.getData().getData(), 0);
		}

		_boots = armour[0] == null ? null : armour[0].getType();
		_leggings = armour[1] == null ? null : armour[1].getType();
		_chestplate = armour[2] == null ? null : armour[2].getType();
		_helmet = armour[3] == null ? null : armour[3].getType();

		if (_entity != null)
		{
			_entity.getEquipment().setItemInHand(inHand);
			_entity.getEquipment().setArmorContents(armour);
		}
	}

	public List<Column<?>> toDatabaseQuery()
	{
		return Arrays.asList(
				new ColumnVarChar("entity_type", 32, _entityType.name()),
				new ColumnVarChar("name", 32, _name),
				new ColumnVarChar("info", 128, _info.isEmpty() ? null : _info.stream()
						.collect(Collectors.joining(LINE_DELIMITER))),
				new ColumnVarChar("world", 32, _spawn.getWorld().getName()),
				new ColumnDouble("x", _spawn.getBlockX() + 0.5),
				new ColumnDouble("y", _spawn.getY()),
				new ColumnDouble("z", _spawn.getBlockZ() + 0.5),
				new ColumnInt("yaw", (int) _spawn.getYaw()),
				new ColumnInt("pitch", (int) _spawn.getPitch()),
				new ColumnVarChar("in_hand", 32, _inHand == null ? null : _inHand.name()),
				new ColumnByte("in_hand_data", _inHandData),
				new ColumnVarChar("helmet", 32, _helmet == null ? null : _helmet.name()),
				new ColumnVarChar("chestplate", 32, _chestplate == null ? null : _chestplate.name()),
				new ColumnVarChar("leggings", 32, _leggings == null ? null : _leggings.name()),
				new ColumnVarChar("boots", 32, _boots == null ? null : _boots.name()),
				new ColumnVarChar("metadata", 32, _metadata),
				new ColumnVarChar("skin_value", 400, null),
				new ColumnVarChar("skin_signature", 700, null)
		);
	}
}
