package nautilus.game.arcade.game.games.snowfight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.snowfight.kits.KitMedic;
import nautilus.game.arcade.game.games.snowfight.kits.KitSportsman;
import nautilus.game.arcade.game.games.snowfight.kits.KitTactician;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class SnowFight extends TeamGame
{

	public enum Perm implements Permission
	{
		DEBUG_COMMANDS
	}

	private static final String[] DESCRIPTION =
			{
					"Collect " + C.cAqua + "Snow Balls" + C.cWhite + " by punching the ground.",
					"Use them to defeat the enemy teams.",
					"You regenerate " + C.cRed + "❤" + C.cWhite + " when out of combat.",
					"Last team standing wins!"
			};
	private static final int MAX_SNOW_BALLS = 16;
	private static final int MAX_SNOW_HEIGHT = 8;
	private static final int SNOW_BALL_DAMAGE = 3;
	private static final ItemStack SNOW_BALL = new ItemStack(Material.SNOW_BALL);
	private static final long GUN_MODE_TIME = TimeUnit.MINUTES.toMillis(3);
	private static final long BLIZZARD_TIME = TimeUnit.MINUTES.toMillis(5);
	private static final ItemStack GUN = new ItemBuilder(Material.DIAMOND_BARDING)
			.setTitle(C.cAquaB + "Snowball Launcher 8000")
			.build();
	private static final int GUN_BULLETS = 5;
	private static final long COMBAT_TIME = TimeUnit.SECONDS.toMillis(8);
	private static final String CONDITION_REASON = "Out of Combat";

	private boolean _gunMode;
	private long _gunModeTime = GUN_MODE_TIME;

	private boolean _blizzard;
	private long _blizzardTime = BLIZZARD_TIME;

	private final Map<Player, Long> _lastDamage;

	public SnowFight(ArcadeManager manager)
	{
		super(manager, GameType.SnowFight, new Kit[]
				{
						new KitSportsman(manager),
						new KitTactician(manager),
						new KitMedic(manager)
				}, DESCRIPTION);


		_lastDamage = new HashMap<>();

		PrepareFreeze = false;
		HungerSet = 20;

		registerChatStats(
				Kills,
				Assists,
				BlankLine,
				DamageTaken,
				DamageDealt
		);

		new TeamArmorModule()
				.giveTeamArmor()
				.giveHotbarItem()
				.register(this);

		new CompassModule()
				.setGiveCompassToAlive(true)
				.setGiveCompass(false)
				.register(this);

		registerDebugCommand("launchers", Perm.DEBUG_COMMANDS, PermissionGroup.ADMIN, (player, args) -> _gunModeTime = 0);
		registerDebugCommand("blizzard", Perm.DEBUG_COMMANDS, PermissionGroup.ADMIN, (player, args) -> _blizzardTime = 0);
	}

	@EventHandler
	public void weatherUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		World world = WorldData.World;

		if (_blizzard && IsLive())
		{
			world.setStorm(true);
			world.setThundering(false);
		}
		else if (WorldWeatherEnabled)
		{
			world.setStorm(false);
			WorldWeatherEnabled = false;
		}
	}

	@EventHandler
	public void battleAnnounce(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare)
		{
			UtilTextMiddle.display(C.cGreenB + "Get Ready", "Collect Snowballs!", 20, 60, 10, UtilServer.getPlayers());
		}
		else if (event.GetState() == GameState.Live)
		{
			for (Player player : UtilServer.getPlayersCollection())
			{
				player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
			}

			UtilTextMiddle.display(C.cRedB + "FIGHT", "Good Luck!", 0, 40, 10, UtilServer.getPlayers());
		}
	}

	@EventHandler
	public void blockDamage(PlayerInteractEvent event)
	{
		if (!InProgress() || !UtilEvent.isAction(event, ActionType.L_BLOCK) || _gunMode)
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (!IsAlive(player) || !isSnow(block) || player.getInventory().contains(Material.SNOW_BALL, MAX_SNOW_BALLS))
		{
			return;
		}

		player.getInventory().addItem(SNOW_BALL);
		snowDecrease(block, 1);
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.SNOW_BLOCK);
	}

	private void snowDecrease(Block block, int height)
	{
		if (height <= 0 || !isSnow(block))
		{
			return;
		}

		// Shuffle Up
		while (isSnow(block.getRelative(BlockFace.UP)))
		{
			block = block.getRelative(BlockFace.UP);
		}

		// Snow Block
		int snowLevel = MAX_SNOW_HEIGHT;

		if (block.getType() == Material.SNOW)
		{
			snowLevel = block.getData() + 1;
		}

		// Lower
		if (height >= snowLevel)
		{
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
			snowDecrease(block.getRelative(BlockFace.DOWN), height - snowLevel);
		}
		else
		{
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.SNOW, (byte) (snowLevel - height - 1));
		}
	}

	@EventHandler
	public void healthRegen(EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() == RegainReason.SATIATED)
		{
			event.setAmount(1);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void snowballDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			event.SetCancelled("No Melee");
			return;
		}

		Projectile projectile = event.GetProjectile();

		if (projectile == null || !(projectile instanceof Snowball))
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();

		if (damagee != null)
		{
			_lastDamage.put(damagee, System.currentTimeMillis());
			Manager.GetCondition().EndCondition(damagee, ConditionType.REGENERATION, CONDITION_REASON);
		}

		Player shooter = (Player) projectile.getShooter();

		event.AddMod(GetName(), "Snowball", SNOW_BALL_DAMAGE, true);
		event.SetIgnoreArmor(true);

		projectile.getWorld().playEffect(projectile.getLocation(), Effect.STEP_SOUND, Material.SNOW_BLOCK);
		shooter.playSound(shooter.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

		if (_blizzard)
		{
			UtilPlayer.hunger(shooter, 4);
		}
	}

	private boolean isSnow(Block block)
	{
		return block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK;
	}

	@EventHandler
	public void updateEvents(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		if (!_gunMode && UtilTime.elapsed(GetStateTime(), _gunModeTime))
		{
			List<Player> alive = GetPlayers(true);

			Announce(F.main("Game", F.name("Snowball Launchers") + " given to everyone!"));
			UtilTextMiddle.display(C.cAquaB + "Snowball Launchers", "Hold down to fire snowballs!", 0, 40, 10, alive.toArray(new Player[0]));

			alive.forEach(player -> player.getInventory().setItem(0, GUN));
			_gunMode = true;
		}
		else if (UtilTime.elapsed(GetStateTime(), _blizzardTime))
		{
			List<Player> alive = GetPlayers(true);

			if (!_blizzard)
			{
				Announce(F.main("Game", F.color("Blizzard", C.cRedB) + " incoming! Attack players to restore hunger."));
				UtilTextMiddle.display(C.cRedB + "Blizzard", "Attack players to restore hunger", 0, 40, 10, alive.toArray(new Player[0]));
				HungerSet = -1;
				WorldWeatherEnabled = true;
				_blizzard = true;
			}

			alive.forEach(player ->
			{
				if (player.getFoodLevel() == 0)
				{
					Manager.GetDamage().NewDamageEvent(player, null, null,
							DamageCause.STARVATION, 1, false, true, true,
							GetName(), "Blizzard"
					);
				}

				UtilPlayer.hunger(player, -1);
			});
		}
	}

	@EventHandler
	public void gunInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R) || !_gunMode)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || !itemStack.equals(GUN))
		{
			return;
		}

		event.setCancelled(true);

		Location location = player.getEyeLocation();
		player.playSound(location, Sound.CHICKEN_EGG_POP, 1, 1);

		for (int i = 0; i < GUN_BULLETS; i++)
		{
			Snowball snowball = player.launchProjectile(Snowball.class);
			Vector vector = location.getDirection().add(new Vector(
					(Math.random() - 0.5) / 3,
					(Math.random() - 0.2) / 3,
					(Math.random() - 0.5) / 3
			)).multiply(1.3);

			UtilAction.velocity(snowball, vector);
		}
	}

	@EventHandler
	public void updateRegeneration(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		_lastDamage.entrySet().removeIf(entry ->
		{
			Player player = entry.getKey();
			long lastDamage = entry.getValue();

			if (_blizzard)
			{
				Manager.GetCondition().EndCondition(player, ConditionType.REGENERATION, CONDITION_REASON);
				return true;
			}

			if (UtilTime.elapsed(lastDamage, COMBAT_TIME))
			{
				Manager.GetCondition().Factory().Regen(CONDITION_REASON, player, player, 10, 1, false, false, true);
			}

			return !IsAlive(player);
		});
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Scoreboard.writeNewLine();

		if (GetPlayers(true).size() <= 8)
		{
			GetTeamList().forEach(team ->
			{
				if (!team.IsTeamAlive())
				{
					return;
				}

				team.GetPlayers(true).forEach(player -> Scoreboard.write(team.GetColor() + player.getName()));

				Scoreboard.writeNewLine();
			});
		}
		else
		{
			GetTeamList().forEach(team ->
			{
				Scoreboard.write(team.GetFormattedName());
				Scoreboard.write(team.GetColor() + String.valueOf(team.GetPlayers(true).size()) + " Alive");
				Scoreboard.writeNewLine();
			});
		}

		if (IsLive())
		{
			long delta = System.currentTimeMillis() - GetStateTime();
			long gunTime = _gunModeTime - delta;
			long blizzardTime = _blizzardTime - delta;

			if (gunTime > 0)
			{
				Scoreboard.write(C.cAquaB + "Launchers");
				Scoreboard.write(UtilTime.MakeStr(gunTime));
			}
			else if (blizzardTime > 0)
			{
				Scoreboard.write(C.cRedB + "Blizzard");
				Scoreboard.write(UtilTime.MakeStr(blizzardTime));
			}
			else
			{
				Scoreboard.write(C.cRedB + "Blizzard!");
			}
		}

		Scoreboard.draw();
	}
}
