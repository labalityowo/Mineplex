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
public class AccountStat extends org.jooq.impl.TableImpl<mineplex.database.tables.records.AccountStatRecord> implements java.io.Serializable, java.lang.Cloneable {

	private static final long serialVersionUID = 34581219;

	/**
	 * The reference instance of <code>Account.accountStat</code>
	 */
	public static final mineplex.database.tables.AccountStat accountStat = new mineplex.database.tables.AccountStat();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<mineplex.database.tables.records.AccountStatRecord> getRecordType() {
		return mineplex.database.tables.records.AccountStatRecord.class;
	}

	/**
	 * The column <code>Account.accountStat.accountId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountStatRecord, java.lang.Integer> accountId = createField("accountId", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.accountStat.statId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountStatRecord, java.lang.Integer> statId = createField("statId", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.accountStat.value</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountStatRecord, org.jooq.types.ULong> value = createField("value", org.jooq.impl.SQLDataType.BIGINTUNSIGNED.defaulted(true), this, "");

	/**
	 * Create a <code>Account.accountStat</code> table reference
	 */
	public AccountStat() {
		this("accountStat", null);
	}

	/**
	 * Create an aliased <code>Account.accountStat</code> table reference
	 */
	public AccountStat(java.lang.String alias) {
		this(alias, mineplex.database.tables.AccountStat.accountStat);
	}

	private AccountStat(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.AccountStatRecord> aliased) {
		this(alias, aliased, null);
	}

	private AccountStat(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.AccountStatRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, mineplex.database.Account.Account, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<mineplex.database.tables.records.AccountStatRecord> getPrimaryKey() {
		return mineplex.database.Keys.KEY_accountStat_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<mineplex.database.tables.records.AccountStatRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<mineplex.database.tables.records.AccountStatRecord>>asList(mineplex.database.Keys.KEY_accountStat_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<mineplex.database.tables.records.AccountStatRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<mineplex.database.tables.records.AccountStatRecord, ?>>asList(mineplex.database.Keys.accountStat_account, mineplex.database.Keys.accountStat_stat);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public mineplex.database.tables.AccountStat as(java.lang.String alias) {
		return new mineplex.database.tables.AccountStat(alias, this);
	}

	/**
	 * Rename this table
	 */
	public mineplex.database.tables.AccountStat rename(java.lang.String name) {
		return new mineplex.database.tables.AccountStat(name, null);
	}
}
