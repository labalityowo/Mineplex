package nautilus.game.arcade.game.games.mineware.effect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.hologram.Hologram;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;

/**
 * Triggers a death effect that contains a rotating chicken head, food items and a hologram with random next.
 */
public class DeathEffect
{
	private static final double FOOD_DROP_HEIGHT = 0.5;
	private static final double HOLOGRAM_HEIGHT = 2.2;
	private static final double CLOUD_PARTICLES_HEIGHT = 1.7;

	private static final int FOOD_SPAWN_AMOUNT = 14;
	private static final int FOOD_MERGE_RANDOM_SEED = 999999;
	private static final String FOOD_ITEM_TITLE = "Food";
	private static final double FOOD_VELOCITY_SUBTRACT = 0.5;
	private static final double FOOD_VELOCITY_MULTIPLIER = 0.5;
	private static final int FOOD_REMOVE_AFTER_TICKS = 60;

	private static final byte CHICKEN_HEAD_DATA = 3;
	private static final int CHICKEN_HEAD_ROTATION_ADD = 12;
	private static final int CHICKEN_HEAD_ROTATION_LIMIT = 360;

	private static final float CHICKEN_HEAD_REMOVE_PARTICLE_OFFSET = 0.2F;
	private static final int CHICKEN_HEAD_REMOVE_PARTICLE_AMOUNT = 10;
	private static final int CHICKEN_HEAD_REMOVE_AFTER_TICKS = 60;

	private static final float CHICKEN_SOUND_VOLUME = 1.5F;
	private static final float CHICKEN_SOUND_PITCH = 1.0F;
	private static final float EAT_SOUND_VOLUME = 1.5F;
	private static final float EAT_SOUND_PITCH = 1.0F;
	private static final float BURP_SOUND_VOLUME = 1.5F;
	private static final float BURP_SOUND_PITCH = 1.0F;
	private static final int EAT_BURP_SOUND_EMIT_AFTER_TICKS = 10;

	private BawkBawkBattles _host;
	private JavaPlugin _plugin;
	private DeathText _deathText = new DeathText();
	private ArrayList<DeathEffectData> _data = new ArrayList<DeathEffectData>();

	public DeathEffect(BawkBawkBattles host)
	{
		_host = host;
		_plugin = host.Manager.getPlugin();
	}

	public void playDeath(Player player, Location death)
	{
		DeathEffectData data = new DeathEffectData(player, death);
		_data.add(data);

		Block belowFirst = death.getBlock().getRelative(BlockFace.DOWN);
		Block belowSecond = belowFirst.getRelative(BlockFace.DOWN);

		spawnChickenHead(data);
		playChickenSounds(data);

		if (!belowFirst.isEmpty() || !belowSecond.isEmpty())
		{
			startFoodSpawnTask(data);
		}
	}

	private void startFoodSpawnTask(DeathEffectData data)
	{
		Location dropsite = data.getLocation().clone().add(0, FOOD_DROP_HEIGHT, 0);
		List<Item> foodItems = new ArrayList<>();

		for (int i = 0; i <= FOOD_SPAWN_AMOUNT; i++)
		{
			Material material = Material.EGG;

			if (UtilMath.random.nextBoolean())
				material = Material.COOKED_CHICKEN;

			ItemBuilder builder = new ItemBuilder(material);
			builder.setTitle(UtilMath.r(FOOD_MERGE_RANDOM_SEED) + FOOD_ITEM_TITLE);

			Item food = dropsite.getWorld().dropItem(dropsite, builder.build());
			Vector velocity = new Vector((Math.random() - FOOD_VELOCITY_SUBTRACT) * FOOD_VELOCITY_MULTIPLIER, 0, (Math.random() - FOOD_VELOCITY_SUBTRACT) * FOOD_VELOCITY_MULTIPLIER);
			food.setVelocity(velocity);

			foodItems.add(food);
		}

		data.addFoodItems(foodItems);
		removeSpawnedFoodTask(data);
	}

