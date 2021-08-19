package nautilus.game.arcade.game.games.moba.kit.bob;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
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

import java.util.Random;

public class SkillPaint extends HeroSkill implements IThrown
{

	private static final String[] DESCRIPTION = {
			"Bob Ross"
	};
	private static final byte[] COLOURS = { 14, 1, 4, 5, 3, 11, 0};
	private static final int DAMAGE = 2;

	private static final ItemStack SKILL_ITEM = new ItemStack(Material.DIAMOND_BARDING);

	public SkillPaint(int slot)
	{
		super("1-Inch Brush", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!_kit.useAmmo(player, 1))
		{
			return;
		}

		useSkill(player);

		Snowball snowball = player.launchProjectile(Snowball.class);

		((Moba) Manager.GetGame()).getTowerManager().addProjectile(player, snowball, DAMAGE);
		Manager.GetProjectile().AddThrow(snowball, player, this, -1, true, true, true, false, 0.5F);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Player thrower = (Player) data.getThrower();
		Random random = UtilMath.random;

		if (target != null)
		{
			thrower.playSound(thrower.getLocation(), Sound.LAVA_POP, 1, 1.3F);
			Manager.GetDamage().NewDamageEvent(target, thrower, (Projectile) data.getThrown(), DamageCause.PROJECTILE, DAMAGE, true, true, false, UtilEnt.getName(thrower), GetName());
		}

		for (Block nearby : UtilBlock.getBlocksInRadius(data.getThrown().getLocation(), 2))
		{
			if (UtilBlock.airFoliage(nearby))
			{
				continue;
			}

			Manager.GetBlockRestore().add(nearby, Material.STAINED_CLAY.getId(), COLOURS[random.nextInt(COLOURS.length)], (long) (3000 + (Math.random() * 500)));
		}

		for (LivingEntity entity : UtilEnt.getInRadius(data.getThrown().getLocation(), 2).keySet())
		{
			Manager.GetDamage().NewDamageEvent(entity, thrower, (Projectile) data.getThrown(), DamageCause.PROJECTILE, 2, true, true, false, UtilEnt.getName(thrower), GetName());
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