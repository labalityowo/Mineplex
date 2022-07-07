package nautilus.game.arcade.game.games.castlesiegenew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mineplex.anticheat.checks.combat.KillauraTypeD;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.ConditionFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.FirstBloodEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.castlesiegenew.kits.KitHumanKnight;
import nautilus.game.arcade.game.games.castlesiegenew.kits.KitHumanMarksman;
import nautilus.game.arcade.game.games.castlesiegenew.kits.KitHumanPaladin;
import nautilus.game.arcade.game.games.castlesiegenew.kits.KitHumanWolf;
import nautilus.game.arcade.game.games.castlesiegenew.kits.KitUndeadArcher;
import nautilus.game.arcade.game.games.castlesiegenew.kits.KitUndeadGhoul;
import nautilus.game.arcade.game.games.castlesiegenew.kits.KitUndeadSummoner;
import nautilus.game.arcade.game.games.castlesiegenew.kits.KitUndeadZombie;
import nautilus.game.arcade.game.modules.SpawnShieldModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.NullKit;
import nautilus.game.arcade.stats.BloodThirstyStatTracker;
import nautilus.game.arcade.stats.TeamDeathsStatTracker;
import nautilus.game.arcade.stats.TeamKillsStatTracker;
import nautilus.game.arcade.stats.WinAsTeamStatTracker;

public class CastleSiegeNew extends TeamGame
{

	private static final String[] DESCRIPTION = {
			C.cAqua + "Defenders" + C.cWhite + " must defend the King.",
			C.cAqua + "Defenders" + C.cWhite + " win when the sun rises.",
			C.cAqua + "Defenders" + C.cWhite + " respawn as wolves.",
			"",
			C.cRed + "Undead" + C.cWhite + " must kill the King.",
			C.cRed + "Undead" + C.cWhite + " lose when the sun rises."
	};
	private static final String[] TIPS = {
			"TNT randomly spawns at 3 different locations outside the undead forest.",
			"Right-click TNT to pick it up.",
			"TNT will automatically explode 30 seconds after being picked up.",
			"Undead respawn instantly.",
			"Defenders can right-click a fence to pass through it.",
			"Avoid retreating as Defenders.",
			"Castle Marksmen are important to defense because of their arrows.",
			"Defenders respawn as wolves with no armor or weapons.",
			"Wolves must wait 6 seconds in between respawns.",
			"Coordination and teamwork are important to winning as Defenders."
	};
	private static final int START_TIME = 14000;
	private static final int UNDEAD_BURN_TIME = 24000;
	private static final int DEFENDER_WIN_TIME = UNDEAD_BURN_TIME + 200;
	private static final int WOLF_RESPAWN_TIME = 6;
	private static final long FENCE_NO_CLIP_TIME = TimeUnit.SECONDS.toMillis(2);
	private static final int MAX_ARROW_TICKS = 30 * 20;

	private final Set<Listener> _listeners = new HashSet<>();

	private GameTeam _defenders;
	private GameTeam _undead;
	private final Set<Player> _wolves = new HashSet<>();

	private CastleSiegeKing _king;

	private List<Location> _kitNPCSpawns;
	private ArrayList<Location> _wolfSpawns;

	private Kit _wolfKit;

	public CastleSiegeNew(ArcadeManager manager)
	{
		super(manager, GameType.CastleSiege, new Kit[]
				{
						new KitHumanWolf(manager),
						new KitHumanMarksman(manager),
						new KitHumanKnight(manager),
						new KitHumanPaladin(manager),
						new NullKit(manager),
						new KitUndeadGhoul(manager),
						new KitUndeadArcher(manager),
						new KitUndeadZombie(manager),
						new KitUndeadSummoner(manager)

				}, DESCRIPTION);

		_help = TIPS;
		
		AnticheatDisabled = true;
		StrictAntiHack = true;
		HungerSet = 20;
		DeathOut = false;
		WorldTimeSet = START_TIME;
		WorldSoilTrample = true;
		BlockBreakAllow.add(Material.FENCE.getId());
		BlockPlaceAllow.add(Material.FENCE.getId());
		InventoryClick = true;
		SplitKitXP = true;

		manager.GetCreature().SetDisableCustomDrops(true);

		registerStatTrackers(
				new BloodThirstyStatTracker(this, "KingGuard", 5, player -> getDefenders().HasPlayer(player), player -> UtilMath.offsetSquared(player, getKing().getEntity()) < 8 * 8 && WorldTimeSet > UNDEAD_BURN_TIME - 60 * 20),
				new BloodThirstyStatTracker(this, "WolfKill", 12, this::isWolf, player -> true),
				new BloodThirstyStatTracker(this, "BloodThirsty", 50, player -> true, player -> GetTeam(player).equals(getUndead())),
				new TeamKillsStatTracker(this),
				new TeamDeathsStatTracker(this)
		);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageDealt,
				DamageTaken
		);

