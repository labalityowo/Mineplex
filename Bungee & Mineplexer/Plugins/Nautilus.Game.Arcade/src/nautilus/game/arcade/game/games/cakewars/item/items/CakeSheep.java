package nautilus.game.arcade.game.games.cakewars.item.items;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.item.CakeSpecialItem;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeSheep extends CakeSpecialItem implements Listener
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.SHEEP))
			.setTitle(C.cPurpleB + "Polly The Sheep")
			.addLore(
					"",
					"Spawns Polly The Sheep...",
					"After " + C.cRed + "4 seconds" + C.cGray + " she explodes destroying",
					"nearby player placed blocks.",
					"If she is killed she does not explode.",
					"Warning! Polly has a", C.cRed + "20 second" + C.cGray + " cooldown between uses."
			)
			.build();
	private static final long EXPLOSION_TIME = TimeUnit.SECONDS.toMillis(4);
	private static final int EXPLOSION_RADIUS = 7;
	private static final int NO_PLACE_RADIUS_SQUARED = 225;

	public CakeSheep(CakeWars game)
	{
		super(game, ITEM_STACK, "Polly The Sheep", TimeUnit.SECONDS.toMillis(20));
	}

	@Override
	protected void setup()
	{
		UtilServer.RegisterEvents(this);
	}

	@Override
	protected void cleanup()
	{
		UtilServer.Unregister(this);
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, CakeTeam cakeTeam)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return false;
		}

		Player player = event.getPlayer();
		Location location = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);

		if (UtilMath.offset2dSquared(location, cakeTeam.getCake()) < NO_PLACE_RADIUS_SQUARED)
		{
			player.sendMessage(F.main("Game", "You cannot place " + F.name(getName()) + " this close to your cake."));
			return false;
		}

		location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, player.getLocation())));

		DecimalFormat format = new DecimalFormat("0.0");
		long start = System.currentTimeMillis();

		_game.CreatureAllowOverride = true;

		Sheep sheep = location.getWorld().spawn(location, Sheep.class);
		sheep.setColor(cakeTeam.getGameTeam().getDyeColor());
		sheep.setCustomNameVisible(true);
		location.getWorld().playSound(location, Sound.SHEEP_SHEAR, 2, 0.8F);
		UtilEnt.vegetate(sheep);

		_game.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			int tick;

			@Override
			public void run()
			{
				if (!sheep.isValid())
				{
					cancel();
					return;
				}

				long left = start + EXPLOSION_TIME - System.currentTimeMillis();
				Location sheepLocation = sheep.getLocation();

				if (left <= 0)
				{
					Map<Block, Double> blocks = UtilBlock.getInRadius(sheepLocation, EXPLOSION_RADIUS);
					Collection<Block> placedBlocks = _game.getCakePlayerModule().getPlacedBlocks();

					location.getWorld().playSound(sheepLocation, Sound.EXPLODE, 2, 1);
					UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, sheepLocation.add(0, 0.5, 0), null, 0, 1, ViewDist.LONG);
					blocks.entrySet().removeIf(entry ->
					{
						Block block = entry.getKey();
						double scale = entry.getValue();

						if (placedBlocks.contains(block))
						{
							double chance;

							// I originally was going to use the NMS way to get the "hardness" of a block
							// but I decided against it as the values were way too varied for this game.
							// The value for chance should be taken from if the block was right next to the sheep.
							switch (block.getType())
							{
								case WOOL:
									chance = 1;
									break;
								case STAINED_CLAY:
									chance = 0.7;
									break;
								case WOOD:
									chance = 0.7;
									break;
								case ENDER_STONE:
									chance = 0.4;
									break;
								case OBSIDIAN:
									chance = 0.05;
									break;
								default:
									chance = 0.5;
									break;
							}

							chance *= scale * 2;

							if (Math.random() < chance)
							{
								placedBlocks.remove(block);
								return false;
							}
						}

						return true;
					});

					_game.getArcadeManager().GetExplosion().BlockExplosion(blocks.keySet(), sheepLocation, false);
					UtilPlayer.getInRadius(sheepLocation, EXPLOSION_RADIUS).forEach((nearby, scale) ->
					{
						_game.getArcadeManager().GetDamage().NewDamageEvent(nearby, player, null, sheepLocation, DamageCause.CUSTOM, 40 * scale, true, true, false, player.getName(), getName());
					});

					sheep.remove();
					cancel();
					return;
				}

				sheep.setCustomName((tick % 2 == 0 ? cakeTeam.getGameTeam().GetColor() + C.Bold : C.cWhiteB) + format.format(left / 1000D));

				if (tick % 8 == 0)
				{
					location.getWorld().playSound(sheepLocation, Sound.SHEEP_IDLE, 2, 0.8F);
				}

				tick++;
			}
		}, 0, 2);

		_game.CreatureAllowOverride = false;

		return true;
	}

	@EventHandler
	public void sheepDeath(EntityDeathEvent event)
	{
		if (event.getEntity() instanceof Sheep)
		{
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}

	@EventHandler
	public void sheepDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Sheep && event.GetDamagerPlayer(true) == null)
		{
			event.SetCancelled("Sheep World Damage");
		}
	}

	@EventHandler
	public void sheepShear(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Sheep)
		{
			event.setCancelled(true);
		}
	}
}
