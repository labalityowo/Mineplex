package mineplex.core.common.currency;

import mineplex.core.common.util.C;
import org.bukkit.Material;

/**
 * A GlobalCurrency is one whose state is shared between all Mineplex servers.
 * GlobalCurrencies can be retrieved and modified through DonationManager/Donor in Mineplex.Core
 */
public class GlobalCurrency extends Currency {
	public static final GlobalCurrency TREASURE_SHARD = new GlobalCurrency("Treasure Shards", "Treasure Shard", C.cAqua, Material.PRISMARINE_SHARD);
	public static final GlobalCurrency GEM = new GlobalCurrency("Gems", "Gem", C.cGreen, Material.EMERALD);

	public GlobalCurrency(String plural, String singular, String color, Material displayMaterial) {
		super(plural, singular, color, displayMaterial);
	}
}
