package nautilus.game.arcade.game.games.smash.perks.skeletalhorse;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import nautilus.game.arcade.kit.Perk;

public class SmashSkeletalHorse extends SmashUltimate
{

	public SmashSkeletalHorse()
	{
		super("Bone Storm", new String[] {}, Sound.HORSE_SKELETON_DEATH, 0);
	} 
	
	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		player.getInventory().remove(Material.IRON_SPADE);
		player.getInventory().remove(Material.IRON_AXE);
		
		for (Perk perk : Kit.GetPerks())
		{
			if (perk instanceof PerkBoneRush)
			{
				PerkBoneRush boneRush = (PerkBoneRush) perk;
				
				boneRush.activate(player);
			}
		}
	}
	
	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		for (Perk perk : Kit.GetPerks())
		{
			if (perk instanceof PerkBoneRush)
			{
				PerkBoneRush boneRush = (PerkBoneRush) perk;
				
				boneRush.deactivate(player);
			}
		}
	}

}
