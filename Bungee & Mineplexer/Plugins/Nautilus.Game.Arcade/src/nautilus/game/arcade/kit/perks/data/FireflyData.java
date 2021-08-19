package nautilus.game.arcade.kit.perks.data;

public class FireflyData
{
	public org.bukkit.entity.Player Player;
	public org.bukkit.Location Location;
	public long Time;
	
	public FireflyData(org.bukkit.entity.Player player)
	{
		Player = player;
		Location = player.getLocation();
		Time = System.currentTimeMillis();
	}
}
