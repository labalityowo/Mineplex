package nautilus.game.arcade.game.games.lobbers.kits.perks;

import java.util.HashSet;
import java.util.Set;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import nautilus.game.arcade.game.games.lobbers.events.TNTThrowEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkWaller extends Perk
{
	private BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

	private Set<TNTPrimed> _tnt = new HashSet<TNTPrimed>();
	
	private Set<Location> _wallBlocks = new HashSet<Location>();

	public PerkWaller()
	{
		super("Waller", new String[]
				{
				C.cYellow + "Click Block" + C.cGray + " with shovel to " + C.cGreen + "Place Wall"
				});
	}
	
	@EventHandler
	public void onThrow(TNTThrowEvent event)
	{
		_tnt.add(event.getTNT());
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent event)
	{
		if (_tnt.contains(event.getEntity()))
		{
			_tnt.remove(event.getEntity());
		}
	}

	@EventHandler
	public void onPlace(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;

		if (!Manager.GetGame().IsLive())
			return;

		if (!Manager.IsAlive(event.getPlayer()))
			return;

		if (!Kit.HasKit(event.getPlayer()))
			return;

		if (!UtilInv.IsItem(event.getItem(), Material.STONE_SPADE, (byte) 0))
			return;
		
		if (event.getClickedBlock().isLiquid())
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You may not place a wall in a liquid!"));
			return;
		}
		
		if (!Recharge.Instance.usable(event.getPlayer(), "Waller"))
			return;
		
		Recharge.Instance.use(event.getPlayer(), "Waller", 100, false, false);
		
		UtilInv.remove(event.getPlayer(), Material.STONE_SPADE, (byte) 0, 1);
		
		_wallBlocks.addAll(buildWall(event.getClickedBlock().getLocation(), event.getPlayer().getLocation().getYaw()));
	}
	
	@SuppressWarnings("deprecation")
	private Set<Location> buildWall(final Location source, final float playerYaw)
	{
		BlockFace facing = getFace(playerYaw + 180F);
		
		Set<Location> allWallBlocks = new HashSet<Location>();
		Set<Location> baseWallBlocks = new HashSet<Location>();
		
		Location centerWallBlock = source.getBlock().getRelative(facing).getRelative(BlockFace.UP).getLocation();
		
		float leftYaw = playerYaw - 90;
		float rightYaw = playerYaw + 90;
		
		BlockFace leftSide = getFace(leftYaw);
		BlockFace rightSide = getFace(rightYaw);
		
		baseWallBlocks.add(centerWallBlock.getBlock().getRelative(leftSide).getRelative(leftSide).getLocation());
		baseWallBlocks.add(centerWallBlock.getBlock().getRelative(leftSide).getLocation());
		baseWallBlocks.add(centerWallBlock);
		baseWallBlocks.add(centerWallBlock.getBlock().getRelative(rightSide).getLocation());
		baseWallBlocks.add(centerWallBlock.getBlock().getRelative(rightSide).getRelative(rightSide).getLocation());
		
		for (Location base : baseWallBlocks)
		{
			for (int height = 0 ; height < 3 ; height++)
			{
				allWallBlocks.add(base.clone().add(0, height, 0));
			}
		}
		for (Location block : allWallBlocks)
		{
			block.getBlock().setType(Material.SMOOTH_BRICK);
			block.getBlock().setData((byte) 0);
			
			block.getWorld().playEffect(block, Effect.STEP_SOUND, block.getBlock().getType());
		}
		return allWallBlocks;
	}
	
	private BlockFace getFace(float yaw)
	{
        return axis[Math.round(yaw / 90f) & 0x3];
	}
}
