package mineplex.game.clans.clans.worldevent.raid.wither.challenge.one;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;

public class ChallengeOne extends RaidChallenge<WitherRaid>
{
	private Location _altar;
	private Player _trigger;
	
	public ChallengeOne(WitherRaid raid)
	{
		super(raid, "Entering Hell");
		
		_altar = raid.getWorldData().getCustomLocs("MAIN_ALTAR").get(0);
	}
	
	private void sealDoor()
	{
		Location doorBase1 = getRaid().getWorldData().getCustomLocs("C_ONE_TRAP").get(0);
		Location doorBase2 = getRaid().getWorldData().getCustomLocs("C_ONE_TRAP").get(1);
		int minX = Math.min(doorBase1.getBlockX(), doorBase2.getBlockX());
		int maxX = Math.max(doorBase1.getBlockX(), doorBase2.getBlockX());
		int minY = Math.min(doorBase1.getBlockY(), doorBase2.getBlockY());
		int maxY = Math.max(doorBase1.getBlockY(), doorBase2.getBlockY()) + 1;
		int minZ = Math.min(doorBase1.getBlockZ(), doorBase2.getBlockZ());
		int maxZ = Math.max(doorBase1.getBlockZ(), doorBase2.getBlockZ());
		
		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					Block block = getRaid().getWorldData().World.getBlockAt(x, y, z);
					ClansManager.getInstance().getBlockRestore().restore(block);
					block.setType(Material.NETHER_BRICK);
				}
			}
		}
	}
	
	private void setupStoneBrickTraps()
	{
		for (Location loc : getRaid().getWorldData().getCustomLocs("C_ONE_SBT"))
		{
			Block b = loc.getBlock();
			ClansManager.getInstance().getBlockRestore().restore(b);
			b.setType(Material.SMOOTH_BRICK);
			ClansManager.getInstance().getBlockRestore().restore(b.getRelative(BlockFace.UP));
			b.getRelative(BlockFace.UP).setType(Material.SMOOTH_BRICK);
		}
	}

	@Override
	public void customStart()
	{
		setupStoneBrickTraps();
		getRaid().getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Enter the Altar Room")));
	}

	@Override
	public void customComplete()
	{
		getRaid().getPlayers().forEach(player ->
		{
			player.getWorld().strikeLightningEffect(player.getLocation());
			if (_trigger != null)
			{
				if (UtilMath.offset(player.getLocation(), _altar) >= 17)
				{
					player.teleport(_trigger);
				}
			}
		});
		sealDoor();
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			for (Player player : getRaid().getPlayers())
			{
				if (UtilMath.offset(player.getLocation(), _altar) <= 10)
				{
					_trigger = player;
					complete();
					return;
				}
			}
		}
	}
}