package mineplex.core.gadget.gadgets.wineffect.rankrooms;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;

/**
 * Helper class to create win effects based on ranks
 */
public abstract class WinEffectRankBased extends WinEffectGadget
{
	private static final String RANK_SCHEMATIC_PREFIX = "WinRank";
	private final PermissionGroup _rank;

	public WinEffectRankBased(GadgetManager manager, String name, String[] lore, Material material, byte data, int cost, PermissionGroup rank, String... alternativeSalepackageNames)
	{
		super(manager, name, lore, cost, material, data, true, alternativeSalepackageNames);
		_rank = rank;
		_schematicName = RANK_SCHEMATIC_PREFIX + rank.name();
	}

	@Override
	public boolean ownsGadget(Player player)
	{
		return Manager.getClientManager().Get(player).inheritsFrom(_rank);
	}

	public PermissionGroup getRank()
	{
		return _rank;
	}
}
