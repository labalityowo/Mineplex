package mineplex.core.botspam.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import mineplex.core.database.MinecraftRepository;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.serverdata.database.DBPool;
import mineplex.core.botspam.SpamText;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class BotSpamRepository extends RepositoryBase
{
	private static final String GET_SPAM_TEXT = "SELECT * FROM botSpam";
	private static final String ADD_SPAM_TEXT = "INSERT INTO botSpam (text, createdBy, enabledBy) VALUES (?, ?, ?)";
	private static final String DELETE_SPAM_TEXT = "DELETE FROM botSpam WHERE id = ?";
	private static final String ENABLE_SPAM_TEXT = "UPDATE botSpam SET enabled = 1, enabledBy = ? WHERE id = ?";
	private static final String DISABLE_SPAM_TEXT = "UPDATE botSpam SET enabled = 0 AND disabledBy = ? WHERE id = ?";
	private static final String ADD_PUNISHMENT = "UPDATE botSpam SET punishments = punishments + 1 WHERE id = ?";

	public BotSpamRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	public ArrayList<SpamText> getSpamText()
	{
		final ArrayList<SpamText> list = new ArrayList<SpamText>();

		executeQuery(GET_SPAM_TEXT, resultSet ->
		{
			while (resultSet.next())
			{
				int id = resultSet.getInt(1);
				String text = resultSet.getString(2);
				int punishments = resultSet.getInt(3);
				boolean enabled = resultSet.getBoolean(4);
				String createdBy = resultSet.getString(5);
				String enabledBy = resultSet.getString(6);
				String disabledBy = resultSet.getString(7);

				list.add(new SpamText(id, text, punishments, enabled, createdBy, enabledBy, disabledBy));
			}
		});

		return list;
	}

	public void addPunishment(SpamText text)
	{
		executeUpdate(ADD_PUNISHMENT, new ColumnInt("id", text.getId()));
	}

	public void disableSpamText(String caller, SpamText text)
	{
		executeUpdate(DISABLE_SPAM_TEXT, new ColumnVarChar("disabledBy", 100, caller), new ColumnInt("id", text.getId()));
	}

	public void enableSpamText(String caller, SpamText text)
	{
		executeUpdate(ENABLE_SPAM_TEXT, new ColumnVarChar("enabledBy", 100, caller), new ColumnInt("id", text.getId()));
	}

	public void addSpamText(String caller, String spamText)
	{
		executeInsert(ADD_SPAM_TEXT, null, new ColumnVarChar("text", 200, spamText), new ColumnVarChar("createdBy", 100, caller), new ColumnVarChar("enabledBy", 100, caller));
	}
}
