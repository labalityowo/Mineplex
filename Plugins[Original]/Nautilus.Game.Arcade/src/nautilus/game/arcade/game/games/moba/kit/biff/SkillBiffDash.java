package nautilus.game.arcade.game.games.moba.kit.biff;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SkillBiffDash extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Dash into the air.",
			"When you land any enemies near you are damaged",
			"and thrown up into the air."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);

	private final Map<Player, Long> _active = new HashMap<>();

	public SkillBiffDash(int slot)
	{
		super("Battle Leap", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(11000);
		setSneakActivate(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		useSkill(player);
	}

	@EventHandler
	public void toggleSneak(PlayerToggleSneakEvent event)
	{
		if (!isSkillSneak(event))
		{
			return;
		}

		Player player = event.getPlayer();

		useSkill(player);
	}

	@Override
	public void useSkill(Player player)
	{
		if (_active.containsKey(player))
		{
			return;
		}

		super.useSkill(player);

		Vector direction = player.getLocation().getDirection().setY(1);

		UtilAction.velocity(player, direction);

		player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 1, 1F);
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, player.getLocation().add(0, 0.6, 0), 0.5F, 0.5F, 0.5F, 0.1F, 15, ViewDist.LONG);

		_active.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void landed(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		Iterator<Player> iterator = _active.keySet().iterator();

		while (iterator.hasNext())
		{
			Player player = iterator.next();
			long start = _active.get(player);

			if (!player.isOnline())
			{
				iterator.remove();
			}

			// They have just activated it
			if (!UtilTime.elapsed(start, 1000) || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
			{
				continue;
			}

			for (Block block : UtilBlock.getBlocksInRadius(player.getLocation(), 5))
			{
				if (block.getType() == Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR || Math.random() > 0.5)
				{
					continue;
				}

				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
			}


			for (LivingEntity entity : UtilEnt.getInRadius(player.getLocation(), 5).keySet())
			{
				if (isTeamDamage(player, entity))
				{
					continue;
				}

				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, player.getLocation().add(0, 0.6, 0), 0.5F, 0.5F, 0.5F, 0.1F, 15, ViewDist.LONG);
				Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.CUSTOM, 4, false, true, false, UtilEnt.getName(player), GetName());
				UtilAction.velocity(entity, new Vector(0, 0.6 + Math.random() / 2, 0));
			}

			iterator.remove();
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_active.remove(event.getEntity());
	}
}

