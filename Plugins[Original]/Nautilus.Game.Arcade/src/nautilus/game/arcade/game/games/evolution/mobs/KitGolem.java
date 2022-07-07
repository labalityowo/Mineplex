package nautilus.game.arcade.game.games.evolution.mobs;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseIronGolem;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.evolution.EvoKit;
import nautilus.game.arcade.game.games.evolution.mobs.perks.PerkSiesmicSlamEVO;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkKnockbackTaken;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class KitGolem extends EvoKit
{
	/**
	 * @author Mysticate
	 */
	
	public KitGolem(ArcadeManager manager)
	{
		super(manager, "Iron Golem", new String[]
		{
				C.cYellow + "Abililty: " + C.cWhite + "Seismic Slam", C.Line,
				C.cGreen + "50% Knockback"
		}, 40, 6, new Perk[]
		{
			new PerkSiesmicSlamEVO(), new PerkKnockbackTaken(.5)
		}, EntityType.IRON_GOLEM);
	}
	
	@Override
	protected void giveItems(Player player)
	{
		player.getInventory().setItem(0, new ItemBuilder(Material.IRON_INGOT).setTitle(C.cYellow + C.Bold + "Right Click" + C.cWhite + " - " + C.cGreen + C.Bold + "Seismic Slam").build());
				
		//Disguise
		DisguiseIronGolem disguise = new DisguiseIronGolem(player);
		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
		disguise.setCustomNameVisible(true);

		Manager.GetDisguise().disguise(disguise);
	
		player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 4f, 1f);
	}
}
