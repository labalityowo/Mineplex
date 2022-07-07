package nautilus.game.arcade.game.games.minestrike.items.grenades;

import java.util.HashMap;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.Radio;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Smoke extends Grenade
{
	private boolean _createdBlocks = false;
	
	public Smoke()
	{
		super("Smoke",  new String[] 
				{
				
				},
				300, 0, Material.POTATO_ITEM, 1);
	}

	@Override
	public boolean updateCustom(final GunModule game, Entity ent)
	{
		if (UtilTime.elapsed(_throwTime, 2000) && (UtilEnt.isGrounded(ent) || !ent.isValid()))
		{
//			UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, ent.getLocation(), 0.3f, 0.3f, 0.3f, 0, 1,
//					ViewDist.MAX, UtilServer.getPlayers());


			ent.getWorld().playSound(ent.getLocation(), Sound.FIZZ, 0.1f, 0.1f);

			//Remove Fire
			for (Location loc : game.Manager.GetBlockRestore().restoreBlockAround(Material.FIRE, ent.getLocation(), 5))
			{
				loc.getWorld().playSound(loc, Sound.FIZZ, 1f, 1f);
			}
			
			//Smoke Blocks
			if (!_createdBlocks)
			{
				final HashMap<Block, Double> blocks = UtilBlock.getInRadius(ent.getLocation().add(0, 1, 0), 4d);
				final int round = game.getRound();
				for (final Block block : blocks.keySet())
				{
					if (block.getType() != Material.AIR && block.getType() != Material.PORTAL && block.getType() != Material.FIRE)
						continue;
										
					UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(game.Manager.getPlugin(), new Runnable()
					{
						public void run()
						{
							if (round == game.getRound() && !game.isFreezeTime())
							{
								//18 seconds
								long duration = (long) (15000 + blocks.get(block) * 3000);
								
								game.registerSmokeBlock(block, System.currentTimeMillis() + duration);
							}
						}
					}, 10 - (int)(10d * blocks.get(block)));
				}
				
				_createdBlocks = true;
			}

			ent.getWorld().playSound(ent.getLocation(), Sound.FIZZ, 0.1f, 0.1f);
			
			
			
			return false;
		}
		
		//18 seconds
		return UtilTime.elapsed(_throwTime, 18000);
	}
	
	@Override
	public void playSound(GunModule game, Player player)
	{
		GameTeam team = game.getHost().GetTeam(player);
		if (team == null)
			return;
		
		game.playSound(team.GetColor() == ChatColor.RED ? Radio.T_GRENADE_SMOKE : Radio.CT_GRENADE_SMOKE, player, null);
	}
}
