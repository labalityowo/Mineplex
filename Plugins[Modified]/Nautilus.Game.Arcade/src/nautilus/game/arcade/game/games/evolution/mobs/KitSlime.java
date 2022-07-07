package nautilus.game.arcade.game.games.evolution.mobs;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.evolution.EvoKit;
import nautilus.game.arcade.game.games.evolution.mobs.perks.PerkBounceEVO;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFallModifier;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class KitSlime extends EvoKit
{
	/**
	 * @author Mysticate
	 */
	
	public KitSlime(ArcadeManager manager)
	{
		super(manager, "Slime", new String[]
		{
				C.cYellow + "Ability: " + C.cWhite + "Slime Slam", C.Line,
				C.cGreen + "50% Fall Damage"
		}, 22, 4, new Perk[]
		{
			new PerkBounceEVO(), new PerkFallModifier(.5)
		}, EntityType.SLIME);
	}

	@Override
	protected void giveItems(Player player)
	{
		player.getInventory().setItem(0, new ItemBuilder(Material.SLIME_BALL).setTitle(C.cYellow + C.Bold + "Right Click" + C.cWhite + " - " + C.cGreen + C.Bold + "Slime Slam").build());

		//Disguise
		DisguiseSlime disguise = new DisguiseSlime(player);
		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
		disguise.setCustomNameVisible(true);
		disguise.SetSize(2);
		
		Manager.GetDisguise().undisguise(player);
		Manager.GetDisguise().disguise(disguise);
				
		player.getWorld().playSound(player.getLocation(), Sound.SLIME_WALK, 4f, 1f);
	}
}
