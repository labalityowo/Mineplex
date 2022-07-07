package mineplex.core.progression.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Create and manager a prepared statement for inserting into SQL
 */
public class SQLStatement
{
	/**
	 * The initial string literal statement
	 */
	private String _base;
	/**
	 * The _map for inserting objects into a prepared statement
	 */
	private Map<Integer, Object> _map;

	/**
	 * Create a new instance of an SQLStatement
	 *
	 * @param base The literal string MySQL query ('?' are allowed)
	 */
	public SQLStatement(String base)
	{
		this._base = base;
		this._map = new HashMap<>();
	}

	/**
	 * Set an object to a value for inserting upon completion
	 *
	 * @param i      The position of the object
	 * @param object The object to insert into the statement for the given position
	 * @return The local instance with an updated map
	 */
	public SQLStatement set(int i, Object object)
	{
		this._map.put(i, object);
		return this;
	}

	/**
	 * Prepare a fully built statement for running
	 *
	 * @param connection The connection used to establish the statement
	 * @return The fully built PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement prepare(Connection connection) throws SQLException
	{
		PreparedStatement statement = connection.prepareStatement(_base);

		for (Map.Entry<Integer, Object> entry : _map.entrySet())
		{
			int slot = entry.getKey();
			Object object = entry.getValue();

			if (object instanceof UUID)
			{
				statement.setObject(slot, object.toString());
				continue;
			}

			statement.setObject(slot, object);
		}
		return statement;
	}

}
