package nautilus.game.arcade.game.games.mineware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;
import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.HeadRoll;
import com.mineplex.anticheat.checks.move.Speed;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GamePrepareCountdownCommence;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeData;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeList;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeSettings;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeAnvilDance;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeArrowRampage;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeBlockLobbers;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeBouncingBlock;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeBuildRace;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeChickenShooting;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeColorChange;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeDeadlyTnt;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeDiamondHunt;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeEggSmash;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeFallingBlocks;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeFastFood;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeKangarooJump;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeKingOfTheLadder;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeLavaRun;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeMilkACow;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeMinecartDance;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeMiniOneInTheQuiver;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeOreRun;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengePickASide;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengePunchThePig;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeRedLightGreenLight;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeReverseTag;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeRushPush;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeSmashOff;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeTreasureDigger;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeWaterHorror;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeWaveCrush;
import nautilus.game.arcade.game.games.mineware.effect.ChickenAttack;
import nautilus.game.arcade.game.games.mineware.effect.DeathEffect;
import nautilus.game.arcade.game.games.mineware.effect.DeathEffectData;
import nautilus.game.arcade.game.games.mineware.events.ChallengeEndEvent;
import nautilus.game.arcade.game.games.mineware.kit.KitBawksFood;
import nautilus.game.arcade.game.games.mineware.tracker.BouncingShadowTracker;
import nautilus.game.arcade.game.games.mineware.tracker.DragonKingTracker;
import nautilus.game.arcade.game.games.mineware.tracker.EliteArcherTracker;
import nautilus.game.arcade.game.games.mineware.tracker.MilkManTracker;
import nautilus.game.arcade.game.games.mineware.tracker.PinataMasterTracker;
import nautilus.game.arcade.game.games.mineware.tracker.PixelNinjaTracker;
import nautilus.game.arcade.game.games.mineware.tracker.SpeedyBuildersTracker;
import nautilus.game.arcade.game.games.mineware.tracker.SurfUpTracker;
import nautilus.game.arcade.game.games.mineware.tracker.TagMasterTracker;
import nautilus.game.arcade.game.games.mineware.tracker.VeteranTracker;
import nautilus.game.arcade.kit.Kit;

/**
 * <p>
 * Bawk Bawk Battles is a minigame based on MineWare that contains a set of
 * challenges where players have to complete a list of tasks to maintain their lives.
 * </p>
 * If a player fails to complete the task, he loses one life.
 * <br>
 * If one runs out of lives, chickens will attack him.
 */
public class BawkBawkBattles extends TeamGame implements IThrown
{
	public static final int MAX_LIVES = 5;
	private static final double SPAWN_CENTER_ADD = 0.5;
	private static final double SPECTATOR_SPAWN_HEIGHT = 7;

	private static final int ROTATION_DELAY = 1000; // milliseconds
	private static final int DESCRIPTION_DELAY = 1000; // milliseconds

	private static final int SILENCE_DURATION = 5000;
	private static final int DESCRIPTION_LINE_DELAY = 1000;
	private static final int DESCRIPTION_LINE_DELAY_MULTIPLIER = 55;

	private static final int COUNTDOWN_EFFECT_AMPLIFIER = 2;
	private static final float DESCRIPTION_SOUND_VOLUME = 1.5F;
	private static final float DESCRIPTION_SOUND_PITCH = 1.0F;
	private static final int DESCRIPTION_TITLE_FADE_IN_TICKS = 0;
	private static final int DESCRIPTION_TITLE_STAY_TICKS = 60;
	private static final int DESCRIPTION_TITLE_FADE_OUT_TICKS = 20;
	private static final float COUNTDOWN_FINISH_SOUND_VOLUME = 1.0F;
	private static final float COUNTDOWN_FINISH_SOUND_PITCH = 1.0F;
	private static final float COUNTDOWN_STEP_SOUND_VOLUME = 1.0F;
	private static final float COUNTDOWN_STEP_SOUND_PITCH = 1.5F;
	private static final int COUNTDOWN_SUBTRACT_TICKS = 15;

	private static final int SEMIFINAL_INDEX = 2;
	private static final int FINAL_INDEX = 1;
	private static final int FIRST_PLACE_GEM_REWARD = 40;
	private static final int SECOND_PLACE_GEM_REWARD = 30;
	private static final int THIRD_PLACE_GEM_REWARD = 20;
	private static final int PARTICIPATION_GEMS = 10;
	private static final int FIRST_WINNER_INDEX = 0;
	private static final int SECOND_WINNER_INDEX = 1;
	private static final int THIRD_WINNER_INDEX = 2;

	private static final int CRUMBLE_DIVIDER = 2;

	private static final float CHICKEN_ATTACK_SOUND_VOLUME = 1.0F;
	private static final float CHICKEN_ATTACK_SOUND_PITCH = 1.3F;
	private static final double CHICKEN_VELOCITY_HEIGHT = 0.4;
	private static final double CHICKEN_VELOCITY_ADD = 0.6;
	private static final float CHICKEN_ATTACK_HITBOX_GROW = 0.8F;

