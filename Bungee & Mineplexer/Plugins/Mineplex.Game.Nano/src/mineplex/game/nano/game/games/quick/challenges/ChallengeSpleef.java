package mineplex.game.nano.game.games.quick.challenges;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ChallengeSpleef extends Challenge
{

	public ChallengeSpleef(Quick game)
	{
		super(game, ChallengeType.SPLEEF);

		_timeout = TimeUnit.MINUTES.toMillis(1);
		_winConditions.setLastOne(true);
	}

	@Override
	public void challengeSelect()
	{
		ItemStack itemStack = new ItemStack(Material.DIAMOND_SPADE);

		_game.getManager().runSyncLater(() ->
		{
			for (Player player : _players)
			{
				player.getInventory().addItem(itemStack);
				player.getInventory().setHeldItemSlot(0);
				player.setGameMode(GameMode.SURVIVAL);
			}
		}, 20);

		ItemStack snowball = new ItemStack(Material.SNOW_BALL);

		_game.getManager().runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!isRunning())
				{
					cancel();
					return;
				}

				for (Player player : _players)
				{
					player.getInventory().addItem(snowball);
				}
			}
		}, 15 * 20, 15);

		List<Location> corners = _game.getOrangePoints();
		UtilBlock.getInBoundingBox(corners.get(0), corners.get(1), false).forEach(block -> MapUtil.QuickChangeBlockAt(block.getLocation(), Material.SNOW_BLOCK));
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void blockDamage(BlockDamageEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!isParticipating(player) || itemStack == null || itemStack.getType() != Material.DIAMOND_SPADE)
		{
			return;
		}

		Block block = event.getBlock();

		if (block.getType() != Material.SNOW_BLOCK)
		{
			return;
		}

		event.setCancelled(true);

		Location location = block.getLocation();

		location.getWorld().playEffect(location, Effect.STEP_SOUND, block.getType());
		MapUtil.QuickChangeBlockAt(location, Material.AIR);
	}

	@EventHandler
	public void snowballDamage(CustomDamageEvent event)
	{
		if (event.GetProjectile() != null)
		{
			LivingEntity damagee = event.GetDamageeEntity();

			damagee.playEffect(EntityEffect.HURT);
			UtilAction.velocity(damagee, UtilAlg.getTrajectory2d(event.GetProjectile(), damagee).setY(0.4));
		}
	}
}
