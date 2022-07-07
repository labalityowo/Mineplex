package mineplex.core.packethandler;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mineplex.spigot.IPacketVerifier;

import mineplex.core.common.util.UtilPlayer;
import net.minecraft.server.v1_8_R3.Packet;

public class PacketVerifier implements IPacketVerifier
{
	private Player _owner;
	private PacketHandler _packetHandler;

	public PacketVerifier(Player player, PacketHandler packetHandler)
	{
		_owner = player;
		_packetHandler = packetHandler;
	}

	public void bypassProcess(Packet packet)
	{
		((CraftPlayer) _owner).getHandle().playerConnection.networkManager.handle(packet);
	}

	public void Deactivate()
	{
		_owner = null;
	}

	public void process(Packet packet)
	{
		UtilPlayer.sendPacket(_owner, packet);
	}

	@Override
	public boolean handlePacket(Packet packet)
	{
		return _packetHandler.handlePacket(new PacketInfo(_owner, packet, this));
	}

	public Player getOwner()
	{
		return this._owner;
	}
}
