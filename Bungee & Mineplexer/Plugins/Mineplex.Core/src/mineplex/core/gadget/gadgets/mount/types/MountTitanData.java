package mineplex.core.gadget.gadgets.mount.types;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.gadget.gadgets.mount.MountData;

public class MountTitanData extends MountData
{

	private Slime _head;

	private List<ArmorStand> _nodes;

	MountTitanData(Player player, String name)
	{
		super(player);

		Location loc = player.getLocation();
		loc.setPitch(0);
		loc.setYaw(0);

		//Nodes
		_nodes = new ArrayList<>();

		for (int i = 0; i < 20; i++)
		{
			ArmorStand node = loc.getWorld().spawn(loc, ArmorStand.class);

			node.setVisible(false);
			node.setGravity(false);
			node.setGhost(true);

			node.setHelmet(new ItemStack(Material.NETHERRACK));

			UtilEnt.setFakeHead(node, true);

			_nodes.add(node);
		}

		//Head
		_head = player.getWorld().spawn(loc, MagmaCube.class);
		_head.setSize(2);
		_head.setCustomName(player.getName() + "'s " + name);
		_head.setCustomNameVisible(true);
		UtilEnt.ghost(_head, true, false);
	}

	public void update()
	{
		//Head
		if (_head.getPassenger() != null)
		{
			Vector dir = _head.getPassenger().getLocation().getDirection().multiply(0.8).add(new Vector(0, 0.2, 0));
			_head.setVelocity(dir);
			UtilEnt.CreatureLook(_head, dir);
		}

		Location inFront = _head.getLocation().add(0, -1.5, 0);

		//Move
		for (int i = 0; i < 20; i++)
		{
			ArmorStand node = _nodes.get(i);

			Location old = node.getLocation();

			inFront.setPitch(node.getLocation().getPitch());
			inFront.setYaw(node.getLocation().getYaw());

			//Move
			if (i == 0)
				node.teleport(inFront);
			else if (UtilMath.offset(node.getLocation(), inFront) > 0.6)
				node.teleport(inFront.add(UtilAlg.getTrajectory(inFront, node.getLocation()).multiply(0.6)));

			//Rotation
			Vector vector = UtilAlg.getTrajectory(old, node.getLocation());

			node.setHeadPose(new EulerAngle(
					Math.toRadians(UtilAlg.GetPitch(vector)),
					Math.toRadians(UtilAlg.GetYaw(vector)),
					0));
			node.setFireTicks(0);

			inFront = node.getLocation();
		}


		//Shuffle In
		if (_head.getPassenger() == null)
		{
			for (int i = _nodes.size() - 1; i >= 0; i--)
			{
				ArmorStand node = _nodes.get(i);

				if (i > 0)
					inFront = _nodes.get(i - 1).getLocation();
				else
					inFront = _head.getLocation().add(0, -1.5, 0);

				inFront.setPitch(node.getLocation().getPitch());
				inFront.setYaw(node.getLocation().getYaw());

				node.teleport(inFront);
			}
		}
	}

	public Slime getHead()
	{
		return _head;
	}

	@Override
	public List<Entity> getEntityParts()
	{
		List<Entity> entities = new ArrayList<>();
		entities.add(_head);
		entities.addAll(_nodes);
		return entities;
	}
}
