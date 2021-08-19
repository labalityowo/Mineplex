package nautilus.game.arcade.game.games.deathtag.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDamageSet;
import nautilus.game.arcade.kit.perks.PerkIronSkin;

public class KitChaser extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDamageSet(4),
					new PerkIronSkin(2)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE)
			};

	public KitChaser(ArcadeManager manager)
	{
		super(manager, GameKit.DEATH_TAG_CHASER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);

		//Disguise
		DisguiseSkeleton disguise = new DisguiseSkeleton(player);
		disguise.setName(C.cRed + player.getName());
		disguise.setCustomNameVisible(true);
		disguise.hideArmor();
		Manager.GetDisguise().disguise(disguise);
	}
}
