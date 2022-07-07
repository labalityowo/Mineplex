package nautilus.game.arcade.kit.perks.data;

public class BlockTossData 
{
	public int Type;
	public byte Data;
	public long Time;
	
	public BlockTossData(int type, byte data, long time)
	{
		Type = type;
		Data = data;
		Time = time;
	}
}
