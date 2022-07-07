package nautilus.game.arcade.game.modules;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IWorldAccess;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Iterator;
import java.util.function.Predicate;

public class SafezoneModule extends Module
{
	private Predicate<Location> _isInSafezone = location -> true;

	public SafezoneModule filter(Predicate<Location> predicate)
	{
		this._isInSafezone = predicate;
		return this;
	}

	@Override
	public void setup()
	{
		for (World world : Bukkit.getWorlds())
		{
			registerWorldAccess(world);
		}
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event)
	{
		registerWorldAccess(event.getWorld());
	}

	private void registerWorldAccess(World world)
	{
		IWorldAccess access = new IWorldAccess()
		{
			@Override
			public void a(BlockPosition blockPosition)
			{
				Location location = new Location(world, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
				if (isInSafeZone(location))
				{
					Block block = location.getBlock();
					if (block.getType() == Material.COBBLESTONE || block.getType() == Material.OBSIDIAN || block.getType() == Material.STONE)
					{
						block.setType(Material.AIR);
					}
				}
			}

			@Override
			public void b(BlockPosition blockPosition)
			{

			}

			@Override
			public void a(int i, int i1, int i2, int i3, int i4, int i5)
			{

			}

			@Override
			public void a(String s, double v, double v1, double v2, float v3, float v4)
			{

			}

			@Override
			public void a(EntityHuman entityHuman, String s, double v, double v1, double v2, float v3, float v4)
			{

			}

			@Override
			public void a(int i, boolean b, double v, double v1, double v2, double v3, double v4, double v5, int... ints)
			{

			}

			@Override
			public void a(Entity entity)
			{

			}

			@Override
			public void b(Entity entity)
			{

			}

			@Override
			public void a(String s, BlockPosition blockPosition)
			{

			}

			@Override
			public void a(int i, BlockPosition blockPosition, int i1)
			{

			}

			@Override
			public void a(EntityHuman entityHuman, int i, BlockPosition blockPosition, int i1)
			{

			}

			@Override
			public void b(int i, BlockPosition blockPosition, int i1)
			{

			}
		};

//		((CraftWorld) world).getHandle().u.add(access);
	}

	// fixme flowing water and stuff

	@EventHandler
	public void preventBlockPlacement(BlockPlaceEvent event)
	{
		if (isInSafeZone(event.getBlock().getLocation()))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot build in this area!"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void preventStructureGrow(StructureGrowEvent event)
	{
		Iterator<BlockState> blocks = event.getBlocks().iterator();
		while (blocks.hasNext())
		{
			BlockState next = blocks.next();
			if (isInSafeZone(next.getLocation()))
			{
				blocks.remove();
			}
		}
	}

	@EventHandler
	public void preventBlockGrow(BlockGrowEvent event)
	{
		if (isInSafeZone(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void preventBoneMeal(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			boolean isIllegal = false;
			if (!isIllegal)
			{
				isIllegal = event.getPlayer().getItemInHand().getType() == Material.INK_SACK &&
						event.getPlayer().getItemInHand().getData().getData() == (byte) 15;
			}

			if (isIllegal && isInSafeZone(event.getClickedBlock().getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void preventPistons(BlockPistonExtendEvent event)
	{
		boolean willBeUnsafe = false;
		for (Block block : event.getBlocks())
		{
			if (isInSafeZone(block.getRelative(event.getDirection()).getLocation()))
			{
				willBeUnsafe = true;
				break;
			}
		}
		if (willBeUnsafe)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void preventPistons(BlockPistonRetractEvent event)
	{
		boolean willBeUnsafe = false;
		for (Block block : event.getBlocks())
		{
			if (isInSafeZone(block.getLocation()))
			{
				willBeUnsafe = true;
				break;
			}
		}
		if (willBeUnsafe)
		{
			event.setCancelled(true);
		}
	}

	private boolean isInSafeZone(Location location)
	{
		return _isInSafezone.test(location);
	}
}
