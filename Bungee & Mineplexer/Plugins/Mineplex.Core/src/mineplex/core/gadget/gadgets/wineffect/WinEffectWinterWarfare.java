package mineplex.core.gadget.gadgets.wineffect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.SchematicData;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectWinterWarfare extends WinEffectGadget
{

	private Schematic _sleigh;
	private Location _sleighLocation;
	private DisguisePlayer _npc;
	private Entity _entity;
	private SchematicData _data;
	private List<ArmorStand> _playersLeft;

	public WinEffectWinterWarfare(GadgetManager manager)
	{
		super(manager, "Winter Warfare", UtilText.splitLinesToArray(new String[]{C.cGray +
				"Santa isn't only packing coal for the bad girls and boys this year!"}, LineFormat.LORE), -16, Material.TNT, (byte) 0);
		_schematicName = "WinterWarfare";
		try
		{
			_sleigh = UtilSchematic.loadSchematic(new File("../../update/schematic/WinterWarfareSleigh.schematic"));
		} catch (IOException e)
		{
			_sleigh = null;
			e.printStackTrace();
		}
	}

	@Override
	public void play()
	{
		_sleighLocation = getBaseLocation().clone().subtract(0, -2, 0);
		pasteSleigh();
		Location npcLocation = _sleighLocation.clone().add(44, 11, 4);
		npcLocation.setYaw(90);
		npcLocation.setPitch(0);
		_npc = getNPC(_player, npcLocation);
		List<Location> circle = UtilShapes.getPointsInCircle(getBaseLocation().clone().subtract(0, 2, 0), _other.size(), 3);

		_playersLeft = new ArrayList<>();

		for(int i = 0; i < _other.size(); i++)
		{
			_playersLeft.add((ArmorStand) getNPC(_other.get(i), circle.get(i)).getEntity().getBukkitEntity());
		}
	}

	@Override
	public void finish()
	{
		Manager.getDisguiseManager().undisguise(_npc);
		_npc = null;
	}

	@Override
	public void teleport()
	{
		Location loc = getBaseLocation().clone().subtract(0, -5, 15);
		loc.setDirection(getBaseLocation().subtract(loc).toVector());
		super.teleport(loc);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (!isRunning())
			return;

		if (_sleighLocation == null)
			return;

		if (event.getType() == UpdateType.FAST)
		{
			if (_npc == null)
				return;

			TNTPrimed tnt = _sleighLocation.getWorld().spawn(_npc.getEntity().getBukkitEntity().getLocation().add(0, 3, 0), TNTPrimed.class);
			int r = UtilMath.random.nextInt(50);
			Vector vel = new Vector(Math.sin(r * 9/5d), 0, Math.cos(r * 9/5d));
			UtilAction.velocity(tnt, vel, Math.abs(Math.sin(r * 12/3000d)), false, 0, 0.2 + Math.abs(Math.cos(r * 12/3000d))*0.6, 1, false);
		}

		if (event.getType() != UpdateType.TICK)
			return;

		// Teleports sleigh
		_sleighLocation = _sleighLocation.subtract(1, 0, 0);
		pasteSleigh();

		if (_npc == null)
			return;

		Location npcLocation = _sleighLocation.clone().add(44, 11, 4);
		npcLocation.setYaw(90);
		npcLocation.setPitch(0);
		_npc.getEntity().getBukkitEntity().teleport(npcLocation);
	}

	private void pasteSleigh()
	{
		if (_sleigh != null)
		{
			_data = _sleigh.paste(_sleighLocation);
		}
	}

	@EventHandler
	public void onTNTExplode(EntityExplodeEvent event)
	{
		if (!isRunning())
			return;

		if (_playersLeft == null)
			return;

		if (_playersLeft.size() < 1)
			return;

		if (!(event.getEntity() instanceof TNTPrimed))
			return;

		boolean removePlayers = false;

		for(ArmorStand armorStand : _playersLeft)
		{
			if (armorStand.getLocation().distanceSquared(event.getEntity().getLocation()) <= 25)
			{
				removePlayers = true;
				break;
			}
		}

		if (!removePlayers)
			return;

		for(ArmorStand player : _playersLeft)
		{
			player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HIT, 2, 1);
			player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_DEATH, 1, 1);
			player.remove();
		}

		_playersLeft.clear();
	}

}
