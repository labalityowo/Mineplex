package nautilus.game.arcade.game.games.baconbrawl.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguisePig;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBodySlam;

public class KitPig extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkBodySlam(6, 2),
			};

	public KitPig(ArcadeManager manager)
	{
		super(manager, GameKit.BACON_BRAWL_PIG, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_AXE));

		//Disguise
		DisguisePig disguise = new DisguisePig(player);
		disguise.setName(C.cYellow + player.getName());
		disguise.setCustomNameVisible(false);
		Manager.GetDisguise().disguise(disguise);
	}
}
