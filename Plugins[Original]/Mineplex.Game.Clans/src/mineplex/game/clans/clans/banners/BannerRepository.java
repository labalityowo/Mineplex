package mineplex.game.clans.clans.banners;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.PatternType;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.database.MinecraftRepository;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

/**
 * Database repository class for banners
 */
public class BannerRepository extends RepositoryBase
{
	private static final String CREATE = "CREATE TABLE IF NOT EXISTS clanBanners (clanId INT NOT NULL,"
			+ "baseColor VARCHAR(15),"
            + "patterns VARCHAR(300),"
            + "PRIMARY KEY (clanId));";
	
	private static final String GET_BANNER_BY_CLAN = "SELECT * FROM clanBanners WHERE clanId=? LIMIT 1;";
	private static final String GET_BANNERS_BY_SERVER = "SELECT clans.name, clanBanners.baseColor, clanBanners.patterns FROM clans INNER JOIN clanBanners ON clans.id=clanBanners.clanId AND clans.serverId=?;";
	private static final String INSERT_BANNER = "INSERT INTO clanBanners (clanId, baseColor, patterns) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE baseColor=VALUES(baseColor), patterns=VALUES(patterns);";
	private static final String DELETE_BANNER = "DELETE FROM clanBanners WHERE clanId=?;";
	
	private BannerManager _bannerManager;
	
	public BannerRepository(JavaPlugin plugin, BannerManager bannerManager)
	{
		super(DBPool.getAccount());
		_bannerManager = bannerManager;
	}
	
	/**
	 * Loads a banner for a certain clan into a given hashmap
	 * @param map The hashmap to load the banner into
	 * @param clan The clan whose banner to fetch
	 */
	public void loadBanner(final Map<String, ClanBanner> map, ClanInfo clan)
	{
		_bannerManager.runAsync(() ->
		{
			executeQuery(GET_BANNER_BY_CLAN, resultSet ->
			{
				while(resultSet.next())
				{
					DyeColor baseColor = DyeColor.valueOf(resultSet.getString("baseColor"));
					List<String> patternStrs = Arrays.asList(resultSet.getString("patterns").split("/"));
					LinkedList<BannerPattern> patterns = new LinkedList<>();
					int layer = 1;
					for (String patternStr : patternStrs)
					{
						if (patternStr.equalsIgnoreCase("Blank"))
						{
							patterns.add(new BannerPattern(layer));
						}
						else
						{
							try
							{
								DyeColor patternColor = DyeColor.valueOf(patternStr.split(",")[0]);
								PatternType patternType = PatternType.valueOf(patternStr.split(",")[1]);
								patterns.add(new BannerPattern(layer, patternColor, patternType));
							}
							catch (Exception e)
							{
								e.printStackTrace();
								patterns.add(new BannerPattern(layer));
							}
						}
						layer++;
					}
					map.put(clan.getName(), new ClanBanner(_bannerManager, clan, baseColor, patterns));
				}
			}, new ColumnInt("clanId", clan.getId()));
		});
	}
	
	/**
	 * Loads all banners for a certain clans server into a given hashmap
	 * @param map The hashmap to load the banner into
	 * @param clan The clan whose banner to fetch
	 */
	public void loadBanners(final Map<String, ClanBanner> map, ClansManager clanManager)
	{
		_bannerManager.runAsync(() ->
		{
			executeQuery(GET_BANNERS_BY_SERVER, resultSet ->
			{
				while(resultSet.next())
				{
					String clanName = resultSet.getString("name");
					DyeColor baseColor = DyeColor.valueOf(resultSet.getString("baseColor"));
					List<String> patternStrs = Arrays.asList(resultSet.getString("patterns").split("/"));
					LinkedList<BannerPattern> patterns = new LinkedList<>();
					int layer = 1;
					for (String patternStr : patternStrs)
					{
						if (patternStr.equalsIgnoreCase("Blank"))
						{
							patterns.add(new BannerPattern(layer));
						}
						else
						{
							try
							{
								DyeColor patternColor = DyeColor.valueOf(patternStr.split(",")[0]);
								PatternType patternType = PatternType.valueOf(patternStr.split(",")[1]);
								patterns.add(new BannerPattern(layer, patternColor, patternType));
							}
							catch (Exception e)
							{
								e.printStackTrace();
								patterns.add(new BannerPattern(layer));
							}
						}
						layer++;
					}
					map.put(clanName, new ClanBanner(_bannerManager, clanManager.getClanMap().get(clanName), baseColor, patterns));
				}
			}, new ColumnInt("serverId", clanManager.getServerId()));
		});
	}
	
	/**
	 * Saves a banner into the database
	 * @param banner The banner to save
	 */
	public void saveBanner(ClanBanner banner)
	{
		_bannerManager.runAsync(() ->
		{
			String patternStr = "";
			for (BannerPattern pattern : banner.getPatterns())
			{
				if (!patternStr.equalsIgnoreCase(""))
				{
					patternStr = patternStr + "/";
				}
				patternStr = patternStr + pattern.getDatabaseForm();
			}
			executeUpdate(INSERT_BANNER, new ColumnInt("clanId", banner.getClan().getId()), new ColumnVarChar("baseColor", 15, banner.getBaseColor().toString()), new ColumnVarChar("patterns", 300, patternStr));
		});
	}
	
	/**
	 * Deletes a banner from the database
	 * @param clan The clan whose banner to delete
	 */
	public void deleteBanner(ClanInfo clan)
	{
		_bannerManager.runAsync(() ->
		{
			executeUpdate(DELETE_BANNER, new ColumnInt("clanId", clan.getId()));
		});
	}
}
