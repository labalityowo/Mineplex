package nautilus.game.arcade.game.games.milkcow.kits.perk;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkCowBomb extends Perk implements IThrown
{

	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(3);

	public PerkCowBomb()
	{
		super("Cow Bomb", new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Cow Bomb"
				});
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void Shoot(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Block block = event.getClickedBlock();

		if (UtilBlock.usable(block))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!UtilItem.isAxe(itemStack) || !hasPerk(player) || !Recharge.Instance.use(player, GetName(), COOLDOWN, true, true))
		{
			return;
		}

		event.setCancelled(true);

		Manager.GetGame().CreatureAllowOverride = true;

		Location location = player.getEyeLocation();
		Cow cow = location.getWorld().spawn(location.add(location.getDirection()), Cow.class);
		cow.setBaby();
		cow.setAgeLock(true);

		Manager.GetGame().CreatureAllowOverride = false;

		UtilAction.velocity(cow, player.getLocation().getDirection(), 1.4, false, 0, 0.3, 10, false);

		Manager.GetProjectile().AddThrow(cow, player, this, -1, true, true, true, true, 0.5f);

		player.sendMessage(F.main(Manager.getName(), "You used " + F.skill(GetName()) + "."));

		player.getWorld().playSound(player.getLocation(), Sound.COW_IDLE, 2f, 1.5f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Explode(data);

		if (target == null)
		{
			return;
		}

		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.PROJECTILE, 4, true, true, false, data.getThrower().getName(), GetName());
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
		Location location = data.getThrown().getLocation();

		location.getWorld().playSound(location, Sound.COW_HURT, 2f, 1.2f);
		location.getWorld().createExplosion(location, 0.5f);

		data.getThrown().remove();
	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), 5);
	}
}