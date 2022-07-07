package nautilus.game.arcade.game.games.smash.perks.chicken;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashChicken extends SmashUltimate
{

	public SmashChicken()
	{
		super("Aerial Gunner", new String[] {}, Sound.CHICKEN_IDLE, 0);
	}
	
	/**
	 * See {@link PerkEggGun} for smash code.
	 */
	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		player.getInventory().remove(Material.IRON_SWORD);
		player.getInventory().remove(Material.IRON_AXE);
	}
}