	private void removeSpawnedFoodTask(DeathEffectData data)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (Item food : data.getFoodItems())
				{
					if (food.isValid())
					{
						UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, food.getLocation(), 0.0F, 0.0F, 0.0F, 0.0F, 1, ViewDist.NORMAL);
						food.remove();
					}
				}

				data.getFoodItems().clear();
			}
		}.runTaskLater(_plugin, FOOD_REMOVE_AFTER_TICKS);
	}

	@SuppressWarnings("deprecation")
	private void spawnChickenHead(DeathEffectData data)
	{
		Location dropsite = data.getLocation();

		_host.CreatureAllow = true;

		ArmorStand chickenHead = _host.WorldData.World.spawn(dropsite, ArmorStand.class);

		_host.CreatureAllow = false;

		chickenHead.setVisible(false);
		chickenHead.setGravity(false);
		chickenHead.setBasePlate(false);

		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 0, CHICKEN_HEAD_DATA);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(UtilSkull.getPlayerHeadName(EntityType.CHICKEN));
		skull.setItemMeta(meta);

		chickenHead.getEquipment().setHelmet(skull);

		data.setChickenHead(chickenHead);
		playHeadRotation(data);

		Hologram hologram = getRandomHologram(dropsite);
		hologram.start();
		data.setHologram(hologram);
	}

	private Hologram getRandomHologram(Location loc)
	{
		Hologram hologram = new Hologram(
			_host.getArcadeManager().getHologramManager(),
			loc.clone().add(0, HOLOGRAM_HEIGHT, 0),
			C.cAqua + C.Bold + _deathText.getRandom() + C.Reset);

		return hologram;
	}

	private void playHeadRotation(DeathEffectData data)
	{
		ArmorStand chickenHead = data.getChickenHead();

		new BukkitRunnable()
		{
			int i;

			@Override
			public void run()
			{
				if (!chickenHead.isValid())
				{
					cancel();
					return;
				}

				i += CHICKEN_HEAD_ROTATION_ADD;

				if (i <= CHICKEN_HEAD_ROTATION_LIMIT)
				{
					chickenHead.setHeadPose(new EulerAngle(0, Math.toRadians(i), 0));
				}
				else
				{
					removeChickenHead(data);
				}
			}
		}.runTaskTimer(_plugin, 1L, 1L);
	}

	private void removeChickenHead(DeathEffectData data)
	{
		ArmorStand chickenHead = data.getChickenHead();

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!chickenHead.isValid())
				{
					cancel();
					return;
				}

				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, data.getLocation().clone().add(0, CLOUD_PARTICLES_HEIGHT, 0), CHICKEN_HEAD_REMOVE_PARTICLE_OFFSET, CHICKEN_HEAD_REMOVE_PARTICLE_OFFSET, CHICKEN_HEAD_REMOVE_PARTICLE_OFFSET, 0.0F, CHICKEN_HEAD_REMOVE_PARTICLE_AMOUNT, ViewDist.NORMAL);

				chickenHead.remove();
				data.getHologram().stop();
			}
		}.runTaskLater(_plugin, CHICKEN_HEAD_REMOVE_AFTER_TICKS);
	}

	private void playChickenSounds(DeathEffectData data)
	{
		Location loc = data.getLocation();
		loc.getWorld().playSound(loc, Sound.CHICKEN_HURT, CHICKEN_SOUND_VOLUME, CHICKEN_SOUND_PITCH);

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				loc.getWorld().playSound(loc, Sound.EAT, EAT_SOUND_VOLUME, EAT_SOUND_PITCH);
				loc.getWorld().playSound(loc, Sound.BURP, BURP_SOUND_VOLUME, BURP_SOUND_PITCH);
			}
		}.runTaskLater(_plugin, EAT_BURP_SOUND_EMIT_AFTER_TICKS);
	}

	public void removeSpawnedEntities()
	{
		for (DeathEffectData data : _data)
		{
			if (data.hasDroppedFoodItems())
			{
				for (Item food : data.getFoodItems())
				{
					food.remove();
				}
			}

			data.getFoodItems().clear();
			data.getChickenHead().remove();
			data.getHologram().stop();
		}

		_data.clear();
	}

	public boolean isDeathEffectItem(Entity entity)
	{
		if (entity instanceof Item)
		{
			Item item = (Item) entity;
			ItemStack itemStack = item.getItemStack();

			if (itemStack.hasItemMeta())
			{
				if (itemStack.getItemMeta().hasDisplayName())
				{
					String name = itemStack.getItemMeta().getDisplayName();
					return name.contains(FOOD_ITEM_TITLE);
				}
			}
		}

		return false;
	}

	public ArrayList<DeathEffectData> getData()
	{
		return _data;
	}
}
