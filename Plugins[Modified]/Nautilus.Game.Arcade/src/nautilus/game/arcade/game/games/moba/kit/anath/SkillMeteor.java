package nautilus.game.arcade.game.games.moba.kit.anath;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.kit.perks.data.MeteorShowerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SkillMeteor extends HeroSkill implements IThrown
{

	private static final String[] DESCRIPTION = {
			"Shoot out a Block of Netherrack which is placed where it lands.",
			"Meteors rain from the sky around the block.",
			"Enemies within the area are damaged.",
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);

	private final Set<MeteorShowerData> _data = new HashSet<>();

	public SkillMeteor(int slot)
	{
		super("Meteor Shower", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(60000);
		setDropItemActivate(true);
	}

	@Override
	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!Recharge.Instance.use(player, GetName() + "Trigger", 2000, false, false))
		{
			return;
		}

		for (MeteorShowerData data : _data)
		{
			if (data.Shooter.equals(player))
			{
				return;
			}
		}

		FallingBlock block = player.getWorld().spawnFallingBlock(player.getEyeLocation().add(player.getLocation().getDirection()), Material.NETHERRACK, (byte) 0);
		block.setVelocity(player.getLocation().getDirection());

		Manager.GetProjectile().AddThrow(block, player, this, 2000, true, true, true, false, 0.5F);

		broadcast(player);
		useActiveSkill(player, 7000);
	}

	@EventHandler
	public void entityChangeBlock(EntityChangeBlockEvent event)
	{
		if (event.getEntity() instanceof FallingBlock)
		{
			event.getEntity().remove();
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void updateShower(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		_data.removeIf(MeteorShowerData::update);

		for (MeteorShowerData data : _data)
		{
			Location location = data.Target;
			GameTeam team = Manager.GetGame().GetTeam(data.Shooter);
			DustSpellColor colour = new DustSpellColor(team.GetColor() == ChatColor.RED ? Color.RED : Color.CYAN);

			for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 100)
			{
				double x = 10 * Math.sin(theta);
				double z = 10 * Math.cos(theta);

				location.add(x, 0.25, z);

				new ColoredParticle(ParticleType.RED_DUST, colour, location).display(ViewDist.LONG);

				location.subtract(x, 0.25, z);
			}

			for (LivingEntity nearby : UtilEnt.getInRadius(location, 10).keySet())
			{
				if (isTeamDamage(data.Shooter, nearby))
				{
					continue;
				}

				nearby.setFireTicks(20);
				Manager.GetDamage().NewDamageEvent(nearby, data.Shooter, null, DamageCause.CUSTOM, 2, true, true, false, UtilEnt.getName(data.Shooter), GetName());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void projectileHit(EntityExplodeEvent event)
	{
		if (event.getEntity() instanceof LargeFireball)
		{
			event.blockList().clear();
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Entity thrown = data.getThrown();

		startShower(data);

		thrown.remove();
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		Expire(data);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		for (MeteorShowerData showerData : _data)
		{
			if (showerData.Shooter.equals(data.getThrower()))
			{
				return;
			}
		}

		startShower(data);
		data.getThrown().remove();
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	private void startShower(ProjectileUser data)
	{
		Manager.GetBlockRestore().add(data.getThrown().getLocation().getBlock(), Material.NETHERRACK.getId(), (byte) 0, 6000);
		_data.add(new MeteorShowerData((Player) data.getThrower(), data.getThrown().getLocation(), 6000));
	}
}