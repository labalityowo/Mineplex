package mineplex.game.clans.tutorial.tutorials.clans.objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.npc.Npc;
import mineplex.core.npc.NpcManager;
import mineplex.database.tables.records.NpcsRecord;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.OrderedObjective;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.HoldItemGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.shops.GoToShopsGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.shops.SellPotatoesGoal;

public class ShopsObjective extends OrderedObjective<ClansMainTutorial>
{
	private HashMap<UUID, List<Npc>> _npcMap;
	private NpcManager _npcManager;

	public ShopsObjective(ClansMainTutorial clansMainTutorial, NpcManager npcManager, JavaPlugin javaPlugin)
	{
		super(clansMainTutorial, javaPlugin, "Shops Tutorial", "Learn your way around our shops");

		_npcMap = new HashMap<>();
		_npcManager = npcManager;

		addGoal(new HoldItemGoal(
				this,
				Material.MAP,
				"Identify Shops on Map",
				"Find the Blue Striped Area on your map",
				"Shops are marked on the map by the " + C.cDAqua + "Blue Stripes" + C.mBody + ".",
				60L
		));
		addGoal(new GoToShopsGoal(this));
		addGoal(new SellPotatoesGoal(this));

		setStartMessageDelay(60);
	}

	@Override
	public void clean(Player player, TutorialRegion region)
	{
		super.clean(player, region);

		if (_npcMap.containsKey(player.getUniqueId()))
		{
			_npcMap.get(player.getUniqueId()).forEach(npc -> _npcManager.removeFakeNpc(npc));
		}
	}

	@Override
	protected void customLeave(Player player)
	{
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@Override
	protected void customStart(Player player)
	{
		super.customStart(player);

		TutorialRegion region = getPlugin().getRegion(player);


		ArrayList<Npc> npcs = new ArrayList<>();
		// Spawn NPC's
		Location pvpGear = getPlugin().getPoint(region, ClansMainTutorial.Point.PVP_SHOP);
		Location energyShop = getPlugin().getPoint(region, ClansMainTutorial.Point.ENERGY_SHOP);
		Location farmingShop = getPlugin().getPoint(region, ClansMainTutorial.Point.FARMING_SHOP);
		Location miningShop = getPlugin().getPoint(region, ClansMainTutorial.Point.MINING_SHOP);
		npcs.add(spawnNpc(pvpGear, "Pvp Gear"));
		npcs.add(spawnNpc(energyShop, "Energy Shop"));
		npcs.add(spawnNpc(farmingShop, "Organic Produce"));
		npcs.add(spawnNpc(miningShop, "Mining Shop"));
		_npcMap.put(player.getUniqueId(), npcs);

		TutorialSession session = getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getPlugin().getCenter(session.getRegion(), ClansMainTutorial.Bounds.SHOPS));

	}

	private Npc spawnNpc(Location location, String name)
	{
		NpcsRecord npcsRecord = new NpcsRecord();
		npcsRecord.setServer(_npcManager.getServerName());
		npcsRecord.setName(name);
		npcsRecord.setWorld(location.getWorld().getName());
		npcsRecord.setX(location.getX());
		npcsRecord.setY(location.getY());
		npcsRecord.setZ(location.getZ());
		npcsRecord.setRadius(0D);
		npcsRecord.setEntityType(EntityType.VILLAGER.name());
		npcsRecord.setAdult(true);

		Npc npc = new Npc(_npcManager, npcsRecord);
		_npcManager.spawnNpc(npc);
		_npcManager.addFakeNpc(npc);
		return npc;
	}
}
