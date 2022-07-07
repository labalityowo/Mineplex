package mineplex.core.gadget.gadgets.item;

import java.text.DecimalFormat;
import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;

public class ItemMobBomb extends ItemGadget
{

	private static final PotionEffect SLOWNESS = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5, false, false);
	private static final int TIME_TIL_EXPLODE = 4;
	private static final ItemStack[] GORE = new ItemStack[]
			{
					new ItemStack(Material.BONE),
					new ItemStack(Material.INK_SACK, 1, (short) 0, (byte) 1)
			};
	private static final DecimalFormat FORMAT = new DecimalFormat("0.0");
	private static final String[] ONE_LINERS =
			{
					"Guess he... burned the bacon...",
					"Guess he couldn't... bear it...",
					"What do you call a cow with no legs? Ground beef.",
					"What do you call a cow during an earthquake? A milkshake.",
					"Letting the cat out of the bag is a whole lot easier than putting it back in.",
					"My email password has been hacked. That's the third time I've had to rename the cat."
			};

	private final Map<Player, MobType> _typeMap;
	private final Set<LivingEntity> _exploding;

	public ItemMobBomb(GadgetManager manager)
	{
		super(manager, "Mob Bomb", new String[]
				{
						C.cGray + "Tick.. tick.. tick..",
						"",
						C.cWhite + "Left click to cycle through",
						C.cWhite + "different mobs.",
						C.cWhite + "Right click to launch the",
						C.cWhite + "Mob bomb!"
				}, CostConstants.POWERPLAY_BONUS, Material.DIAMOND_BARDING, (byte) 0, TimeUnit.SECONDS.toMillis(8), null);

		Free = false;
		setPPCYearMonth(YearMonth.of(2018, Month.JANUARY));

		_typeMap = new HashMap<>();
		_exploding = new HashSet<>();
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		_typeMap.put(player, MobType.PIG);

		super.enableCustom(player, message);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		_typeMap.remove(player);
	}

	@Override
	protected void giveItem(Player player)
	{
		player.getInventory().setItem(Manager.getActiveItemSlot(), new ItemBuilder(getDisplayMaterial(), getDisplayData())
				.setTitle(F.item(getName()) + " - " + F.skill(_typeMap.get(player).Name) + " - " + F.name(" Left") + " - " + F.name("Cycle") + "/" + F.name("Right") + " - " + F.name("Fire"))
				.build());
	}

	@Override
	public void ActivateCustom(Player player)
	{

	}

	@Override
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player) || !IsItem(player))
		{
			return;
		}

		if (UtilEvent.isAction(event, ActionType.L) && Recharge.Instance.use(player, getName() + " Switch", 250, false, false))
		{
			_typeMap.put(player, _typeMap.get(player).next());
			giveItem(player);
		}
		else if (UtilEvent.isAction(event, ActionType.R) && Recharge.Instance.use(player, getName(), _recharge, true, true))
		{
			launchBomb(player, _typeMap.get(player));
		}
	}

	private void launchBomb(Player player, MobType type)
	{
		Location location = player.getEyeLocation();
		LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type.Type);

		UtilEnt.ghost(entity, true, false);
		entity.addPotionEffect(SLOWNESS);
		entity.setCustomNameVisible(true);
		entity.setVelocity(location.getDirection());

		_exploding.add(entity);

		Manager.runSyncTimer(new BukkitRunnable()
		{

			double timer = TIME_TIL_EXPLODE;
			boolean tick = false;

			@Override
			public void run()
			{
				timer -= 0.1;
				tick = !tick;

				Location entityLocation = entity.getLocation().add(0, 0.8, 0);

				if (timer < 0 && !entity.isValid())
				{
					applyVelocity(UtilItem.dropItem(type.ToDrop, entityLocation, false, false, 25 + UtilMath.r(10), false));

					for (ItemStack itemStack : GORE)
					{
						applyVelocity(UtilItem.dropItem(itemStack, entityLocation, false, false, 25 + UtilMath.r(10), false));
					}

					if (timer < -1.5)
					{
						UtilParticle.PlayParticleToAll(ParticleType.CLOUD, entityLocation, 0.8F, 0.4F, 0.8F, 0, 30, ViewDist.NORMAL);
						cancel();
						_exploding.remove(entity);
					}
				}
				else
				{
					if (tick && entity instanceof Sheep)
					{
						((Sheep) entity).setColor(timer < 1.5 ? DyeColor.RED : DyeColor.YELLOW);
					}

					entity.setCustomName((tick ? C.cRedB : C.cWhiteB) + FORMAT.format(timer));
					UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, entityLocation, 0.8F, 0.4F, 0.8F, 0, (int) (8 - (timer * 2)), ViewDist.NORMAL);

					if (timer < 0)
					{
						entityLocation.getWorld().playSound(location, type.DeathSound, 1, 1);
						entity.remove();
						UtilFirework.playFirework(entityLocation, FireworkEffect.builder()
								.with(Type.BURST)
								.withColor(type.Colour)
								.withFade(Color.WHITE)
								.withFlicker()
								.build());
						player.sendMessage(F.main(Manager.getName(), UtilMath.randomElement(ONE_LINERS)));

						for (Player nearby : UtilPlayer.getNearby(entityLocation, 4))
						{
							UtilAction.velocity(nearby, UtilAlg.getTrajectory2d(entity, player).setY(0.5 + (Math.random() / 3)));
						}
					}
				}
			}
		}, 1, 2);
	}

	private void applyVelocity(Item item)
	{
		item.setVelocity(new Vector((Math.random() - 0.5) / 1.5, Math.random() / 2 + 0.3, (Math.random() - 0.5) / 1.5));
	}

	@EventHandler
	public void entityExplode(ExplosionPrimeEvent event)
	{
		if (_exploding.contains(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void entityCombust(EntityCombustEvent event)
	{
		if (_exploding.contains(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	private enum MobType
	{

		PIG(EntityType.PIG, Sound.PIG_DEATH, Material.GRILLED_PORK, Color.FUCHSIA),
		CHICKEN(EntityType.CHICKEN, Sound.CHICKEN_HURT, Material.COOKED_CHICKEN, Color.WHITE),
		COW(EntityType.COW, Sound.COW_HURT, Material.COOKED_BEEF, Color.MAROON),
		MOOSHROOM(EntityType.MUSHROOM_COW, Sound.COW_IDLE, Material.MUSHROOM_SOUP, Color.RED, Color.MAROON),
		SHEEP(EntityType.SHEEP, Sound.SHEEP_IDLE, Material.COOKED_MUTTON, Color.RED, Color.YELLOW),
		VILLAGER(EntityType.VILLAGER, Sound.VILLAGER_YES, Material.EMERALD, Color.GREEN, Color.LIME),
		PIG_ZOMBIE(EntityType.PIG_ZOMBIE, Sound.ZOMBIE_PIG_ANGRY, Material.GOLD_SWORD, Color.FUCHSIA, Color.YELLOW),
		CAVE_SPIDER(EntityType.CAVE_SPIDER, Sound.SPIDER_DEATH, Material.FERMENTED_SPIDER_EYE, Color.RED, Color.BLUE),
		CREEPER(EntityType.CREEPER, Sound.CREEPER_DEATH, Material.SULPHUR, Color.LIME, Color.BLACK),
		SKELETON(EntityType.SKELETON, Sound.SKELETON_DEATH, Material.BOW, Color.GRAY, Color.BLACK),
		SLIME(EntityType.SLIME, Sound.SLIME_WALK, Material.SLIME_BALL, Color.LIME),
		ZOMBIE(EntityType.ZOMBIE, Sound.ZOMBIE_DEATH, Material.ROTTEN_FLESH, Color.GREEN),
		OCELOT(EntityType.OCELOT, Sound.CAT_HISS, Material.RAW_FISH, Color.YELLOW),
		WOLF(EntityType.WOLF, Sound.WOLF_DEATH, Material.BONE, Color.RED, Color.WHITE),
		GOLEM(EntityType.IRON_GOLEM, Sound.IRONGOLEM_DEATH, Material.IRON_BLOCK, Color.GRAY);

		EntityType Type;
		String Name;
		Sound DeathSound;
		ItemStack ToDrop;
		Color[] Colour;

		MobType(EntityType type, Sound sound, Material toDrop, Color... colours)
		{
			Type = type;
			Name = UtilEnt.getName(type);
			DeathSound = sound;
			ToDrop = new ItemStack(toDrop);
			Colour = colours;
		}

		public MobType next()
		{
			return values()[(ordinal() + 1) % values().length];
		}
	}
}
