package mineplex.game.nano.game.games.oits;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class SnowballTrouble extends ScoredSoloGame
{

	private static final int MAX_BALLS = 5;

	private final ItemStack _snowball;

	public SnowballTrouble(NanoManager manager)
	{
		super(manager, GameType.SNOWBALL_TROUBLE, new String[]
				{
						"Snowballs are " + C.cYellow + "One-Hit" + C.Reset + "!",
						"You have " + C.cGreen + "Infinite Respawns" + C.Reset + ".",
						C.cYellow + "Most kills" + C.Reset + " wins!"
				});

		_snowball = new ItemStack(Material.SNOW_BALL);

		_prepareComponent.setPrepareFreeze(false);

		_teamComponent.setRespawnRechargeTime(500);

		_damageComponent.setFall(false);

		_spectatorComponent.setDeathOut(false);

		_endComponent.setTimeout(TimeUnit.SECONDS.toMillis(100));
	}

	@Override
	protected void parseData()
	{

	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : getAlivePlayers())
		{
			incrementScore(player, 0);
		}
	}

	@Override
	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		event.getPlayer().getInventory().addItem(_snowball);
	}

	@EventHandler
	public void updateGiveBalls(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !isLive())
		{
			return;
		}

		for (Player player : getAlivePlayers())
		{
			if (!player.getInventory().contains(Material.SNOW_BALL, MAX_BALLS))
			{
				player.getInventory().addItem(_snowball);
			}
		}
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (damagee == null || !(event.GetProjectile() instanceof Snowball))
		{
			return;
		}

		event.AddMod(event.GetDamagerPlayer(true).getName(), "Snowball", 20, true);
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.SetBroadcastType(DeathMessageType.Simple);

		CombatComponent killerComp = event.GetLog().GetKiller();

		if (killerComp == null)
		{
			return;
		}

		Player killer = UtilPlayer.searchExact(killerComp.getUniqueIdOfEntity());

		if (killer == null)
		{
			return;
		}

		incrementScore(killer, 1);
	}
}
