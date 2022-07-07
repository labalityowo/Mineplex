package nautilus.game.arcade.game.games.evolution.mobs;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseBlaze;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.evolution.EvoKit;
import nautilus.game.arcade.game.games.evolution.mobs.perks.PerkFlamingSwordEVO;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkNoFallDamage;
import nautilus.game.arcade.kit.perks.PerkWaterDamage;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class KitBlaze extends EvoKit
{
	/**
	 * @author Mysticate
	 */
	
	public KitBlaze(ArcadeManager manager)
	{
		super(manager, "Blaze", new String[]
		{
				C.cYellow + "Ability: " + C.cWhite + "Flamethrower", C.Line,
				C.cGreen + "No Fall Damage", C.cRed + "Water Damage"
		}, 36, 5, new Perk[]
		{
				new PerkFlamingSwordEVO(), new PerkWaterDamage(1, 0.25), new PerkNoFallDamage()
		}, EntityType.BLAZE);
	}

	@Override
	protected void giveItems(Player player)
	{
		player.getInventory().setItem(0, new ItemBuilder(Material.BLAZE_ROD).setTitle(C.cYellow + C.Bold + "Right Click" + C.cWhite + " - " + C.cGreen + C.Bold + "Flamethrower").build());
		
		//Disguise
		DisguiseBlaze disguise = new DisguiseBlaze(player);
		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
		disguise.setCustomNameVisible(true);
		
		Manager.GetDisguise().undisguise(player);
		Manager.GetDisguise().disguise(disguise);		
		
		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 4f, 1f);
	}	
}
