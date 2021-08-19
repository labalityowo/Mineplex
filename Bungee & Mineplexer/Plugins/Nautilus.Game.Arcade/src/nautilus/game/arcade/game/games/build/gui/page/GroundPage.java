package nautilus.game.arcade.game.games.build.gui.page;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.games.build.Build;
import nautilus.game.arcade.game.games.build.BuildData;
import nautilus.game.arcade.game.games.build.GroundData;
import nautilus.game.arcade.game.games.build.gui.OptionsShop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroundPage extends ShopPageBase<ArcadeManager, OptionsShop>
{
	private List<GroundData> _grounds = new ArrayList<>(Arrays.asList(new GroundData[]{
			new GroundData(Material.STONE), new GroundData(Material.GRASS), new GroundData(Material.DIRT),
			new GroundData(Material.SAND), new GroundData(Material.WATER_BUCKET), new GroundData(Material.LAVA_BUCKET),
			new GroundData(Material.WOOD), new GroundData(Material.COBBLESTONE), new GroundData(Material.NETHERRACK),
			new GroundData(Material.SMOOTH_BRICK), new GroundData(Material.ENDER_STONE), new GroundData(Material.MYCEL),
			new GroundData(Material.STAINED_CLAY, (byte) 0), new GroundData(Material.STAINED_CLAY, (byte) 15),
			new GroundData(Material.STAINED_CLAY, (byte) 4), new GroundData(Material.STAINED_CLAY, (byte) 3),
			new GroundData(Material.STAINED_CLAY, (byte) 5), new GroundData(Material.STAINED_CLAY, (byte) 6),
			new GroundData(Material.QUARTZ_BLOCK), new GroundData(Material.PACKED_ICE), new GroundData(Material.IRON_BLOCK),
			new GroundData(Material.GOLD_BLOCK), new GroundData(Material.DIAMOND_BLOCK)}));

	private Build _game;

	public GroundPage(Build game, ArcadeManager plugin, OptionsShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Change Ground", player, 9 * 4);
		_game = game;
		
		File schematicDirectory = new File(".." + File.separatorChar + ".." + File.separatorChar + "update" + File.separatorChar + "schematic");
		
		if(_game.GetType() == GameType.BuildMavericks)
		{
			try
			{
				GroundData basketFloor = new GroundData(Material.SLIME_BALL, (byte) 0, "Basketball Floor", UtilSchematic.loadSchematic(new File(schematicDirectory, "basketball_floor.schematic")));
				_grounds.add(basketFloor);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		final BuildData buildData = _game.getBuildData(getPlayer());

		if (buildData == null)
		{
			getPlayer().closeInventory();
			return;
		}

		int index = 0;
		for (final GroundData data : _grounds)
		{
			String name = data.getName();
			if(name == null) name = ItemStackFactory.Instance.GetName(data.getMaterial(), data.getData(), true);
			
			ShopItem shopItem = new ShopItem(data.getMaterial(), data.getData(), name, null, 0, false, false);
			
			addButton(index, shopItem, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					buildData.setGround(player, data);
				}
			});
			index++;
		}

		addButton((9 * 3) + 4, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getShop().openPageForPlayer(player, new OptionsPage(_game, getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
			}
		});
	}
}
