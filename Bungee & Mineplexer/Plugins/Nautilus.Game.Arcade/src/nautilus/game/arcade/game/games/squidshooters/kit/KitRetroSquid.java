package nautilus.game.arcade.game.games.squidshooters.kit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitRetroSquid extends Kit
{

	private static final String NAME = "Squid Laser";
	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.IRON_AXE)
							.setTitle(C.cYellowB + "Right-Click" + C.cWhiteB + " - " + C.cGreenB + NAME)
							.setUnbreakable(true)
							.build()
			};

	public KitRetroSquid(ArcadeManager manager)
	{
		super(manager, GameKit.SQUID_SHOOTER);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Player)
		{
			attemptLaser(event.getPlayer(), null);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (attemptLaser(event.getPlayer(), event.getClickedBlock()))
		{
			event.setCancelled(true);
		}
	}

	private boolean attemptLaser(Player player, Block block)
	{
		ItemStack itemStack = player.getItemInHand();

		if (!HasKit(player) || !UtilItem.isAxe(itemStack) || UtilBlock.usable(block) || !Recharge.Instance.use(player, NAME, 1200, false, true))
		{
			return false;
		}

		Location location = player.getEyeLocation();
		location.add(location.getDirection());
		location.getWorld().playSound(location, Sound.FIREWORK_LAUNCH, 1, 1);

		LineParticle lineParticle = new LineParticle(location, location.getDirection(), 0.3, 30, ParticleType.FIREWORKS_SPARK, UtilServer.getPlayers());

		while (!lineParticle.update())
		{
			Location from = lineParticle.getLastLocation().subtract(0, 1, 0);
			Player closest = UtilPlayer.getClosest(from, 2, player);

			if (closest == null)
			{
				continue;
			}

			from = closest.getLocation();

			UtilPlayer.getInRadius(from, 2).forEach((hit, scale) ->
			{
				if (player.equals(hit))
				{
					return;
				}

				player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, closest.getLocation().add(0, 1, 0), null, 0.1F, 10, ViewDist.LONG);
				Manager.GetDamage().NewDamageEvent(hit, player, null, DamageCause.CUSTOM, 7 * scale, true, true, true, player.getName(), NAME);
			});

			break;
		}

		return true;
	}
}
