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
public class Selectedgadgets extends org.jooq.impl.TableImpl<mineplex.database.tables.records.SelectedgadgetsRecord> implements java.io.Serializable, java.lang.Cloneable {

	private static final long serialVersionUID = -1690277437;

	/**
	 * The reference instance of <code>Account.selectedgadgets</code>
	 */
	public static final mineplex.database.tables.Selectedgadgets selectedgadgets = new mineplex.database.tables.Selectedgadgets();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<mineplex.database.tables.records.SelectedgadgetsRecord> getRecordType() {
		return mineplex.database.tables.records.SelectedgadgetsRecord.class;
	}

	/**
	 * The column <code>Account.selectedgadgets.accountId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.SelectedgadgetsRecord, java.lang.Integer> accountId = createField("accountId", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.selectedgadgets.selected</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.SelectedgadgetsRecord, java.lang.String> selected = createField("selected", org.jooq.impl.SQLDataType.CLOB.length(65535), this, "");

	/**
	 * Create a <code>Account.selectedgadgets</code> table reference
	 */
	public Selectedgadgets() {
		this("selectedgadgets", null);
	}

	/**
	 * Create an aliased <code>Account.selectedgadgets</code> table reference
	 */
	public Selectedgadgets(java.lang.String alias) {
		this(alias, mineplex.database.tables.Selectedgadgets.selectedgadgets);
	}

	private Selectedgadgets(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.SelectedgadgetsRecord> aliased) {
		this(alias, aliased, null);
	}

	private Selectedgadgets(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.SelectedgadgetsRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, mineplex.database.Account.Account, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public mineplex.database.tables.Selectedgadgets as(java.lang.String alias) {
		return new mineplex.database.tables.Selectedgadgets(alias, this);
	}

	/**
	 * Rename this table
	 */
	public mineplex.database.tables.Selectedgadgets rename(java.lang.String name) {
		return new mineplex.database.tables.Selectedgadgets(name, null);
	}
}
