package mineplex.serverdata.database.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnLong extends Column<Long>
{
	public ColumnLong(String name)
	{
		super(name);
		Value = 0L;
	}
	
	public ColumnLong(String name, Long value)
	{
		super(name, value);
	}

	@Override
	public String getCreateString() 
	{
		return Name + " LONG";
	}

	@Override
	public Long getValue(ResultSet resultSet) throws SQLException 
	{
		return resultSet.getLong(Name);
	}
	
	@Override
	public void setValue(PreparedStatement preparedStatement, int columnNumber) throws SQLException
	{
		preparedStatement.setLong(columnNumber, Value);
	}

	@Override
	public ColumnLong clone()
	{
		return new ColumnLong(Name, Value);
	}
}
