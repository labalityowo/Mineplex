package nautilus.game.arcade.game.games.lobbers.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.PlayerKitApplyEvent;
import nautilus.game.arcade.game.games.lobbers.kits.perks.PerkCraftman;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.PerkDummy;

public class KitArmorer extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Double Jump", 0.9, 0.9, false),
					new PerkDummy("Armorer", new String[]
							{
									C.cGray + "Receive " + C.cYellow + "Full Iron Armor"
							}),
					new PerkCraftman(),
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.IRON_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.IRON_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.IRON_HELMET),
			};

	public KitArmorer(ArcadeManager manager)
	{
		super(manager, GameKit.BOMB_LOBBERS_ARMORER, PERKS);
	}

	@Override
	public void ApplyKit(Player player)
	{
		PlayerKitApplyEvent applyEvent = new PlayerKitApplyEvent(Manager.GetGame(), this, player);
		UtilServer.CallEvent(applyEvent);

		if (applyEvent.isCancelled())
		{
			return;
		}

		UtilInv.Clear(player);

		for (Perk perk : GetPerks())
		{
			perk.Apply(player);
		}

		GiveItemsCall(player);

		player.getInventory().setArmorContents(PLAYER_ARMOR);

		UtilInv.Update(player);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
