package nautilus.game.arcade.game.games.lobbers.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.PlayerKitApplyEvent;
import nautilus.game.arcade.game.games.lobbers.events.TNTThrowEvent;
import nautilus.game.arcade.game.games.lobbers.kits.perks.PerkCraftman;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitPitcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Double Jump", 0.9, 0.9, false),
					new PerkCraftman()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.LEVER, (byte) 0, 2, F.item("Velocity Selector"))
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET),
			};

	public KitPitcher(ArcadeManager manager)
	{
		super(manager, GameKit.BOMB_LOBBERS_PITCHER, PERKS);
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

		player.getInventory().setItem(1, PLAYER_ITEMS[0]);
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}

	@Override
	public void GiveItems(Player player)
	{
	}

	@EventHandler
	public void setFuse(TNTThrowEvent event)
	{
		if (!Manager.GetGame().IsLive())
		{
			return;
		}
		if (!HasKit(event.getPlayer()))
		{
			return;
		}
		ItemStack lever = event.getPlayer().getInventory().getItem(1);

		if (lever == null || lever.getType() == Material.AIR)
		{
			GiveItems(event.getPlayer());
		}
		else
		{
			if (lever.getAmount() < 1 || lever.getAmount() > 3)
			{
				GiveItems(event.getPlayer());
			}

			UtilAction.velocity(event.getTNT(), event.getPlayer().getLocation().getDirection(), getVelocity(lever.getAmount()), false, 0.0D, 0.1D, 10.0D, false);
		}
	}

	@EventHandler
	public void changeFuse(PlayerInteractEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;

		if (!Manager.IsAlive(event.getPlayer()))
			return;

		if (!HasKit(event.getPlayer()))
			return;

		if (!UtilInv.IsItem(event.getItem(), Material.LEVER, (byte) 0))
			return;

		int amount = event.getPlayer().getInventory().getItem(1).getAmount();

		//Right
		if (UtilEvent.isAction(event, ActionType.R))
		{
			if (amount >= 3)
				return;

			UtilInv.insert(event.getPlayer(), new ItemBuilder(Material.LEVER).setTitle(F.item("Velocity Selector")).build());
			UtilInv.Update(event.getPlayer());
		}
		//Left
		else if (UtilEvent.isAction(event, ActionType.L))
		{
			if (amount <= 1)
				return;

			UtilInv.remove(event.getPlayer(), Material.LEVER, (byte) 0, 1);
			UtilInv.Update(event.getPlayer());
		}
	}

	private double getVelocity(int amount)
	{
		if (amount == 1)
			return 1.75;

		if (amount == 3)
			return 2.25;

		return 2.0;
	}
}
