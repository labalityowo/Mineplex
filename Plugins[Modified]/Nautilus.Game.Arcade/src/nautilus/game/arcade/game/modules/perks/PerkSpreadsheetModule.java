package nautilus.game.arcade.game.modules.perks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mineplex.core.command.CommandBase;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.Pair;
import mineplex.core.common.util.F;
import mineplex.core.google.GoogleSheetsManager;
import mineplex.core.google.SheetObjectDeserialiser;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class PerkSpreadsheetModule extends Module
{

	private static final SheetObjectDeserialiser<Pair<String, String>> DESERIALISER = values -> Pair.create(values[0], values.length == 1 ? "" : values[1]);
	private static final Map<String, String> VALUE_OVERRIDE_MAP = new HashMap<>();
	static Map<String, String> getValueOverrideMap()
	{
		return VALUE_OVERRIDE_MAP;
	}

	private final String _fileName;
	private final Map<String, String> _dataMap;
	private boolean _initialised;

	private CommandBase<ArcadeManager> _valueCommand;
	private CommandBase<ArcadeManager> _loadCommand;

	public PerkSpreadsheetModule(String fileName)
	{
		_fileName = fileName;
		_dataMap = new HashMap<>();
	}

	@Override
	public void setup()
	{
		if (!_initialised)
		{
			_initialised = true;

			_valueCommand = new PerkOverrideValueCommand(getGame().getArcadeManager(), this);
			_loadCommand = new PerkOverrideLoadCommand(getGame().getArcadeManager(), this);
			getGame().getArcadeManager().addCommand(_valueCommand);
			getGame().getArcadeManager().addCommand(_loadCommand);
		}

		// File Name must not be null
		Objects.requireNonNull(_fileName);

		// Make sure this is clear
		_dataMap.clear();

		GoogleSheetsManager manager = getGame().getArcadeManager().getSheetsManager();
		Map<String, List<List<String>>> map = manager.getSheetData(_fileName);

		for (Map.Entry<String, List<List<String>>> entry : map.entrySet())
		{
			String key = entry.getKey();
			List<List<String>> value = entry.getValue();

			Kit kit = getFromName(key);

			if (kit == null)
			{
				continue;
			}

			Perk currentPerk = null;

			for (List<String> rows : value)
			{
				try
				{
					Pair<String, String> pair = DESERIALISER.deserialise(rows.toArray(new String[0]));

					Perk perk = getFromName(kit, pair.getLeft());

					if (perk != null)
					{
						currentPerk = perk;
						continue;
					}

					if (currentPerk != null)
					{
						String id = getKey(kit, currentPerk, pair.getLeft());
						_dataMap.put(id, pair.getRight());
					}
				}
				catch (Exception e)
				{
					// Continue the loop
				}
			}
		}

		for (Kit kit : getGame().GetKits())
		{
			for (Perk perk : kit.GetPerks())
			{
				try
				{
					perk.setSpreadsheet(this);
				}
				catch (Exception e)
				{
					getGame().Announce(F.main("Game", "An error occurred at Kit " + F.name(kit.GetName()) + ", Perk " + F.name(perk.GetName()) + "."), false);
					return;
				}
			}
		}
	}

	@Override
	public void cleanup()
	{
		_dataMap.clear();
		CommandCenter.Instance.removeCommand(_valueCommand);
		CommandCenter.Instance.removeCommand(_loadCommand);
	}

	private Kit getFromName(String name)
	{
		for (Kit kit : getGame().GetKits())
		{
			if (kit.GetName().equalsIgnoreCase(name))
			{
				return kit;
			}
		}

		return null;
	}

	private Perk getFromName(Kit kit, String name)
	{
		for (Perk perk : kit.GetPerks())
		{
			if (perk.GetName().equalsIgnoreCase(name))
			{
				return perk;
			}
		}

		return null;
	}

	public String getKey(Kit kit, Perk perk, String value)
	{
		return kit.GetName() + "." + perk.GetName() + "." + value;
	}

	public String getValue(String key)
	{
		String overrideValue = VALUE_OVERRIDE_MAP.get(key);

		if (overrideValue != null)
		{
			return overrideValue;
		}

		return _dataMap.get(key);
	}

	Map<String, String> getDataMap()
	{
		return _dataMap;
	}

}
