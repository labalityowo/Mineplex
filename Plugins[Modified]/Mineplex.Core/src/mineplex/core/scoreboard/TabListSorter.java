package mineplex.core.scoreboard;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;

import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;

/**
 * Sorts the tab list by rank weight (or some other arbitrary metric if you want)
 * @author Dan
 */
public class TabListSorter extends MiniPlugin implements IPacketHandler
{
	public enum Perm implements Permission
	{
		RANDOM_TAB_LIST
	}

	private static final Map<PermissionGroup, Integer> WEIGHT;

	static
	{
		WEIGHT = new HashMap<>();
		for (PermissionGroup group : PermissionGroup.values())
		{
			if (group.canBePrimary())
			{
				WEIGHT.put(group, group.ordinal());
			}
		}
	}

	private PacketHandler _packetHandler;
	private boolean _randomized = false;

	public TabListSorter()
	{
		super("TabListSorter");

		generatePermissions();

		// /randomtab - straightforward toggle
		addCommand(new CommandBase<TabListSorter>(this, Perm.RANDOM_TAB_LIST, "randomtab")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				_randomized = !_randomized;
			}
		});
	}

	private void generatePermissions()
	{
		PermissionGroup.DEV.setPermission(Perm.RANDOM_TAB_LIST, true, true);
	}

	@Override
	public void enable()
	{
		_packetHandler = require(PacketHandler.class);
		_packetHandler.addPacketHandler(this, PacketPlayOutPlayerInfo.class);
	}

	@Override
	public void disable()
	{
		_packetHandler.removePacketHandler(this);
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.getPacket() instanceof PacketPlayOutPlayerInfo)
		{
			PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) packetInfo.getPacket();

			if (_randomized)
			{
				Collections.shuffle(packet.b);
			} else
			{
				packet.b.sort(Comparator.comparingInt(info -> WEIGHT.getOrDefault(info, 0)));
			}
		}
	}
}
