package nautilus.game.arcade.game.games.moba.kit.rowena;

import mineplex.core.common.events.EntityVelocityChangeEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SkillBombardment extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Take to the sky!",
			"Your shots become fast high power explosive shots."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);
	private static final int DAMAGE_FACTOR = 2;
	private static final int SHOTS = 3;
	private static final int MAX_TIME = 10000;
	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.DIAMOND_BARDING)
			.setTitle(C.cDRedB + "Bombardment")
			.build();

	private Set<BombardmentData> _data = new HashSet<>();

	public SkillBombardment(int slot)
	{
		super("Bombardment", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

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

		for (BombardmentData data : _data)
		{
			if (data.Shooter.equals(player))
			{
				return;
			}
		}

		Location toTeleport = player.getLocation().add(0, 15, 0);

		player.teleport(toTeleport);
		player.getInventory().setItem(0, ACTIVE_ITEM);
		player.getInventory().setHeldItemSlot(0);
		broadcast(player);
		_data.add(new BombardmentData(player));
	}

	@EventHandler
	public void interactActive(PlayerInteractEvent event)
	{
		if (event.getItem() == null || !event.getItem().isSimilar(ACTIVE_ITEM))
		{
			return;
		}

		Player player = event.getPlayer();

		for (BombardmentData data : _data)
		{
			if (data.Shooter.equals(player))
			{
				Location location = player.getEyeLocation();
				LineParticle lineParticle = new LineParticle(location, location.getDirection(), 0.4, 40, ParticleType.FIREWORKS_SPARK, UtilServer.getPlayers());
				double damage = MobaUtil.scaleDamageWithBow(player.getInventory().getItem(0), 0) * DAMAGE_FACTOR;

				while (!lineParticle.update())
				{
				}

				player.playSound(player.getLocation(), Sound.EXPLODE, 1, 0.8F);
				UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, lineParticle.getDestination(), 0, 0, 0, 0.1F, 1, ViewDist.LONG);

				for (LivingEntity entity : UtilEnt.getInRadius(lineParticle.getLastLocation(), 5).keySet())
				{
					if (isTeamDamage(entity, player))
					{
						continue;
					}

					Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.CUSTOM, damage, true, true, false, player.getName(), GetName());
				}

				data.Shots--;
				return;
			}
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<BombardmentData> iterator = _data.iterator();

		while (iterator.hasNext())
		{
			BombardmentData data = iterator.next();
			Player player = data.Shooter;

			if (UtilTime.elapsed(data.Start, MAX_TIME) || data.Shots <= 0)
			{
				useSkill(player);
				Kit.GiveItems(player);
				data.Block.setType(Material.AIR);
				iterator.remove();
			}
		}
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();

		for (BombardmentData data : _data)
		{
			if (data.Block != null && data.Shooter.equals(player) && (event.getTo().getX() != event.getFrom().getX() || event.getTo().getZ() != event.getFrom().getZ()))
			{
				event.setTo(event.getFrom());
			}
		}
	}

	@EventHandler
	public void playerVelocity(EntityVelocityChangeEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		for (BombardmentData data : _data)
		{
			if (data.Block != null && data.Shooter.equals(player))
			{
				event.setCancelled(true);
			}
		}
	}

	private class BombardmentData
	{

		Player Shooter;
		int Shots;
		Block Block;
		long Start;

		BombardmentData(Player shooter)
		{
			Shooter = shooter;
			Shots = SHOTS;
			Block = shooter.getLocation().getBlock().getRelative(BlockFace.DOWN);
			Block.setType(Material.BARRIER);
			Start = System.currentTimeMillis();
		}
	}
}

