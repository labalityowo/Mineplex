package mineplex.game.clans.clans.tntgenerator;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;

public class TntGeneratorManager extends MiniPlugin
{
	public static final int SECONDS_PER_TNT = 60 * 60 * 12; // 12 Hours
	public static final int MAX_GENERATOR_STOCK = 3;

	private ClansManager _clansManager;

	public TntGeneratorManager(JavaPlugin plugin, ClansManager clansManager)
	{
		super("Tnt Generator", plugin);

		_clansManager = clansManager;
	}

	@EventHandler
	public void updateGenerators(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (ClanInfo clanInfo : _clansManager.getClanMap().values())
		{
			TntGenerator generator = clanInfo.getGenerator();
			if (generator != null)
			{
				if (generator.getStock() >= MAX_GENERATOR_STOCK)
				{
					generator.setTicks(0);
				}
				else
				{
					if (generator.getTicks() >= SECONDS_PER_TNT)
					{
						_clansManager.messageClan(clanInfo, F.main("Clans", "Your " + F.elem("TNT Generator") + " in the " + F.elem("PvP Shop") + " has a new TNT available"));
						generator.setStock(generator.getStock() + 1);
						generator.setTicks(0);
						_clansManager.getClanDataAccess().updateGenerator(clanInfo, null);
					}

					generator.incrementTicks();
				}
			}
		}
	}
}
