package mineplex.game.clans.clans.worldevent.raid.wither.challenge.five;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.api.EventCreature;
import mineplex.game.clans.clans.worldevent.api.EventCreatureDeathEvent;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.archer.DecayingArcher;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.corpse.ReanimatedCorpse;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.giant.Goliath;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.mage.UndeadKnight;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class ChallengeFive extends RaidChallenge<WitherRaid>
{
	private Location _altar;
	private boolean _teleported = false;
	private LinkedList<IronGate> _gates = new LinkedList<>();
	private Goliath _goliath;
	
	public ChallengeFive(WitherRaid raid)
	{
		super(raid, "Rapid Escape");
		
		_altar = raid.getWorldData().getCustomLocs("MAIN_ALTAR").get(0);
	}
	
	private void teleportIn()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Run!"));
			player.teleport(getRaid().getWorldData().getCustomLocs("C_FIVE_ENTER").get(0));
		});
	}

	@Override
	public void customStart()
	{
		int i = 1;
		while (getRaid().getWorldData().getAllCustomLocs().containsKey("C_FIVE_G" + i))
		{
			List<EventCreature<?>> creatures = new ArrayList<>();
			for (Location loc : getRaid().getWorldData().getCustomLocs("C_FIVE_G" + i + "G"))
			{
				int type = UtilMath.r(3) + 1;
				if (type == 1)
				{
					UndeadKnight knight = new UndeadKnight(this, loc);
					getRaid().registerCreature(knight);
					creatures.add(knight);
				}
				if (type == 2)
				{
					ReanimatedCorpse corpse = new ReanimatedCorpse(this, loc);
					getRaid().registerCreature(corpse);
					creatures.add(corpse);
				}
				if (type == 3)
				{
					DecayingArcher archer = new DecayingArcher(this, loc);
					getRaid().registerCreature(archer);
					creatures.add(archer);
				}
			}
			_gates.add(new IronGate(this, getRaid().getWorldData().getCustomLocs("C_FIVE_G" + i), creatures));
			i++;
		}
		_goliath = new Goliath(this, getRaid().getWorldData().getCustomLocs("C_FIVE_GIANT").get(0));
		getRaid().registerCreature(_goliath);
		getRaid().getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Use the third gate!")));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void customComplete()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "You made it!"));
			player.teleport(_altar);
		});
		ClansManager.getInstance().getBlockRestore().restore(getRaid().getWorldData().getCustomLocs("GATE_THREE").get(0).getBlock().getRelative(BlockFace.DOWN));
		getRaid().getWorldData().getCustomLocs("GATE_THREE").get(0).getBlock().getRelative(BlockFace.DOWN).setType(Material.OBSIDIAN);
		Block gate = getRaid().getWorldData().getCustomLocs("GATE_FOUR").get(0).getBlock();
		ClansManager.getInstance().getBlockRestore().restore(gate);
		ClansManager.getInstance().getBlockRestore().restore(gate.getRelative(BlockFace.DOWN));
		gate.getRelative(BlockFace.DOWN).setType(Material.GLOWSTONE);
		gate.setType(Material.SKULL);
		gate.setData((byte)1);
		_altar.getWorld().dropItem(_altar.clone().add(0, 2, 0), new ItemStack(Material.EMERALD, UtilMath.rRange(15, 17)));
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			for (Player player : getRaid().getPlayers())
			{
				if (UtilMath.offset(player.getLocation(), getRaid().getWorldData().getCustomLocs("C_FIVE_EXIT").get(0)) <= 3)
				{
					complete();
					return;
				}
			}
		}
		if (event.getType() == UpdateType.TICK)
		{
			if (!_gates.isEmpty() && _teleported)
			{
				IronGate gate = _gates.peek();
				if (UtilMath.offset2d(_goliath.getEntity().getLocation(), gate.getCenter()) > 5)
				{
					Location to = gate.getCenter().clone();
					to.setY(_goliath.getEntity().getLocation().getY());
					_goliath.getEntity().setVelocity(UtilAlg.getTrajectory(_goliath.getEntity().getLocation(), to).normalize().multiply(0.15));
				}
				if (gate.update())
				{
					_gates.poll();
				}
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
			if (clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_THREE").get(0).getBlock()) || clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_THREE").get(0).getBlock().getRelative(BlockFace.DOWN)))
			{
				_teleported = true;
				teleportIn();
			}
		}
	}
	
	@EventHandler
	public void onIcePrison(SkillTriggerEvent event)
	{
		if (getRaid().getPlayers().contains(event.GetPlayer()) && _teleported && (event.GetSkillName().equals("Ice Prison") || event.GetSkillName().equals("Fissure")))
		{
			event.SetCancelled(true);
		}
	}
	
	@EventHandler
	public void onDeath(EventCreatureDeathEvent event)
	{
		if (event.getCreature().getEvent() instanceof WitherRaid)
		{
			if (((WitherRaid)event.getCreature().getEvent()).getId() == getRaid().getId())
			{
				_gates.forEach(gate -> gate.handleDeath(event.getCreature()));
			}
		}
	}
}