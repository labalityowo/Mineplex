package nautilus.game.arcade.game.games.moba.kit.dana;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.moba.kit.common.DashSkill;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class SkillDanaDash extends DashSkill
{

	private static final String[] DESCRIPTION = {
			"Dash along the ground.",
			"Nearby enemies are thrown up into the air."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);

	public SkillDanaDash(int slot)
	{
		super("Knock up", DESCRIPTION, SKILL_ITEM, slot);

		setCooldown(10000);

		_velocityTime = 600;
		_horizontial = true;
		_velocityStopOnEnd = true;
	}

	@Override
	public void preDash(Player player)
	{
		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_HIT, 2, 0.4F);
	}

	@Override
	public void dashTick(Player player)
	{
		Random random = UtilMath.random;
		Location location = player.getLocation().add(random.nextInt(5) - 2.5, random.nextInt(3), random.nextInt(5) - 2.5);
		UtilParticle.PlayParticle(ParticleType.CLOUD, location.add(0, 1, 0), 0.5F, 0.5F, 0.5f, 0.1F, 10, ViewDist.LONG);
	}

	@Override
	public void collideEntity(LivingEntity entity, Player damager, double scale, boolean sameTeam)
	{
		double damage;

		if (entity instanceof Player)
		{
			damage = 12;
			UtilAction.velocity(entity, new Vector(Math.random() / 2 - 0.25, 1, Math.random() / 2 - 0.25));
		}
		else
		{
			damage = 8;
			UtilAction.velocity(entity, new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5));
		}

		entity.getWorld().playSound(entity.getLocation(), Sound.IRONGOLEM_HIT, 1, 0.5F);
		Manager.GetDamage().NewDamageEvent(entity, damager, null, DamageCause.CUSTOM, damage, false, true, false, UtilEnt.getName(damager), GetName());
	}

	@Override
	public void postDash(Player player)
	{
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 2, 0.4F);
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, player.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5f, 0.1F, 3, ViewDist.LONG);
	}
}

