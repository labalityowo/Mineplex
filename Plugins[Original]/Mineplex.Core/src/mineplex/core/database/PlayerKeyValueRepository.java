package mineplex.core.database;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import mineplex.serverdata.database.DBPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * A SQL-backed repository supporting {@link String} keys and
 * values of type {@link V}
 * <p>
 * Each java primitive (sans char) and String are supported by default.
 * Serializing functions for any additional types can be supplied
 * to {@link PlayerKeyValueRepository(String, Serializer, Deserializer)}.
 * For example, if {@link String} was not supported, one could use:
 * <p>
 * {@code new PlayerKeyValueRepository("tableName", PreparedStatement::setString, ResultSet::getString, "VARCHAR(255)")}
 * <p>
 * Compatible backing table schemas can be written as follows (replace $VARS as appropriate):
 * <p><blockquote><pre>
 *     CREATE TABLE IF NOT EXISTS $TABLE_NAME (
 *         accountId INT NOT NULL,
 *         kvKey VARCHAR(255) NOT NULL,
 *         kvValue $VALUE_COLUMN_TYPE
 *         PRIMARY KEY (accountId,kvKey),
 *         INDEX acc_ind (accountId),
 *         FOREIGN KEY (accountId) REFERENCES accounts(id)
 *     )}
 * </pre></blockquote></p>
 *
 * @param <V> The value type to use for this repository
 */
public class PlayerKeyValueRepository<V>
{
    private static final ImmutableMap<Class<?>, ValueMapper<?>> PRIM_MAPPERS = ImmutableMap.<Class<?>, ValueMapper<?>>builder()
        .put(String.class,  new ValueMapper<>(PreparedStatement::setString,  ResultSet::getString))
        .put(Boolean.class, new ValueMapper<>(PreparedStatement::setBoolean, ResultSet::getBoolean))
        .put(Byte.class,    new ValueMapper<>(PreparedStatement::setByte,    ResultSet::getByte))
        .put(Short.class,   new ValueMapper<>(PreparedStatement::setShort,   ResultSet::getShort))
        .put(Integer.class, new ValueMapper<>(PreparedStatement::setInt,     ResultSet::getInt))
        .put(Long.class,    new ValueMapper<>(PreparedStatement::setLong,    ResultSet::getLong))
        .put(Float.class,   new ValueMapper<>(PreparedStatement::setFloat,   ResultSet::getFloat))
        .put(Double.class,  new ValueMapper<>(PreparedStatement::setDouble,  ResultSet::getDouble))
        .build();
    private final String _tableName;
    private final ValueMapper<V> _mapper;

    /**
     * Build a PlayerKeyValueRepository with the given class'
     * built-in deserializer.
     *
     * @param tableName the underlying table's name
     * @param clazz the type of values to used
     * @throws IllegalArgumentException if the provided class isn't a supported type
     */
    @SuppressWarnings("unchecked") // java's generics are garbage.
    public PlayerKeyValueRepository(String tableName, Class<V> clazz) // we could infer the type parameter at runtime, but it's super ugly
    {
        ValueMapper<V> mapper = (ValueMapper<V>) PRIM_MAPPERS.get(clazz);
        Preconditions.checkNotNull(mapper, "Unsupported value type: " + clazz.getName() + ". (use the other constructor)");

        this._tableName = tableName;
        this._mapper = mapper;
    }

    /**
     * Build a PlayerKeyValueRepository with an explicit deserializer.
     * This is the constructor to use if the type you're deserializing
     * isn't supported by default.
     *
     * @param tableName the underlying table's name
     * @param serializer the serializing function used to insert values
     * @param deserializer the deserializing function used to retrieve
     *                     values
     */
    public PlayerKeyValueRepository(String tableName, Serializer<V> serializer, Deserializer<V> deserializer)
    {
        this._tableName = tableName;
        this._mapper = new ValueMapper<V>(serializer, deserializer);
    }