		_king = new CastleSiegeKing(this);
		_listeners.add(_king);

		_listeners.add(new CastleSiegeTNTManager(this));
		_listeners.add(new CastleSiegeHorseManager(this));

		new CompassModule()
				.register(this);

		// Disable specific GWEN checks for this game
		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(KillauraTypeD.class);
	}

	@Override
	public void ParseData()
	{
		_defenders = GetTeam(ChatColor.AQUA);
		_defenders.SetName("Defenders");
		_defenders.SetRespawnTime(WOLF_RESPAWN_TIME);

		_undead = GetTeam(ChatColor.RED);
		_undead.SetName("Undead");

		boolean undead = false;

		for (Kit kit : GetKits())
		{
			if (kit instanceof NullKit)
			{
				undead = true;
			}
			else if (undead)
			{
				_defenders.GetRestrictedKits().add(kit);
			}
			else
			{
				_undead.GetRestrictedKits().add(kit);
			}
		}

		_kitNPCSpawns = WorldData.GetDataLocs("PINK");
		_wolfSpawns = WorldData.GetDataLocs("GREEN");

		for (Kit kit : GetKits())
		{
			if (kit.GetName().contains("Wolf"))
			{
				_wolfKit = kit;
				break;
			}
		}

		_listeners.forEach(UtilServer::RegisterEvents);

		new SpawnShieldModule()
				.registerShield(_wolves::contains, WorldData.GetCustomLocs("129"), UtilAlg.getAverageLocation(_wolfSpawns))
				.register(this);
	}

	@EventHandler
	@Override
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		Scoreboard.writeNewLine();

		Scoreboard.write(_defenders.GetFormattedName());
		Scoreboard.write((_defenders.GetPlayers(true).size() - _wolves.size()) + " Players");

		Scoreboard.writeNewLine();

		Scoreboard.write(C.cDAquaB + "Wolves");
		Scoreboard.write(_wolves.size() + " Players");

		Scoreboard.writeNewLine();

		Scoreboard.write(_undead.GetFormattedName());
		Scoreboard.write(_undead.GetPlayers(true).size() + " Players");

		Scoreboard.writeNewLine();

		Scoreboard.write(_king.getEntity().getCustomName());
		Scoreboard.write((int) _king.getEntity().getHealth() + " Health");

		Scoreboard.writeNewLine();

		// Convert ticks to milliseconds
		int timeLeft = (UNDEAD_BURN_TIME - WorldTimeSet) * 50;
		Scoreboard.write(C.cGoldB + "Sunrise");

		if (timeLeft > 0)
		{
			Scoreboard.write(UtilTime.MakeStr(timeLeft));
		}
		else
		{
			Scoreboard.write("Undead Burning!");
		}

		Scoreboard.draw();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		// Register Defender win tracker
		registerStatTrackers(new WinAsTeamStatTracker(this, _defenders, "ForTheKing"));

		CreatureAllowOverride = true;

		// Move Kit NPCS
		int i = 0;
		for (Kit kit : GetKits())
		{
			if (kit instanceof NullKit || _undead.GetRestrictedKits().contains(kit) || _kitNPCSpawns.size() <= i)
			{
				continue;
			}

			Location location = _kitNPCSpawns.get(i++);
			kit.getGameKit().createNPC(location);
		}

		CreatureAllowOverride = false;
	}

	@EventHandler
	public void advanceTime(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive())
		{
			return;
		}

		WorldTimeSet++;
	}

	@EventHandler
	public void burnUndead(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !IsLive())
		{
			return;
		}

		if (WorldTimeSet >= UNDEAD_BURN_TIME)
		{
			ConditionFactory factory = Manager.GetCondition().Factory();

			for (Player player : _undead.GetPlayers(true))
			{
				factory.Ignite("Sun Damage", player, null, 2, false, false);
			}
		}
	}

	@EventHandler
	public void setWolfSpawns(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_defenders.SetSpawns(_wolfSpawns);
	}

	@EventHandler
	public void setWolfKit(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			Kit kit = GetKit(player);

			if (kit instanceof KitHumanWolf)
			{
				SetKit(player, GetKits()[1], false);
			}
		}
	}

	@EventHandler
	public void setWolfKit(PlayerGameRespawnEvent event)
	{
		Player player = event.GetPlayer();

		if (_defenders.HasPlayer(player) && _wolves.add(player))
		{
			SetKit(player, _wolfKit, true);
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_wolves.remove(event.getPlayer());
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		LivingEntity king = _king.getEntity();

		if (WorldTimeSet > DEFENDER_WIN_TIME || _undead.GetPlayers(true).isEmpty())
		{
			// King at full health
			if (king.getHealth() == king.getMaxHealth())
			{
				_defenders.GetPlayers(true).forEach(player -> AddStat(player, "KingFull", 1, true, false));
			}

			SetCustomWinLine(king.getCustomName() + C.Reset + " has survived the siege!");
			AnnounceEnd(_defenders);
		}
		else if (king.isDead() || !king.isValid() || _defenders.GetPlayers(true).isEmpty())
		{
			String winLine = king.getCustomName() + " " + C.Reset;
			Map<Player, Integer> damagers = _king.getDamagers();

			if (damagers.isEmpty())
			{
				winLine += "has died!";
			}
			else
			{
				Player mostDamager = null;
				int mostDamage = 0;

				for (Entry<Player, Integer> entry : damagers.entrySet())
				{
					if (mostDamager == null || mostDamage < entry.getValue())
					{
						mostDamager = entry.getKey();
						mostDamage = entry.getValue();
					}
				}

				// Not possible but keeps the IDE happy
				if (mostDamager == null)
				{
					return;
				}

				if (mostDamager.equals(_king.getLastDamager()))
				{
					winLine += "was slaughtered by " + _undead.GetColor() + mostDamager.getName();
				}
				else
				{
					winLine += "was killed by " + _undead.GetColor() + _king.getLastDamager().getName() + C.Reset + " while " + _undead.GetColor() + mostDamager.getName() + C.Reset + " dealt most of the damage!";
				}

				// 50%+ damage to the king
				if (mostDamage >= (king.getMaxHealth() - king.getHealth()) * 0.5)
				{
					AddStat(mostDamager, "Assassin", 1, true, false);
				}

				// Player who killed the king
				AddStat(_king.getLastDamager(), "KingSlayer", 1, true, false);
			}

			if (WorldTimeSet > UNDEAD_BURN_TIME - 60 * 20)
			{
				_undead.GetPlayers(true).forEach(player -> AddStat(player, "CloseCall", 1, true, false));
			}

			SetCustomWinLine(winLine);
			AnnounceEnd(_undead);
		}
		else
		{
			return;
		}

		for (GameTeam team : GetTeamList())
		{
			if (WinnerTeam != null && team.equals(WinnerTeam))
			{
				for (Player player : team.GetPlayers(false))
				{
					AddGems(player, 10, "Winning", false, false);
				}
			}

			for (Player player : team.GetPlayers(false))
			{
				if (player.isOnline())
				{
					AddGems(player, 10, "Participation", false, false);
				}
			}
		}

		SetState(GameState.End);
	}

	@Override
	public void disable()
	{
		super.disable();
		_wolves.clear();
		_listeners.forEach(UtilServer::Unregister);
		_listeners.clear();
	}

	@EventHandler
	public void allowNoClipFences(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();

		if (event.isCancelled() || block == null || block.getType() != Material.FENCE)
		{
			return;
		}

		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player) || !_defenders.HasPlayer(player) || player.getItemInHand() != null && player.getItemInHand().getType() == Material.FENCE)
		{
			return;
		}

		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, block.getLocation().add(0.5, 0.5, 0.5), 0.2F, 0.2F, 0.2F, 0.01F, 3, ViewDist.SHORT);
		block.getWorld().playSound(block.getLocation(), Sound.NOTE_STICKS, 1, 1);
		Manager.GetBlockRestore().add(block, Material.AIR.getId(), (byte) 0, FENCE_NO_CLIP_TIME);
	}

	@EventHandler
	public void removeFences(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().getType() == Material.FENCE)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void removeOldArrows(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Entity entity : WorldData.World.getEntities())
		{
			if (entity instanceof Arrow && entity.getTicksLived() > MAX_ARROW_TICKS)
			{
				entity.remove();
			}
		}
	}

	@EventHandler
	public void pickupArrows(PlayerPickupItemEvent event)
	{
		if (event.getItem().getItemStack().getType() != Material.ARROW)
		{
			return;
		}

		Kit kit = GetKit(event.getPlayer());

		event.setCancelled(kit == null || !(kit instanceof KitUndeadArcher));
	}

	@EventHandler
	public void firstBlood(FirstBloodEvent event)
	{
		AddStat(event.getPlayer(), "FirstBlood", 1, true, false);
	}

	@EventHandler
	public void handleResistance(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (damagee == null)
		{
			return;
		}

		for (PotionEffect effect : damagee.getActivePotionEffects())
		{
			if (effect.getType().toString().equals(PotionEffectType.DAMAGE_RESISTANCE.toString()))
			{
				event.AddMod("Resistance", (effect.getAmplifier() + 1) * -0.2 * event.GetDamage());
			}
		}
	}

	public boolean isWolf(Player player)
	{
		return _wolves.contains(player);
	}

	public CastleSiegeKing getKing()
	{
		return _king;
	}

	public GameTeam getDefenders()
	{
		return _defenders;
	}

	public GameTeam getUndead()
	{
		return _undead;
	}
}
