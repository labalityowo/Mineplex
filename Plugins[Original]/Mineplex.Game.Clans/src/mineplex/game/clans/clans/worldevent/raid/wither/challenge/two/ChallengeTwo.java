package mineplex.game.clans.clans.worldevent.raid.wither.challenge.two;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.api.EventCreatureDeathEvent;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.mage.UndeadKnight;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.mage.UndeadMage;

public class ChallengeTwo extends RaidChallenge<WitherRaid>
{
	private Location _altar;
	
	public ChallengeTwo(WitherRaid raid)
	{
		super(raid, "Undead Encounter");
		
		_altar = raid.getWorldData().getCustomLocs("MAIN_ALTAR").get(0);
	}
	
	private void openStoneBrickTraps()
	{
		for (Location loc : getRaid().getWorldData().getCustomLocs("C_ONE_SBT"))
		{
			Block b = loc.getBlock();
			ClansManager.getInstance().getBlockRestore().restore(b);
			b.setType(Material.AIR);
			ClansManager.getInstance().getBlockRestore().restore(b.getRelative(BlockFace.UP));
			b.getRelative(BlockFace.UP).setType(Material.AIR);
			getRaid().registerCreature(new UndeadKnight(this, loc));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void customStart()
	{
		openStoneBrickTraps();
		_altar.getBlock().getRelative(BlockFace.DOWN).setData(DyeColor.BLACK.getWoolData());
		getRaid().getPlayers().forEach(player -> UtilPlayer.message(player, F.main("Undead Mage", "MINIONS, ATTACK!")));
		getRaid().registerCreature(new UndeadMage(this, _altar, false, getRaid().getWorldData().getCustomLocs("C_ONE_SBT"), getRaid().getWorldData().getCustomLocs("C_TWO_MTP")));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void customComplete()
	{
		getRaid().getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "The Undead Mage has fallen!")));
		Block gate = getRaid().getWorldData().getCustomLocs("GATE_ONE").get(0).getBlock();
		ClansManager.getInstance().getBlockRestore().restore(gate);
		ClansManager.getInstance().getBlockRestore().restore(gate.getRelative(BlockFace.DOWN));
		gate.getRelative(BlockFace.DOWN).setType(Material.GLOWSTONE);
		gate.setType(Material.SKULL);
		gate.setData((byte)1);
	}
	
	@EventHandler
	public void onDeath(EventCreatureDeathEvent event)
	{
		if (event.getCreature() instanceof UndeadMage)
		{
			if (((WitherRaid)event.getCreature().getEvent()).getId() == getRaid().getId())
			{
				complete();
			}
		}
	}
}