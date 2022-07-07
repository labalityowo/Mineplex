package mineplex.gemhunters.loot.deserialisers;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import mineplex.core.google.SheetObjectDeserialiser;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.gemhunters.loot.LootItem;
import net.md_5.bungee.api.ChatColor;

/**
 * This is a {@link LootItem} deserialiser for Google Sheet interpretation.<br>
 * <br>
 * Arguments should follow the form:<br>
 * <ul>
 * <li>Material</li>
 * <li>Material Data</li>
 * <li>Max Durability</li>
 * <li>Amount</li>
 * <li>Item Name <i>(optional)</i></li>
 * <li>Item Lore <i>(optional) each line separated by colons</i></li>
 * <li>Enchantments <i>(optional) Has a NAME:LEVEL format with multiple
 * enchantments being separated by commas</i></li>
 * <li>Probability</li>
 * <li>Metadata <i>(optional)</i></li>
 * </ul>
 * Thus derserialise is guaranteed to have at least 8 strings passed in.<br>
 * If an illegal argument is passed in, derserialise will throw an exception,
 * these should be handled by the caller.
 * 
 * @see SheetObjectDeserialiser
 */
public class LootItemDeserialiser implements SheetObjectDeserialiser<LootItem>
{

	@Override
	public LootItem deserialise(String[] values) throws ArrayIndexOutOfBoundsException, IllegalArgumentException, NumberFormatException
	{
		Material material = Material.valueOf(values[0]);
		byte data = values[1].equals("") ? 0 : Byte.parseByte(values[1]);
		int minAmount = 1;
		int maxAmount = 1;
		short durability = values[2].equals("") ? 0 : Short.valueOf(values[2]);

		String[] numbers = values[3].split("-");

		if (numbers.length != 2)
		{
			minAmount = Integer.parseInt(values[3].equals("") ? "1" : values[3]);
			maxAmount = minAmount;
		}
		else
		{
			minAmount = Integer.parseInt(numbers[0]);
			maxAmount = Integer.parseInt(numbers[1]);
		}

		ItemBuilder builder = new ItemBuilder(material, data);

		builder.setDurability(durability);

		String title = ChatColor.translateAlternateColorCodes('&', values[4]);

		builder.setTitle(title);

		if (!values[5].equals(""))
		{
			String[] lore = values[5].split(":");
			String[] colouredLore = new String[lore.length];

			int loreIndex = 0;
			for (String line : lore)
			{
				colouredLore[loreIndex++] = ChatColor.translateAlternateColorCodes('&', line);
			}

			builder.setLore(colouredLore);
		}

		String[] enchants = String.valueOf(values[6]).split(",");

		for (String enchant : enchants)
		{
			String[] enchantData = enchant.split(":");

			if (enchantData.length < 2)
			{
				continue;
			}

			builder.addEnchantment(Enchantment.getByName(enchantData[0]), Integer.parseInt(enchantData[1]));
		}

		double proability = Double.parseDouble(values[7]);
		String metadata = values.length > 8 ? values[8] : null;

		return new LootItem(builder.build(), minAmount, maxAmount, proability, metadata);
	}

}
