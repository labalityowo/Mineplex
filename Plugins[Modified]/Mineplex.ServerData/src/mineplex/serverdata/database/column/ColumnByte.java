package mineplex.serverdata.database.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnByte extends Column<Byte>
{
	public ColumnByte(String name)
	{
		super(name);
		Value = (byte)0;
	}
	
	public ColumnByte(String name, Byte value)
	{
		super(name, value);
	}

	@Override
	public String getCreateString() 
	{
		return Name + " TINYINT";
	}

	@Override
	public Byte getValue(ResultSet resultSet) throws SQLException 
	{
		return resultSet.getByte(Name);
	}
	
	@Override
	public void setValue(PreparedStatement preparedStatement, int columnNumber) throws SQLException
	{
		preparedStatement.setLong(columnNumber, Value);
	}

	@Override
	public ColumnByte clone()
	{
		return new ColumnByte(Name, Value);
	}
}
