package nautilus.game.arcade.game.games.minestrike.items.grenades;

import java.util.HashMap;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.Radio;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class FlashBang extends Grenade
{
	public FlashBang()
	{
		super("Flash Bang",  new String[] 
				{
				
				},
				200, 0, Material.CARROT_ITEM, 2);
	}

	@Override
	public boolean updateCustom(GunModule game, Entity ent)
	{
		if (UtilTime.elapsed(_throwTime, 2000))
		{
			FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(Color.WHITE).with(Type.BALL_LARGE).trail(false).build();
			UtilFirework.playFirework(ent.getLocation().add(0, 0.5, 0), effect);
			
			HashMap<Player, Double> players = UtilPlayer.getInRadius(ent.getLocation(), 48);
			for (Player player : players.keySet())
			{
				if (!game.getHost().IsAlive(player))
					continue;
				
				//Line of Sight
				Location loc = player.getEyeLocation(); 
				
				boolean sight = true;
				while (UtilMath.offset(loc, ent.getLocation()) > 0.5)
				{
					if (UtilBlock.solid(loc.getBlock()))
					{
						sight = false;
						break;
					}
					
					loc.add(UtilAlg.getTrajectory(loc, ent.getLocation()).multiply(0.2));
				}
				
				if (!sight)
					continue;
				
				//Calculate if player is looking away
				Location eyeToGrenade = player.getEyeLocation().add(UtilAlg.getTrajectory(player.getEyeLocation(), ent.getLocation()));
				double flashIntensity = 2 - UtilMath.offset(player.getEyeLocation().add(player.getLocation().getDirection()), eyeToGrenade);
				
				//Duration
				double duration = (2 + (2 * (players.get(player)))) * flashIntensity;
				duration = (Math.min(5, duration));
		
				//Blind
				game.Manager.GetCondition().Factory().Blind(getName(), player, _thrower, duration, 0, false, false, false);
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public void playSound(GunModule game, Player player)
	{
		GameTeam team = game.getHost().GetTeam(player);
		if (team == null)
			return;
		
		game.playSound(team.GetColor() == ChatColor.RED ? Radio.T_GRENADE_FLASH : Radio.CT_GRENADE_FLASH, player, null);
	}
}
