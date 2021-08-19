package nautilus.game.arcade.game.games.minestrike.items.grenades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.Radio;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class HighExplosive extends Grenade
{
	public HighExplosive()
	{
		super("High Explosive",  new String[] 
				{
				
				},
				300, 0, Material.APPLE, 1);
	}

	@Override
	public boolean updateCustom(GunModule game, Entity ent)
	{
		if (UtilTime.elapsed(_throwTime, 2000))
		{
			UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION,
					ent.getLocation(), 0, 0, 0, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
			ent.getWorld().playSound(ent.getLocation(),
					Sound.EXPLODE, 3f, 0.8f);



			HashMap<Player, Double> players = UtilPlayer.getInRadius(ent.getLocation(), 9);
			List<Player> damagedPlayers = new ArrayList<>();

			GameTeam throwerTeam = game.getHost().GetTeam(_thrower);

			for (Player player : players.keySet())
			{
				if (!game.getHost().IsAlive(player))
					continue;

				if (throwerTeam != null && !_thrower.equals(player))
				{
					GameTeam damagedTeam = game.getHost().GetTeam(player);

					if (damagedTeam != null && throwerTeam.equals(damagedTeam))
					{
						continue;
					}
				}
				
				// Damage Event
				Player damager = null;
				if (game.getHost().IsAlive(_thrower))
				{
					damager = _thrower;
				}
				
				game.Manager.GetDamage().NewDamageEvent(player, damager, null, ent.getLocation(),
						DamageCause.CUSTOM, 1 + (players.get(player) * 18),
						false, true, false, _thrower.getName(), getName());

				damagedPlayers.add(player);
			}

			Bukkit.getPluginManager().callEvent(new GrenadeExplodeEvent(this, _thrower, damagedPlayers));

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
		
		game.playSound(team.GetColor() == ChatColor.RED ? Radio.T_GRENADE_HE : Radio.CT_GRENADE_HE, player, null);
	}
}
