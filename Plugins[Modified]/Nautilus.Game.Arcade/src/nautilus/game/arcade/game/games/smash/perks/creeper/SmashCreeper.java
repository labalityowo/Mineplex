package nautilus.game.arcade.game.games.smash.perks.creeper;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import nautilus.game.arcade.kit.Perk;

public class SmashCreeper extends SmashUltimate
{

	public SmashCreeper()
	{
		super("Atomic Blast", new String[] {}, Sound.CREEPER_HISS, 0);
	}
	
	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		for (Perk perk : Kit.GetPerks())
		{
			if (!(perk instanceof PerkCreeperExplode))
			{
				continue;
			}
			
			PerkCreeperExplode explode = (PerkCreeperExplode) perk;
			
			explode.IncreaseSize(player);
			explode.activate(player);
		}
	}

}
