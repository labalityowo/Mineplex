package nautilus.game.arcade.game.games.mineware.challenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.events.ChallengeEndEvent;
import nautilus.game.arcade.game.games.mineware.events.ChallengeStartEvent;
import nautilus.game.arcade.world.WorldData;

/**
 * <p>
 * This class contains the base structure of a challenge.
 * All challenges should trigger any functionality inside
 * {@link #createSpawns()}, {@link #createMap()}, {@link #onStart()} and {@link #onEnd()}.
 * </p>
 *
 * Additionally, {@link #onTimerFinish()} and {@link #onCollide(LivingEntity, Block, ProjectileUser)} can be overrided.
 */
public abstract class Challenge implements Listener
{
	protected static final int TICK_MULTIPLIER = 20;
	private static final int TITLE_FADE_IN_TICKS = 5;
	private static final int TITLE_STAY_TICKS = 40;
	private static final int TITLE_FADE_OUT_TICKS = 5;

	private static final int BORDER_MIN_X = -100;
	private static final int BORDER_MAX_X = 100;
	private static final int BORDER_MIN_Y = 0;
	private static final int BORDER_MAX_Y = 256;
	private static final int BORDER_MIN_Z = -100;
	private static final int BORDER_MAX_Z = 100;
	private static final int COMPLETE_COUNT_DIVIDER = 2;

	public static final int WINNER_ADD_CRITERIA = 3; // players
	public static final int CHICKEN_ATTACK_CRITERIA = 2; // players

	private static final int CHALLENGE_CLOAK_DURATION = 7777;
	private static final int COMPLETION_GEMS = 3;
	private static final int STARTING_SOON_MESSAGE_CRITERIA = 2; // players

	private static final double BLOCK_CENTER_ADD = 0.5;
	private static final int CRUMBLE_CHANCE = 4;

	private static final int PLAYER_COUNT_FOR_GAME_END = 1;
	private static final int COMPLETION_TITLE_STAY_TICKS = 30;
	private static final float COMPLETION_SOUND_VOLUME = 2.0F;
	private static final float COMPLETION_SOUND_PITCH = 1.0F;

	private static final float LOST_SOUND_VOLUME = 2.0F;
	private static final float LOST_SOUND_PITCH = 1.0F;

	protected static final int INVENTORY_HOTBAR_SLOTS = 8;

	private static final int BLOCK_BREAK_PARTICLE_COUNT = 10;
	private static final int GRASS_SPAWN_CHANCE = 4;
	private static final int FLOWER_SPAWN_CHANCE = 8;
	private static final int FLOWER_DATA_RANGE = 7;

	private static final int DOUBLE_PLANT_CHANCE = 3;
	private static final int DOUBLE_PLANT_DATA_RANGE = 5;
	private static final byte DOUBLE_PLANT_PART_DATA = 8;

	private static final int DISPLAY_COUNT_EXPIRE_AFTER = 1000;
	private static final double DISPLAY_COUNT_ELEVATION_RATE = 0.05;

	private static final int DEFAULT_ARENA_SIZE = 8;
	private static final int ARENA_SIZE_DIVIDER = 2;
	private static final int ARENA_SIZE_LIMIT = 40;

	protected final BawkBawkBattles Host;
	protected final ChallengeSettings Settings;
	protected final ChallengeData Data;

	private ChallengeType _type;
	private String _name;
	private String[] _description;

	public Challenge(BawkBawkBattles host, ChallengeType type, String name, String... description)
	{
		Host = host;
		_type = type;
		_name = name;
		_description = description;

		Settings = new ChallengeSettings(this);
		Data = new ChallengeData();
	}

	public void markSpawnLocations()
	{
		for (Location spawn : Data.getDefinedSpawns())
		{
			Block markedBlock = spawn.getBlock().getRelative(BlockFace.DOWN);
			markedBlock.setType(Material.EMERALD_BLOCK);
			addBlock(markedBlock);
		}
	}

	/**
	 * The list of spawn locations where players will be teleported.
	 *
	 * @return ArrayList<Location>
	 */
	public abstract ArrayList<Location> createSpawns();

	/**
	 * The list of actions to perform in order for the map to be created.
	 */
	public abstract void createMap();