	private static final float CHICKEN_ATTACK_PARTICLE_OFFSET = 0.1F;
	private static final float CHICKEN_ATTACK_PARTICLE_SPEED = 0.02F;

	private static final int SPECTATOR_KNOCKBACK_RADIUS = 6;
	private static final int SPECTATOR_KNOCKBACK_NEXT_DELAY = 500;
	private static final double SPECTATOR_KNOCKBACK_POWER = 1.6;
	private static final double SPECTATOR_KNOCKBACK_HEIGHT = 0.9;
	private static final double SPECTATOR_KNOCKBACK_HEIGHT_MAX = 10;
	private static final float SPECTATOR_KNOCKBACK_SOUND_VOLUME = 2.0F;
	private static final float SPECTATOR_KNOCKBACK_SOUND_PITCH = 0.5F;

	private static final int GENERIC_SCOREBOARD_PLAYER_COUNT = 15;

	private static final int LOST_ONE_LIFE = MAX_LIVES - 1;
	private static final int LOST_TWO_LIVES = MAX_LIVES - 2;
	private static final int LOST_THREE_LIVES = MAX_LIVES - 3;
	private static final int LOST_FOUR_LIVES = MAX_LIVES - 4;

	private static final float CHICKEN_HIT_PLAYER_SOUND_VOLUME = 2.0F;
	private static final float CHICKEN_HIT_PLAYER_SOUND_PITCH = 1.0F;
	private static final double CHICKEN_HIT_PLAYER_DAMAGE = 5.0;

	private BawkBawkBattlesSettings _settings = new BawkBawkBattlesSettings();
	private ChallengeList _list = new ChallengeList();
	private Map<UUID, Integer> _lives = new HashMap<>();
	private LinkedList<Player> _winners = new LinkedList<>();
	private GameTeam _playerTeam;
	private DeathEffect _deathEffect = new DeathEffect(this);
	private ChickenAttack _chickenAttack;
	private Location _chickenAttackCenter;
	private Challenge _challenge;
	private List<Block> _lastChallengeBlocks;
	private long _delay;

	public final Set<UUID> _beingAttacked = new HashSet<>();

	private List<String> _countdown = Arrays.asList(
			C.cRed + C.Bold + "3",
			C.cYellow + C.Bold + "2",
			C.cGreen + C.Bold + "1",
			C.cWhite + C.Bold + "GO!");

	@SuppressWarnings("unchecked")
	public BawkBawkBattles(ArcadeManager manager)
	{
		super(manager,
				GameType.BawkBawkBattles,
				new Kit[]{new KitBawksFood(manager)},
				new String[]{
						"Follow Bawk Bawk's instructions in chat.",
						"Complete a task first or be the last one to stay alive.",
						"If you fail a challenge, you lose one life.",
						"If you run out of lives, chickens will attack you.",
						"Last player with lives wins.",
				});

		DamagePvP = false;
		DamagePvE = false;
		DamageEvP = false;
		DamageSelf = false;
		DamageFall = false;
		DamageTeamSelf = true;
		DamageTeamOther = false;

		SpawnTeleport = false; // Disabled for custom spawn teleportation.

		DeathOut = false;
		DeathTeleport = false;
		DeathMessages = false;
		FixSpawnFacing = false;

		Manager.GetCreature().SetDisableCustomDrops(true);
		
		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(Speed.class);
		antiHack.addIgnoredCheck(Glide.class);
		antiHack.addIgnoredCheck(HeadRoll.class);

		populateChallenges();

		registerStatTrackers(
				new BouncingShadowTracker(this),
				new DragonKingTracker(this),
				new EliteArcherTracker(this),
				new MilkManTracker(this),
				new PinataMasterTracker(this),
				new PixelNinjaTracker(this),
				new SpeedyBuildersTracker(this),
				new SurfUpTracker(this),
				new TagMasterTracker(this),
				new VeteranTracker(this));
	}

	public void populateChallenges()
	{
		_list.add(
				new ChallengeAnvilDance(this),
				new ChallengeArrowRampage(this),
				new ChallengeBlockLobbers(this),
				new ChallengeBouncingBlock(this),
				new ChallengeBuildRace(this),
				new ChallengeColorChange(this),
				new ChallengeChickenShooting(this),
				new ChallengeDeadlyTnt(this),
				new ChallengeDiamondHunt(this),
				new ChallengeEggSmash(this),
				new ChallengeFallingBlocks(this),
				new ChallengeFastFood(this),
				new ChallengeWaterHorror(this),
				new ChallengeKangarooJump(this),
				new ChallengeKingOfTheLadder(this),
				new ChallengeLavaRun(this),
				new ChallengeMilkACow(this),
				new ChallengeOreRun(this),
				new ChallengeMinecartDance(this),
				new ChallengeMiniOneInTheQuiver(this),
				new ChallengePickASide(this),
				new ChallengePunchThePig(this),
				new ChallengeRedLightGreenLight(this),
				new ChallengeReverseTag(this),
				new ChallengeRushPush(this),
				new ChallengeSmashOff(this),
				new ChallengeTreasureDigger(this),
				new ChallengeWaveCrush(this));

		/*
		 * Removed:
		 * 
		 * Cloud Fall
		 * Dogs Vs Cats
		 * Fishing Day
		 * Navigation Maze
		 * Volley Pig
		 * Zombie Infection
		 */
	}

