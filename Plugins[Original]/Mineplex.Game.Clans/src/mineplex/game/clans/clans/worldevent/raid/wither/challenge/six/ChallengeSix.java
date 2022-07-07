package mineplex.game.clans.clans.worldevent.raid.wither.challenge.six;

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
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.api.EventCreatureDeathEvent;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.magma.Magmus;

public class ChallengeSix extends RaidChallenge<WitherRaid>
{
	private Location _altar;
	private boolean _teleported = false;
	
	public ChallengeSix(WitherRaid raid)
	{
		super(raid, "Fiery Gates");
		
		_altar = raid.getWorldData().getCustomLocs("MAIN_ALTAR").get(0);
	}
	
	private void teleportIn()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Slay the gatekeeper or perish!"));
			player.teleport(getRaid().getWorldData().getCustomLocs("C_SIX_ENTER").get(0));
		});
		getRaid().registerCreature(new Magmus(this, getRaid().getWorldData().getCustomLocs("C_SIX_MCS").get(0)));
	}

	@Override
	public void customStart()
	{
		getRaid().getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Use the fourth gate!")));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void customComplete()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Your final battle awaits!"));
			player.teleport(_altar);
		});
		ClansManager.getInstance().getBlockRestore().restore(getRaid().getWorldData().getCustomLocs("GATE_FOUR").get(0).getBlock().getRelative(BlockFace.DOWN));
		getRaid().getWorldData().getCustomLocs("GATE_FOUR").get(0).getBlock().getRelative(BlockFace.DOWN).setType(Material.OBSIDIAN);
		Block gate = getRaid().getWorldData().getCustomLocs("GATE_FIVE").get(0).getBlock();
		ClansManager.getInstance().getBlockRestore().restore(gate);
		ClansManager.getInstance().getBlockRestore().restore(gate.getRelative(BlockFace.DOWN));
		gate.getRelative(BlockFace.DOWN).setType(Material.GLOWSTONE);
		gate.setType(Material.SKULL);
		gate.setData((byte)1);
		_altar.getWorld().dropItem(_altar.clone().add(0, 2, 0), new ItemStack(Material.EMERALD, UtilMath.rRange(15, 20)));
	}
	
	@EventHandler
	public void onDeath(EventCreatureDeathEvent event)
	{
		if (event.getCreature() instanceof Magmus)
		{
			if (((WitherRaid)event.getCreature().getEvent()).getId() == getRaid().getId())
			{
				complete();
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
			if (clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_FOUR").get(0).getBlock()) || clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_FOUR").get(0).getBlock().getRelative(BlockFace.DOWN)))
			{
				_teleported = true;
				teleportIn();
			}
		}
	}
}