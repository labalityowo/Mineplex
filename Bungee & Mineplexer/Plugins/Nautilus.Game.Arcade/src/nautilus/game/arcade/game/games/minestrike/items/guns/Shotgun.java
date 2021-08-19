package nautilus.game.arcade.game.games.minestrike.items.guns;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItemType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class Shotgun extends Gun
{
	private int _pellets;

	public Shotgun(GunStats gunStats, GunModule module)
	{
		super(gunStats, module);
		
		_pellets = gunStats.getPellets();
	} 

	@Override
	public void shoot(Player player, GunModule game)
	{
		if (_reloading)
			return;
		
		//Check Ammo
		if (!ammoCheck(player))
		{ 
			reload(player);
			return;
		}
			
		if (!Recharge.Instance.use(player, getName() + " Shoot", _gunStats.getFireRate(), false, false))
			return;

		//Use Ammo
		_loadedAmmo--;
		//updateWeaponName(false); 
		
		//Effect
		soundFire(player.getLocation());
		UtilParticle.PlayParticle(ParticleType.CLOUD, player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.5)), 0, 0, 0, 0, 1,
				ViewDist.LONG, UtilServer.getPlayers());
		
		for (int i=0 ; i<_pellets ; i++)
			game.registerBullet(fireBullet(player, game));
		
		//Reload
		if (_loadedAmmo == 0)
			reload(player);
	}
	
	@Override
	public void soundRefire(Location loc)
	{
		if (_gunStats.getFireRate() >= 500)
		{
			loc.getWorld().playSound(loc, Sound.PISTON_RETRACT, 0.8f, 1.2f);
			loc.getWorld().playSound(loc, Sound.PISTON_RETRACT, 0.8f, 1.2f);
		}
	}
	
	@Override
	public double getCone(Player player)
	{
		return _cone;
	}
	
	@Override
	public void moveEvent(PlayerMoveEvent event)
	{
		
	}
	
	@Override
	public long getReloadTime()
	{
		long time = _gunStats.getReloadTime() + _gunStats.getReloadTime() * Math.min(_reserveAmmo, _gunStats.getClipSize() - _loadedAmmo);
		
		return time;
	}
}
