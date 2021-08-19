package mineplex.serverdata.database.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnDouble extends Column<Double>
{
	public ColumnDouble(String name)
	{
		super(name);
		Value = 0.0;
	}
	
	public ColumnDouble(String name, Double value)
	{
		super(name, value);
	}

	@Override
	public String getCreateString() 
	{
		return Name + " DOUBLE";
	}

	@Override
	public Double getValue(ResultSet resultSet) throws SQLException 
	{
		return resultSet.getDouble(Name);
	}
	
	@Override
	public void setValue(PreparedStatement preparedStatement, int columnNumber) throws SQLException
	{
		preparedStatement.setDouble(columnNumber, Value);
	}

	@Override
	public ColumnDouble clone()
	{
		return new ColumnDouble(Name, Value);
	}
}
