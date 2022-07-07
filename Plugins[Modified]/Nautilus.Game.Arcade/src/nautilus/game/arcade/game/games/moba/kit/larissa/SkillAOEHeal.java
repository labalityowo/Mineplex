package nautilus.game.arcade.game.games.moba.kit.larissa;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaParticles;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SkillAOEHeal extends HeroSkill implements IThrown
{

	private static final String[] DESCRIPTION = {
			"Throws a water bucket which upon coming into",
			"contact with the ground will heal nearby",
			"players and minions."
	};
	private static final long DURATION = TimeUnit.SECONDS.toMillis(6);
	private static final int RADIUS = 3;
	private static final int HEALTH_PER_SECOND = 4;
	private static final int DAMAGE_PER_SECOND = 2;
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.WATER_BUCKET);
	private static final ItemStack THROWN_ITEM = new ItemStack(Material.WATER_BUCKET);

	private final Set<AOEHealData> _data = new HashSet<>();

	public SkillAOEHeal(int slot)
	{
		super("Dancing Fountain", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(14000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();
		Vector direction = player.getLocation().getDirection();
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(direction), THROWN_ITEM);
		item.setVelocity(direction);

		useSkill(player);
		Manager.GetProjectile().AddThrow(item, player, this, 1000, true, true, true, false, 1F);
	}

	@EventHandler
	public void emptyBucket(PlayerBucketEmptyEvent event)
	{
		event.setCancelled(true);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		deployAoe((Player) data.getThrower(), data.getThrown(), data.getThrown().getLocation());
	}

	@Override
	public void Idle(ProjectileUser data)
	{
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		deployAoe((Player) data.getThrower(), data.getThrown(), data.getThrown().getLocation());
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@EventHandler
	public void updateAOE(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<AOEHealData> iterator = _data.iterator();

		while (iterator.hasNext())
		{
			AOEHealData data = iterator.next();
			Player owner = data.Owner;
			GameTeam team = Manager.GetGame().GetTeam(owner);

			if (team == null)
			{
				iterator.remove();
				continue;
			}

			DustSpellColor colour = new DustSpellColor(team.GetColor() == ChatColor.RED ? java.awt.Color.RED : java.awt.Color.CYAN);

			if (UtilTime.elapsed(data.Start, DURATION))
			{
				iterator.remove();
			}
			else if (UtilTime.elapsed(data.LastHeal, 1000))
			{
				data.LastHeal = System.currentTimeMillis();

				for (LivingEntity entity : UtilEnt.getInRadius(data.Center, RADIUS).keySet())
				{
					if (isTeamDamage(entity, owner))
					{
						MobaParticles.healing(entity, HEALTH_PER_SECOND);
						MobaUtil.heal(entity, owner, HEALTH_PER_SECOND);
					}
					else
					{
						Manager.GetDamage().NewDamageEvent(entity, owner, null, DamageCause.CUSTOM, DAMAGE_PER_SECOND, true, true, false, owner.getName(), GetName());
					}
				}
			}

			UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, data.Center, RADIUS - 0.5F, 0.2F, RADIUS - 0.5F, 0.1F, 5, ViewDist.LONG);

			for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 15)
			{
				double x = RADIUS * Math.cos(theta);
				double z = RADIUS * Math.sin(theta);

				data.Center.add(x, 0, z);

				new ColoredParticle(ParticleType.RED_DUST, colour, data.Center).display(ViewDist.LONG);

				data.Center.subtract(x, 0, z);
			}
		}
	}

	private void deployAoe(Player thrower, Entity item, Location location)
	{
		GameTeam team = Manager.GetGame().GetTeam(thrower);

		if (team == null)
		{
			return;
		}

		UtilFirework.playFirework(location, FireworkEffect.builder().with(Type.BURST).withColor(team.GetColorBase()).withFade(Color.WHITE).withFlicker().build());
		thrower.getWorld().playSound(location, Sound.SPLASH2, 1, 1);
		item.remove();
		_data.add(new AOEHealData(thrower, location));
	}

	private class AOEHealData
	{

		Player Owner;
		Location Center;
		long Start;
		long LastHeal;

		AOEHealData(Player owner, Location center)
		{
			Owner = owner;
			Center = center;
			Start = System.currentTimeMillis();
			LastHeal = Start;
		}
	}
}