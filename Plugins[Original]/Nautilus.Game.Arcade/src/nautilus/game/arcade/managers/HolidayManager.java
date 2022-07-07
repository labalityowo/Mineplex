package nautilus.game.arcade.managers;

import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockAction;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.gadget.set.SetHalloween;
import mineplex.core.gadget.types.GadgetSet;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.rankGiveaway.eternal.EternalGiveawayAnimation;
import mineplex.core.rankGiveaway.eternal.EternalGiveawayManager;
import mineplex.core.rankGiveaway.titangiveaway.TitanGiveawayManager;
import mineplex.core.treasure.types.TreasureType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.uhc.UHC;
import nautilus.game.arcade.managers.events.SpecialEntityDeathEvent;

public class HolidayManager implements Listener
{
	private static final BlockFace[] BLOCK_FACES = {
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST,
			BlockFace.NORTH_EAST,
			BlockFace.SOUTH_EAST,
			BlockFace.SOUTH_WEST,
			BlockFace.NORTH_WEST
	};

	private static final String[] EGG_SKINS = {
			"KingCrazy_",
			"Trajectories"
	};

	public enum HolidayType
	{
		CHRISTMAS(Material.CHEST, "Present", Sound.CAT_MEOW),
		HALLOWEEN(Material.PUMPKIN, "Pumpkin", Sound.ZOMBIE_REMEDY),
		EASTER(Material.SKULL, "Easter Egg", Sound.CAT_MEOW),
		THANKSGIVING(null, C.cGoldB + "Thanksgiving Chicken", null);

		private Material _blockType;
		private String _blockName;
		private Sound _blockBreakSound;

		HolidayType(Material blockType, String blockName, Sound blockBreakSound)
		{
			_blockType = blockType;
			_blockName = blockName;
			_blockBreakSound = blockBreakSound;
		}

		public String getBlockName()
		{
			return _blockName;
		}

		public Sound getBlockSound()
		{
			return _blockBreakSound;
		}

		public Material getBlockType()
		{
			return _blockType;
		}
	}

	private HolidayType _type = HolidayType.HALLOWEEN;
	private String _statName = "Halloween Pumpkins 2017";

	private ArcadeManager _arcadeManager;
	private TitanGiveawayManager _titanManager;
	private EternalGiveawayManager _eternalGiveawayManager;

	public HashSet<Block> _active = new HashSet<>();
	public HashSet<Entity> _activeEntities = new HashSet<>();

	private HashSet<Item> _items = new HashSet<>();

	private HashSet<Item> _shards = new HashSet<>();
	private HashSet<Item> _gems = new HashSet<>();

	private static final double CHEST_CHANCE = 0.001;
	private static final double SPAWN_CHANCE = 0.01;
	private static final double CHICKEN_DAMAGE = 0.5;

	public long _lastSpawn = System.currentTimeMillis();

	public HolidayManager(ArcadeManager arcadeManager, TitanGiveawayManager titanManager, EternalGiveawayManager eternalGiveawayManager)
	{
		_arcadeManager = arcadeManager;
		_titanManager = titanManager;
		_eternalGiveawayManager = eternalGiveawayManager;

		_arcadeManager.getPluginManager().registerEvents(this, _arcadeManager.getPlugin());
	}

	@EventHandler
	public void reset(GameStateChangeEvent event)
	{
		_active.clear();
		_activeEntities.forEach(Entity::remove);
		_activeEntities.clear();

		_lastSpawn = System.currentTimeMillis();
	}