    /**
     * Get all value for a player's key
     *
     * @param uuid the {@link UUID} of the player
     * @return a CompletableFuture containing all key/value pairs
     *         associated with the player
     */
    public CompletableFuture<V> get(UUID uuid, String key)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection conn = DBPool.getAccount().getConnection())
            {
                PreparedStatement stmt = conn.prepareStatement("SELECT kvValue FROM " + _tableName + " WHERE accountId = (SELECT id FROM accounts WHERE uuid=?) AND kvKey=?");
                stmt.setString(1, uuid.toString());
                stmt.setString(2, key);

                ResultSet set = stmt.executeQuery();
                if (set.next())
                {
                    return _mapper._deserializer.read(set, 1);
                }
                return null;
            }
            catch (SQLException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Get all key/value pairs for a player
     *
     * @param uuid the {@link UUID} of the player
     * @return a CompletableFuture containing all key/value pairs
     *         associated with the player
     */
    public CompletableFuture<Map<String,V>> getAll(UUID uuid)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection conn = DBPool.getAccount().getConnection())
            {
                PreparedStatement stmt = conn.prepareStatement("SELECT kvKey, kvValue FROM " + _tableName + " WHERE accountId = (SELECT id FROM accounts WHERE uuid=?)");
                stmt.setString(1, uuid.toString());

                ResultSet set = stmt.executeQuery();
                Map<String, V> results = new HashMap<>();
                while (set.next())
                {
                    results.put(set.getString(1), _mapper._deserializer.read(set, 2));
                }
                return results;
            }
            catch (SQLException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Insert a key/value pair for a player
     *
     * @param uuid the {@link UUID} of the player
     * @param key the key to insert
     * @param value the value to insert
     * @return a {@link CompletableFuture} whose value indicates
     *         success or failure
     */
    public CompletableFuture<Void> put(UUID uuid, String key, V value)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection conn = DBPool.getAccount().getConnection())
            {
                PreparedStatement stmt = conn.prepareStatement("REPLACE INTO " + _tableName + " (accountId, kvKey, kvValue) SELECT accounts.id, ?, ? FROM accounts WHERE uuid=?");
                stmt.setString(1, key);
                _mapper._serializer.write(stmt, 2, value);
                stmt.setString(3, uuid.toString());
                stmt.executeUpdate();
                return null;

            }
            catch (SQLException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Insert many key/value pairs for a player
     *
     * @param uuid the {@link UUID} of the player
     * @param values the map whose entries will be inserted for the
     *               player
     * @return a {@link CompletableFuture} whose value indicates
     *         success or failure
     */
    public CompletableFuture<Void> putAll(UUID uuid, Map<String,V> values)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection conn = DBPool.getAccount().getConnection())
            {
                PreparedStatement stmt = conn.prepareStatement("REPLACE INTO " + _tableName + " (accountId, kvKey, kvValue) SELECT accounts.id, ?, ? FROM accounts WHERE uuid=?");
                stmt.setString(3, uuid.toString());

                for (Map.Entry<String, V> entry : values.entrySet())
                {
                    stmt.setString(1, entry.getKey());
                    _mapper._serializer.write(stmt, 2, entry.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                return null;

            }
            catch (SQLException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Remove a key's value for a player
     *
     * @param uuid the {@link UUID} of the player
     * @param key the key to remove
     * @return a {@link CompletableFuture} whose value indicates
     *         success or failure
     */
    public CompletableFuture<Void> remove(UUID uuid, String key)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection conn = DBPool.getAccount().getConnection())
            {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + _tableName + " WHERE accountId=(SELECT id FROM accounts WHERE uuid=?) AND kvKey=?");
                stmt.setString(1, uuid.toString());
                stmt.setString(2, key);
                stmt.executeUpdate();
                return null;

            }
            catch (SQLException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Remove all key/value pairs for a player
     *
     * @param uuid the {@link UUID} of the player
     * @return a {@link CompletableFuture} whose value indicates
     *         success or failure
     */
    public CompletableFuture<Void> removeAll(UUID uuid)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection conn = DBPool.getAccount().getConnection())
            {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + _tableName + " WHERE accountId=(SELECT id FROM accounts WHERE uuid=?)");
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                return null;

            }
            catch (SQLException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    private static class ValueMapper<V>
    {
        private final Serializer<V> _serializer;
        private final Deserializer<V> _deserializer;

        private ValueMapper(Serializer<V> serializer, Deserializer<V> deserializer)
        {
            _serializer = serializer;
            _deserializer = deserializer;
        }
    }

    @FunctionalInterface
    public interface Serializer<V>
    {
        void write(PreparedStatement statement, int index, V value) throws SQLException;
    }

    @FunctionalInterface
    public interface Deserializer<V>
    {
        V read(ResultSet resultSet, int index) throws SQLException;
    }
}
