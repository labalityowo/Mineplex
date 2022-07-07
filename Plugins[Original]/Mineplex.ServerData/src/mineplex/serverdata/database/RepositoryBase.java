package mineplex.serverdata.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import mineplex.serverdata.database.column.Column;

public abstract class RepositoryBase
{
	static
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	private DataSource _dataSource;	// Connection pool

	/**
	 * Constructor
	 * @param dataSource - the {@link DataSource} responsible for providing the connection pool to this repository.
	 */
	public RepositoryBase(DataSource dataSource)
	{
		_dataSource = dataSource;
		
		new Thread(() -> {
			initialize();
			update();
		}).start();
	}

	protected void initialize()
	{

	}
	
	protected void update()
	{

	}
	
	/**
	 * @return the {@link DataSource} used by the repository for connection pooling.
	 */
	protected DataSource getConnectionPool()
	{
		return _dataSource;
	}

	/**
	 * Requirements: {@link Connection}s must be closed after usage so they may be returned to the pool!
	 * @see Connection#close()
	 * @return a newly fetched {@link Connection} from the connection pool, if a connection can be made, null otherwise.
	 */
	protected Connection getConnection()
	{
		try
		{
			return _dataSource.getConnection();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			// TODO: Log connection failures?
			return null;
		}
	}
	
	protected int executeUpdate(Connection connection, String query, Runnable onSQLError, Column<?>...columns)
	{
		return executeInsert(connection, query, null, onSQLError, columns);
	}
	
	protected int executeUpdate(String query, Runnable onSQLError, Column<?>...columns)
	{
		return executeInsert(query, null, onSQLError, columns);
	}

	/**
	 * Execute a query against the repository.
	 * @param query - the concatenated query to execute in string form.
	 * @param columns - the column data values used for insertion into the query.
	 * @return the number of rows affected by this query in the repository.
	 */
	protected int executeUpdate(String query, Column<?>...columns)
	{
		return executeInsert(query, null, columns);
	}
	
	protected int executeInsert(Connection connection, String query, ResultSetCallable callable, Runnable onSQLError, Column<?>...columns)
	{
		int affectedRows = 0;
		
		// Automatic resource management for handling/closing objects.
		try (
				PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
			)
		{
			for (int i=0; i < columns.length; i++)
			{
				columns[i].setValue(preparedStatement, i+1);
			}
			
			affectedRows = preparedStatement.executeUpdate();

			if (callable != null)
			{
				callable.processResultSet(preparedStatement.getGeneratedKeys());
			}
		}
		catch (SQLException exception)
		{
			exception.printStackTrace();
			if (onSQLError != null)
			{
				onSQLError.run();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		
		return affectedRows;
	}
	
	protected int executeInsert(String query, ResultSetCallable callable, Runnable onSQLError, Column<?>...columns)
	{
		int affectedRows = 0;
		
		// Automatic resource management for handling/closing objects.
		try (
				Connection connection = getConnection();
			)
		{
			affectedRows = executeInsert(connection, query, callable, onSQLError, columns);
		}
		catch (SQLException exception)
		{
			exception.printStackTrace();
			if (onSQLError != null)
			{
				onSQLError.run();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		
		return affectedRows;
	}
	
	protected int executeInsert(String query, ResultSetCallable callable, Column<?>...columns)
	{
		int affectedRows = 0;
		
		// Automatic resource management for handling/closing objects.
		try (
				Connection connection = getConnection();
			)
		{
			affectedRows = executeInsert(connection, query, callable, null, columns);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		
		return affectedRows;
	}
	
	protected void executeQuery(PreparedStatement statement, ResultSetCallable callable, Runnable onSQLError, Column<?>...columns)
	{
		try
		{
			for (int i=0; i < columns.length; i++)
			{
				columns[i].setValue(statement, i+1);
			}
			
			try (ResultSet resultSet = statement.executeQuery())
			{
				if (callable != null)
				{
					callable.processResultSet(resultSet);	
				}
			}
		}
		catch (SQLException exception)
		{
			exception.printStackTrace();
			if (onSQLError != null)
			{
				onSQLError.run();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	protected void executeQuery(PreparedStatement statement, ResultSetCallable callable, Column<?>...columns)
	{
		executeQuery(statement, callable, null, columns);
	}
	
	protected void executeQuery(Connection connection, String query, ResultSetCallable callable, Runnable onSQLError, Column<?>...columns)
	{
		// Automatic resource management for handling/closing objects.
		try (
				PreparedStatement preparedStatement = connection.prepareStatement(query)
			)
		{
			executeQuery(preparedStatement, callable, onSQLError, columns);
		}
		catch (SQLException exception)
		{
			exception.printStackTrace();
			if (onSQLError != null)
			{
				onSQLError.run();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	protected void executeQuery(Connection connection, String query, ResultSetCallable callable, Column<?>...columns)
	{
		// Automatic resource management for handling/closing objects.
		try (
				PreparedStatement preparedStatement = connection.prepareStatement(query)
			)
		{
			executeQuery(preparedStatement, callable, columns);
		}
		catch (MySQLSyntaxErrorException syntaxException)
		{
			System.err.println("Query \"" + query + "\" contained a syntax error:");
			syntaxException.printStackTrace();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	protected void executeQuery(String query, ResultSetCallable callable, Runnable onSQLError, Column<?>...columns)
	{
		// Automatic resource management for handling/closing objects.
		try (
				Connection connection = getConnection();
			)
		{
			executeQuery(connection, query, callable, onSQLError, columns);
		}
		catch (SQLException exception)
		{
			exception.printStackTrace();
			if (onSQLError != null)
			{
				onSQLError.run();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	protected void executeQuery(String query, ResultSetCallable callable, Column<?>...columns)
	{
		// Automatic resource management for handling/closing objects.
		try (
				Connection connection = getConnection();
			)
		{
			executeQuery(connection, query, callable, columns);
		}
		catch (SQLException exception)
		{
			exception.printStackTrace();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	protected DSLContext jooq()
	{
		return DSL.using(DBPool.getAccount(), SQLDialect.MYSQL);
	}
}