	@Override
	public void ParseData() // Load the location where all chickens on chicken attack will spawn.
	{
		_chickenAttackCenter = WorldData.GetDataLocs("WHITE").get(0);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void createTeams(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Recruit)
		{
			GetTeamList().clear();
			_playerTeam = new GameTeam(this, "Players", ChatColor.YELLOW, new ArrayList<>());
			AddTeam(_playerTeam);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;

		addPlayerLives();

		_challenge = selectChallenge();

		if (isChallengeNull())
			return;

		setupChallengeSpawnLocations();
		_challenge.createMap();

		spawnAndResetPlayers(true);
	}

	private void addPlayerLives()
	{
		for (Player player : GetPlayers(true))
		{
			_lives.put(player.getUniqueId(), MAX_LIVES);
		}
	}

	private Challenge selectChallenge()
	{
		int limit = _list.size();
		int attemps = 0;

		Challenge instance = _list.random();

		while (!isSuitable(instance))
		{
			if (attemps < limit)
			{
				instance = _list.random();
				attemps++;
			}
			else
			{
				_list.resetPlayed();
				attemps = 0;
			}
		}

		return instance;
	}

	private boolean isSuitable(Challenge instance)
	{
		ChallengeSettings settings = instance.getSettings();

		int participants = getPlayersWithRemainingLives();
		int minCount = settings.getMinPlayers();
		int maxCount = settings.getMaxPlayers();

		return participants >= minCount && participants <= maxCount;
	}

	private boolean isChallengeNull()
	{
		if (_challenge == null)
		{
			SetState(GameState.Dead);
			UtilServer.broadcast(F.main("Game", "No suitable challenge was found."));
			return true;
		}
		else
		{
			return false;
		}
	}

	/*
	 * Spawns
	 */

	private ArrayList<Location> setupChallengeSpawnLocations()
	{
		ArrayList<Location> selected = _challenge.createSpawns();

		for (Location spawn : selected)
		{
			spawn = spawn.add(SPAWN_CENTER_ADD, SPAWN_CENTER_ADD, SPAWN_CENTER_ADD);
			spawn.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory2d(spawn, _challenge.getCenter())));
		}

		_challenge.getData().setSpawns(selected);
		_playerTeam.SetSpawns(selected);
		SpectatorSpawn = _challenge.getCenter().add(0.5, SPECTATOR_SPAWN_HEIGHT, 0.5);