	/**
	 * The list of actions to perform once the challenge is started.
	 */
	public void onStart()
	{
	}

	/**
	 * The list of actions to perform after the challenge has ended.
	 */
	public void onEnd()
	{
	}

	public void spawn(boolean firstRun)
	{
		if (firstRun)
		{
			ArrayList<Player> players = Host.GetPlayers(true);

			for (int i = 0; i < players.size(); i++)
			{
				Player player = players.get(i);

				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						Host.getPlayerTeam().SpawnTeleport(player);
					}
				}.runTaskLater(Host.Manager.getPlugin(), i);
			}
		}
		else
		{
			Host.getPlayerTeam().SpawnTeleport(true);
		}
	}

	public void start()
	{
		setBorder(BORDER_MIN_X, BORDER_MAX_X, BORDER_MIN_Y, BORDER_MAX_Y, BORDER_MIN_Z, BORDER_MAX_Z);

		Settings.setStartTime(System.currentTimeMillis());
		Settings.setMaxCompletedCount((int) Math.ceil(getPlayersAlive().size() / COMPLETE_COUNT_DIVIDER));

		if (Settings.isInventoryLocked())
		{
			setLockedInventorySlot();
		}

		onStart();
		callStartEvent();
	}

	private void setLockedInventorySlot()
	{
		for (Player player : getPlayersAlive())
		{
			player.getInventory().setHeldItemSlot(Settings.getLockedSlot());
		}
	}

	private void callStartEvent()
	{
		Bukkit.getServer().getPluginManager().callEvent(new ChallengeStartEvent(this));
		Host.Manager.getPluginManager().registerEvents(this, Host.Manager.getPlugin());
	}

	public boolean canFinish()
	{
		ArrayList<Player> players = getPlayersAlive();

		int alive = getPlayersIn(true).size();
		int completed = Data.getCompletedPlayers().size();
		int lost = Data.getLostPlayers().size();

		boolean maxTimeReached = UtilTime.elapsed(Settings.getStartTime(), Settings.getDuration());

		if (maxTimeReached)
		{
			if (!Settings.shouldHideTimerRanOutMessage())
			{
				UtilServer.broadcast(F.main("Game", "Challenge timer has ran out."));
			}

			onTimerFinish();
			return true;
		}
		else if (alive <= completed)
		{
			return true;
		}
		else if (_type == ChallengeType.LastStanding)
		{
			if (alive <= Settings.getMaxCompletedCount())
			{
				for (Player player : players)
				{
					setCompleted(player);
				}

				return true;
			}
			else if (lost > alive)
			{
				return true;
			}
		}
		else if (_type == ChallengeType.FirstComplete && completed >= Settings.getMaxCompletedCount())
		{
			return true;
		}

		return maxTimeReached;
	}

	public void end()
	{
		handleRemainingPlayers();
		callEndEvent();

		if (Data.hasInvisiblePlayers())
		{
			removeCloakedPlayers();
		}

		onEnd();
		Data.reset();
	}

	private void removeCloakedPlayers()
	{
		for (Player player : Data.getInvisiblePlayers())
		{
			Host.Manager.GetCondition().EndCondition(player, ConditionType.CLOAK, "Challenge Ended");
		}
	}

	private void callEndEvent()
	{
		Host.Manager.getPluginManager().callEvent(new ChallengeEndEvent(this));
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void death(PlayerDeathEvent event)
	{
		if (!Host.IsLive() || !Host.IsAlive(event.getEntity()))
			return;

		Player player = event.getEntity();

		Location death = player.getLocation().clone();
		Data.addLostPlayer(player);

		Host.getDeathEffect().playDeath(player, death);
		handleDeath(player);
	}

	private void handleDeath(Player player)
	{
		int lives = loseLife(player);

		if (lives <= 0)
		{
			Host.getPlayerTeam().SetPlayerState(player, GameTeam.PlayerState.OUT);

			int alive = Host.getPlayersWithRemainingLives();
			if (Host.lives(player) <= 0)
			{
				Host.getWinners().addFirst(player);
				if (alive > Challenge.CHICKEN_ATTACK_CRITERIA)
					Host.getChickenAttack().start(player);
				else
					Host.getChickenAttack().kill(player, true);
			}
		}
		else
		{
			handleFailure(player);
		}
	}

	private int loseLife(Player player)
	{
		int lives = Host.lives(player);
		lives--;

		if (lives >= 0)
		{
			Host.setLives(player, lives);
		}

		return lives;
	}

	private void handleFailure(Player player)
	{
		UtilPlayer.message(player, F.main("Game", C.cRed + "You failed to complete the task."));
		Host.showLivesLeft(player);
		Host.Manager.addSpectator(player, true);
		if (UtilItem.matchesMaterial(player.getInventory().getItem(8), Material.WATCH))
			player.getInventory().setItem(8, null);
		Host.Manager.GetCondition().Factory().Cloak("Challenge Death", player, player, CHALLENGE_CLOAK_DURATION, true, true);
	}

	private void handleRemainingPlayers()
	{
		for (Player player : getPlayersAlive())
		{
			if (Data.hasAnyoneCompleted() && !Data.isDone(player))
			{
				int lives = Host.lives(player);

				if (lives > 1)
				{
					setLost(player);
				}
				else
				{
					handleDeath(player);
				}
			}

			addGems(player);
		}
	}

	private void addGems(Player player)
	{
		if (Data.isCompleted(player))
		{
			Host.AddGems(player, COMPLETION_GEMS, "Completed Challenges", true, true);
		}
	}

	@EventHandler
	public void startingSoon(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !Host.IsLive() || Host.getPlayersWithRemainingLives() <= STARTING_SOON_MESSAGE_CRITERIA)
			return;

		HashSet<Player> players = new HashSet<Player>();
		players.addAll(Data.getCompletedPlayers());
		players.addAll(Data.getLostPlayers());

		for (Player player : players)
		{
			if (!Host.IsAlive(player) && Host.lives(player) > 0)
			{
				UtilTextBottom.display(C.Bold + "Next challenge will begin shortly.", player);
			}
		}
	}

	@EventHandler
	public void lockInventory(PlayerItemHeldEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (Settings.isInventoryLocked())
			event.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void crumble(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || !Host.IsLive() || !Settings.canCrumble() || !Host.getSettings().isCrumbling())
			return;

		Block qualifiedBlock = null;
		double furthestDistance = 0;

		if (Data.getModifiedBlocks().isEmpty())
		{
			Host.getSettings().setCrumbling(false);
			return;
		}

		for (Block currentBlock : Data.getModifiedBlocks())
		{
			double theBlocksDistance = UtilMath.offset2d(Host.GetSpectatorLocation(), currentBlock.getLocation().add(BLOCK_CENTER_ADD, BLOCK_CENTER_ADD, BLOCK_CENTER_ADD));

			if (qualifiedBlock == null || furthestDistance < theBlocksDistance)
			{
				qualifiedBlock = currentBlock;
				furthestDistance = theBlocksDistance;
			}
		}

		while (!qualifiedBlock.getRelative(BlockFace.DOWN).isEmpty())
		{
			qualifiedBlock = qualifiedBlock.getRelative(BlockFace.DOWN);
		}

		Data.removeModifiedBlock(qualifiedBlock);

		if (UtilMath.r(CRUMBLE_CHANCE) == 0)
		{
			qualifiedBlock.getWorld().spawnFallingBlock(qualifiedBlock.getLocation().add(BLOCK_CENTER_ADD, BLOCK_CENTER_ADD, BLOCK_CENTER_ADD), qualifiedBlock.getType(), qualifiedBlock.getData());
		}

		resetBlock(qualifiedBlock);
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
		if (!Host.IsLive())
			return;

		Data.removePlayer(event.getPlayer());
	}

	protected void setCompleted(Player player, boolean cloak)
	{
		if (Data.isDone(player))
			return;

		if (shouldShowEffects()) // Check if the game is not about to end.
		{
			if (cloak)
			{
				cloak(player, true);
			}

			alert(player, C.cGreen + "You have completed the challenge!", COMPLETION_TITLE_STAY_TICKS);
			player.playSound(player.getLocation(), Sound.LEVEL_UP, COMPLETION_SOUND_VOLUME, COMPLETION_SOUND_PITCH);
		}

		Data.addCompletedPlayer(player);
		UtilPlayer.clearPotionEffects(player);
		UtilInv.Clear(player);
	}

	private boolean shouldShowEffects()
	{
		if (Host.getPlayersWithRemainingLives() == PLAYER_COUNT_FOR_GAME_END)
		{
			int playerOneLives = Host.lives(getPlayersAlive().get(0));
			int playerTwoLives = Host.lives(getPlayersAlive().get(1));

			boolean playerOneCompleted = Data.isDone(getPlayersAlive().get(0));
			boolean playerTwoCompleted = Data.isDone(getPlayersAlive().get(1));

			if (!playerOneCompleted && !playerTwoCompleted && (playerOneLives == 1 || playerTwoLives == 2))
			{
				return false;
			}
		}

		return true;
	}

	protected void setCompleted(Player player)
	{
		setCompleted(player, false);
	}

	protected void setLost(Player player)
	{
		if (Data.isDone(player))
			return;

		Data.addLostPlayer(player);

		handleDeath(player);
	}

	private void cloak(Player player, boolean completed)
	{
		Data.addInvisiblePlayer(player);
		String reason = "Lost";

		if (completed)
		{
			reason = "Completed";
		}

		Host.Manager.GetCondition().Factory().Cloak(reason, player, player, CHALLENGE_CLOAK_DURATION, true, false);
	}

	protected void alert(Player player, String message)
	{
		alert(player, message, TITLE_STAY_TICKS);
	}

	protected void alert(Player player, String message, int stayTicks)
	{
		UtilTextMiddle.display(null, message, TITLE_FADE_IN_TICKS, stayTicks, TITLE_FADE_OUT_TICKS, player);
	}

	@SuppressWarnings("deprecation")
	public void setBlock(Block block, Material type, byte data)
	{
		UtilBlock.setQuick(block.getWorld(), block.getX(), block.getY(), block.getZ(), type.getId(), data);
	}

	public void setBlockReallyQuicklyAndDangerously(Block block, Material type, byte data)
	{
		World world = block.getWorld();
		int x = block.getLocation().getBlockX();
		int y = block.getLocation().getBlockY();
		int z = block.getLocation().getBlockZ();
		int i = x & 15;
		int j = y;
		int k = z & 15;
		int cx = block.getX() >> 4;
		int cz = block.getZ() >> 4;
		if (!world.isChunkLoaded(cx, cz))
		{
			world.loadChunk(cx, cz, true);
		}

		WorldServer nmsWorld = ((CraftWorld) world).getHandle();

		net.minecraft.server.v1_8_R3.Chunk chunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
		BlockPosition pos = new BlockPosition(x, y, z);
		IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getById(type.getId()).fromLegacyData(data);
		ChunkSection chunksection = chunk.getSections()[y >> 4];
		if (chunksection == null)
		{
			if (block != Blocks.AIR)
			{
				chunksection = chunk.getSections()[y >> 4] = new ChunkSection(y >> 4 << 4, chunk, !nmsWorld.worldProvider.o());
			}
		}
		chunksection.setType(i, j & 15, k, ibd);
		nmsWorld.notify(pos);
	}

	public void setBlock(Block block, Material type)
	{
		setBlock(block, type, (byte) 0);
	}

	public void setData(Block block, byte data)
	{
		setBlock(block, block.getType(), data);
	}

	public void resetBlock(Block block)
	{
		setBlock(block, Material.AIR, (byte) 0);
	}

	protected void addBlock(Block... blocks)
	{
		for (Block block : blocks)
		{
			if (!block.isEmpty())
			{
				Data.addModifiedBlock(block);
			}
		}
	}

	protected void addItem(ItemStack... items)
	{
		for (Player player : getPlayersAlive())
		{
			for (ItemStack item : items)
			{
				UtilInv.insert(player, item);
			}
		}
	}

	protected void setItem(int slot, ItemStack item)
	{
		for (Player player : getPlayersAlive())
		{
			player.getInventory().setItem(slot, item);
		}
	}

	protected void fillItem(ItemStack item)
	{
		for (Player player : getPlayersAlive())
		{
			for (int i = 0; i <= INVENTORY_HOTBAR_SLOTS; i++)
			{
				player.getInventory().setItem(i, item);
			}
		}
	}

	protected void removeItem(Material type, byte data)
	{
		for (Player player : getPlayersAlive())
		{
			UtilInv.removeAll(player, type, data);
		}
	}

	@SuppressWarnings("deprecation")
	protected void blockBreakEffect(Block block, boolean resetBlock)
	{
		UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(block.getType(), block.getData()), block.getLocation(), 0, 0, 0, 0, BLOCK_BREAK_PARTICLE_COUNT, ViewDist.NORMAL, UtilServer.getPlayers());
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());

		if (resetBlock)
		{
			resetBlock(block);
		}
	}

	protected void addEffect(PotionEffectType type, int duration, int amplifier)
	{
		for (Player player : getPlayersAlive())
		{
			player.addPotionEffect(new PotionEffect(type, duration, amplifier));
		}
	}

	protected void addEffect(PotionEffectType type, int amplifier)
	{
		addEffect(type, Integer.MAX_VALUE, amplifier);
	}

	protected void removeEffect(PotionEffectType type)
	{
		for (Player player : getPlayersAlive())
		{
			player.removePotionEffect(type);
		}
	}

	protected void remove(EntityType type)
	{
		for (Entity entity : Host.WorldData.World.getEntities())
		{
			if (entity.getType() == type && !Host.getDeathEffect().isDeathEffectItem(entity))
			{
				entity.remove();
			}
		}
	}

	protected Block generateGrass(Block block)
	{
		return generateGrass(block, false);
	}

	protected Block generateGrass(Block block, boolean bushes)
	{
		if (UtilMath.r(GRASS_SPAWN_CHANCE) == 0)
		{
			if (UtilMath.r(FLOWER_SPAWN_CHANCE) == 0)
			{
				makeFlower(block);
			}
			else
			{
				makeGrass(block, bushes);
			}
		}

		return block;
	}

	private void makeFlower(Block block)
	{
		Material flower = Material.YELLOW_FLOWER;
		byte data = 0;

		if (UtilMath.random.nextBoolean())
		{
			flower = Material.RED_ROSE;

			if (UtilMath.random.nextBoolean())
			{
				data = (byte) (UtilMath.r(FLOWER_DATA_RANGE) + 1);
			}
		}

		setBlock(block, flower, data);
	}

	@SuppressWarnings("deprecation")
	private void makeGrass(Block block, boolean bushes)
	{
		if (bushes && UtilMath.r(DOUBLE_PLANT_CHANCE) == 0)
		{
			Block above = block.getRelative(BlockFace.UP);
			byte plantData = (byte) UtilMath.r(DOUBLE_PLANT_DATA_RANGE);

			setBlock(block, Material.DOUBLE_PLANT, plantData);
			setBlock(above, Material.DOUBLE_PLANT, DOUBLE_PLANT_PART_DATA);

			addBlock(above);
		}
		else
		{
			block.setType(Material.LONG_GRASS);
			block.setData((byte) 1);
		}
	}

	public boolean isChallengeValid()
	{
		return Host.IsLive() && Host.getSettings().isChallengeStarted();
	}

	protected boolean isPlayerValid(Player player)
	{
		return getPlayersAlive().contains(player) && Host.IsAlive(player) && !Data.isDone(player);
	}

	public void setBorder(int minX, int maxX, int minY, int maxY, int minZ, int maxZ)
	{
		WorldData data = Host.WorldData;

		data.MinX = minX;
		data.MaxX = (int) (maxX + Settings.getMapCenter().getX());
		data.MinY = minY;
		data.MaxY = (int) (maxY + Settings.getMapCenter().getY());
		data.MinZ = minZ;
		data.MaxZ = (int) (maxZ + Settings.getMapCenter().getZ());
	}

	protected void displayCount(Player player, Location loc, String text)
	{
		Hologram hologram = createHologram(player, loc, text);
		long expiry = System.currentTimeMillis() + DISPLAY_COUNT_EXPIRE_AFTER;

		new BukkitRunnable()
		{
			public void run()
			{
				if (!Host.IsLive() || expiry < System.currentTimeMillis())
				{
					hologram.stop();
					cancel();
				}
				else
				{
					elevateHologram(hologram);
				}
			}
		}.runTaskTimer(Host.Manager.getPlugin(), 0L, 1L);
	}

	private Hologram createHologram(Player player, Location loc, String text)
	{
		Hologram hologram = new Hologram(Host.Manager.getHologramManager(), loc, text);
		hologram.setHologramTarget(HologramTarget.WHITELIST);
		hologram.addPlayer(player);
		hologram.start();

		return hologram;
	}

	private void elevateHologram(Hologram hologram)
	{
		hologram.setLocation(hologram.getLocation().add(0, DISPLAY_COUNT_ELEVATION_RATE, 0));
	}

	public boolean isInsideMap(Player player)
	{
		return Host.isInsideMap(player.getLocation());
	}

	private static final int TIME_LEFT_DIVIDER = 1000;

	public int getTimeLeft()
	{
		return (int) ((Settings.getDuration() - (System.currentTimeMillis() - Settings.getStartTime())) / TIME_LEFT_DIVIDER);
	}

	public ArrayList<Player> getPlayersAlive()
	{
		return Host.GetPlayers(true);
	}

	public ArrayList<Player> getPlayersIn(boolean ignoreCompleted)
	{
		ArrayList<Player> list = new ArrayList<Player>();

		for (Player player : getPlayersAlive())
		{
			if (Data.isLost(player) || (ignoreCompleted && Data.isCompleted(player)))
			{
				continue;
			}

			list.add(player);
		}

		return list;
	}

	public float getTimeLeftPercent()
	{
		float a = (float) (Settings.getDuration() - (System.currentTimeMillis() - Settings.getStartTime()));
		float b = (float) (Settings.getDuration());
		return a / b;
	}

	public final int getRemainingPlaces()
	{
		if (_type == ChallengeType.FirstComplete)
			return Settings.getMaxCompletedCount() - Data.getCompletedPlayers().size();
		else if (_type == ChallengeType.LastStanding)
			return getPlayersAlive().size() - Settings.getMaxCompletedCount();

		return 0;
	}

	public int getArenaSize()
	{
		return getArenaSize(DEFAULT_ARENA_SIZE);
	}

	public int getArenaSize(int minBlocks)
	{
		int size = (int) (minBlocks + Math.ceil(Host.getPlayersWithRemainingLives() / ARENA_SIZE_DIVIDER));
		return Math.min(size, ARENA_SIZE_LIMIT);
	}

	public BawkBawkBattles getHost()
	{
		return Host;
	}

	public ChallengeType getType()
	{
		return _type;
	}

	public String getName()
	{
		return _name;
	}

	public String[] getDescription()
	{
		return _description;
	}

	public Location getCenter()
	{
		return Settings.getMapCenter();
	}

	public ChallengeSettings getSettings()
	{
		return Settings;
	}

	public ChallengeData getData()
	{
		return Data;
	}

	/**
	 * tadahtech's circle method (temporarily used instead of UtilShapes.getCircle)
	 */
	protected List<Location> circle(Location loc, Integer r, Integer h, Boolean hollow, Boolean sphere, int plusY)
	{
		List<Location> circleblocks = new ArrayList<>();

		int cx = loc.getBlockX();
		int cy = loc.getBlockY();
		int cz = loc.getBlockZ();

		for (int x = cx - r; x <= cx + r; x++)
		{
			for (int z = cz - r; z <= cz + r; z++)
			{
				for (int y = (sphere ? cy - r : cy); y < (sphere ? cy + r : cy + h); y++)
				{
					double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
					if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1)))
					{
						Location l = new Location(loc.getWorld(), x, y + plusY, z);
						circleblocks.add(l);
					}
				}
			}
		}

		return circleblocks;
	}

	/**
	 * This method is called when the challenge timer runs out. <br>
	 * Used for overriding purposes only.
	 */
	public void onTimerFinish()
	{

	}

	public void onCollide(LivingEntity target, Block block, ProjectileUser data)
	{

	}
}
