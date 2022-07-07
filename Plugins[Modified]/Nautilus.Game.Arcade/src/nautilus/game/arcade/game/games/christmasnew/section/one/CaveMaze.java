package nautilus.game.arcade.game.games.christmasnew.section.one;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;

public class CaveMaze extends SectionChallenge
{

	private static final int STARTING_MOBS = 12;
	private static final int MAX_MOBS = 25;
	private static final int HEALTH = 5;
	private static final ItemStack[] IN_HAND =
			{
					new ItemStack(Material.WOOD_SWORD),
					new ItemStack(Material.STONE_SWORD),
					new ItemStack(Material.STONE_PICKAXE),
					new ItemStack(Material.IRON_PICKAXE)
			};

	private final List<Location> _mobSpawns;
	private final List<Location> _quickOutAir;

	CaveMaze(ChristmasNew host, Location present, Section section)
	{
		super(host, present, section);

		_mobSpawns = _worldData.GetDataLocs("BROWN");
		_quickOutAir = _worldData.GetCustomLocs(String.valueOf(Material.SOUL_SAND.getId()));

		_quickOutAir.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.IRON_FENCE));
	}

	@Override
	public void onPresentCollect()
	{
		_quickOutAir.forEach(location ->
		{
			if (Math.random() > 0.95)
			{
				location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.IRON_FENCE);
			}

			MapUtil.QuickChangeBlockAt(location, Material.AIR);
		});
	}

	@Override
	public void onRegister()
	{
		for (int i = 0; i < STARTING_MOBS; i++)
		{
			spawn();
		}
	}

	@Override
	public void onUnregister()
	{

	}

	@EventHandler
	public void updateMobSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _entities.size() >= MAX_MOBS)
		{
			return;
		}

		spawn();
	}

	private void spawn()
	{
		Skeleton skeleton = spawn(UtilAlg.Random(_mobSpawns), Skeleton.class);

		skeleton.setHealth(HEALTH);
		skeleton.setMaxHealth(HEALTH);
		skeleton.getEquipment().setItemInHand(UtilMath.randomElement(IN_HAND));
		skeleton.setRemoveWhenFarAway(false);
	}
}
