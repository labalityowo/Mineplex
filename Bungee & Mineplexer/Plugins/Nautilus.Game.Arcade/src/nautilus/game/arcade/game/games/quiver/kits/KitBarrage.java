package nautilus.game.arcade.game.games.quiver.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.quiver.Quiver;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;
import nautilus.game.arcade.game.games.quiver.module.ModuleUltimate;
import nautilus.game.arcade.game.games.quiver.ultimates.UltimateBarrage;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitBarrage extends Kit
{

	private static final String DOUBLE_JUMP = "Double Jump";
	private static final String SUPER_ARROW = "Super Arrow";

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump(DOUBLE_JUMP, 0.9, 0.9, true),
					new PerkFletcher(5, 1, true, -1, true, SUPER_ARROW),
					new UltimateBarrage(10)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.IRON_AXE).setUnbreakable(true).build(),
					new ItemBuilder(Material.BOW).setUnbreakable(true).build(),
			};

	public KitBarrage(ArcadeManager manager)
	{
		super(manager, GameKit.OITQP_BARRAGE, PERKS);

	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);

		if (Manager.GetGame().GetState() == GameState.Live)
		{
			player.getInventory().addItem(Quiver.SUPER_ARROW);

			UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), () -> UtilInv.Update(player), 10);
		}
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
