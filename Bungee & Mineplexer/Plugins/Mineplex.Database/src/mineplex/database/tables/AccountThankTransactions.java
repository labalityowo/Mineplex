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
public class AccountThankTransactions extends org.jooq.impl.TableImpl<mineplex.database.tables.records.AccountThankTransactionsRecord> implements java.io.Serializable, java.lang.Cloneable {

	private static final long serialVersionUID = 809892717;

	/**
	 * The reference instance of <code>Account.accountThankTransactions</code>
	 */
	public static final mineplex.database.tables.AccountThankTransactions accountThankTransactions = new mineplex.database.tables.AccountThankTransactions();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<mineplex.database.tables.records.AccountThankTransactionsRecord> getRecordType() {
		return mineplex.database.tables.records.AccountThankTransactionsRecord.class;
	}

	/**
	 * The column <code>Account.accountThankTransactions.id</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.lang.Integer> id = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.accountThankTransactions.receiverId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.lang.Integer> receiverId = createField("receiverId", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>Account.accountThankTransactions.senderId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.lang.Integer> senderId = createField("senderId", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.accountThankTransactions.thankAmount</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.lang.Integer> thankAmount = createField("thankAmount", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>Account.accountThankTransactions.reason</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.lang.String> reason = createField("reason", org.jooq.impl.SQLDataType.VARCHAR.length(32), this, "");

	/**
	 * The column <code>Account.accountThankTransactions.ignoreCooldown</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.lang.Byte> ignoreCooldown = createField("ignoreCooldown", org.jooq.impl.SQLDataType.TINYINT.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>Account.accountThankTransactions.claimed</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.lang.Byte> claimed = createField("claimed", org.jooq.impl.SQLDataType.TINYINT.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>Account.accountThankTransactions.sentTime</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.sql.Timestamp> sentTime = createField("sentTime", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>Account.accountThankTransactions.claimTime</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountThankTransactionsRecord, java.sql.Timestamp> claimTime = createField("claimTime", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

	/**
	 * Create a <code>Account.accountThankTransactions</code> table reference
	 */
	public AccountThankTransactions() {
		this("accountThankTransactions", null);
	}

	/**
	 * Create an aliased <code>Account.accountThankTransactions</code> table reference
	 */
	public AccountThankTransactions(java.lang.String alias) {
		this(alias, mineplex.database.tables.AccountThankTransactions.accountThankTransactions);
	}

	private AccountThankTransactions(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.AccountThankTransactionsRecord> aliased) {
		this(alias, aliased, null);
	}

	private AccountThankTransactions(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.AccountThankTransactionsRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, mineplex.database.Account.Account, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<mineplex.database.tables.records.AccountThankTransactionsRecord, java.lang.Integer> getIdentity() {
		return mineplex.database.Keys.IDENTITY_accountThankTransactions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<mineplex.database.tables.records.AccountThankTransactionsRecord> getPrimaryKey() {
		return mineplex.database.Keys.KEY_accountThankTransactions_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<mineplex.database.tables.records.AccountThankTransactionsRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<mineplex.database.tables.records.AccountThankTransactionsRecord>>asList(mineplex.database.Keys.KEY_accountThankTransactions_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<mineplex.database.tables.records.AccountThankTransactionsRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<mineplex.database.tables.records.AccountThankTransactionsRecord, ?>>asList(mineplex.database.Keys.accountThankTransactions_accounts_id_fk2, mineplex.database.Keys.accountThankTransactions_accounts_id_fk);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public mineplex.database.tables.AccountThankTransactions as(java.lang.String alias) {
		return new mineplex.database.tables.AccountThankTransactions(alias, this);
	}

	/**
	 * Rename this table
	 */
	public mineplex.database.tables.AccountThankTransactions rename(java.lang.String name) {
		return new mineplex.database.tables.AccountThankTransactions(name, null);
	}
}
