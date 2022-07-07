package mineplex.hub.news;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class NewsRepository extends RepositoryBase
{

	private static final String GET_NEWS = "SELECT * FROM hubNews ORDER BY newsId DESC;";
	private static final String INSERT_NEWS = "INSERT INTO hubNews (newsValue) VALUES (?);";
	private static final String DELETE_NEWS = "DELETE FROM hubNews WHERE newsId=?;";

	NewsRepository()
	{
		super(DBPool.getAccount());
	}

	public void fetchNews(Consumer<List<NewsElement>> callback)
	{
		executeQuery(GET_NEWS, resultSet ->
		{
			List<NewsElement> news = new ArrayList<>();

			while (resultSet.next())
			{
				int newsId = resultSet.getInt("newsId");
				String newsValue = resultSet.getString("newsValue");

				news.add(new NewsElement(newsId, newsValue));
			}

			callback.accept(news);
		});
	}

	public void insertNews(Consumer<NewsElement> callback, String value)
	{
		executeInsert(INSERT_NEWS, resultSet ->
		{
			if (resultSet.next())
			{
				int newsId = resultSet.getInt(1);

				callback.accept(new NewsElement(newsId, value));
			}
		}, new ColumnVarChar("newsValue", 64, value));
	}

	public void deleteNews(NewsElement element)
	{
		executeUpdate(DELETE_NEWS, new ColumnInt("newsId", element.getId()));
	}
}