		return selected;
	}

	private void spawnAndResetPlayers(boolean firstRun)
	{
		teleportPlayersToSpawns(firstRun);
		resetPlayers();
		addEffectsToPlayers();
		teleportSpectatorsToSpawn();
		clearInventories();
	}

	private void resetPlayers()
	{
		for (Player player : GetPlayers(true))
		{
			if (_lives.containsKey(player.getUniqueId()) && _lives.get(player.getUniqueId()) > 0)
			{
				Manager.Clear(player);
				Scoreboard.setPlayerTeam(player, _playerTeam);
			}
		}
	}

	private void teleportSpectatorsToSpawn()
	{
		for (Player player : GetPlayers(false))
		{
			if (!IsAlive(player) && !_beingAttacked.contains(player.getUniqueId()))
			{
				player.teleport(GetSpectatorLocation());
			}
		}
	}

	private void teleportPlayersToSpawns(boolean firstRun)
	{
		_challenge.spawn(firstRun);
		removeSolidBlockForPlayers();
	}

	private void removeSolidBlockForPlayers()
	{
		for (Player player : GetPlayers(true))
		{
			removeSolidBlock(player.getLocation());
		}
	}

	private void removeSolidBlock(Location location)
	{
		Block block = location.getBlock();
		Block upper = block.getRelative(BlockFace.UP);

		if (!block.isEmpty() && _challenge.getData().isModifiedBlock(block))
		{
			_challenge.resetBlock(block);
			_challenge.getData().removeModifiedBlock(block);
		}

		if (!upper.isEmpty() && _challenge.getData().isModifiedBlock(upper))
		{
			_challenge.resetBlock(upper);
			_challenge.getData().removeModifiedBlock(upper);
		}
	}

	/*
	 * Start
	 */

	@EventHandler
	public void start(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;

		_delay = System.currentTimeMillis();

		_chickenAttack = new ChickenAttack(this, _chickenAttackCenter);
	}

	@EventHandler
	public void addEffectsDuringCountdown(GamePrepareCountdownCommence event)
	{
		for (Player player : GetPlayers(true))
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, COUNTDOWN_EFFECT_AMPLIFIER));
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, COUNTDOWN_EFFECT_AMPLIFIER));
		}
	}

	@EventHandler
	public void freeze(PlayerMoveEvent event)
	{
		if (!IsLive())
			return;

		Player player = event.getPlayer();

		if (!IsAlive(player))
			return;

		if (!IsAlive(player))
			return;

		if (!PrepareFreeze)
			return;

		if (!_settings.isWaiting())
			return;

		Location from = event.getFrom();
		Location to = event.getTo();

		if (UtilMath.offset2d(from, to) <= 0)
			return;

		from.setPitch(to.getPitch());
		from.setYaw(to.getYaw());
		event.setTo(from);
	}

	/*
	 * Challenge rotation
	 */

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		if (!IsLive())
			return;

		if (_challenge == null) // When challenge is null, a new one should start.
		{
			if (!UtilTime.elapsed(_delay, ROTATION_DELAY))
			{
				return;
			}

			_challenge = selectChallenge();

			if (isChallengeNull())
				return;

			Damage = true;
			resetLastChallengeMap();

			_deathEffect.removeSpawnedEntities();

			setupChallengeSpawnLocations();
			_challenge.createMap();

			spawnAndResetPlayers(false);

			_delay = System.currentTimeMillis();
			_settings.setWaiting(true);
		}
		else if (_settings.isWaiting()) // If challenge is waiting, show description.
		{
			if (!UtilTime.elapsed(_delay, DESCRIPTION_DELAY))
			{
				return;
			}

			displayDescriptionAndStartChallenge();
		}
		else // Otherwise, check if the challenge is ended.
		{
			checkChallengeEnd();
		}
	}

	private void resetLastChallengeMap()
	{
		_settings.setCrumbling(false);

		for (Block block : _lastChallengeBlocks)
		{
			if (block.getState() instanceof InventoryHolder)
			{
				InventoryHolder holder = (InventoryHolder) block.getState();
				holder.getInventory().clear();
			}

			_challenge.resetBlock(block);
		}
	}

	private void displayDescriptionAndStartChallenge()
	{
		if (_settings.areMessagesSent())
		{
			removeEffectsFromPlayers();

			_settings.setWaiting(false);
			_settings.setChallengeStarted(true);

			_challenge.start();

			_settings.markMessagesAsSent(false);
		}
		else if (!_settings.areMessagesBeingSent())
		{
			showChallengeDescription();
		}
	}

	private void removeEffectsFromPlayers()
	{
		for (Player player : GetPlayers(true))
		{
			if (player.hasPotionEffect(PotionEffectType.BLINDNESS) && player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
			{
				player.removePotionEffect(PotionEffectType.BLINDNESS);
				player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			}
		}
	}

	private void clearInventories()
	{
		for (Player player : GetPlayers(true))
		{
			UtilInv.Clear(player);
		}
	}

	private void addEffectsToPlayers()
	{
		for (Player player : GetPlayers(true))
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 2));
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 2));
		}
	}

	private void showChallengeDescription()
	{
		silenceChat();
		setMessageState();
		showDescriptionText();
	}

	private void silenceChat()
	{
		if (Manager.GetChat().getChatSilence() < 0)
		{
			Manager.GetChat().setChatSilence(SILENCE_DURATION, false);
		}
	}

	private void setMessageState()
	{
		_settings.markMessagesAsSending(true);
		_settings.markMessagesAsSent(false);
	}

	private void showDescriptionText()
	{
		List<String> messages = Lists.newArrayList(_challenge.getDescription());
		messages.add(0, C.cGray + "Bawk Bawk commands...");

		new BukkitRunnable()
		{
			private int index = 0;
			private long delay = 0;

			@Override
			public void run()
			{
				if (!IsLive() || _challenge == null)
				{
					cancel();
					return;
				}

				if (delay > System.currentTimeMillis())
				{
					return;
				}

				if (index >= messages.size())
				{
					prepareCountdown();
					cancel();
					return;
				}

				delay = System.currentTimeMillis() + (DESCRIPTION_LINE_DELAY + (DESCRIPTION_LINE_DELAY_MULTIPLIER * messages.get(index).length()));

				String title = null;
				String subtitle = C.cYellow + messages.get(index);

				if (index > 0)
				{
					if (index == 1)
					{
						showDescriptionInChat();
					}

					title = _challenge.getName();

					for (Player player : GetPlayers(true))
					{
						player.playSound(player.getLocation(), Sound.CHICKEN_IDLE, DESCRIPTION_SOUND_VOLUME, DESCRIPTION_SOUND_PITCH);
					}
				}

				for (Player player : GetPlayers(true))
				{
					UtilTextMiddle.display(title, subtitle, DESCRIPTION_TITLE_FADE_IN_TICKS, DESCRIPTION_TITLE_STAY_TICKS, DESCRIPTION_TITLE_FADE_OUT_TICKS, player);
				}

				index++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 1L);
	}

	private void prepareCountdown()
	{
		new BukkitRunnable()
		{
			private int index = 0;

			@Override
			public void run()
			{
				if (!IsLive() || _challenge == null)
				{
					cancel();
					return;
				}

				for (Player player : GetPlayers(true))
				{
					String message = _countdown.get(index);
					UtilTextMiddle.display(message, null, player);

					if (index == _countdown.size() - 1)
					{
						player.playSound(player.getLocation(), Sound.CHICKEN_HURT, COUNTDOWN_FINISH_SOUND_VOLUME, COUNTDOWN_FINISH_SOUND_PITCH);
					}
					else
					{
						player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, COUNTDOWN_STEP_SOUND_VOLUME, COUNTDOWN_STEP_SOUND_PITCH);
					}
				}

				index++;

				if (index >= _countdown.size())
				{
					_settings.markMessagesAsSent(true);
					_settings.markMessagesAsSending(false);
					cancel();
				}
			}
		}.runTaskTimer(Manager.getPlugin(), 0, COUNTDOWN_SUBTRACT_TICKS);
	}

	private void showDescriptionInChat()
	{
		List<String> messages = Lists.newArrayList(_challenge.getDescription());
		String type = _challenge.getType().toString();

		if (_challenge.getSettings().isTeamBased())
		{
			type = "Team Based";
		}

		for (Player player : UtilServer.getPlayers())
		{
			UtilPlayer.message(player, "");

			UtilPlayer.message(player, " " + C.cYellow + _challenge.getName() + C.Reset + " " + C.cGray + type);
			UtilPlayer.message(player, "");

			for (String currentMessage : messages)
			{
				UtilPlayer.message(player, C.cGray + " - " + C.cWhite + currentMessage);
			}

			UtilPlayer.message(player, "");
		}
	}

	private void checkChallengeEnd()
	{
		if (_challenge.canFinish())
		{
			endCurrentChallenge();
			return;
		}
		else
		{
			updateChallengeTimer();
		}

		if (hasCrumbleSetting() && canStartCrumbling())
		{
			_settings.setCrumbling(true);
			announceCrumbling();
		}
	}

	public void endCurrentChallenge()
	{
		if (_challenge == null)
			return;

		_delay = System.currentTimeMillis();

		sortLastChallengeBlocks();
		_challenge.end();

		_settings.setChallengeStarted(false);
		_settings.setCrumbling(false);
		_settings.markMessagesAsSending(false);
		_settings.markMessagesAsSent(false);

		_list.addPlayed(_challenge);

		Damage = false;
		_challenge = null;
		EndCheck();
	}

	private void sortLastChallengeBlocks()
	{
		_lastChallengeBlocks = new ArrayList<>(_challenge.getData().getModifiedBlocks());

		Collections.sort(_lastChallengeBlocks, new Comparator<Block>()
		{
			@Override
			public int compare(Block o1, Block o2)
			{
				return new Integer(o2.getY()).compareTo(o1.getY());
			}
		});
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if (getPlayersWithRemainingLives() <= 1)
		{
			if (GetPlayers(true).size() > 0)
			{
				Player additional = GetPlayers(true).get(0);
				_winners.addFirst(additional);
			}

			List<Player> actualWinners = _winners.subList(0, Math.min(_winners.size(), 3));

			if (actualWinners.size() >= 1)
			{
				AddGems(actualWinners.get(FIRST_WINNER_INDEX), FIRST_PLACE_GEM_REWARD, "First Place", false, false);

				if (actualWinners.size() >= 2)
				{
					AddGems(actualWinners.get(SECOND_WINNER_INDEX), SECOND_PLACE_GEM_REWARD, "Second Place", false, false);

					if (actualWinners.size() >= 3)
					{
						AddGems(actualWinners.get(THIRD_WINNER_INDEX), THIRD_PLACE_GEM_REWARD, "Third Place", false, false);
					}
				}
			}

			for (Player player : GetPlayers(false))
				AddGems(player, PARTICIPATION_GEMS, "Participation", false, false);

			AnnounceEnd(actualWinners);
			SetState(GameState.End);
		}
	}

	private void updateChallengeTimer()
	{
		for (Player player : UtilServer.getPlayers())
		{
			UtilTextTop.displayTextBar(player, _challenge.getTimeLeftPercent(), C.cYellowB + _challenge.getName());
			player.setLevel(_challenge.getRemainingPlaces());
			player.setExp(_challenge.getTimeLeftPercent());
		}
	}

	private boolean hasCrumbleSetting()
	{
		return _challenge.getSettings().canCrumble() && !_challenge.getData().getModifiedBlocks().isEmpty();
	}

	private boolean canStartCrumbling()
	{
		int lost = _challenge.getData().getLostPlayers().size();
		int current = GetPlayers(true).size();

		return !_settings.isCrumbling() && lost > current / CRUMBLE_DIVIDER;
	}

	private void announceCrumbling()
	{
		for (Player player : GetPlayers(true))
		{
			UtilPlayer.message(player, F.main("Game", "The map has started to crumble."));
		}
	}

	/*
	 * Messages
	 */

	public void showLivesLeft(Player player)
	{
		int lives = lives(player);
		String suffix = "lives";

		if (lives == 1)
			suffix = "life";

		String msg = "You have " + F.elem(lives) + " " + suffix + " left.";

		if (lives <= 0)
			msg = "You don't have any lives left.";

		UtilPlayer.message(player, F.main("Game", msg));
	}

	/*
	 * End reset
	 */

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;

		if (_settings.isChallengeStarted())
			return;

		for (Player player : GetPlayers(true))
		{
			UtilPlayer.clearPotionEffects(player);
			UtilTextMiddle.display(null, null, player);
		}
	}

	/*
	 * Chicken attack
	 */

	@EventHandler
	public void chickenAttack(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		_beingAttacked.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player ->
		{
			Chicken chicken = UtilMath.randomElement(_chickenAttack.getChickens());
			Material feetType = chicken.getLocation().getBlock().getType();

			if (chicken.isOnGround() && feetType != Material.STATIONARY_WATER && feetType != Material.WATER)
			{
				UtilEnt.CreatureLook(chicken, player);
				player.playSound(chicken.getLocation(), Sound.BAT_TAKEOFF, CHICKEN_ATTACK_SOUND_VOLUME, CHICKEN_ATTACK_SOUND_PITCH);
				UtilAction.velocity(
						chicken,
						UtilAlg.getTrajectory2d(chicken, player),
						UtilAlg.calculateVelocity(chicken.getLocation().toVector(), player.getLocation().toVector(), CHICKEN_VELOCITY_HEIGHT).length() + CHICKEN_VELOCITY_ADD,
						false,
						0,
						CHICKEN_VELOCITY_HEIGHT,
						CHICKEN_VELOCITY_HEIGHT + 1,
						false);

				Manager.GetProjectile().AddThrow(chicken, null, this, -1, true, false, false, true, CHICKEN_ATTACK_HITBOX_GROW);
			}
		});
	}

	@EventHandler
	public void chickenAttackParticle(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Chicken chicken : _chickenAttack.getChickens())
		{
			Material feetType = chicken.getLocation().getBlock().getType();
			Block below = chicken.getLocation().getBlock().getRelative(BlockFace.DOWN);

			if (!chicken.isOnGround() && feetType != Material.STATIONARY_WATER && feetType != Material.WATER && !below.isEmpty())
			{
				UtilParticle.PlayParticle(ParticleType.FLAME, chicken.getLocation(), CHICKEN_ATTACK_PARTICLE_OFFSET, CHICKEN_ATTACK_PARTICLE_OFFSET, CHICKEN_ATTACK_PARTICLE_OFFSET, CHICKEN_ATTACK_PARTICLE_SPEED, 1, ViewDist.NORMAL);
			}
		}
	}

	@EventHandler
	public void chickenAttackPlayerDeath(PlayerDeathEvent event)
	{
		if (!IsLive())
			return;

		if (_challenge == null)
			return;

		Player player = event.getEntity();

		if (IsAlive(player))
			return;

		_chickenAttack.kill(player, true);
	}

	@Override
	public void disqualify(Player player)
	{
		getTeamModule().getPreferences().remove(player);
		GetPlayerKits().remove(player);
		GetPlayerGems().remove(player);

		//Remove Team
		GameTeam team = GetTeam(player);
		if (team != null)
		{
			if (InProgress())
			{
				getPlayerTeam().SetPlayerState(player, GameTeam.PlayerState.OUT);

				int alive = getPlayersWithRemainingLives();

				if (alive > Challenge.CHICKEN_ATTACK_CRITERIA)
					getChickenAttack().start(player);
				else
					getChickenAttack().kill(player, true);
			}
			else
				team.RemovePlayer(player);
		}
	}

	@EventHandler
	public void blockChickenAttackMemberDamage(EntityDamageEvent event)
	{
		if (!IsLive())
			return;

		if (event.getEntity() instanceof Chicken)
		{
			Chicken chicken = (Chicken) event.getEntity();

			if (_chickenAttack.isGroupMember(chicken))
			{
				event.setCancelled(true);
			}

			if (event.getCause() == DamageCause.VOID)
			{
				chicken.teleport(_chickenAttack.getPlatformCenter());
			}
		}
	}

	@EventHandler
	public void spectatorApproachPlayer(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : GetPlayers(true))
		{
			for (Player other : GetPlayers(false))
			{
				if (IsAlive(other))
					continue;

				if (_beingAttacked.contains(other.getUniqueId()))
					continue;

				if (player.equals(other))
					continue;

				// Allow staff members to go near players while vanished
				if (getArcadeManager().isVanished(player) && getArcadeManager().GetClients().Get(player).hasPermission(IncognitoManager.Perm.USE_INCOGNITO))
					continue;

				if (UtilMath.offset(other, player) >= SPECTATOR_KNOCKBACK_RADIUS)
					continue;

				if (Recharge.Instance.use(player, "Approach Alive Player", SPECTATOR_KNOCKBACK_NEXT_DELAY, false, false))
				{
					UtilAction.velocity(other, UtilAlg.getTrajectory2d(player, other), SPECTATOR_KNOCKBACK_POWER, true, SPECTATOR_KNOCKBACK_HEIGHT, 0, SPECTATOR_KNOCKBACK_HEIGHT_MAX, true);
					other.playSound(other.getLocation(), Sound.CHICKEN_EGG_POP, SPECTATOR_KNOCKBACK_SOUND_VOLUME, SPECTATOR_KNOCKBACK_SOUND_PITCH);
				}
			}
		}
	}

	/*
	 * Completed players
	 */

	@EventHandler
	public void preventVoidDeath(PlayerMoveEvent event)
	{
		if (!IsLive())
			return;

		if (_challenge == null)
			return;

		Player player = event.getPlayer();

		if (_challenge.getData().isDone(player) && IsAlive(player))
		{
			if (event.getTo().getY() <= 0)
			{
				player.teleport(_challenge.getCenter().add(0, 1, 0));
			}
		}
	}

	@EventHandler
	public void cancelSpectatorCloak(ConditionApplyEvent event)
	{
		if (!IsLive())
			return;

		if (event.GetCondition().GetType() != ConditionType.CLOAK)
			return;

		if (!event.GetCondition().GetReason().equals("Spectator"))
			return;

		if (!(event.GetCondition().GetEnt() instanceof Player))
			return;

		Player player = (Player) event.GetCondition().GetEnt();

		if (GetTeam(player) == null)
			return;

		event.setCancelled(true);
	}

	/*
	 * Cancel/Quit
	 */

	@EventHandler
	public void cancel(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
			return;

		if (_challenge == null)
			return;

		if (_settings.isWaiting())
			return;

		Manager.getPluginManager().callEvent(new ChallengeEndEvent(_challenge));
		HandlerList.unregisterAll(_challenge);

		_challenge.end();
		_settings.setChallengeStarted(false);
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_lives.remove(player.getUniqueId());
		_winners.remove(player);
	}

	//	@EventHandler
	//	public void blockStartAttempt(UpdateEvent event)
	//	{
	//		if (event.getType() != UpdateType.TICK)
	//			return;
	//
	//		// If there is only 1 player and the game starts, do not teleport him to the map.
	//
	//		if (GetState() == GameState.Recruit && GetCountdown() >= 0 && GetPlayers(false).size() <= MIN_PLAYERS_BLOCK_ATTEMPT)
	//		{
	//			UtilServer.broadcast(F.main("Game", C.cRed + "This game requires at least 2 players to start."));
	//			SetCountdown(-1);
	//			Manager.GetLobby().DisplayWaiting();
	//		}
	//	}

	@Override
	public boolean shouldHeal(Player player)
	{
		return !_beingAttacked.contains(player.getUniqueId());
	}

	/*
	 * Miscellaneous
	 */

	@EventHandler
	public void blockDeathEffectHeadModification(PlayerArmorStandManipulateEvent event)
	{
		ArmorStand armorStand = event.getRightClicked();

		for (DeathEffectData data : _deathEffect.getData())
		{
			if (data.isChickenHead(armorStand))
			{
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler
	public void blockDeathEffectHeadDamage(EntityDamageByEntityEvent event)
	{
		if (event.getEntity() instanceof ArmorStand)
		{
			ArmorStand armorStand = (ArmorStand) event.getEntity();

			for (DeathEffectData data : _deathEffect.getData())
			{
				if (data.getChickenHead().equals(armorStand))
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void blockBlood(ItemSpawnEvent event)
	{
		if (!IsLive())
			return;

		Item blood = event.getEntity();
		ItemStack bloodItem = blood.getItemStack();

		if (bloodItem.getType() == Material.INK_SACK && bloodItem.getData().getData() == 1)
		{
			if (bloodItem.hasItemMeta())
			{
				if (bloodItem.getItemMeta().hasDisplayName())
				{
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void blockInteract(PlayerInteractEvent event)
	{
		if (!_settings.isWaiting())
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void blockDrops(PlayerDropItemEvent event)
	{
		event.getItemDrop().remove();
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		event.SetDamageToLevel(false);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void blockDragonEgg(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;

		// Added to control dragon egg locations.

		Action action = event.getAction();

		if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();

			if (block.getType() == Material.DRAGON_EGG)
			{
				if (event.isCancelled()) // If controlled by another challenge, do not continue.
					return;

				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void removeFeetBlocks(GamePrepareCountdownCommence event)
	{
		removeSolidBlockForPlayers();
	}

	/*
	 * Scoreboard
	 */

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!IsLive())
			return;

		Scoreboard.reset();

		if (getPlayersWithRemainingLives() >= GENERIC_SCOREBOARD_PLAYER_COUNT)
		{
			displayGenericScoreboard();
		}
		else
		{
			displayDetailedScoreboard();
		}

		Scoreboard.draw();
	}

	private void displayGenericScoreboard()
	{
		displayPlayerTotal();
		displayAlivePlayers();
		displayCompletedPlayers();
		displayDeadPlayers();
	}

	private void displayPlayerTotal()
	{
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Players");
		Scoreboard.write(getPlayersWithRemainingLives() + " ");
	}

	private void displayAlivePlayers()
	{
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreen + C.Bold + "Alive");

		if (_settings.isChallengeStarted())
		{
			int data = _challenge.getPlayersIn(false).size();
			Scoreboard.write("" + data);
		}
		else
		{
			Scoreboard.write("-");
		}
	}

	private void displayCompletedPlayers()
	{
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGold + C.Bold + "Completed");

		if (_settings.isChallengeStarted())
		{
			int data = _challenge.getData().getCompletedPlayers().size();
			Scoreboard.write("" + data);
		}
		else
		{
			Scoreboard.write("-");
		}
	}

	private void displayDeadPlayers()
	{
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGray + C.Bold + "Dead");

		if (_settings.isChallengeStarted())
		{
			int data = _challenge.getData().getLostPlayers().size();
			Scoreboard.write("" + data);
		}
		else
		{
			Scoreboard.write("-");
		}
	}

	private void displayDetailedScoreboard()
	{
		Scoreboard.writeNewLine();

		Scoreboard.writeGroup(GetPlayers(true), player ->
		{
			int lives = lives(player);
			String state = definePlayerState(player);
			String display = definePlayerDisplay(player.getName(), lives, state);
			return Pair.create(display, lives);
		}, true);
	}

	private String definePlayerDisplay(String name, int lives, String state)
	{
		switch (lives)
		{
			case MAX_LIVES:
				return state + C.cGreen + name;
			case LOST_ONE_LIFE:
				return state + C.cYellow + name;
			case LOST_TWO_LIVES:
				return state + C.cGold + name;
			case LOST_THREE_LIVES:
				return state + C.cRed + name;
			case LOST_FOUR_LIVES:
				return state + C.cRed + name;
			default:
				return C.cGray + C.Strike + name;
		}
	}

	private String definePlayerState(Player player)
	{
		if (_settings.isChallengeStarted())
		{
			ChallengeData data = _challenge.getData();

			if (data.isLost(player))
			{
				return C.cDRed + "X ";
			}
			else if (data.isCompleted(player))
			{
				return C.cGreen + "✔ ";
			}
		}

		return "";
	}

	/*
	 * Helper methods
	 */

	@Override
	public boolean isInsideMap(Player player)
	{
		if (_challenge != null && !_settings.isWaiting())
		{
			return _challenge.isInsideMap(player);
		}

		return true;
	}

	public int lives(Player player)
	{
		if (!_lives.containsKey(player.getUniqueId()))
		{
			return 0;
		}

		return _lives.get(player.getUniqueId());
	}

	/*
	 * Setter methods
	 */

	public void setLives(Player player, int amount)
	{
		_lives.put(player.getUniqueId(), amount);
	}

	/*
	 * Getter methods
	 */

	public GameTeam getPlayerTeam()
	{
		return _playerTeam;
	}

	public Challenge getCurrentChallenge()
	{
		return _challenge;
	}

	public long getCurrentDelay()
	{
		return _delay;
	}

	@Override
	public LinkedList<Player> getWinners()
	{
		return _winners;
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> players = new ArrayList<>(UtilServer.getPlayersCollection());
		players.removeAll(getWinners());
		return players;
	}

	public BawkBawkBattlesSettings getSettings()
	{
		return _settings;
	}

	public ChallengeList getChallengeList()
	{
		return _list;
	}

	public DeathEffect getDeathEffect()
	{
		return _deathEffect;
	}

	public ChickenAttack getChickenAttack()
	{
		return _chickenAttack;
	}

	/*
	 * Player related getter methods
	 */

	/**
	 * Returns the amount of players with more than one life.
	 */
	public int getPlayersWithRemainingLives()
	{
		int amount = 0;

		for (Player player : GetPlayers(true))
		{
			if (lives(player) > 0)
			{
				amount++;
			}
		}

		return amount;
	}

	/*
	 * Inherited methods
	 */

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (_challenge != null)
		{
			if (_challenge.isChallengeValid())
			{
				_challenge.onCollide(target, block, data);
			}
		}

		if (data.getThrown() instanceof Chicken)
		{
			if (target instanceof Player)
			{
				Player player = (Player) target;

				if (_beingAttacked.contains(player.getUniqueId()))
				{
					player.playSound(player.getLocation(), Sound.CHICKEN_HURT, CHICKEN_HIT_PLAYER_SOUND_VOLUME, CHICKEN_HIT_PLAYER_SOUND_PITCH);
					player.damage(CHICKEN_HIT_PLAYER_DAMAGE);

					if (UtilMath.random.nextBoolean())
					{
						UtilTextBottom.display(C.cRed + C.Bold + "bawk bawk!", player);
					}
				}
			}
		}
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Idle(ProjectileUser data)
	{

	}

	@Override
	public void Expire(ProjectileUser data)
	{

	}
}