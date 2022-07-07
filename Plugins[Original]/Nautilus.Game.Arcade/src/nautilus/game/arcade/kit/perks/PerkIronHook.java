package nautilus.game.arcade.kit.perks;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.kit.Perk;

public class PerkIronHook extends Perk implements IThrown
{
	
	private long _cooldown;
	private int _damage;
	
	public PerkIronHook()
	{
		super("Iron Hook", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Pickaxe to " + C.cGreen + "Iron Hook" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_damage = getPerkInt("Damage");
	}

	@EventHandler
	public void Activate(PlayerInteractEvent event)
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

		if (!UtilItem.isPickaxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		// Action
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), ItemStackFactory.Instance.CreateStack(131));
		UtilAction.velocity(item, player.getLocation().getDirection(), 1.8, false, 0, 0.2, 10, false);

		Manager.GetProjectile().AddThrow(item, player, this, -1, true, true, true, true, Sound.FIRE_IGNITE, 1.4f, 0.8f, ParticleType.CRIT, null, 0, UpdateType.TICK, 0.6f);

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));

		// Effect
		item.getWorld().playSound(item.getLocation(), Sound.IRONGOLEM_THROW, 2f, 0.8f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		// Remove
		double velocity = data.getThrown().getVelocity().length();
		data.getThrown().remove();

		if (!(data.getThrower() instanceof Player))
		{
			return;
		}
		
		Player player = (Player) data.getThrower();

		if (target == null)
		{
			return;
		}
		
		if(TeamSuperSmash.getTeam(Manager, player, true).contains(target))
		{
			return;
		}
		
		// Pull
		UtilAction.velocity(target, UtilAlg.getTrajectory(target.getLocation(), player.getLocation()), 2, false, 0, 0.8, 1.5, true);

		// Condition
		Manager.GetCondition().Factory().Falling(GetName(), target, player, 10, false, true);

		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, player, null, DamageCause.CUSTOM, velocity * _damage, false, true, false, player.getName(), GetName());

		// Inform
		UtilPlayer.message(target, F.main("Skill", F.name(player.getName()) + " hit you with " + F.skill(GetName()) + "."));
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}