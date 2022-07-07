package mineplex.game.clans.clans.siege.repository.tokens;

import org.bukkit.Location;

import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.siege.outpost.OutpostState;
import mineplex.game.clans.clans.siege.outpost.OutpostType;

public class OutpostToken
{
	public int UniqueId;
	public Location Origin;
	public OutpostType Type;
	public ClanInfo OwnerClan;
	public long TimeSpawned;
	public OutpostState OutpostState;
}
