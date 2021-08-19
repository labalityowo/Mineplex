package nautilus.game.arcade.game.games.smash.perks.blaze;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import nautilus.game.arcade.kit.Perk;

public class SmashBlaze extends SmashUltimate
{

	public SmashBlaze()
	{
		super("Phoenix", new String[] {}, Sound.BLAZE_DEATH, 0);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		player.getInventory().remove(Material.IRON_SWORD);
		player.getInventory().remove(Material.IRON_AXE);
		
		for (Perk perk : Kit.GetPerks())
		{
			if (!(perk instanceof PerkFirefly))
			{
				continue;
			}
			
			PerkFirefly firefly = (PerkFirefly) perk;
			
			firefly.activate(player, this);
		}
	}
}
