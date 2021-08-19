package nautilus.game.arcade.game.games.cakewars.kits.perk;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeDeployPlatform;
import nautilus.game.arcade.kit.Perk;

public class PerkPassiveWoolGain extends Perk
{

	private static final long WOOL_RECHARGE = TimeUnit.SECONDS.toMillis(4);
	private static final long PLATFORM_RECHARGE = TimeUnit.SECONDS.toMillis(10);
	private static final int MAX_WOOL = 32;
	private static final int MAX_PLATFORMS = 5;
	private static final String WOOL_NAME = "Knitted Wool";
	private static final String PLATFORM_NAME = "Knitted Platform";
	private static final ItemStack PLATFORM_ITEM = new ItemBuilder(CakeDeployPlatform.ITEM_STACK)
			.setTitle(C.cYellowB + PLATFORM_NAME)
			.build();

	public PerkPassiveWoolGain()
	{
		super("Knitter");
	}

	@EventHandler
	public void updateGain(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !Manager.GetGame().IsLive())
		{
			return;
		}

		for (GameTeam team: Manager.GetGame().GetTeamList())
		{
			for (Player player : team.GetPlayers(true))
			{
				if (UtilPlayer.isSpectator(player) || !hasPerk(player))
				{
					continue;
				}

				if (!player.getInventory().contains(Material.WOOL, MAX_WOOL) && Recharge.Instance.use(player, WOOL_NAME, WOOL_RECHARGE, false, false))
				{
					ItemStack itemStack = new ItemStack(Material.WOOL, 1, (short) 0, Manager.GetGame().GetTeam(player).GetColorData());

					player.getInventory().addItem(itemStack);
				}

				if (!UtilInv.contains(player, PLATFORM_NAME, PLATFORM_ITEM.getType(), team.getDyeColor().getDyeData(), MAX_PLATFORMS) && Recharge.Instance.use(player, PLATFORM_NAME, PLATFORM_RECHARGE, false, false))
				{
					ItemStack itemStack = new ItemBuilder(PLATFORM_ITEM)
							.setData(team.getDyeColor().getDyeData())
							.build();

					player.getInventory().addItem(itemStack);
				}
			}
		}
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		GameTeam team = Manager.GetGame().GetTeam(player);

		if (!hasPerk(player) || team == null)
		{
			return;
		}

		UtilInv.DisallowMovementOf(event, PLATFORM_NAME, PLATFORM_ITEM.getType(), team.getDyeColor().getDyeData(), true);
	}

	@EventHandler
	public void playerDrop(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemDrop().getItemStack();
		GameTeam team = Manager.GetGame().GetTeam(player);

		if (hasPerk(player) && team != null && UtilInv.IsItem(itemStack, PLATFORM_NAME, PLATFORM_ITEM.getType(), team.getDyeColor().getDyeData()))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Game", "You cannot drop " + F.item(PLATFORM_NAME) + "."));
		}
	}
}
