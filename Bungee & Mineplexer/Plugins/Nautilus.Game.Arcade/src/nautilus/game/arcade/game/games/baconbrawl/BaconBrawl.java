package nautilus.game.arcade.game.games.baconbrawl;

import java.util.concurrent.TimeUnit;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.baconbrawl.kits.KitChrisPBacon;
import nautilus.game.arcade.game.games.baconbrawl.kits.KitMamaPig;
import nautilus.game.arcade.game.games.baconbrawl.kits.KitPig;
import nautilus.game.arcade.game.games.baconbrawl.kits.KitSheepPig;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.stats.KillsWithinGameStatTracker;

public class BaconBrawl extends SoloGame
{

	private static final String[] DESCRIPTION =
			{
					"Knock other pigs out of the arena!",
					"Last pig in the arena wins!",
					"Knockback increases over time!"
			};

	private double _knockbackMagnitude = 1.5;
	private long _increaseLast, _increaseTime = TimeUnit.SECONDS.toMillis(50);

	public BaconBrawl(ArcadeManager manager)
	{
		super(manager, GameType.BaconBrawl, new Kit[]
				{
						new KitPig(manager),
						new KitMamaPig(manager),
						new KitSheepPig(manager),
						new KitChrisPBacon(manager)
				}, DESCRIPTION);

		DamageTeamSelf = true;
		PrepareFreeze = false;
		StrictAntiHack = true;
		PlayerGameMode = GameMode.ADVENTURE;

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageDealt,
				DamageTaken
		);

		registerStatTrackers(
				new KillsWithinGameStatTracker(this, 6, "KillsInGame")
		);

		new CompassModule()
				.register(this);
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_increaseLast = System.currentTimeMillis();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (player.getFoodLevel() <= 0)
			{
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.STARVATION, 4, false, true, false, GetName(), "Starvation");
			}

			UtilPlayer.hunger(player, -1);
		}

		if (_increaseTime > 0 && UtilTime.elapsed(_increaseLast, _increaseTime))
		{
			_increaseLast = System.currentTimeMillis();
			_increaseTime -= TimeUnit.SECONDS.toMillis(5);
			_knockbackMagnitude *= 1.2;

			String message = C.cGreenB + "The ground begins to tremble... All pigs now take more knockback!";

			for (Player player : UtilServer.getPlayersCollection())
			{
				player.sendMessage(message);
				player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 0.5F);
			}
		}
	}

	@EventHandler
	public void hungerRestore(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);

		if (damager != null)
		{
			UtilPlayer.hunger(damager, 2);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void knockback(CustomDamageEvent event)
	{
		if (!IsLive() || event.GetDamageePlayer() == null)
		{
			return;
		}

		DamageCause cause = event.GetCause();

		if (cause == DamageCause.FIRE_TICK)
		{
			event.SetCancelled("Fire Damage");
		}
		else if (cause == DamageCause.ENTITY_ATTACK || cause == DamageCause.CUSTOM || cause == DamageCause.PROJECTILE)
		{
			UtilPlayer.health(event.GetDamageePlayer(), event.GetDamage());
			event.AddKnockback("Pig Wrestle", _knockbackMagnitude);
		}
	}
}
