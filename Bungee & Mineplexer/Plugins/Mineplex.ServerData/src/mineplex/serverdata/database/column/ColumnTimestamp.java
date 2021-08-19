package mineplex.serverdata.database.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ColumnTimestamp extends Column<Timestamp>
{
	public ColumnTimestamp(String name)
	{
		super(name);
	}
	
	public ColumnTimestamp(String name, Timestamp value)
	{
		super(name, value);
	}

	@Override
	public String getCreateString() 
	{
		return Name + " TIMESTAMP";
	}

	@Override
	public Timestamp getValue(ResultSet resultSet) throws SQLException 
	{
		return resultSet.getTimestamp(Name);
	}
	
	@Override
	public void setValue(PreparedStatement preparedStatement, int columnNumber) throws SQLException
	{
		preparedStatement.setTimestamp(columnNumber, Value);
	}

	@Override
	public ColumnTimestamp clone()
	{
		return new ColumnTimestamp(Name, Value);
	}
}
