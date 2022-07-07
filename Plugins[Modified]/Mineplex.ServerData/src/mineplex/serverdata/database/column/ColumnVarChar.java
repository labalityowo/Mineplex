package mineplex.serverdata.database.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnVarChar extends Column<String>
{
	public int Length = 25;
	
	public ColumnVarChar(String name, int length)
	{
		this(name, length, "");
	}
	
	public ColumnVarChar(String name, int length, String value)
	{
		super(name);
		
		Length = length;
		Value = value;
	}

	public String getCreateString() 
	{	
		return Name + " VARCHAR(" + Length + ")";
	}

	@Override
	public String getValue(ResultSet resultSet) throws SQLException 
	{
		return resultSet.getString(Name);
	}
	
	@Override
	public void setValue(PreparedStatement preparedStatement, int columnNumber) throws SQLException 
	{
		preparedStatement.setString(columnNumber, Value);
	}

	@Override
	public ColumnVarChar clone()
	{
		return new ColumnVarChar(Name, Length, Value);
	}
}
