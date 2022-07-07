/**
 * This class is generated by jOOQ
 */
package mineplex.database.tables.records;

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
public class AccountThankRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.AccountThankRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record2<java.lang.Integer, java.lang.Integer> {

	private static final long serialVersionUID = -183734569;

	/**
	 * Setter for <code>Account.accountThank.accountId</code>.
	 */
	public void setAccountId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.accountThank.accountId</code>.
	 */
	public java.lang.Integer getAccountId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.accountThank.thanks</code>.
	 */
	public void setThanks(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.accountThank.thanks</code>.
	 */
	public java.lang.Integer getThanks() {
		return (java.lang.Integer) getValue(1);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record2 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.Integer> fieldsRow() {
		return (org.jooq.Row2) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.Integer> valuesRow() {
		return (org.jooq.Row2) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return mineplex.database.tables.AccountThank.accountThank.accountId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return mineplex.database.tables.AccountThank.accountThank.thanks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getAccountId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value2() {
		return getThanks();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountThankRecord value1(java.lang.Integer value) {
		setAccountId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountThankRecord value2(java.lang.Integer value) {
		setThanks(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountThankRecord values(java.lang.Integer value1, java.lang.Integer value2) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached AccountThankRecord
	 */
	public AccountThankRecord() {
		super(mineplex.database.tables.AccountThank.accountThank);
	}

	/**
	 * Create a detached, initialised AccountThankRecord
	 */
	public AccountThankRecord(java.lang.Integer accountId, java.lang.Integer thanks) {
		super(mineplex.database.tables.AccountThank.accountThank);

		setValue(0, accountId);
		setValue(1, thanks);
	}
}
