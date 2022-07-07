package nautilus.game.arcade.game.modules.compass.menu.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilMath;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageInventory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.modules.compass.CompassEntry;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.compass.menu.CompassMenu;
import nautilus.game.arcade.game.modules.compass.menu.button.CompassButton;

public class CompassPage extends
		ShopPageInventory<Game, CompassMenu>
{
	private CompassModule _compassModule;
	private IButton[] _buttons;
	private ItemStack[] _items;

	public CompassPage(CompassMenu menu, CompassModule compassModule, Player player)
	{
		super(compassModule.getGame(),
				menu,
				compassModule.getGame().getArcadeManager().GetClients(),
				compassModule.getGame().getArcadeManager().GetDonation(),
				"Spectator Menu",
				player);
		_compassModule = compassModule;
		buildPage();
	}

	@Override
	protected void buildItems()
	{
		_buttons = new IButton[54];
		_items = new ItemStack[54];

		List<GameTeam> teamList = new ArrayList<>(_compassModule.getGame().GetTeamList());
		List<CompassEntry> entries = _compassModule.stream().collect(Collectors.toList());

		if (teamList.size() == 1 && entries.size() < 28)
		{
			buildSingleTeam(teamList.get(0), entries);
		}
		else
		{
			buildMultipleTeams(teamList, entries);
		}
	}

	private void buildSingleTeam(GameTeam team, List<CompassEntry> entries)
	{
		Collections.sort(entries, (o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()));

		_buttons = new IButton[19 + entries.size()];
		_items = new ItemStack[_buttons.length];

		_items[13] = getTeamItem(team, entries.size());

		int slot = 19;

		for (CompassEntry other : entries)
		{
			addPlayerItem(slot, team, other);

			if ((slot + 2) % 9 == 0)
			{
				_buttons = Arrays.copyOf(_buttons, _buttons.length + 3);
				_items = Arrays.copyOf(_items, _items.length + 3);

				slot += 3;
			}
			else
			{
				slot++;
			}
		}
	}

	private void buildMultipleTeams(List<GameTeam> teamList, List<CompassEntry> entries)
	{
		Collections.sort(teamList, (o1, o2) ->
		{
			int returns = o1.getDisplayName().compareToIgnoreCase(
					o2.getDisplayName());

			if (returns == 0)
			{
				return Long.compare(o1.getCreatedTime(),
						o2.getCreatedTime());
			}

			return returns;
		});

		_buttons = new IButton[0];
		_items = new ItemStack[0];

		int currentRow = 0;

		for (GameTeam team : teamList)
		{
			List<CompassEntry> teamPlayers = entries.stream().filter(ent -> ent.getTeam() == team).collect(Collectors.toList());

			Collections.sort(teamPlayers, (o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()));

			int rowsNeeded = (int) Math.ceil(teamPlayers.size() / 8.0);

			_buttons = Arrays.copyOf(_buttons, _buttons.length + (rowsNeeded * 9));
			_items = Arrays.copyOf(_items, _items.length + (rowsNeeded * 9));

			for (int row = 0; row < rowsNeeded; row++)
			{
				int woolSlot = (row * 9) + (currentRow * 9);

				_items[woolSlot] = getTeamItem(team, teamPlayers.size());

				int playerIndex = row * 8;
				for (int i = 0; i < 8 && playerIndex < teamPlayers.size(); i++, playerIndex++)
				{
					CompassEntry other = teamPlayers.get(playerIndex);
					int slot = woolSlot + 1 + i;

					addPlayerItem(slot, team, other);
				}
			}

			// Add a line in between teams if the player count is low enough and
			// there are less than 4 teams
			if (rowsNeeded == 1 && teamList.size() < 4 && entries.size() <= 26)
			{
				currentRow += 2;

				_buttons = Arrays.copyOf(_buttons, _buttons.length + 9);
				_items = Arrays.copyOf(_items, _items.length + 9);
			}
			else
			{
				currentRow += rowsNeeded;
			}
		}
	}

	private void addPlayerItem(int slot, GameTeam team, CompassEntry other)
	{
		ItemStack playerItem = getPlayerItem(team, other);

		ShopItem shopItem = new ShopItem(playerItem, other.getDisplayName(),
				other.getDisplayName(), 1, false, false);

		_items[slot] = shopItem;
		_buttons[slot] = new CompassButton(_compassModule.getGame().getArcadeManager(), getPlayer(), other);
	}

	private ItemStack getTeamItem(GameTeam team, int playerCount)
	{
		ItemStack item = new ItemStack(Material.WOOL, 1, (short) 0,
				UtilColor.chatColorToWoolData(team.GetColor()));

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(team.GetFormattedName());
		meta.setLore(Arrays.asList(" ", ChatColor.RESET + C.cYellow
				+ "Players Alive: " + C.cWhite + playerCount));
		item.setItemMeta(meta);

		return item;
	}

	private ItemStack getPlayerItem(GameTeam team, CompassEntry other)
	{
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

		double distance = UtilMath.offset(getPlayer(), other.getEntity());
		double heightDifference = other.getEntity().getLocation().getY()
				- getPlayer().getLocation().getY();

		ArrayList<String> lore = new ArrayList<String>();
		lore.add(" ");
		lore.add(ChatColor.RESET + C.cYellow + "Kit: " + C.cWhite
				+ other.getKit().GetName());
		lore.add(ChatColor.RESET + C.cYellow + "Distance: " + C.cWhite
				+ UtilMath.trim(1, distance));
		lore.add(ChatColor.RESET + C.cYellow + "Height Difference: " + C.cWhite
				+ UtilMath.trim(1, heightDifference));
		lore.add(" ");
		lore.add(ChatColor.YELLOW + "Left Click" + ChatColor.RESET + " Teleport");
		lore.add(ChatColor.YELLOW + "Right Click" + ChatColor.RESET + " Spectate");
		SkullMeta skullMeta = ((SkullMeta) item.getItemMeta());
		skullMeta.setOwner(other.getName());
		skullMeta.setDisplayName(team.GetColor() + other.getDisplayName());
		skullMeta.setLore(lore);
		item.setItemMeta(skullMeta);

		return item;
	}

	@Override
	protected IButton[] getButtons()
	{
		return _buttons;
	}

	@Override
	protected ItemStack[] getItems()
	{
		return _items;
	}

}