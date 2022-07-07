package mineplex.game.clans.clans.worldevent.raid.wither.challenge.three;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.archer.DecayingArcher;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.corpse.ReanimatedCorpse;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.mage.UndeadMage;

public class ChallengeThree extends RaidChallenge<WitherRaid>
{
	private Location _altar;
	private boolean _teleported = false;
	private List<ChallengeTorch> _torches = new ArrayList<>();
	protected int LitTorches;
	private long _lastSpawn;
	private int _spawnTotal;
	
	public ChallengeThree(WitherRaid raid)
	{
		super(raid, "Light The Fires");
		
		_altar = raid.getWorldData().getCustomLocs("MAIN_ALTAR").get(0);
		LitTorches = 0;
	}
	
	private void teleportIn()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Light the torches!"));
			player.teleport(getRaid().getWorldData().getCustomLocs("C_THREE_ENTER").get(0));
		});
		getRaid().registerCreature(new UndeadMage(this, getRaid().getWorldData().getCustomLocs("C_THREE_UM").get(0), true, getRaid().getWorldData().getCustomLocs("C_THREE_UMS"), getRaid().getWorldData().getCustomLocs("C_THREE_UMT")));
	}

	@Override
	public void customStart()
	{
		for (Location loc : getRaid().getWorldData().getCustomLocs("C_THREE_TORCH"))
		{
			ClansManager.getInstance().getBlockRestore().restore(loc.getBlock());
			loc.getBlock().setType(Material.NETHERRACK);
			_torches.add(new ChallengeTorch(this, loc.getBlock()));
		}
		getRaid().getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Use the first gate!")));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void customComplete()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "The Torches are lit!"));
			player.teleport(_altar);
		});
		ClansManager.getInstance().getBlockRestore().restore(getRaid().getWorldData().getCustomLocs("GATE_ONE").get(0).getBlock().getRelative(BlockFace.DOWN));
		getRaid().getWorldData().getCustomLocs("GATE_ONE").get(0).getBlock().getRelative(BlockFace.DOWN).setType(Material.OBSIDIAN);
		Block gate = getRaid().getWorldData().getCustomLocs("GATE_TWO").get(0).getBlock();
		ClansManager.getInstance().getBlockRestore().restore(gate);
		ClansManager.getInstance().getBlockRestore().restore(gate.getRelative(BlockFace.DOWN));
		gate.getRelative(BlockFace.DOWN).setType(Material.GLOWSTONE);
		gate.setType(Material.SKULL);
		gate.setData((byte)1);
		_altar.getWorld().dropItem(_altar.clone().add(0, 2, 0), new ItemStack(Material.EMERALD, UtilMath.rRange(10, 15)));
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			if (LitTorches >= 4)
			{
				complete();
			}
		}
		if (event.getType() == UpdateType.TICK)
		{
			_torches.forEach(ChallengeTorch::update);
			if (UtilTime.elapsed(_lastSpawn, 7000) && _teleported)
			{
				_lastSpawn = System.currentTimeMillis();
				if (_spawnTotal > 100)
				{
					return;
				}
				getRaid().getWorldData().getCustomLocs("C_THREE_RC").forEach(loc ->
				{
					_spawnTotal++;
					getRaid().registerCreature(new ReanimatedCorpse(this, loc));
				});
				getRaid().getWorldData().getCustomLocs("C_THREE_DA").forEach(loc ->
				{
					_spawnTotal++;
					getRaid().registerCreature(new DecayingArcher(this, loc));
				});
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (!event.hasBlock())
		{
			return;
		}
		
		Block clicked = event.getClickedBlock();
		if (!_teleported)
		{
			if (clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_ONE").get(0).getBlock()) || clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_ONE").get(0).getBlock().getRelative(BlockFace.DOWN)))
			{
				_lastSpawn = System.currentTimeMillis();
				_teleported = true;
				teleportIn();
			}
		}
		_torches.forEach(torch -> torch.handleInteract(clicked));
	}
}