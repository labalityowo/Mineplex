package mineplex.game.clans.clans.worldevent.raid.wither.challenge.seven;

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
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.api.EventCreatureDeathEvent;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.mage.UndeadKnight;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.wither.CharlesWitherton;
import mineplex.game.clans.items.ItemType;
import mineplex.game.clans.items.RareItemFactory;
import mineplex.game.clans.items.attributes.weapon.ConqueringAttribute;
import mineplex.game.clans.items.attributes.weapon.FlamingAttribute;
import mineplex.game.clans.items.attributes.weapon.SharpAttribute;
import mineplex.game.clans.items.legendaries.DemonicScythe;

public class ChallengeSeven extends RaidChallenge<WitherRaid>
{
	private Location _altar;
	private boolean _teleported = false;
	private boolean _bottomLayer = false;
	private CharlesWitherton _charlie;
	private long _lastKnightRespawn;
	
	public ChallengeSeven(WitherRaid raid)
	{
		super(raid, "The Final Battle");
		
		_altar = raid.getWorldData().getCustomLocs("MAIN_ALTAR").get(0);
	}
	
	private void teleportToBottom()
	{
		_charlie.getEntity().teleport(getRaid().getWorldData().getCustomLocs("C_SEVEN_RWC").get(0));
		for (Player player : getRaid().getPlayers())
		{
			player.teleport(getRaid().getWorldData().getCustomLocs("C_SEVEN_RWC").get(0));
		}
	}
	
	private void teleportIn()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName(), "GUARDS! ATTACK!"));
			player.teleport(getRaid().getWorldData().getCustomLocs("C_SEVEN_ENTER").get(0));
		});
		for (Location loc : getRaid().getWorldData().getCustomLocs("C_SEVEN_UK"))
		{
			getRaid().registerCreature(new UndeadKnight(this, loc));
		}
		_lastKnightRespawn = System.currentTimeMillis();
		_charlie = new CharlesWitherton(this, getRaid().getWorldData().getCustomLocs("C_SEVEN_CHARLES").get(0));
		getRaid().registerCreature(_charlie);
		for (Location loc : getRaid().getWorldData().getCustomLocs("C_SEVEN_UK"))
		{
			getRaid().registerCreature(new UndeadKnight(this, loc));
		}
	}

	@Override
	public void customStart()
	{
		getRaid().getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "Use the final gate!")));
	}

	@Override
	public void customComplete()
	{
		getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(getRaid().getName() + " Raid", "The evil reign of Charles Witherton is over! You will be returned to spawn in 2 minutes!"));
			player.teleport(_altar);
		});
		List<Location> anim = UtilShapes.getPointsInCircle(_altar.clone().add(0, 2, 0), 5, 3);
		int emeralds = UtilMath.rRange((int)Math.ceil(45 / anim.size()), (int)Math.ceil(80 / anim.size()));
		for (Location drop : anim)
		{
			ClansManager.getInstance().getLootManager().dropRaid(drop);
			drop.getWorld().dropItem(drop, new ItemStack(Material.EMERALD, emeralds));
		}
		if (Math.random() <= 0.03)
		{
			RareItemFactory mainFactory = RareItemFactory.begin(ItemType.LEGENDARY).setLegendary(DemonicScythe.class);
			if (Math.random() < 0.1)
			{
				mainFactory.setSuperPrefix(FlamingAttribute.class);
				mainFactory.setPrefix(SharpAttribute.class);
				mainFactory.setSuffix(ConqueringAttribute.class);
			}
			_altar.getWorld().dropItem(_altar.clone().add(0, 2, 0), mainFactory.fabricate());
		}
		ClansManager.getInstance().getBlockRestore().restore(getRaid().getWorldData().getCustomLocs("GATE_FIVE").get(0).getBlock().getRelative(BlockFace.DOWN));
		getRaid().getWorldData().getCustomLocs("GATE_FIVE").get(0).getBlock().getRelative(BlockFace.DOWN).setType(Material.OBSIDIAN);
		getRaid().setForceEnd(System.currentTimeMillis() + UtilTime.convert(2, TimeUnit.MINUTES, TimeUnit.MILLISECONDS));
	}
	
	@EventHandler
	public void onDeath(EventCreatureDeathEvent event)
	{
		if (event.getCreature() instanceof CharlesWitherton)
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
			if (clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_FIVE").get(0).getBlock()) || clicked.equals(getRaid().getWorldData().getCustomLocs("GATE_FIVE").get(0).getBlock().getRelative(BlockFace.DOWN)))
			{
				_teleported = true;
				teleportIn();
			}
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			if (_teleported && !_bottomLayer && _charlie != null)
			{
				if (_charlie.getHealthPercent() <= 0.75)
				{
					_bottomLayer = true;
					teleportToBottom();
					return;
				}
				
				if (UtilTime.elapsed(_lastKnightRespawn, 30000))
				{
					_lastKnightRespawn = System.currentTimeMillis();
					long knights = getRaid().getCreatures().stream().filter(UndeadKnight.class::isInstance).count();
					int total = getRaid().getWorldData().getCustomLocs("C_SEVEN_UK").size();
					if (knights < total)
					{
						long needed = total - knights;
						for (int i = 0; i < needed; i++)
						{
							Location loc = getRaid().getWorldData().getCustomLocs("C_SEVEN_UK").get(i);
							getRaid().registerCreature(new UndeadKnight(this, loc));
						}
					}
				}
			}
		}
	}
}