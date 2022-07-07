package mineplex.gemhunters.shop.deserialisers;

import mineplex.core.google.SheetObjectDeserialiser;
import mineplex.gemhunters.shop.VillagerProperties;

public class VillagerPropertiesDeserialiser implements SheetObjectDeserialiser<VillagerProperties>
{

	@Override
	public VillagerProperties deserialise(String[] values) throws ArrayIndexOutOfBoundsException
	{
		String name = values[0];
		String dataKey = values[1];

		boolean selling = values[2].equalsIgnoreCase("Selling");

		int spawnRate = Integer.parseInt(values[4]);
		int expireRate = Integer.parseInt(values[5]);
		
		int max = Integer.parseInt(values[7]);
		
		return new VillagerProperties(name, dataKey, selling, spawnRate, expireRate, max);
	}

}
