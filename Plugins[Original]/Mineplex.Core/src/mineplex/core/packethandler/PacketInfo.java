package mineplex.core.packethandler;


import org.bukkit.entity.Player;
import net.minecraft.server.v1_8_R3.Packet;

public class PacketInfo
{
    private Player _player;
    private Packet _packet;
    private PacketVerifier _verifier;
    
    private boolean _cancelled = false;
    
    public PacketInfo(Player player, Packet packet, PacketVerifier verifier) 
    {
    	_player = player;
    	_packet = packet;
    	_verifier = verifier;
    }

	public Packet getPacket() 
	{
		return _packet;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public PacketVerifier getVerifier()
	{
		return _verifier;
	}
	
	public void setCancelled(boolean cancel)
	{
		_cancelled = cancel;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
}
