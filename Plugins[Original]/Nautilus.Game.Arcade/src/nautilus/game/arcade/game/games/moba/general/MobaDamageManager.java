package nautilus.game.arcade.game.games.moba.general;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

public class MobaDamageManager implements Listener
{

	private final Moba _host;

	public MobaDamageManager(Moba host)
	{
		_host = host;
	}

	@EventHandler
	public void preventTeamDamage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);

		if (damagee == null || damager == null)
		{
			return;
		}

		GameTeam damageeTeam = _host.GetTeam(damagee);
		GameTeam damagerTeam = _host.GetTeam(damager);

		if (damageeTeam == null || damagerTeam == null)
		{
			return;
		}

		_host.getScoreboardModule().refreshAsSubject(damagee);
	}

	@EventHandler
	public void preventTeamFire(ConditionApplyEvent event)
	{
		Condition condition = event.GetCondition();

		if (condition.GetType() != ConditionType.BURNING)
		{
			return;
		}

		if (condition.GetEnt() == null || condition.GetSource() == null)
		{
			return;
		}

		if (!(condition.GetEnt() instanceof Player && condition.GetSource() instanceof Player))
		{
			return;
		}

		if (!_host.GetTeam((Player) condition.GetEnt()).equals(_host.GetTeam((Player) condition.GetSource())))
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void updateDamageScoreboard(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_host.getScoreboardModule().refresh();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void unifyKilledWith(CombatDeathEvent event)
	{
		String word = event.getKilledWord();
		String[] split = word.split("-");

		if (word.contains("Click") && split.length > 1)
		{
			word = split[1].trim();
			event.setKilledWord(word);
		}
	}

	@EventHandler
	public void mageStrength(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(MobaConstants.BASIC_ATTACK))
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);

		if (damager == null)
		{
			return;
		}

		damager.getActivePotionEffects().forEach(effect ->
		{

			if (effect.getType().toString().equals(PotionEffectType.INCREASE_DAMAGE.toString()))
			{
				event.AddMod("Strength", (effect.getAmplifier() + 1) * 2 + 1);
			}

		});
	}
}
