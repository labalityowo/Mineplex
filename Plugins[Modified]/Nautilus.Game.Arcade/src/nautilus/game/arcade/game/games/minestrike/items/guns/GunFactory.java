package nautilus.game.arcade.game.games.minestrike.items.guns;

import nautilus.game.arcade.game.games.minestrike.GunModule;

public class GunFactory
{
	public Gun createGun(GunStats gun, GunModule module)
	{
		if (gun.getGunType() == GunType.SHOTGUN)
			return new Shotgun(gun, module);
		
		return new Gun(gun, module);
	}
}
