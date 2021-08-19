package nautilus.game.arcade.game.games.quiver.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;
import nautilus.game.arcade.game.games.quiver.module.ModuleUltimate;
import nautilus.game.arcade.game.games.quiver.ultimates.UltimateNinja;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDash;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitNewNinja extends Kit
{

	private static final String DOUBLE_JUMP = "Double Jump";

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump(DOUBLE_JUMP, 0.9, 0.9, true),
					new PerkDash(5000, 10, 8),
					new UltimateNinja(6000)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.GOLD_SWORD).setUnbreakable(true).build(),
					new ItemBuilder(Material.BOW).setUnbreakable(true).build(),
			};

	public KitNewNinja(ArcadeManager manager)
	{
		super(manager, GameKit.OITQP_NINJA, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}

	@Override
	public void Selected(Player player)
	{
		if (!Manager.GetGame().IsLive())
		{
			return;
		}

		QuiverTeamBase game = (QuiverTeamBase) Manager.GetGame();

		game.getQuiverTeamModule(ModuleUltimate.class).resetUltimate(player, true);
	}
}
