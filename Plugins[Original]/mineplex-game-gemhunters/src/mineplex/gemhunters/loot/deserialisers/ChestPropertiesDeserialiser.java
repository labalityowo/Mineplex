package mineplex.gemhunters.loot.deserialisers;

import org.bukkit.Material;

import mineplex.core.google.SheetObjectDeserialiser;
import mineplex.gemhunters.loot.ChestProperties;

public class ChestPropertiesDeserialiser implements SheetObjectDeserialiser<ChestProperties>
{

	@Override
	public ChestProperties deserialise(String[] values) throws ArrayIndexOutOfBoundsException
	{
		String name = values[0];
		Material blockMaterial = Material.valueOf(values[1]);
		String dataKey = values[2];

		int minAmount = 1;
		int maxAmount = 1;

		String[] numbers = values[3].split("-");

		if (numbers.length != 2)
		{
			minAmount = Integer.parseInt(String.valueOf(values[3]));
			maxAmount = minAmount;
		}
		else
		{
			minAmount = Integer.parseInt(numbers[0]);
			maxAmount = Integer.parseInt(numbers[1]);
		}

		int spawnRate = Integer.parseInt(values[4]);
		int expireRate = Integer.parseInt(values[5]);
		int maxChestsPerLoc = Integer.parseInt(values[6]);
		int spawnRadius = Integer.parseInt(values[7]);
		int maxActive = Integer.parseInt(values[8]);

		return new ChestProperties(name, blockMaterial, dataKey, minAmount, maxAmount, maxChestsPerLoc, spawnRate, expireRate, spawnRadius, maxActive);
	}

}