	@EventHandler
	public void blockEffect(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Block> blockIterator = _active.iterator();

		while (blockIterator.hasNext())
		{
			Block block = blockIterator.next();

			//Break
			if (block.getType() != Material.PUMPKIN &&
					block.getType() != Material.JACK_O_LANTERN &&
					block.getType() != Material.CHEST &&
					block.getType() != Material.SKULL)
			{
				specialBlockBreak(null, block);
				blockIterator.remove();
				continue;
			}

			if (_type.equals(HolidayType.HALLOWEEN))
			{
				UtilParticle.PlayParticle(ParticleType.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 0, 0, 0, 0.06f, 4,
						ViewDist.LONG, UtilServer.getPlayers());
				if (Math.random() > 0.90)
				{
					if (block.getType() == Material.PUMPKIN)
					{
						block.setType(Material.JACK_O_LANTERN);
					}
					else
					{
						block.setType(Material.PUMPKIN);
					}
				}
			}
			else if (_type.equals(HolidayType.EASTER))
			{
				UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.2, 0.5), 0.3f, 0.2f, 0.3f, 0, 1,
						ViewDist.LONG, UtilServer.getPlayers());

				if (Math.random() > 0.90)
				{
					Item egg = block.getWorld().dropItem(block.getLocation().add(0.5, 0.8, 0.5),
							ItemStackFactory.Instance.CreateStack(Material.EGG, (byte) 0, 1, System.currentTimeMillis() + "Egg"));
					egg.setVelocity(new Vector((Math.random() - 0.5) * 0.3, Math.random() - 0.4, (Math.random() - 0.5) * 0.3));

					_items.add(egg);

					block.getWorld().playSound(block.getLocation(), Sound.CHICKEN_EGG_POP, 0.25f + (float) Math.random() * 0.75f, 0.75f + (float) Math.random() * 0.5f);
				}

				/*if (Math.random() > 0.95)
				{
					sendChestPackets(block);
				}*/
			}
			else if (_type.equals(HolidayType.CHRISTMAS))
			{
				UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, block.getLocation().add(0.5, 1, 0.5), 0.5f, 0.5f, 0.5f, 0, 3,
						ViewDist.LONG, UtilServer.getPlayers());
			}
		}

		Iterator<org.bukkit.entity.Entity> entityIterator = _activeEntities.iterator();

		while (entityIterator.hasNext())
		{
			org.bukkit.entity.Entity entity = entityIterator.next();

			if (!(entity instanceof Chicken))
			{
				specialEntityKill(null, entity);
				entityIterator.remove();
			}

			if (_type.equals(HolidayType.THANKSGIVING))
			{
				if (Math.random() > 0.90)
				{
					UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, entity.getLocation().add(0.5, 1, 0.5), 0.5f, 0.5f, 0.5f, 0, 3,
							ViewDist.LONG, UtilServer.getPlayers());
				}
			}

		}
	}

	@EventHandler
	public void spawnSpecialBlockUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (_arcadeManager.GetGame() == null)
			return;

		if (_arcadeManager.GetGameHostManager().isPrivateServer())
			return;

		Game game = _arcadeManager.GetGame();

		int requirement = (int) ((double) _arcadeManager.GetPlayerFull() * 0.5d);
		if (UtilServer.getPlayers().length < requirement)
			return;

		if (game.GetState() != GameState.Live)
			return;

		if (game instanceof UHC)
			return;

		if (game instanceof Christmas)
			return;

		if (!UtilTime.elapsed(_lastSpawn, 90000))
			return;

		if (Math.random() > SPAWN_CHANCE)
			return;

		double toDrop = Math.max(1, game.GetPlayers(false).size() / 6);
		GadgetSet set = game.getArcadeManager().getCosmeticManager().getGadgetManager().getGadgetSet(SetHalloween.class);

		for (Player player : game.GetPlayers(true))
		{
			if (set.isActive(player))
			{
				toDrop += 0.4;
			}
		}

		for (int i = 0; i < toDrop; i++)
		{
			double interval = 1 / toDrop;

			if (Math.random() >= (i * interval)) // Diminishing per growth
			{
				spawnSpecialBlock(findSpecialBlockLocation(game));
			}
		}

		_lastSpawn = System.currentTimeMillis();
	}

	private void spawnSpecialBlock(Block block)
	{
		if (block == null)
		{
			System.out.println("Holiday Block: Could Not Find Suitable Block");
			return;
		}

		if (_type.getBlockType() == null && _type.equals(HolidayType.THANKSGIVING))
		{
			// Spawns thanksgiving chicken
			boolean oldValue = _arcadeManager.GetGame().CreatureAllowOverride;
			_arcadeManager.GetGame().CreatureAllowOverride = true;
			Chicken chicken = block.getWorld().spawn(block.getLocation().clone().add(.5, .5, .5), Chicken.class);
			_arcadeManager.GetGame().CreatureAllowOverride = oldValue;
			chicken.setAdult();
			chicken.setCustomName(_type.getBlockName());
			chicken.setCustomNameVisible(true);
			chicken.setVelocity(chicken.getVelocity().multiply(15));
			block.getWorld().playSound(block.getLocation(), Sound.CHICKEN_IDLE, 1f, 1f);
			_activeEntities.add(chicken);
			return;
		}

		block.setType(_type.getBlockType());
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, _type.getBlockType());

		if (_type.getBlockType() == Material.CHEST)
		{
			sendChestPackets(block);
		}

		if (_type.getBlockType() == Material.SKULL)
		{
			block.setData((byte) 1);
			Skull skull = (Skull) block.getState();
			skull.setSkullType(SkullType.PLAYER);
			skull.setOwner(EGG_SKINS[UtilMath.r(EGG_SKINS.length)]);
			skull.setRotation(BLOCK_FACES[UtilMath.r(BLOCK_FACES.length)]);
			skull.update();
		}

		_active.add(block);

		System.out.println("Spawned Holiday Block: " + UtilWorld.locToStrClean(block.getLocation()));
	}

	private void sendChestPackets(Block block)
	{
		PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(new BlockPosition(block.getX(), block.getY(), block.getZ()),
				CraftMagicNumbers.getBlock(block), 1, 1);

		for (Player other : UtilServer.getPlayers())
			UtilPlayer.sendPacket(other, packet);
	}

	private Block findSpecialBlockLocation(Game game)
	{
		Block block = null;
		int attempts = 2000;
		while (attempts > 0)
		{
			attempts--;

			int x = game.WorldData.MinX + UtilMath.r(Math.abs(game.WorldData.MaxX - game.WorldData.MinX));
			int z = game.WorldData.MinZ + UtilMath.r(Math.abs(game.WorldData.MaxZ - game.WorldData.MinZ));

			block = UtilBlock.getHighest(game.WorldData.World, x, z, null);

			if (block.getLocation().getY() <= 2 || block.getLocation().getY() < game.WorldData.MinY || block.getLocation().getY() > game.WorldData.MaxY)
				continue;

			if (block.getRelative(BlockFace.DOWN).isLiquid())
				continue;

			if (!UtilBlock.airFoliage(block) || !UtilBlock.airFoliage(block.getRelative(BlockFace.UP)))
				continue;

			if (!UtilBlock.solid(block.getRelative(BlockFace.DOWN)))
				continue;

			boolean nextToChest = false;
			for (Block other : UtilBlock.getSurrounding(block, false))
			{
				if (other.getType() == Material.CHEST)
					nextToChest = true;
			}
			if (nextToChest)
				continue;

			return block;
		}

		return null;
	}

	@EventHandler
	public void specialBlockDamage(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.L_BLOCK))
			return;

		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;

		if (_arcadeManager.GetGame() != null && !_arcadeManager.GetGame().IsAlive(event.getPlayer()))
			return;

		specialBlockBreak(event.getPlayer(), event.getClickedBlock());
	}

	@EventHandler
	public void specialEntityDeath(SpecialEntityDeathEvent event)
	{
		if (!_type.equals(HolidayType.THANKSGIVING))
			return;

		if (!(event.getEntity() instanceof Chicken))
			return;

		if (event.getKiller() == null)
			return;

		Chicken chicken = (Chicken) event.getEntity();
		Player killer = event.getKiller();

		if (_arcadeManager.GetGame() != null && !_arcadeManager.GetGame().IsAlive(killer))
			return;

		if (!_activeEntities.contains(chicken))
			return;

		_activeEntities.remove(chicken);
		specialEntityKill(killer, chicken);
		killer.getWorld().playSound(killer.getLocation(), Sound.CHICKEN_HURT, 1f, 1f);
	}

	@EventHandler
	public void specialEntityDamage(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Chicken))
			return;

		if (_arcadeManager.GetGame() == null)
			return;

		Chicken chicken = (Chicken) event.getEntity();

		if (!_activeEntities.contains(chicken))
			return;

		if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID) || event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) ||
				event.getCause().equals(EntityDamageEvent.DamageCause.FALL))
		{
			specialEntityKill(null, chicken);
			_activeEntities.remove(chicken);
			chicken.remove();
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void specialEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Chicken))
			return;

		if (!(event.getDamager() instanceof Player))
			return;

		if (_arcadeManager.GetGame() == null)
			return;

		if (UtilPlayer.isSpectator(event.getDamager()))
			return;

		Chicken chicken = (Chicken) event.getEntity();

		if (!_activeEntities.contains(chicken))
			return;

		if (chicken.getHealth() <= CHICKEN_DAMAGE)
		{
			SpecialEntityDeathEvent specialEntityDeathEvent = new SpecialEntityDeathEvent(chicken, (Player) event.getDamager());
			Bukkit.getPluginManager().callEvent(specialEntityDeathEvent);
			chicken.remove();
			return;
		}

		chicken.damage(CHICKEN_DAMAGE);
	}

	private void specialBlockBreak(Player player, final Block block)
	{
		if (!_active.contains(block))
			return;

		_active.remove(block);

		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, _type.getBlockType());
		block.setType(Material.AIR);

		if (player != null && _arcadeManager.GetGame() != null)
		{
			_arcadeManager.GetGame().AddStat(player, _statName, 1, false, true);
			System.out.println("Recording Holiday Block Break for " + player.getName());
		}

		//Shards
		for (int i = 0; i < 4 + Math.random() * 8; i++)
		{
			Item shard = block.getWorld().dropItem(block.getLocation().add(0.5, 1, 0.5),
					ItemStackFactory.Instance.CreateStack(Material.PRISMARINE_SHARD, (byte) 0, 1, UtilMath.r(999999) + "Shard"));

			Vector vel = new Vector(
					(Math.random() - 0.5) * 0.5,
					0.1 + Math.random() * 0.3,
					(Math.random() - 0.5) * 0.5);

			shard.setVelocity(vel);

			shard.setPickupDelay(20);

			_shards.add(shard);
		}

		//Gems
		for (int i = 0; i < 4 + Math.random() * 8; i++)
		{
			Item gem = block.getWorld().dropItem(block.getLocation().add(0.5, 1, 0.5),
					ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 1, UtilMath.r(999999) + "Gem"));

			Vector vel = new Vector(
					(Math.random() - 0.5) * 0.5,
					0.1 + Math.random() * 0.3,
					(Math.random() - 0.5) * 0.5);

			gem.setVelocity(vel);

			gem.setPickupDelay(20);

			_gems.add(gem);
		}
		/*
		// Titan Giveaway
		if (player != null)
		{
			_titanManager.openPumpkin(player, new Runnable()
			{
				@Override
				public void run()
				{
					Location location = block.getLocation().add(0.5, 0.5, 0.5);
					new TitanGiveawayAnimation(_titanManager, location, 3000L);
				}
			});
		}*/

		// Eternal Giveaway
		if (player != null)
		{
			_eternalGiveawayManager.openPumpkin(player, () ->
			{
				Location location = block.getLocation().add(0.5, 0.5, 0.5);
				new EternalGiveawayAnimation(_eternalGiveawayManager, location, 3000L);
			});
		}

		if (player != null)
		{
			InventoryManager manager = _arcadeManager.getInventoryManager();
			double rand = UtilMath.random.nextDouble();
			if (rand < CHEST_CHANCE)
			{
				FireworkEffect fireworkEffect = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE)
						.withColor(Color.AQUA).withColor(Color.BLACK).withFade(Color.AQUA)
						.withFade(Color.BLACK).flicker(true).build();
				UtilFirework.playFirework(block.getLocation().add(0.5, 0.5, 0.5), fireworkEffect);
				manager.addItemToInventory(player, TreasureType.TRICK_OR_TREAT_2017.getItemName(), 1);
				UtilPlayer.message(player, F.main("Holiday Rewards", "You found a Trick or Treat Bag in a " + _type.getBlockName() + "!"));
			}
			else
			{
				UtilPlayer.message(player, F.main("Holiday Rewards", "You found a " + _type.getBlockName()));
			}
		}

		//Effect
		block.getWorld().playSound(block.getLocation(), _type.getBlockSound(), 1f, 1f);
	}

	private void specialEntityKill(Player player, org.bukkit.entity.Entity entity)
	{
		if (player != null && _arcadeManager.GetGame() != null)
		{
			_arcadeManager.GetGame().AddStat(player, _statName, 1, false, true);
			System.out.println("Recording Entity Killing for " + player.getName());
		}

		//Coins
		for (int i = 0; i < 4 + Math.random() * 8; i++)
		{
			Item shard = entity.getWorld().dropItem(entity.getLocation().add(0.5, 1, 0.5),
					ItemStackFactory.Instance.CreateStack(Material.PRISMARINE_SHARD, (byte) 0, 1, UtilMath.r(999999) + "Shard"));

			Vector vel = new Vector(
					(Math.random() - 0.5) * 0.5,
					0.1 + Math.random() * 0.3,
					(Math.random() - 0.5) * 0.5);

			shard.setVelocity(vel);

			shard.setPickupDelay(20);

			_shards.add(shard);
		}

		//Gems
		for (int i = 0; i < 4 + Math.random() * 8; i++)
		{
			Item gem = entity.getWorld().dropItem(entity.getLocation().add(0.5, 1, 0.5),
					ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 1, UtilMath.r(999999) + "Gem"));

			Vector vel = new Vector(
					(Math.random() - 0.5) * 0.5,
					0.1 + Math.random() * 0.3,
					(Math.random() - 0.5) * 0.5);

			gem.setVelocity(vel);

			gem.setPickupDelay(20);

			_gems.add(gem);
		}

		// Eternal Giveaway
		if (player != null)
		{
			_eternalGiveawayManager.openPumpkin(player, new Runnable()
			{
				@Override
				public void run()
				{
					Location location = entity.getLocation().add(0.5, 0.5, 0.5);
					new EternalGiveawayAnimation(_eternalGiveawayManager, location, 3000L);
				}
			});
		}
	}

	@EventHandler
	public void coinPickup(PlayerPickupItemEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;

		if (_shards.contains(event.getItem()))
		{
			event.setCancelled(true);
			event.getItem().remove();

			_arcadeManager.GetDonation().rewardCurrency(GlobalCurrency.TREASURE_SHARD, event.getPlayer(), _type + " Shards", 4 * event.getItem().getItemStack().getAmount());

			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 2f);
		}
		else if (_gems.contains(event.getItem()))
		{
			event.setCancelled(true);
			event.getItem().remove();

			_arcadeManager.GetDonation().rewardCurrency(GlobalCurrency.GEM, event.getPlayer(), _type + " Gems", 4 * event.getItem().getItemStack().getAmount());

			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 2f);
		}

		else if (_items.contains(event.getItem()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void itemClean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<Item> coinIterator = _shards.iterator();

		while (coinIterator.hasNext())
		{
			Item coin = coinIterator.next();

			if (!coin.isValid() || coin.getTicksLived() > 1200)
			{
				coin.remove();
				coinIterator.remove();
			}
		}

		Iterator<Item> gemIterator = _gems.iterator();

		while (gemIterator.hasNext())
		{
			Item gem = gemIterator.next();

			if (!gem.isValid() || gem.getTicksLived() > 1200)
			{
				gem.remove();
				gemIterator.remove();
			}
		}

		Iterator<Item> eggIterator = _items.iterator();

		while (eggIterator.hasNext())
		{
			Item egg = eggIterator.next();

			if (!egg.isValid() || egg.getTicksLived() > 40)
			{
				egg.remove();
				eggIterator.remove();
			}
		}
	}

	@EventHandler
	public void spawnDebug(PlayerCommandPreprocessEvent event)
	{
		if (event.getPlayer().isOp() && event.getMessage().toLowerCase().startsWith("/holidayblock"))
		{
			spawnSpecialBlock(event.getPlayer().getLocation().getBlock());
			event.setCancelled(true);
		}
	}
}