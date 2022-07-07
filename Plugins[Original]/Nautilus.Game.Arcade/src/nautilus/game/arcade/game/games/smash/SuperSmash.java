package nautilus.game.arcade.game.games.smash;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Sets;
import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.Speed;

import mineplex.core.Managers;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.antihack.AntiHack;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.hologram.Hologram;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeFormat;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.games.smash.events.SmashActivateEvent;
import nautilus.game.arcade.game.games.smash.kits.KitBlaze;
import nautilus.game.arcade.game.games.smash.kits.KitChicken;
import nautilus.game.arcade.game.games.smash.kits.KitCow;
import nautilus.game.arcade.game.games.smash.kits.KitCreeper;
import nautilus.game.arcade.game.games.smash.kits.KitEnderman;
import nautilus.game.arcade.game.games.smash.kits.KitGolem;
import nautilus.game.arcade.game.games.smash.kits.KitGuardian;
import nautilus.game.arcade.game.games.smash.kits.KitMagmaCube;
import nautilus.game.arcade.game.games.smash.kits.KitPig;
import nautilus.game.arcade.game.games.smash.kits.KitSheep;
import nautilus.game.arcade.game.games.smash.kits.KitSkeletalHorse;
import nautilus.game.arcade.game.games.smash.kits.KitSkeleton;
import nautilus.game.arcade.game.games.smash.kits.KitSkySquid;
import nautilus.game.arcade.game.games.smash.kits.KitSlime;
import nautilus.game.arcade.game.games.smash.kits.KitSnowman;
import nautilus.game.arcade.game.games.smash.kits.KitSpider;
import nautilus.game.arcade.game.games.smash.kits.KitVillager;
import nautilus.game.arcade.game.games.smash.kits.KitWitch;
import nautilus.game.arcade.game.games.smash.kits.KitWitherSkeleton;
import nautilus.game.arcade.game.games.smash.kits.KitWolf;
import nautilus.game.arcade.game.games.smash.kits.KitZombie;
import nautilus.game.arcade.game.games.smash.mission.AirborneTracker;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.perks.PerkSpreadsheetModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public abstract class SuperSmash extends Game
{
	public enum Perm implements Permission
	{
		DEBUG_LIVES_COMMAND,
		DEBUG_SMASH_COMMAND,
		DEBUG_NEXTSMASH_COMMAND,
		DEBUG_PERK_COMMANDS
	}

	private static final int MAX_LIVES = 4;
	private static final int POWERUP_SPAWN_Y_INCREASE = 120;
	private static final int HUNGER_DELAY = 250;
	private static final int RESPAWN_INVUL = 1500;

	private static final String DATA_POINT_POWERUP = "RED";

	private final Map<Player, Integer> _lives = new HashMap<>();
	private final Map<Player, Long> _respawnTime = new HashMap<>();

	private Location _powerupCurrent = null;
	private Location _powerupTarget = null;
	private EnderCrystal _powerup = null;
	private Hologram _powerupHologram = null;
	private boolean _powerupHologramColour;
	private long _nextPowerup = 0;

	private static final Set<Material> REMOVE_ON_ITEM_SPAWN = Sets.newHashSet(Material.CACTUS, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.YELLOW_FLOWER, Material.RED_ROSE);

	public SuperSmash(ArcadeManager manager, GameType type, String[] description)
	{
		this(manager,

				new Kit[] {

						new KitSkeleton(manager), new KitGolem(manager), new KitSpider(manager), new KitSlime(manager), new KitCreeper(manager), new KitEnderman(manager), new KitSnowman(manager),
						new KitWolf(manager), new KitBlaze(manager), new KitWitch(manager), new KitChicken(manager), new KitSkeletalHorse(manager), new KitPig(manager), new KitSkySquid(manager),
						new KitWitherSkeleton(manager), new KitMagmaCube(manager), new KitZombie(manager), new KitCow(manager), new KitSheep(manager), new KitGuardian(manager), new KitVillager(manager)

				}, type, description);
	}

	public SuperSmash(ArcadeManager manager, Kit[] kits, GameType type, String[] description)
	{
		super(manager, type, kits, description);

		DeathOut = false;
		DeathSpectateSecs = 4;
		WorldWaterDamage = 1000;
		HideTeamSheep = true;
		ReplaceTeamsWithKits = true;
		AllowParticles = false;
		PlayerGameMode = GameMode.ADVENTURE;

		registerMissions(new AirborneTracker(this));

		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(Glide.class);
		antiHack.addIgnoredCheck(Speed.class);

		manager.GetExplosion().SetRegenerate(true);
		manager.GetExplosion().setRegenerateTime(TimeUnit.SECONDS.toMillis(30));

		new CompassModule()
				.setGiveCompassToAlive(true)
				.register(this);

		new PerkSpreadsheetModule("SMASH_KITS")
				.register(this);

		registerDebugCommand("nextsmash", Perm.DEBUG_NEXTSMASH_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			_nextPowerup = System.currentTimeMillis() + 1000;
			Announce(C.cWhiteB + caller.getName() + C.cAquaB + " spawned a smash crystal!");
		});
		registerDebugCommand("smash", Perm.DEBUG_SMASH_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			giveSmashItem(caller);
		});
		registerDebugCommand("lives", Perm.DEBUG_LIVES_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			Announce(C.cWhiteB + caller.getName() + C.cAquaB + " reset their lives!");

			if (!IsAlive(caller))
			{
				GetTeamList().get(0).AddPlayer(caller, true);
				RespawnPlayer(caller);
				caller.sendMessage(F.main("Revive", "You are back in the game!"));
			}

			_lives.put(caller, MAX_LIVES);
		});

		PermissionGroup.ADMIN.setPermission(Perm.DEBUG_PERK_COMMANDS, true, true);

		if (UtilServer.isTestServer())
		{
			PermissionGroup.QA.setPermission(Perm.DEBUG_PERK_COMMANDS, true, true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void gameStart(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			_lives.put(player, MAX_LIVES);
		}

		_nextPowerup = getNewSmashTime();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerDeath(PlayerDeathEvent event)
	{
		if (!loseLife(event.getEntity()))
		{
			SetPlayerState(event.getEntity(), PlayerState.OUT);
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_lives.remove(event.getPlayer());
		_respawnTime.remove(event.getPlayer());
	}

	public int getLives(Player player)
	{
		if (!_lives.containsKey(player))
		{
			return 0;
		}

		if (!IsAlive(player))
		{
			return 0;
		}

		return _lives.get(player);
	}

	public boolean loseLife(Player player)
	{
		int lives = getLives(player) - 1;

		if (lives > 0)
		{
			String livesString = (lives == 1 ? "life" : "lives");

			UtilPlayer.message(player, C.cRedB + "You have died!");
			UtilPlayer.message(player, C.cRedB + "You have " + lives + " " + livesString + " left!");
			player.playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 2f, 0.5f);

			_lives.put(player, lives);

			new BukkitRunnable()
			{

				@Override
				public void run()
				{
					UtilTextMiddle.display(null, getLiveColour(lives) + lives + " " + livesString + " left!", 10, 30, 10, player);
				}
			}.runTaskLater(Manager.getPlugin(), (long) DeathSpectateSecs * 20 + 2);

			return true;
		}

		String gameOver = "You ran out of lives!";

		UtilTextMiddle.display(C.cRedB + "GAME OVER", gameOver, 10, 50, 10, player);
		UtilPlayer.message(player, C.cRedB + gameOver);
		player.playSound(player.getLocation(), Sound.EXPLODE, 2f, 1f);

		return false;
	}

	@EventHandler
	public void triggerSuper(PlayerInteractEvent event)
	{
		if (!IsLive() || !UtilEvent.isAction(event, UtilEvent.ActionType.R))
		{
			return;
		}

		if (event.getMaterial() != null && event.getMaterial() != Material.NETHER_STAR)
		{
			return;
		}

		Player player = event.getPlayer();

		for (Perk perk : GetKit(player).GetPerks())
		{
			if (perk instanceof SmashUltimate)
			{
				SmashUltimate ultimate = (SmashUltimate) perk;
				String name = ultimate.GetName();

				if (ultimate.isUsingUltimate(player) || !ultimate.isUsable(player))
				{
					continue;
				}

				SmashActivateEvent smashActivateEvent = new SmashActivateEvent(player);

				UtilServer.CallEvent(smashActivateEvent);

				if (smashActivateEvent.isCancelled())
				{
					return;
				}

				UtilInv.remove(player, Material.NETHER_STAR, (byte) 0, 1);

				player.playSound(player.getLocation(), ultimate.getSound(), 10, 1);

				Announce(C.Bold + event.getPlayer().getName() + " activated " + C.cGreenB + name + ChatColor.RESET + C.Bold + "!");
				UtilTextMiddle.display("Smash Crystal", event.getPlayer().getName() + " used " + C.cGreen + name, 5, 50, 5, UtilServer.getPlayers());
				getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.SSM_SMASH, GetType().getDisplay(), null);

				ultimate.activate(player);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void powerupSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		if (!IsLive())
		{
			return;
		}

		if (_powerupHologram != null)
		{
			_powerupHologram.setText((_powerupHologramColour ? C.cGreenB : C.cWhiteB) + "SMASH CRYSTAL");
			_powerupHologramColour = !_powerupHologramColour;
		}

		if (_powerup == null)
		{
			if (System.currentTimeMillis() < _nextPowerup)
			{
				return;
			}

			if (WorldData.GetDataLocs(DATA_POINT_POWERUP).isEmpty())
			{
				return;
			}

			if (_powerupTarget == null)
			{
				BlockRestore blockRestore = Manager.GetBlockRestore();
				Location newTarget = UtilAlg.Random(WorldData.GetDataLocs(DATA_POINT_POWERUP));
				Block targetBlock = newTarget.getBlock();

				// This relies on this method being called 4 times a second (5
				// ticks, UpdateType.FASTER).
				long restoreTime = (POWERUP_SPAWN_Y_INCREASE / 4 / 2) * 1000 + 500;

				_powerupTarget = newTarget.clone();
				_powerupCurrent = newTarget.clone().add(0, POWERUP_SPAWN_Y_INCREASE, 0);

				// Blocks
				for (int x = -1; x <= 1; x++)
				{
					for (int z = -1; z <= 1; z++)
					{
						blockRestore.add(targetBlock.getRelative(x, -3, z), Material.IRON_BLOCK.getId(), (byte) 0, restoreTime);

						if (x == 0 && z == 0)
						{
							continue;
						}

						blockRestore.add(targetBlock.getRelative(x, -1, z), Material.QUARTZ_BLOCK.getId(), (byte) 0, restoreTime);
					}
				}

				blockRestore.add(targetBlock.getRelative(0, -2, 0), Material.BEACON.getId(), (byte) 0, restoreTime);
				blockRestore.add(targetBlock.getRelative(0, -1, 0), Material.STAINED_GLASS.getId(), (byte) 5, restoreTime);
			}

			if (_powerupTarget.getY() < _powerupCurrent.getY())
			{
				_powerupCurrent.subtract(0, 2, 0);
				UtilFirework.playFirework(_powerupCurrent, Type.BURST, Color.LIME, false, true);
			}
			else
			{
				CreatureAllowOverride = true;
				_powerup = _powerupTarget.getWorld().spawn(_powerupTarget, EnderCrystal.class);
				_powerupHologram = new Hologram(Manager.getHologramManager(), _powerupTarget.add(0, 2, 0), true, "SMASH CRYSTAL").start();
				CreatureAllowOverride = false;

				UtilFirework.playFirework(_powerupTarget, Type.BURST, Color.YELLOW, false, true);

				_powerupTarget = null;
				_powerupCurrent = null;
			}
		}
	}

	@EventHandler
	public void powerupPickup(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		if (!IsLive())
		{
			return;
		}

		if (_powerup == null)
		{
			return;
		}

		Player best = null;
		double bestDist = 0;

		for (Player player : GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}

			double dist = UtilMath.offset(player, _powerup);

			if (dist > 2)
			{
				continue;
			}

			if (best == null || dist < bestDist)
			{
				best = player;
				bestDist = dist;
			}
		}

		if (best != null)
		{
			_powerupHologram.stop();
			_powerupHologram = null;

			_powerup.remove();
			_powerup = null;

			giveSmashItem(best);
			_nextPowerup = getNewSmashTime();
		}
	}

	private void giveSmashItem(Player player)
	{
		int amount = 1;
		if (GetKit(player) instanceof KitSnowman)
		{
			amount = 3;
		}
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, amount, C.cYellowB + "Click" + C.cWhiteB + " - " + C.cGreenB + "Smash"));
		Manager.GetGame().Announce(C.Bold + player.getName() + " collected " + C.cGreen + C.Bold + "Smash Crystal" + ChatColor.RESET + C.Bold + "!");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void powerupDamage(EntityDamageEvent event)
	{
		if (_powerup != null && _powerup.equals(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void fallDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() == DamageCause.FALL)
		{
			event.SetCancelled("No Fall Damage");
		}
	}

	@EventHandler
	public void expireRespawnTime(PlayerInteractEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Player player = event.getPlayer();

		if (player.getItemInHand() != null)
		{
			_respawnTime.remove(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void knockback(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetDamageePlayer() != null)
		{
			Player player = event.GetDamageePlayer();
			Long respawnTime = _respawnTime.get(player);

			if (respawnTime != null && !UtilTime.elapsed(respawnTime, RESPAWN_INVUL))
			{
				event.SetCancelled("Just Respawned");
			}

			event.AddKnockback("Smash Knockback", 1 + 0.1 * (player.getMaxHealth() - player.getHealth()));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void arenaBoundry(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() == DamageCause.VOID || event.GetCause() == DamageCause.LAVA)
		{
			LivingEntity entity = event.GetDamageeEntity();

			entity.eject();
			entity.leaveVehicle();

			if (event.GetDamageePlayer() != null)
			{
				entity.getWorld().strikeLightningEffect(entity.getLocation());
			}

			event.AddMod(GetName(), GetName(), 5000, false);
		}
	}

	@EventHandler
	public void healthRegen(EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() == RegainReason.SATIATED)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void EntityDeath(EntityDeathEvent event)
	{
		event.getDrops().clear();
	}

	@Override
	public void SetKit(Player player, Kit kit, boolean announce)
	{
		GameTeam team = GetTeam(player);
		if (team != null)
		{
			if (!team.KitAllowed(kit))
			{
				player.playSound(player.getLocation(), Sound.NOTE_BASS, 2f, 0.5f);
				UtilPlayer.message(player, F.main("Kit", F.elem(team.GetFormattedName()) + " cannot use " + F.elem(kit.GetFormattedName() + " Kit") + "."));
				return;
			}
		}

		// Deactivate morph if active
		Gadget gadget = Manager.getCosmeticManager().getGadgetManager().getActive(player, GadgetType.MORPH);
		if (gadget != null)
		{
			gadget.disable(player);
		}

		_playerKit.put(player, kit);

		if (announce)
		{
			UtilPlayer.closeInventoryIfOpen(player);
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2f, 1f);
			UtilPlayer.message(player, F.main("Kit", "You equipped " + F.elem(kit.GetFormattedName() + " Kit") + "."));
			kit.ApplyKit(player);
			UtilInv.Update(player);
		}
	}

	@Override
	public void RespawnPlayer(Player player)
	{
		super.RespawnPlayer(player);

		_respawnTime.put(player, System.currentTimeMillis());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void abilityDescription(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || itemStack.getItemMeta() == null || itemStack.getItemMeta().getDisplayName() == null || itemStack.getItemMeta().getLore() == null || !displayKitInfo(player))
		{
			return;
		}

		if (itemStack.getType() == Material.WATCH || itemStack.getType() == Material.BED)
		{
			return;
		}

		for (int i = player.getItemInHand().getItemMeta().getLore().size(); i <= 7; i++)
		{
			UtilPlayer.message(player, "");
		}

		UtilPlayer.message(player, ArcadeFormat.Line);

		UtilPlayer.message(player, "§aAbility - §f§l" + player.getItemInHand().getItemMeta().getDisplayName());

		// Perk Descs
		for (String line : player.getItemInHand().getItemMeta().getLore())
		{
			UtilPlayer.message(player, line);
		}

		UtilPlayer.message(player, ArcadeFormat.Line);

		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 2f);

		event.setCancelled(true);
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		return 4;
	}

	@EventHandler
	public void blockFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	private int hungerTick = 0;

	@EventHandler
	public void hunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (!IsLive())
		{
			return;
		}

		hungerTick = (hungerTick + 1) % 10;

		for (Player player : GetPlayers(true))
		{
			player.setSaturation(3f);
			player.setExhaustion(0f);

			if (player.getFoodLevel() <= 0)
			{
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.STARVATION, 1, false, true, false, "Starvation", GetName());

				UtilPlayer.message(player, F.main("Game", "Attack other players to restore hunger!"));
			}

			if (hungerTick == 0)
			{
				UtilPlayer.hunger(player, -1);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void hungerRestore(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);

		if (damager == null)
		{
			return;
		}

		if (damager.equals(event.GetDamageeEntity()))
		{
			return;
		}

		if (!(event.GetDamageeEntity() instanceof Player))
		{
			return;
		}

		if (!Recharge.Instance.use(damager, "Hunger Restore", HUNGER_DELAY, false, false))
		{
			return;
		}

		int amount = Math.max(1, (int) (event.GetDamage() / 2));
		UtilPlayer.hunger(damager, amount);
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (REMOVE_ON_ITEM_SPAWN.contains(event.getEntity().getItemStack().getType()))
		{
			event.setCancelled(true);
		}
	}

	public long getNewSmashTime()
	{
		return (long) (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3) + TimeUnit.MINUTES.toMillis(5) * Math.random());
	}

	public String getLiveColour(int lives)
	{
		switch (lives)
		{
			case 3:
				return C.cYellow;
			case 2:
				return C.cGold;
			case 1:
				return C.cRed;
			case 0:
				return C.cGray + C.Strike;
			default:
				return C.cGreen;
		}
	}

	public Map<Player, Integer> getLiveMap()
	{
		return _lives;
	}

	protected boolean displayKitInfo(Player player)
	{
		return GetState() == GameState.Recruit;
	}

	public void setNextPowerupTime(long time)
	{
		_nextPowerup = time;
	}
}
