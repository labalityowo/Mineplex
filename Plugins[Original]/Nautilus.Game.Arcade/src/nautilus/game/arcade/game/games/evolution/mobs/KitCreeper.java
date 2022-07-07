package nautilus.game.arcade.game.games.evolution.mobs;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseCreeper;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.evolution.EvoKit;
import nautilus.game.arcade.game.games.evolution.mobs.perks.PerkSulphurBombEVO;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class KitCreeper extends EvoKit
{
	/**
	 * @author Mysticate
	 */
	
	public KitCreeper(ArcadeManager manager)
	{
		super(manager, "Creeper", new String[]
		{
				C.cYellow + "Ability: " + C.cWhite + "Sulphur Bomb"
		}, 28, 4, new Perk[]
		{
			new PerkSulphurBombEVO()
		}, EntityType.CREEPER);
	}

	@Override
	protected void giveItems(Player player)
	{
		player.getInventory().setItem(0, new ItemBuilder(Material.SULPHUR).setTitle(C.cYellow + C.Bold + "Right Click" + C.cWhite + " - " + C.cGreen + C.Bold + "Sulphur Bomb").build());

		//Disguise
		DisguiseCreeper disguise = new DisguiseCreeper(player);
		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
		disguise.setCustomNameVisible(true);
		
		Manager.GetDisguise().undisguise(player);
		Manager.GetDisguise().disguise(disguise);
		
		player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, 4f, 1f);
	}
}
