package mineplex.minecraft.game.core.condition.conditions;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.ConditionManager;

public class UntrueCloak extends Condition
{

	public UntrueCloak(ConditionManager manager, String reason, LivingEntity ent,
					   LivingEntity source, ConditionType type, int mult, int ticks,
					   boolean add, Material visualType, byte visualData,
					   boolean showIndicator)
	{
		super(manager, reason, ent, source, type, mult, ticks, add, visualType,
				visualData, showIndicator, false);

		_informOn = "You are now invisible.";
		_informOff = "You are no longer invisible.";
	}

	@Override
	public boolean needsForceRemove()
	{
		return true;
	}

	@Override
	public void Add()
	{
		if (!(_ent instanceof Player))
		{
			return;
		}

		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(((CraftPlayer) _ent).getHandle());
		Location location = _ent.getLocation();
		packet.b = location.getBlockX() * 32;
		packet.c = location.getWorld().getMaxHeight() * 32;
		packet.d = location.getBlockZ() * 32;
		packet.g = false;

		for (Player player : _ent.getWorld().getPlayers())
		{
			if (player.equals(_ent))
			{
				continue;
			}

			UtilPlayer.sendPacket(player, packet);
		}
	}

	@Override
	public void Remove()
	{
		if (!(_ent instanceof Player))
		{
			return;
		}

		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(((CraftPlayer) _ent).getHandle());

		for (Player player : _ent.getWorld().getPlayers())
		{
			if (player.equals(_ent))
			{
				continue;
			}

			UtilPlayer.sendPacket(player, packet);
		}
	}
}