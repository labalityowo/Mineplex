package mineplex.serverdata.database.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnInt extends Column<Integer>
{
	public ColumnInt(String name)
	{
		super(name);
		Value = 0;
	}
	
	public ColumnInt(String name, int value)
	{
		super(name, value);
	}

	@Override
	public String getCreateString() 
	{
		return Name + " INT";
	}

	@Override
	public Integer getValue(ResultSet resultSet) throws SQLException
	{
		return resultSet.getInt(Name);
	}
	
	@Override
	public void setValue(PreparedStatement preparedStatement, int columnNumber) throws SQLException
	{
		preparedStatement.setInt(columnNumber, Value);
	}

	@Override
	public ColumnInt clone()
	{
		return new ColumnInt(Name, Value);
	}
}
