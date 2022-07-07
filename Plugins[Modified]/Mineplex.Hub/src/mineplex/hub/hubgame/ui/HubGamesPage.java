package mineplex.hub.hubgame.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.parkour.ParkourData;
import mineplex.hub.parkour.ParkourManager;

public class HubGamesPage extends ShopPageBase<HubGameManager, HubGameShop>
{

	private static final ItemStack LOBBY_GAMES = new ItemBuilder(Material.WOOL, (byte) 14)
			.setTitle(C.cGreenB + "Lobby Games")
			.addLore
					(
							"Goof off and play around with your best buds",
							"or people you just met hanging around the lobby!",
							"These mini-mini-games offer a super quick lobby",
							"only experience.",
							"",
							C.cWhite + "- " + C.cGreenB + "1v1 Gladiators",
							C.cWhite + "- " + C.cGreenB + "Slime Cycles",
							C.cWhite + "- " + C.cGray + "Under construction",
							"",
							"Click to teleport to the games!"
					)
			.build();

	private final ParkourManager _parkourManager;

	public HubGamesPage(HubGameManager plugin, Player player)
	{
		super(plugin, plugin.getShop(), plugin.getClientManager(), plugin.getDonationManager(), "Lobby Stuff", player, 27);

		_parkourManager = plugin.getHubManager().getParkourManager();

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int index = 10;

		for (ParkourData data : _parkourManager.getParkours())
		{
			ItemBuilder builder = new ItemBuilder(Material.GLASS)
					.setTitle(C.cYellowB + data.getName() + C.cGray + " - " + C.cAqua + "Parkour")
					.addLore(data.getDescription())
					.addLore("");

			String difficulty = "";

			switch (data.getDifficulty())
			{
				case ParkourManager.DIFFICULTY_EASY:
					builder.setType(Material.LEATHER_BOOTS);
					difficulty = C.cGreenB + "Easy";
					break;
				case ParkourManager.DIFFICULTY_MEDIUM:
					builder.setType(Material.IRON_BOOTS);
					difficulty = C.cGoldB + "Medium";
					break;
				case ParkourManager.DIFFICULTY_HARD:
					builder.setType(Material.DIAMOND_BOOTS);
					difficulty = C.cRedB + "Hard";
					break;
			}

			builder.addLore(C.cWhite + "Difficulty - " + difficulty);
			addButton(index++, builder.build(), (player, clickType) -> player.teleport(data.getTeleport()));
		}

		addButton(16, LOBBY_GAMES, (player, clickType) -> player.teleport(getPlugin().getTeleport()));
	}
}
