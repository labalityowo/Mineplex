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
public class AccountPets extends org.jooq.impl.TableImpl<mineplex.database.tables.records.AccountPetsRecord> implements java.io.Serializable, java.lang.Cloneable {

	private static final long serialVersionUID = -1968491783;

	/**
	 * The reference instance of <code>Account.accountPets</code>
	 */
	public static final mineplex.database.tables.AccountPets accountPets = new mineplex.database.tables.AccountPets();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<mineplex.database.tables.records.AccountPetsRecord> getRecordType() {
		return mineplex.database.tables.records.AccountPetsRecord.class;
	}

	/**
	 * The column <code>Account.accountPets.accountId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountPetsRecord, java.lang.Integer> accountId = createField("accountId", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>Account.accountPets.petId</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountPetsRecord, java.lang.Short> petId = createField("petId", org.jooq.impl.SQLDataType.SMALLINT, this, "");

	/**
	 * The column <code>Account.accountPets.petName</code>.
	 */
	public final org.jooq.TableField<mineplex.database.tables.records.AccountPetsRecord, java.lang.String> petName = createField("petName", org.jooq.impl.SQLDataType.VARCHAR.length(32), this, "");

	/**
	 * Create a <code>Account.accountPets</code> table reference
	 */
	public AccountPets() {
		this("accountPets", null);
	}

	/**
	 * Create an aliased <code>Account.accountPets</code> table reference
	 */
	public AccountPets(java.lang.String alias) {
		this(alias, mineplex.database.tables.AccountPets.accountPets);
	}

	private AccountPets(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.AccountPetsRecord> aliased) {
		this(alias, aliased, null);
	}

	private AccountPets(java.lang.String alias, org.jooq.Table<mineplex.database.tables.records.AccountPetsRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, mineplex.database.Account.Account, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<mineplex.database.tables.records.AccountPetsRecord> getPrimaryKey() {
		return mineplex.database.Keys.KEY_accountPets_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<mineplex.database.tables.records.AccountPetsRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<mineplex.database.tables.records.AccountPetsRecord>>asList(mineplex.database.Keys.KEY_accountPets_PRIMARY, mineplex.database.Keys.KEY_accountPets_UNIQUE_ACCOUNT_PET);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<mineplex.database.tables.records.AccountPetsRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<mineplex.database.tables.records.AccountPetsRecord, ?>>asList(mineplex.database.Keys.accountIdKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public mineplex.database.tables.AccountPets as(java.lang.String alias) {
		return new mineplex.database.tables.AccountPets(alias, this);
	}

	/**
	 * Rename this table
	 */
	public mineplex.database.tables.AccountPets rename(java.lang.String name) {
		return new mineplex.database.tables.AccountPets(name, null);
	}
}
