package nautilus.game.arcade.game.games.skywars.kits.perks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilTime;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.skywars.TeamSkywars;

public class PerkDirtCannon extends SkywarsPerk implements IThrown
{

	private static final int GIVE_DELAY = 20000;
	private static final int MAX_DIRT = 4;
	private static final long COOLDOWN = 500;

	private long _lastDirt;
	private double _targetKnockback;

	public PerkDirtCannon(ItemStack itemStack, double targetKnockback)
	{
		super("Throwable Dirt", itemStack);

		_targetKnockback = targetKnockback;
		_lastDirt = 0;
	}

	@Override
	public void onUseItem(Player player)
	{
		if (!Recharge.Instance.use(player, GetName(), COOLDOWN, true, true))
		{
			return;
		}

		player.setItemInHand(UtilInv.decrement(player.getItemInHand()));

		FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(player.getEyeLocation().add(player.getLocation().getDirection()), Material.DIRT, (byte) 0);
		fallingBlock.setDropItem(false);
		fallingBlock.setVelocity(player.getLocation().getDirection().multiply(2));
		Manager.GetProjectile().AddThrow(fallingBlock, player, this, -1, true, true, true, true, false, 1);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (UtilTime.elapsed(_lastDirt, GIVE_DELAY))
		{
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (!hasPerk(player) || UtilInv.contains(player, "Throwable", Material.DIRT, (byte) 0, MAX_DIRT))
				{
					continue;
				}

				player.getInventory().addItem(_itemStack);
			}

			_lastDirt = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event)
	{
		if (event.getBlock().getType() == Material.DIRT)
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target == null)
		{
			return;
		}

		if (Manager.GetGame() instanceof TeamSkywars && target instanceof Player && data.getThrower() instanceof Player)
		{
			Game game = Manager.GetGame();

			if (game.GetTeam((Player) target).equals(game.GetTeam((Player) data.getThrower())) || !game.IsAlive(target))
			{
				return;
			}
		}

		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.CUSTOM, 1, false, true, true, data.getThrower().getName(), GetName());

		UtilAction.velocity(target, data.getThrown().getVelocity().normalize().setY(0.5).multiply(_targetKnockback));
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