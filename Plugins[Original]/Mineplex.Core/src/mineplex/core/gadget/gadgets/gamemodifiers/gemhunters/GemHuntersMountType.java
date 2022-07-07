package mineplex.core.gadget.gadgets.gamemodifiers.gemhunters;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseCow;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;

public enum GemHuntersMountType
{

	INFERNAL_HORROR("Infernal Horror Mount Skin", Material.BONE)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					return UtilVariant.spawnHorse(location, Variant.SKELETON_HORSE);
				}

				@Override
				public void onUpdate(UpdateEvent event, Horse horse)
				{
					if (event.getType() != UpdateType.TICK)
					{
						return;
					}

					UtilParticle.PlayParticleToAll(ParticleType.FLAME, horse.getLocation().add(0, 1, 0), 0.25F, 0.25F, 0.25F, 0, 2, ViewDist.NORMAL);
				}
			},
	GLACIAL_STEED("Glacial Steed Mount Skin", Material.SNOW_BALL)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					Horse horse = UtilVariant.spawnHorse(location, Variant.HORSE);

					horse.setColor(Color.WHITE);
					horse.setStyle(Style.WHITE);

					return horse;
				}

				@Override
				public void onUpdate(UpdateEvent event, Horse horse)
				{
					if (event.getType() != UpdateType.TICK)
					{
						return;
					}

					UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, horse.getLocation().add(0, 1, 0), 0.25F, 0.25F, 0.25F, 0.1F, 4, ViewDist.NORMAL);
				}
			},
	ZOMBIE_HORSE("Zombie Horse Mount Skin", Material.ROTTEN_FLESH)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					return UtilVariant.spawnHorse(location, Variant.UNDEAD_HORSE);
				}

				@Override
				public void onUpdate(UpdateEvent event, Horse horse)
				{
					if (event.getType() != UpdateType.TICK)
					{
						return;
					}

					UtilParticle.PlayParticleToAll(ParticleType.FOOTSTEP, horse.getLocation().add(0, 0.2, 0), null, 0, 1, ViewDist.NORMAL);
				}
			},
	RAINBOW_SHEEP("Rainbow Sheep Mount Skin", Material.WOOL, 14)
			{

				private DisguiseManager _disguise = Managers.get(DisguiseManager.class);
				private int _tick;

				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					Horse horse = UtilVariant.spawnHorse(location, Variant.HORSE);

					manager.disguise(new DisguiseSheep(horse));

					return horse;
				}

				@Override
				public void onUpdate(UpdateEvent event, Horse horse)
				{
					if (event.getType() != UpdateType.FAST)
					{
						return;
					}

					DisguiseBase base = _disguise.getActiveDisguise(horse);

					if (base == null || !(base instanceof DisguiseSheep))
					{
						return;
					}

					DisguiseSheep sheep = (DisguiseSheep) base;
					int mod = _tick++ % 4;

					if (mod == 0) sheep.setColor(DyeColor.RED);
					else if (mod == 1) sheep.setColor(DyeColor.YELLOW);
					else if (mod == 2) sheep.setColor(DyeColor.GREEN);
					else if (mod == 3) sheep.setColor(DyeColor.BLUE);

					_disguise.updateDisguise(base);
				}
			},
	ROYAL_STEED("Royal Steed Mount Skin", Material.GOLD_BARDING)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					Horse horse = UtilVariant.spawnHorse(location, Variant.HORSE);

					horse.setColor(Color.WHITE);
					horse.setStyle(Style.WHITE);

					return horse;
				}

				@Override
				public void onUpdate(UpdateEvent event, Horse horse)
				{
					if (event.getType() != UpdateType.TICK)
					{
						return;
					}

					UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.GOLD_BLOCK, 0), horse.getLocation().add(0, 1, 0), 0.25F, 0.25F, 0.25F, 0, 3, ViewDist.NORMAL);
				}
			},
	ROYAL_GUARD_STEED("Royal Guard Steed Mount Skin", Material.DIAMOND_BARDING)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					Horse horse = UtilVariant.spawnHorse(location, Variant.HORSE);

					horse.setColor(Color.WHITE);
					horse.setStyle(Style.WHITE);

					return horse;
				}

				@Override
				public void onUpdate(UpdateEvent event, Horse horse)
				{
					if (event.getType() != UpdateType.TICK)
					{
						return;
					}

					UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.IRON_BLOCK, 0), horse.getLocation().add(0, 1, 0), 0.25F, 0.25F, 0.25F, 0, 3, ViewDist.NORMAL);
				}
			},
	KNIGHT_STEED("Knight Steed Mount Skin", Material.DIAMOND_BARDING)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					Horse horse = UtilVariant.spawnHorse(location, Variant.HORSE);

					horse.setColor(Color.GRAY);
					horse.setStyle(Style.NONE);

					return horse;
				}

				@Override
				public void onUpdate(UpdateEvent event, Horse horse)
				{
					if (event.getType() != UpdateType.TICK)
					{
						return;
					}

					UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.APPLE, 0), horse.getLocation().add(0, 1, 0), 0.25F, 0.25F, 0.25F, 0, 3, ViewDist.NORMAL);
				}
			},
	COW("Cow Mount Skin", Material.MILK_BUCKET)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					Horse horse = UtilVariant.spawnHorse(location, Variant.HORSE);

					manager.disguise(new DisguiseCow(horse));

					return horse;
				}
			},
	SHEEP("Sheep Mount Skin", Material.WOOL)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					Horse horse = UtilVariant.spawnHorse(location, Variant.HORSE);

					manager.disguise(new DisguiseSheep(horse));

					return horse;
				}
			},
	TRUSTY_MULE("Trusty Mule Mount Skin", Material.APPLE)
			{
				@Override
				public Horse spawn(Location location, DisguiseManager manager)
				{
					return UtilVariant.spawnHorse(location, Variant.DONKEY);
				}
			},

	;

	private final String _name;
	private final Material _material;
	private final byte _materialData;

	GemHuntersMountType(String name, Material material)
	{
		this(name, material, 0);
	}

	GemHuntersMountType(String name, Material material, int materialData)
	{
		_name = name;
		_material = material;
		_materialData = (byte) materialData;
	}

	public abstract Horse spawn(Location location, DisguiseManager manager);

	public void onUpdate(UpdateEvent event, Horse horse)
	{
	}

	public String getName()
	{
		return _name;
	}

	public Material getMaterial()
	{
		return _material;
	}

	public byte getMaterialData()
	{
		return _materialData;
	}
}
