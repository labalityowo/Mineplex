package mineplex.game.clans.clans.worldevent.raid.wither.challenge.four;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class ChallengeFour extends RaidChallenge<WitherRaid>
{
	private Location _altar;
	private boolean _teleported = false;
	private List<FakeBlock> _blocks = new ArrayList<>();
	private double[] _damageYRange = new double[2];
	
	public ChallengeFour(WitherRaid raid)
	{
		super(raid, "Infested Pits");
		
		_altar = raid.getWorldData().getCustomLocs("MAIN_ALTAR").get(0);
	}
	
	private void teleportIn()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Complete the parkour!"));
			player.teleport(getRaid().getWorldData().getCustomLocs("C_FOUR_ENTER").get(0));
		});
	}

	@Override
	public void customStart()
	{
		for (Location loc : getRaid().getWorldData().getCustomLocs("C_FOUR_FB"))
		{
			loc.getBlock().setType(Material.NETHER_BRICK);
			_blocks.add(new FakeBlock(this, loc.getBlock()));
		}
		getRaid().getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Use the second gate!")));
		Location pos1 = getRaid().getWorldData().getCustomLocs("C_FOUR_SF").get(0);
		Location pos2 = getRaid().getWorldData().getCustomLocs("C_FOUR_SF").get(1);
		_damageYRange[0] = Math.min(pos1.getY(), pos2.getY());
		_damageYRange[1] = Math.max(pos1.getY(), pos2.getY());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void customComplete()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Excellent jumping!"));
			player.teleport(_altar);
		});
		ClansManager.getInstance().getBlockRestore().restore(getRaid().getWorldData().getCustomLocs("GATE_TWO").get(0).getBlock().getRelative(BlockFace.DOWN));
		getRaid().getWorldData().getCustomLocs("GATE_TWO").get(0).getBlock().getRelative(BlockFace.DOWN).setType(Material.OBSIDIAN);
		Block gate = getRaid().getWorldData().getCustomLocs("GATE_THREE").get(0).getBlock();
		ClansManager.getInstance().getBlockRestore().restore(gate);
		ClansManager.getInstance().getBlockRestore().restore(gate.getRelative(BlockFace.DOWN));
		gate.getRelative(BlockFace.DOWN).setType(Material.GLOWSTONE);
		gate.setType(Material.SKULL);
		gate.setData((byte)1);
		_altar.getWorld().dropItem(_altar.clone().add(0, 2, 0), new ItemStack(Material.EMERALD, UtilMath.rRange(13, 17)));
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC && _teleported)
		{
			for (Player player : getRaid().getPlayers())
			{
				if (UtilMath.offset(player.getLocation(), getRaid().getWorldData().getCustomLocs("C_FOUR_EXIT").get(0)) <= 3)
				{
					complete();
					return;
				}
				if (player.getLocation().getY() <= _damageYRange[1] && player.getLocation().getY() >= _damageYRange[0])
				{
					getRaid().getDamageManager().NewDamageEvent(player, null, null, DamageCause.LAVA, 1, false, true, true, "Burning Cavern", "Hot Ground");
				}
			}
		}
		if (event.getType() == UpdateType.TICK)
		{
			_blocks.forEach(FakeBlock::update);
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
			if (clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_TWO").get(0).getBlock()) || clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_TWO").get(0).getBlock().getRelative(BlockFace.DOWN)))
			{
				_teleported = true;
				teleportIn();
			}
		}
	}
	
	@EventHandler
	public void onIcePrison(SkillTriggerEvent event)
	{
		if (getRaid().getPlayers().contains(event.GetPlayer()) && _teleported)
		{
			event.SetCancelled(true);
		}
	}
}