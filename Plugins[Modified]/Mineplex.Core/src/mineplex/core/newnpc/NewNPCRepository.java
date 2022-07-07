package mineplex.core.newnpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.Column;
import mineplex.serverdata.database.column.ColumnInt;

/**
 * A repository class that provides an interface for getting and saving NPCs.
 */
class NewNPCRepository extends RepositoryBase
{

	/**
	 * A SQL statement that returns all entries form the table.
	 */
	private static final String GET_NPCS = "SELECT * FROM newNPCsNew;";
	/**
	 * A SQL statement that inserts an entry and returns the id field generated.
	 */
	private static final String INSERT_NPC = "INSERT INTO newNPCsNew (entity_type, name, info, world, x, y, z, yaw, pitch, in_hand, in_hand_data, helmet, chestplate, leggings, boots, metadata, skin_value, skin_signature) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
	/**
	 * A SQL statement that deletes an entry with a specific id field.
	 */
	private static final String DELETE_NPC = "DELETE FROM newNPCsNew WHERE id=?;";
	/**
	 * A SQL statement that removes all entries from the table.
	 */
	private static final String CLEAR_NPCS = "DELETE FROM newNPCsNew;";
	/**
	 * A SQL statement that updates all the properties of an NPC.
	 */
	private static final String UPDATE_NPC_PROPERTIES = "UPDATE newNPCsNew SET entity_type=?, name=?, info=?, world=?, x=?, y=?, z=?, yaw=?, pitch=?, in_hand=?, in_hand_data=?, helmet=?, chestplate=?, leggings=?, boots=?, metadata=?, skin_value=?, skin_signature=? WHERE id=?";

	NewNPCRepository()
	{
		super(DBPool.getAccount());
	}

	/**
	 * Executes the SQL query:
	 * <p>
	 * {@value GET_NPCS}
	 * </p>
	 * The values return are parsed into an instance of {@link NPC} or one of it's subclasses such as {@link PlayerNPC}
	 * if appropriate.
	 *
	 * @param response The consumer callback that supplies the list of NPCs.
	 */
	void getNPCs(Consumer<List<StoredNPC>> response)
	{
		executeQuery(GET_NPCS, resultSet ->
		{
			List<StoredNPC> npcs = new ArrayList<>();

			// While there are more entries
			while (resultSet.next())
			{
				int id = resultSet.getInt("id");
				EntityType entityType = EntityType.valueOf(resultSet.getString("entity_type"));
				String name = resultSet.getString("name");
				String info = resultSet.getString("info");
				String worldName = resultSet.getString("world");
				double x = resultSet.getDouble("x");
				double y = resultSet.getDouble("y");
				double z = resultSet.getDouble("z");
				int yaw = resultSet.getInt("yaw");
				int pitch = resultSet.getInt("pitch");
				Material inHand = parseMaterial(resultSet.getString("in_hand"));
				byte inHandData = resultSet.getByte("in_hand_data");
				Material helmet = parseMaterial(resultSet.getString("helmet"));
				Material chestplate = parseMaterial(resultSet.getString("chestplate"));
				Material leggings = parseMaterial(resultSet.getString("leggings"));
				Material boots = parseMaterial(resultSet.getString("boots"));
				String metadata = resultSet.getString("metadata");
				String skinValue = resultSet.getString("skin_value");
				String skinSignature = resultSet.getString("skin_signature");

				StoredNPC npc;
				World world = Bukkit.getWorld(worldName);

				// If the world is not loaded on the server then don't add it to the list
				if (world == null)
				{
					continue;
				}

				List<String> infoList = new ArrayList<>(2);

				if (info != null)
				{
					infoList.addAll(Arrays.asList(info.split(StoredNPC.LINE_DELIMITER)));
				}

				// If the entity type is of player then the NPC must be specified as a PlayerNPC
				if (entityType == EntityType.PLAYER)
				{
					npc = new PlayerNPC(id, name, infoList, new Location(world, x, y, z, yaw, pitch), inHand, inHandData, helmet, chestplate, leggings, boots, metadata, skinValue, skinSignature);
				}
				else
				{
					npc = new StoredNPC(id, entityType, name, infoList, new Location(world, x, y, z, yaw, pitch), inHand, inHandData, helmet, chestplate, leggings, boots, metadata);
				}

				npcs.add(npc);
			}

			response.accept(npcs);
		});
	}

	// Simply calls Material.valueOf, if invalid null
	private Material parseMaterial(String material)
	{
		if (material == null)
		{
			return null;
		}

		try
		{
			return Material.valueOf(material);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	/**
	 * Executes the SQL insert:
	 * <p>
	 * {@value INSERT_NPC}
	 * </p>
	 * If the NPC does not have an id of -1, the insert will not be run.
	 *
	 * @param npc The NPC you want to insert into the database.
	 */
	void insertNPCIfNotExists(StoredNPC npc)
	{
		// If the id isn't -1 then it has already been inserted
		if (npc.getId() != -1)
		{
			return;
		}

		Column<?>[] columns = npc.toDatabaseQuery().toArray(new Column[0]);
		executeInsert(INSERT_NPC, resultSet ->
		{
			// If successful
			if (resultSet.next())
			{
				// Set the NPC's id with that generated by the database
				npc.setId(resultSet.getInt(1));
			}
		}, columns);
	}

	/**
	 * Executes the SQL insert:
	 * <p>
	 * {@value DELETE_NPC}
	 * </p>
	 *
	 * @param npc The NPC you want to delete.
	 */
	void deleteNPC(StoredNPC npc)
	{
		// If the id is -1 then it has never been inserted
		if (npc.getId() == -1)
		{
			return;
		}

		executeUpdate(DELETE_NPC, new ColumnInt("id", npc.getId()));
	}

	/**
	 * Executes the SQL insert:
	 * <p>
	 *     {@value CLEAR_NPCS}
	 * </p>
	 */
	void clearNPCs()
	{
		executeUpdate(CLEAR_NPCS);
	}

	/**
	 * Executes the SQL insert:
	 * <p>
	 *     {@value UPDATE_NPC_PROPERTIES}
	 * </p>
	 *
	 * @param npc The NPC you want to update.
	 */
	void updateNPCProperties(StoredNPC npc)
	{
		Column<?>[] columns = npc.toDatabaseQuery().toArray(new Column[0]);
		Column<?>[] newColumns = new Column[columns.length + 1];

		System.arraycopy(columns, 0, newColumns, 0, columns.length);
		newColumns[columns.length] = new ColumnInt("id", npc.getId());

		executeUpdate(UPDATE_NPC_PROPERTIES, newColumns);
	}
}
