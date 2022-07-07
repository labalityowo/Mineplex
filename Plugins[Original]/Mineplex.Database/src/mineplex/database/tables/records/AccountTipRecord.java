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
public class AccountTipRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.AccountTipRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record2<java.lang.Integer, java.lang.Integer> {

	private static final long serialVersionUID = 1097060857;

	/**
	 * Setter for <code>Account.accountTip.accountId</code>.
	 */
	public void setAccountId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.accountTip.accountId</code>.
	 */
	public java.lang.Integer getAccountId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.accountTip.tips</code>.
	 */
	public void setTips(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.accountTip.tips</code>.
	 */
	public java.lang.Integer getTips() {
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
		return mineplex.database.tables.AccountTip.accountTip.accountId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return mineplex.database.tables.AccountTip.accountTip.tips;
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
		return getTips();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountTipRecord value1(java.lang.Integer value) {
		setAccountId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountTipRecord value2(java.lang.Integer value) {
		setTips(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountTipRecord values(java.lang.Integer value1, java.lang.Integer value2) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached AccountTipRecord
	 */
	public AccountTipRecord() {
		super(mineplex.database.tables.AccountTip.accountTip);
	}

	/**
	 * Create a detached, initialised AccountTipRecord
	 */
	public AccountTipRecord(java.lang.Integer accountId, java.lang.Integer tips) {
		super(mineplex.database.tables.AccountTip.accountTip);

		setValue(0, accountId);
		setValue(1, tips);
	}
}