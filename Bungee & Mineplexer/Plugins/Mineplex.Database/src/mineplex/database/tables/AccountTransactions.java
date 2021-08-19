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
public class AccountTransactions extends org.jooq.impl.TableImpl<mineplex.database.tables.records.AccountTransactionsRecord> implements java.io.Serializable, java.lang.Cloneable {

	private static final long serialVersionUID = 628330770;

	/**
	 * The reference instance of <code>Account.accountTransactions</code>
	 */
	public static final mineplex.database.tables.AccountTransactions accountTransactions = new mineplex.database.tables.AccountTransactions();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<mineplex.database.tables.records.AccountTransactionsRecord> getRecordType() {
		return mineplex.database.tables.records.AccountTransactionsRecord.class;
	}

	/**
	 * The column <code>Account.accountTransactions.id</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountTransactionsRecord, java.lang.Integer> id = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.accountTransactions.accountId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountTransactionsRecord, java.lang.Integer> accountId = createField("accountId", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.accountTransactions.transactionId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountTransactionsRecord, java.lang.Integer> transactionId = createField("transactionId", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.accountTransactions.coins</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountTransactionsRecord, java.lang.Integer> coins = createField("coins", org.jooq.impl.SQLDataType.INTEGER.defaulted(true), this, "");

	/**
	 * The column <code>Account.accountTransactions.gems</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountTransactionsRecord, java.lang.Integer> gems = createField("gems", org.jooq.impl.SQLDataType.INTEGER.defaulted(true), this, "");

	/**
	 * Create a <code>Account.accountTransactions</code> table reference
	 */
	public AccountTransactions() {
		this("accountTransactions", null);
	}

	/**
	 * Create an aliased <code>Account.accountTransactions</code> table reference
	 */
	public AccountTransactions(java.lang.String alias) {
		this(alias, mineplex.database.tables.AccountTransactions.accountTransactions);
	}

	private AccountTransactions(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.AccountTransactionsRecord> aliased) {
		this(alias, aliased, null);
	}

	private AccountTransactions(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.AccountTransactionsRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, mineplex.database.Account.Account, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<mineplex.database.tables.records.AccountTransactionsRecord, java.lang.Integer> getIdentity() {
		return mineplex.database.Keys.IDENTITY_accountTransactions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<mineplex.database.tables.records.AccountTransactionsRecord> getPrimaryKey() {
		return mineplex.database.Keys.KEY_accountTransactions_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<mineplex.database.tables.records.AccountTransactionsRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<mineplex.database.tables.records.AccountTransactionsRecord>>asList(mineplex.database.Keys.KEY_accountTransactions_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<mineplex.database.tables.records.AccountTransactionsRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<mineplex.database.tables.records.AccountTransactionsRecord, ?>>asList(mineplex.database.Keys.ACCOUNTTRANSACTIONS_ACCOUNTID, mineplex.database.Keys.ACCOUNTTRANSACTIONS_TRANSACTIONID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public mineplex.database.tables.AccountTransactions as(java.lang.String alias) {
		return new mineplex.database.tables.AccountTransactions(alias, this);
	}

	/**
	 * Rename this table
	 */
	public mineplex.database.tables.AccountTransactions rename(java.lang.String name) {
		return new mineplex.database.tables.AccountTransactions(name, null);
	}
}