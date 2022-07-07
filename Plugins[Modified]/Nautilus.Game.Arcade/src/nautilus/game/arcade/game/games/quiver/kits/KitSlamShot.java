package nautilus.game.arcade.game.games.quiver.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkSeismicSlamOITQ;

public class KitSlamShot extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkSeismicSlamOITQ()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.DIAMOND_SPADE),
					ItemStackFactory.Instance.CreateStack(Material.BOW)
			};

	public KitSlamShot(ArcadeManager manager)
	{
		super(manager, GameKit.OITQ_SLAM_SHOOTER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);

		if (Manager.GetGame().GetState() == GameState.Live)
		{
			player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(262, (byte) 0, 1, F.item("Super Arrow")));

			final Player fPlayer = player;

			UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					UtilInv.Update(fPlayer);
				}
			}, 10);
		}
	}
}
