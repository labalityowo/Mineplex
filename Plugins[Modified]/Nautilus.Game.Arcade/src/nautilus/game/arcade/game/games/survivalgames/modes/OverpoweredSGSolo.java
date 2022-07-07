package nautilus.game.arcade.game.games.survivalgames.modes;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.survivalgames.SurvivalGamesNewSolo;
import nautilus.game.arcade.game.games.survivalgames.modules.TrackingCompassModule;
import nautilus.game.arcade.game.modules.chest.ChestLootModule;

public class OverpoweredSGSolo extends SurvivalGamesNewSolo
{

	private final OverpoweredSGModule _opModule;

	public OverpoweredSGSolo(ArcadeManager manager)
	{
		super(manager);

		_opModule = new OverpoweredSGModule();
		_opModule.register(this);
	}

	@Override
	protected void setupTier1Loot(ChestLootModule lootModule, TrackingCompassModule compassModule, ItemStack tnt, List<Location> chests)
	{
		_opModule.setupTier1Loot(lootModule, compassModule, tnt, chests);
	}

	@Override
	protected void setupTier2Loot(ChestLootModule lootModule, TrackingCompassModule compassModule, ItemStack tnt, List<Location> chests)
	{
		_opModule.setupTier2Loot(lootModule, compassModule, tnt, chests);
	}

	@Override
	public void setupSupplyDropLoot(Map<Integer, List<ItemStack>> items)
	{
		_opModule.setupSupplyDropLoot(items);
	}

	@Override
	public boolean isAllowingGameStats()
	{
		return false;
	}

	@Override
	public String GetMode()
	{
		return "Overpowered";
	}
}
