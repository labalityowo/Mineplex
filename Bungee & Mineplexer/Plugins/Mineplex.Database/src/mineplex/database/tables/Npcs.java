/**
 * This class is generated by jOOQ
 */
package mineplex.database.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.2"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Npcs extends org.jooq.impl.TableImpl<mineplex.database.tables.records.NpcsRecord> implements java.io.Serializable, java.lang.Cloneable {

	private static final long serialVersionUID = 1276399546;

	/**
	 * The reference instance of <code>Account.npcs</code>
	 */
	public static final mineplex.database.tables.Npcs npcs = new mineplex.database.tables.Npcs();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<mineplex.database.tables.records.NpcsRecord> getRecordType() {
		return mineplex.database.tables.records.NpcsRecord.class;
	}

	/**
	 * The column <code>Account.npcs.id</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Integer> id = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.npcs.server</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> server = createField("server", org.jooq.impl.SQLDataType.VARCHAR.length(50).nullable(false), this, "");

	/**
	 * The column <code>Account.npcs.name</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> name = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>Account.npcs.world</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> world = createField("world", org.jooq.impl.SQLDataType.VARCHAR.length(50).nullable(false), this, "");

	/**
	 * The column <code>Account.npcs.x</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Double> x = createField("x", org.jooq.impl.SQLDataType.FLOAT.nullable(false), this, "");

	/**
	 * The column <code>Account.npcs.y</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Double> y = createField("y", org.jooq.impl.SQLDataType.FLOAT.nullable(false), this, "");

	/**
	 * The column <code>Account.npcs.z</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Double> z = createField("z", org.jooq.impl.SQLDataType.FLOAT.nullable(false), this, "");
	
	/**
	 * The column <code>Account.npcs.yaw</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Double> yaw = createField("yaw", org.jooq.impl.SQLDataType.FLOAT.nullable(false).defaulted(true), this, "");
	
	/**
	 * The column <code>Account.npcs.pitch</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Double> pitch = createField("pitch", org.jooq.impl.SQLDataType.FLOAT.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>Account.npcs.radius</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Double> radius = createField("radius", org.jooq.impl.SQLDataType.FLOAT.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>Account.npcs.entityType</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> entityType = createField("entityType", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");

	/**
	 * The column <code>Account.npcs.entityMeta</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> entityMeta = createField("entityMeta", org.jooq.impl.SQLDataType.VARCHAR.length(100), this, "");

	/**
	 * The column <code>Account.npcs.adult</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Boolean> adult = createField("adult", org.jooq.impl.SQLDataType.BIT.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>Account.npcs.helmet</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> helmet = createField("helmet", org.jooq.impl.SQLDataType.CLOB.length(65535), this, "");

	/**
	 * The column <code>Account.npcs.chestplate</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> chestplate = createField("chestplate", org.jooq.impl.SQLDataType.CLOB.length(65535), this, "");

	/**
	 * The column <code>Account.npcs.leggings</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> leggings = createField("leggings", org.jooq.impl.SQLDataType.CLOB.length(65535), this, "");

	/**
	 * The column <code>Account.npcs.boots</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> boots = createField("boots", org.jooq.impl.SQLDataType.CLOB.length(65535), this, "");

	/**
	 * The column <code>Account.npcs.inHand</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> inHand = createField("inHand", org.jooq.impl.SQLDataType.CLOB.length(65535), this, "");

	/**
	 * The column <code>Account.npcs.info</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.String> info = createField("info", org.jooq.impl.SQLDataType.CLOB.length(65535), this, "");

	/**
	 * The column <code>Account.npcs.infoRadius</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Double> infoRadius = createField("infoRadius", org.jooq.impl.SQLDataType.FLOAT, this, "");

	/**
	 * The column <code>Account.npcs.infoDelay</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.NpcsRecord, java.lang.Integer> infoDelay = createField("infoDelay", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * Create a <code>Account.npcs</code> table reference
	 */
	public Npcs() {
		this("npcs", null);
	}

	/**
	 * Create an aliased <code>Account.npcs</code> table reference
	 */
	public Npcs(java.lang.String alias) {
		this(alias, mineplex.database.tables.Npcs.npcs);
	}

	private Npcs(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.NpcsRecord> aliased) {
		this(alias, aliased, null);
	}

	private Npcs(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.NpcsRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, mineplex.database.Account.Account, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<mineplex.database.tables.records.NpcsRecord, java.lang.Integer> getIdentity() {
		return mineplex.database.Keys.IDENTITY_npcs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<mineplex.database.tables.records.NpcsRecord> getPrimaryKey() {
		return mineplex.database.Keys.KEY_npcs_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<mineplex.database.tables.records.NpcsRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<mineplex.database.tables.records.NpcsRecord>>asList(mineplex.database.Keys.KEY_npcs_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public mineplex.database.tables.Npcs as(java.lang.String alias) {
		return new mineplex.database.tables.Npcs(alias, this);
	}

	/**
	 * Rename this table
	 */
	public mineplex.database.tables.Npcs rename(java.lang.String name) {
		return new mineplex.database.tables.Npcs(name, null);
	}
}
