package nautilus.game.arcade.game.games.moba.kit.ivy;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.moba.kit.HeroSkill;

public class SkillHook extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Fire out a particle beam. Hooking players",
			"and pulling them towards you."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.VINE);
	private static final int RANGE = 9;
	private static final int DAMAGE_PLAYER = 4;
	private static final int DAMAGE_MINION = 6;
	private static final PotionEffect DEBUFF = new PotionEffect(PotionEffectType.SLOW, 60, 2, false, false);

	private final Set<HookData> _data = new HashSet<>();

	public SkillHook(int slot)
	{
		super("Vine Pull", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(10000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();
		LineParticle lineParticle = new LineParticle(player.getEyeLocation(), player.getLocation().getDirection(), 0.25, RANGE, ParticleType.HAPPY_VILLAGER, UtilServer.getPlayers());

		_data.add(new HookData(player, lineParticle));
		useSkill(player);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_data.removeIf(data ->
		{
			Player player = data.Owner;
			LineParticle leash = data.Leash;

			if (player == null || !player.isOnline())
			{
				return true;
			}

			for (int i = 0; i < 10; i++)
			{
				if (leash.update())
				{
					return true;
				}

				if (data.Target == null)
				{
					for (LivingEntity nearby : UtilEnt.getInRadius(leash.getLastLocation().subtract(0, 1.2, 0), 2).keySet())
					{
						if (isTeamDamage(player, nearby))
						{
							continue;
						}

						if (nearby instanceof Player)
						{
							data.Target = (Player) nearby;
							nearby.addPotionEffect(DEBUFF);
							UtilAction.zeroVelocity(nearby);
							Manager.GetDamage().NewDamageEvent(nearby, player, null, DamageCause.CUSTOM, DAMAGE_PLAYER, false, false, false, player.getName(), GetName());
						}
						else
						{
							Manager.GetDamage().NewDamageEvent(nearby, player, null, DamageCause.CUSTOM, DAMAGE_MINION, true, false, false, player.getName(), GetName());
						}
					}
				}
				else
				{
					Player target = data.Target;

					if (!target.isOnline())
					{
						return true;
					}

					if (UtilMath.offset2dSquared(target.getLocation(), player.getLocation()) < 4)
					{
						UtilAction.zeroVelocity(target);
						return true;
					}

					UtilAction.velocity(target, UtilAlg.getTrajectory(target, player).setY(data.First ? 0 : 0.3));
					data.First = true;
				}
			}

			return false;
		});
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_data.removeIf(data -> data.Owner.equals(event.getEntity()) || data.Target.equals(event.getEntity()));
	}

	private class HookData
	{

		Player Owner;
		LineParticle Leash;
		Player Target;
		boolean First;

		HookData(Player owner, LineParticle leash)
		{
			Owner = owner;
			Leash = leash;
		}
	}
}

