package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on bomb lobbers.
 */
public class ChallengeBlockLobbers extends Challenge
{
	private static final int MAP_HEIGHT = 1;
	private static final int MAP_MIN_SIZE = 11;
	private static final int WOOL_DATA_RANGE = 16;
	private static final int INVENTORY_HOTBAR_SIZE = 8;

	private static final double FALLING_BLOCK_HEIGHT_ADD = 0.4;
	private static final double FALLING_BLOCK_VECTOR_MULTIPLY = 1.5;
	private static final double FALLING_BLOCK_VECTOR_HEIGHT_ADD = 0.3;
	private static final double FALLING_BLOCK_VECTOR_HEIGHT_MAX = 10.0;
	private static final float FALLING_BLOCK_HITBOX_GROW = 0.2F;

	private static final double KNOCKBACK_VECTOR_MULTIPLY = 0.8;
	private static final double KNOCKBACK_VECTOR_HEIGHT_ADD = 0.3;
	private static final double KNOCKBACK_VECTOR_HEIGHT_MAX = 0.5;

	private static final List<Material> THROW_TYPES = new ArrayList<>(Arrays.asList(
		Material.STONE,
		Material.COBBLESTONE,
		Material.GRASS,
		Material.DIRT,
		Material.SPONGE,
		Material.WOOD));

	public ChallengeBlockLobbers(BawkBawkBattles host)
	{
		super(
			host, ChallengeType.LastStanding,
			"Block Lobbers",
			"Throw blocks to other players.",
			"Try to knock them off the platform!");

		Settings.setUseMapHeight();
		Settings.setCanCruble();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (Location location : circle(getCenter(), getArenaSize(), 1, false, false, 0))
		{
			spawns.add(location.add(0, MAP_HEIGHT, 0));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (Location location : circle(getCenter(), getArenaSize(MAP_MIN_SIZE), 1, false, false, 0))
		{
			Block block = location.getBlock();
			setBlock(block, Material.WOOL, (byte) UtilMath.r(WOOL_DATA_RANGE));

			addBlock(block);
		}
	}

	@Override
	public void onEnd()
	{
		remove(EntityType.FALLING_BLOCK);
		remove(EntityType.DROPPED_ITEM);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		for (Player player : getPlayersIn(true))
		{
			PlayerInventory inventory = player.getInventory();
			Material material = UtilMath.randomElement(THROW_TYPES);

			if (inventory.contains(material))
			{
				if (UtilInv.getAmount(player, material) <= INVENTORY_HOTBAR_SIZE)
				{
					inventory.addItem(new ItemStack(material));
				}
			}
			else
			{
				inventory.addItem(new ItemStack(material));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		switchToAvailableSlot(player);

		Material material = player.getItemInHand().getType();

		if (!THROW_TYPES.contains(material))
			return;

		FallingBlock falling = player.getWorld().spawnFallingBlock(player.getLocation().add(0, FALLING_BLOCK_HEIGHT_ADD, 0), material, (byte) 0);
		UtilAction.velocity(falling, player.getLocation().getDirection(), FALLING_BLOCK_VECTOR_MULTIPLY, false, 0.0, FALLING_BLOCK_VECTOR_HEIGHT_ADD, FALLING_BLOCK_VECTOR_HEIGHT_MAX, true);
		Host.Manager.GetProjectile().AddThrow(falling, player, Host, -1, true, false, true, true, FALLING_BLOCK_HITBOX_GROW);
		UtilInv.remove(player, material, (byte) 0, 1);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event)
	{
		if (!isChallengeValid())
			return;

		Entity entity = event.getEntity();

		if (entity instanceof FallingBlock)
		{
			FallingBlock falling = (FallingBlock) entity;

			if (falling.isOnGround())
			{
				falling.getWorld().playEffect(falling.getLocation(), Effect.STEP_SOUND, falling.getBlockId());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if (!isChallengeValid())
			return;

		if (THROW_TYPES.contains(event.getEntity().getItemStack().getType()))
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void onCollide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (!(data.getThrown() instanceof FallingBlock))
			return;

		if (!(data.getThrower() instanceof Player))
			return;

		if (!(target instanceof Player))
			return;

		if (!isPlayerValid((Player) target))
			return;

		if (target.equals(data.getThrower()))
			return;

		UtilAction.velocity(target, UtilAlg.getTrajectory2d(data.getThrown().getLocation(), target.getLocation()), KNOCKBACK_VECTOR_MULTIPLY, false, 0, KNOCKBACK_VECTOR_HEIGHT_ADD, KNOCKBACK_VECTOR_HEIGHT_MAX, true);
		data.getThrown().remove();
	}

	public void switchToAvailableSlot(Player player)
	{
		Material handType = player.getItemInHand().getType();

		if (handType == Material.AIR || handType == null)
		{
			for (int i = 0; i <= INVENTORY_HOTBAR_SIZE; i++)
			{
				ItemStack current = player.getInventory().getItem(i);

				if (current == null)
					continue;

				if (current != null && current.getType() != Material.AIR)
				{
					player.getInventory().setHeldItemSlot(i);
					break;
				}
			}
		}
	}
}
