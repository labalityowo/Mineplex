package nautilus.game.arcade.game.games.baconbrawl.kits;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBackstabKnockback;
import nautilus.game.arcade.kit.perks.PerkPigCloak;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class KitSheepPig extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkPigCloak(),
					new PerkBackstabKnockback()
			};

	public KitSheepPig(ArcadeManager manager)
	{
		super(manager, GameKit.BACON_BRAWL_SHEEP, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_AXE));

		//Disguise
		DisguiseSheep disguise = new DisguiseSheep(player);
		disguise.setName(C.cYellow + player.getName());
		disguise.setCustomNameVisible(false);
		disguise.setColor(DyeColor.PINK);
		Manager.GetDisguise().disguise(disguise);
	}
}
