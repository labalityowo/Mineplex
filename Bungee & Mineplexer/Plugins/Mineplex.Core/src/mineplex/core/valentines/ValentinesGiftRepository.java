package mineplex.core.valentines;

import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import org.bukkit.plugin.java.JavaPlugin;

public class ValentinesGiftRepository extends RepositoryBase
{
	private String GIVE_GIFT = "INSERT INTO accountValentinesGift (senderId, targetId) VALUES (?, ?);";

	public ValentinesGiftRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}


	public boolean giveGift(int senderId, int targetId)
	{
		return executeUpdate(GIVE_GIFT, new ColumnInt("senderId", senderId), new ColumnInt("targetId", targetId)) == 1;
	}
}
