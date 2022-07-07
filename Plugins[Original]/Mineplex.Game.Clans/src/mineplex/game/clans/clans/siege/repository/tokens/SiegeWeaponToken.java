package mineplex.game.clans.clans.siege.repository.tokens;

import org.bukkit.Location;

import mineplex.game.clans.clans.ClanInfo;

public class SiegeWeaponToken
{
	public int UniqueId;
	public ClanInfo OwnerClan;
	public byte WeaponType;
	public Location Location;
	public int Health;
	public int Yaw;
	public long LastFired;	
}