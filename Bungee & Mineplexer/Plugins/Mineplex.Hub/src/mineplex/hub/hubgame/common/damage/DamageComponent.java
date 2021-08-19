package mineplex.hub.hubgame.common.damage;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilAction;
import mineplex.core.recharge.Recharge;
import mineplex.core.titles.Titles;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.HubGame;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class DamageComponent extends HubGameComponent<HubGame>
{

	private static final Titles TITLES = Managers.get(Titles.class);

	public DamageComponent(HubGame game)
	{
		super(game);
	}

	@EventHandler
	public void disableTitles(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare && event.getState() != GameState.End)
		{
			return;
		}

		for (Player player : _game.getAlivePlayers())
		{
			if (event.getState() == GameState.Prepare)
			{
				TITLES.forceDisable(player);
			}
			else
			{
				TITLES.forceEnable(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamage(CustomDamageEvent event)
	{
		uncancelDamageEvent(event);
	}

	private void uncancelDamageEvent(CustomDamageEvent event)
	{
		Player player = event.GetDamageePlayer();

		if (player == null || !_game.isAlive(player))
		{
			return;
		}

		event.GetCancellers().clear();

		if (!Recharge.Instance.use(player, "Damage", 400, false, false))
		{
			event.SetCancelled("Damage Component Rate");
		}
	}

	@EventHandler
	public void increaseKnockback(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (damagee == null || !_game.isAlive(damagee))
		{
			return;
		}

		event.AddKnockback("Damage Component", 1.4);
	}

	@EventHandler
	public void playerDeath(CombatDeathEvent event)
	{
		Player player = event.GetEvent().getEntity();

		if (!_game.isAlive(player))
		{
			return;
		}

		event.GetEvent().getDrops().clear();
		player.setHealth(player.getMaxHealth());
		UtilAction.zeroVelocity(player);
		_game.onPlayerDeath(player);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerVelocity(PlayerVelocityEvent event)
	{
		if (_game.isAlive(event.getPlayer()))
		{
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void convertAbsorption(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : _game.getAlivePlayers())
		{
			if (!player.hasPotionEffect(PotionEffectType.ABSORPTION))
			{
				continue;
			}

			for (PotionEffect effect : player.getActivePotionEffects())
			{
				if (effect.getType().toString().equalsIgnoreCase(PotionEffectType.ABSORPTION.toString()))
				{
					player.removePotionEffect(PotionEffectType.ABSORPTION);
					player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles()));
					player.setHealth(Math.min(player.getHealth() + 4, player.getMaxHealth()));
				}
			}
		}
	}
}
