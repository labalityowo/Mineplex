package mineplex.serverdata.database.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Column<Type>
{
	public String Name;
	public Type Value;
	
	public Column(String name)
	{
		Name = name;
	}
	
	public Column(String name, Type value)
	{
		Name = name;
		Value = value;
	}
	
	public abstract String getCreateString();
	
	public abstract Type getValue(ResultSet resultSet) throws SQLException;
	
	public abstract void setValue(PreparedStatement preparedStatement, int columnNumber) throws SQLException;
	
	public abstract Column<Type> clone();
}
