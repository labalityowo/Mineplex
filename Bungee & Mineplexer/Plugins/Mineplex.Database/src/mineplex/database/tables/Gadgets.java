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
public class Gadgets extends org.jooq.impl.TableImpl<mineplex.database.tables.records.GadgetsRecord> implements java.io.Serializable, java.lang.Cloneable {

	private static final long serialVersionUID = -42460699;

	/**
	 * The reference instance of <code>Account.gadgets</code>
	 */
	public static final mineplex.database.tables.Gadgets gadgets = new mineplex.database.tables.Gadgets();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<mineplex.database.tables.records.GadgetsRecord> getRecordType() {
		return mineplex.database.tables.records.GadgetsRecord.class;
	}

	/**
	 * The column <code>Account.gadgets.accountId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.GadgetsRecord, java.lang.Integer> accountId = createField("accountId", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.gadgets.kvKey</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.GadgetsRecord, java.lang.String> kvKey = createField("kvKey", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>Account.gadgets.kvValue</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.GadgetsRecord, java.lang.String> kvValue = createField("kvValue", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * Create a <code>Account.gadgets</code> table reference
	 */
	public Gadgets() {
		this("gadgets", null);
	}

	/**
	 * Create an aliased <code>Account.gadgets</code> table reference
	 */
	public Gadgets(java.lang.String alias) {
		this(alias, mineplex.database.tables.Gadgets.gadgets);
	}

	private Gadgets(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.GadgetsRecord> aliased) {
		this(alias, aliased, null);
	}

	private Gadgets(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.GadgetsRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, mineplex.database.Account.Account, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<mineplex.database.tables.records.GadgetsRecord> getPrimaryKey() {
		return mineplex.database.Keys.KEY_gadgets_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<mineplex.database.tables.records.GadgetsRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<mineplex.database.tables.records.GadgetsRecord>>asList(mineplex.database.Keys.KEY_gadgets_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<mineplex.database.tables.records.GadgetsRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<mineplex.database.tables.records.GadgetsRecord, ?>>asList(mineplex.database.Keys.gadgets_ibfk_1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public mineplex.database.tables.Gadgets as(java.lang.String alias) {
		return new mineplex.database.tables.Gadgets(alias, this);
	}

	/**
	 * Rename this table
	 */
	public mineplex.database.tables.Gadgets rename(java.lang.String name) {
		return new mineplex.database.tables.Gadgets(name, null);
	}
}