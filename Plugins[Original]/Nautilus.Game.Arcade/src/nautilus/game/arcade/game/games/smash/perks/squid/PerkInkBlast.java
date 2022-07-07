package nautilus.game.arcade.game.games.smash.perks.squid;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkInkBlast extends SmashPerk implements IThrown
{

	private int _cooldown;
	private float _spread;
	private float _projectileVelocity;
	private int _knockbackMagnitude;
	private int _bullets;
	private double _damagePerBullet;

	public PerkInkBlast()
	{
		super("Ink Shotgun", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Ink Shotgun" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_spread = getPerkFloat("Spread");
		_projectileVelocity = getPerkFloat("Projectile Velocity");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
		_bullets = getPerkInt("Bullets");
		_damagePerBullet = getPerkDouble("Damage Per Bullet");
	}

	@EventHandler
	public void shoot(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (isSuperActive(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		event.setCancelled(true);

		UtilInv.Update(player);

		for (int i = 0; i < _bullets; i++)
		{
			Item ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), ItemStackFactory.Instance.CreateStack(Material.INK_SACK, (byte) 0, 1, "Ink" + Math
					.random()));

			Vector random = new Vector((Math.random() - 0.5) * _spread, (Math.random() - 0.5) * _spread, (Math.random() - 0.5) * _spread);
			random.normalize();
			random.multiply(_projectileVelocity);

			if (i == 0)
			{
				random.multiply(0);
			}

			UtilAction.velocity(ent, player.getLocation().getDirection().add(random), 1 + 0.4 * Math.random(), false, 0, 0.2, 10, false);

			Manager.GetProjectile().AddThrow(ent, player, this, -1, true, true, true, true, null, 1f, 1f, ParticleType.EXPLODE, UpdateType.FASTEST, 0.5f);
		}

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1.5f, 0.75f);
		player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 0.75f, 1f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Explode(data);

		if (target == null)
		{
			return;
		}

		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.PROJECTILE, _damagePerBullet, true, true, false, UtilEnt.getName(data.getThrower()), GetName());

		UtilParticle.PlayParticle(ParticleType.EXPLODE, target.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0, 12, ViewDist.LONG, UtilServer.getPlayers());
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		Explode(data);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		Explode(data);
	}

	public void Explode(ProjectileUser data)
	{
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.EXPLODE, 0.75f, 1.25f);
		data.getThrown().remove();
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}