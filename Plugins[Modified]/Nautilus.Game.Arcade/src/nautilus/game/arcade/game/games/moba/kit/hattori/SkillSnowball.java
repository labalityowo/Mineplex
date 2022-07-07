package nautilus.game.arcade.game.games.moba.kit.hattori;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillSnowball extends HeroSkill implements IThrown
{

	private static final String[] DESCRIPTION = {
			"Fires 3 snowballs, one after another.",
			"Each snowball deals damage to any enemy it hits."
	};
	private static final int DAMAGE = 3;
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.SNOW_BALL);

	public SkillSnowball(int slot)
	{
		super("Shuriken", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(1500);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		event.setCancelled(true);

		Player player = event.getPlayer();

		useSkill(player);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int balls = 0;

			@Override
			public void run()
			{
				Snowball snowball = player.launchProjectile(Snowball.class);

				((Moba) Manager.GetGame()).getTowerManager().addProjectile(player, snowball, DAMAGE);
				Manager.GetProjectile().AddThrow(snowball, player, SkillSnowball.this, -1, true, true, true, false, 0.5F);

				if (++balls == 3)
				{
					cancel();
				}
			}
		}, 0, 4);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Player thrower = (Player) data.getThrower();

		if (target != null && !isTeamDamage(target, thrower))
		{
			thrower.playSound(thrower.getLocation(), Sound.LAVA_POP, 1, 1.3F);
			Manager.GetDamage().NewDamageEvent(target, thrower, (Projectile) data.getThrown(), DamageCause.CUSTOM, DAMAGE, false, true, false, UtilEnt.getName(thrower), GetName());
		}
	}

	@Override
	public void Idle(ProjectileUser data)
	{
	}

	@Override
	public void Expire(ProjectileUser data)
	{
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}