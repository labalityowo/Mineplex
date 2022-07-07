package mineplex.core.gadget.gadgets.gamemodifiers.moba.emblems;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.SchematicData;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.C;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.gadget.util.CostConstants;

public class EmblemGadget extends GameModifierGadget
{

	private static final String DIRECTORY = ".." + File.separator + ".." + File.separator + "update" + File.separator + "schematic" + File.separator + "moba-emblems";
	private static final Map<String, Schematic> SCHEMATICS = new HashMap<>();

	static
	{
		loadSchematics();
	}

	private static void loadSchematics()
	{
		File directory = new File(DIRECTORY);

		if (!directory.exists())
		{
			return;
		}

		File[] files = directory.listFiles();

		if (files == null)
		{
			return;
		}

		for (File file : files)
		{
			try
			{
				Schematic schematic = UtilSchematic.loadSchematic(file);
				int spongeId = Material.SPONGE.getId();

				// Remove all sponge blocks
				for (int i = 0; i < schematic.getBlocks().length; i++)
				{
					short blockId = schematic.getBlocks()[i];

					if (blockId == spongeId)
					{
						schematic.getBlocks()[i] = 0;
					}
				}

				SCHEMATICS.put(file.getName(), schematic);
			}
			catch (IOException e)
			{
			}
		}
	}

	private final Schematic _schematic;

	public EmblemGadget(GadgetManager manager, GameCosmeticCategory category, EmblemType type)
	{
		this(manager, category, type, CostConstants.FOUND_IN_MOBA_CHESTS);
	}

	public EmblemGadget(GadgetManager manager, GameCosmeticCategory category, EmblemType type, int cost)
	{
		super(manager, category, type.getName(), new String[] {
				C.cGray + "Changed your displayed Emblem",
				C.cGray + "to " + type.getName() + ".",
		}, cost, type.getMaterial(), type.getMaterialData());

		_schematic = SCHEMATICS.get(type.getSchematic() + ".schematic");
	}

	public SchematicData buildAt(Location location)
	{
		return _schematic.paste(location.clone().add(0, _schematic.getHeight() + 2, 0), true, true);
	}
}
