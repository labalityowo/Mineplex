package mineplex.minecraft.game.classcombat.Skill.Assassin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class MarkedForDeath extends SkillActive
{

	private final Map<Player, MarkedData> _data = new HashMap<>();

	public MarkedForDeath(SkillFactory skills, String name, ClassType classType, SkillType skillType,
						  int cost, int levels,
						  int energy, int energyMod,
						  long recharge, long rechargeMod, boolean rechargeInform,
						  Material[] itemArray,
						  Action[] actionArray)
	{
		super(skills, name, classType, skillType,
				cost, levels,
				energy, energyMod,
				recharge, rechargeMod, rechargeInform,
				itemArray,
				actionArray);

		SetDesc(new String[]
				{
						"Your next arrow will mark players,",
						"making them take #2.5#1.5 more damage",
						"from the next melee attack.",
						"",
						"Lasts for #3#1 seconds."
				});
	}

	@Override
	public boolean CustomCheck(Player player, int level)
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main(GetClassType().name(), "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}

		return true;
	}

	@Override
	public void Skill(Player player, int level)
	{
		_data.put(player, new MarkedData(player));

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You prepared " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2.5f, 2.0f);
	}

	@EventHandler
	public void ShootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Arrow))
		{
			return;
		}

		Player player = (Player) event.getEntity();
		MarkedData data = _data.get(player);

		if (data == null || data.Arrow != null)
		{
			return;
		}

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(getLevel(player))) + "."));
		data.Arrow = event.getProjectile();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void DamageMark(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Projectile projectile = event.GetProjectile();

		if (projectile == null)
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);
		MarkedData data = _data.get(damager);
		int level = getLevel(damager);

		if (damager == null || data == null || level == 0 || !projectile.equals(data.Arrow))
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();

		//Effect
		damagee.getWorld().playSound(damagee.getLocation(), Sound.BLAZE_BREATH, 2.5f, 2.0f);

		//Inform
		UtilPlayer.message(event.GetDamageePlayer(), F.main(GetClassType().name(), F.name(damager.getName()) + " hit you with " + F.skill(GetName(level)) + "."));
		UtilPlayer.message(damager, F.main(GetClassType().name(), "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName(level)) + "."));

		//Mark
		data.Timeout = System.currentTimeMillis() + (3000 + 1000 * level);
		data.Damage = 2.5 + 1.5 * level;
		data.Marked = damagee;

		//Remove
		projectile.remove();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void DamageAmplify(CustomDamageEvent event)
	{
		if (event.IsCancelled() || event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(false);

		if (damager == null)
		{
			return;
		}

		MarkedData data = _data.get(damager);

		if (data == null || !event.GetDamageeEntity().equals(data.Marked) || data.isExpired())
		{
			return;
		}

		_data.remove(damager);
		event.AddMod(GetName(), GetName(), data.Damage, true);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player killed = event.getEntity(), killer = killed.getKiller();
		MarkedData data = _data.get(killer);

		if (data == null || !killed.equals(data.Marked))
		{
			return;
		}

		UtilPlayer.health(killer, 2 + getLevel(killer));
	}

	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_data.values().forEach(data ->
		{
			if (data.Arrow != null && data.Arrow.isValid())
			{
				UtilParticle.PlayParticleToAll(ParticleType.MOB_SPELL, data.Arrow.getLocation(), null, 0, 1, ViewDist.LONG);
			}
		});
	}

	@Override
	public void Reset(Player player)
	{
		_data.remove(player);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Reset(event.getPlayer());
	}

	private class MarkedData
	{

		final Player Shooter;
		LivingEntity Marked;
		Entity Arrow;
		double Damage;
		long Timeout;

		MarkedData(Player shooter)
		{
			Shooter = shooter;
		}

		boolean isExpired()
		{
			return Timeout > 0 && System.currentTimeMillis() > Timeout;
		}
	}
}
