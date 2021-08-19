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
public class AccountMortenSpamTestRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.AccountMortenSpamTestRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record5<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.Integer> {

	private static final long serialVersionUID = -846015893;

	/**
	 * Setter for <code>Account.accountMortenSpamTest.id</code>.
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.accountMortenSpamTest.id</code>.
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.accountMortenSpamTest.accountId</code>.
	 */
	public void setAccountId(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.accountMortenSpamTest.accountId</code>.
	 */
	public java.lang.Integer getAccountId() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>Account.accountMortenSpamTest.text</code>.
	 */
	public void setText(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>Account.accountMortenSpamTest.text</code>.
	 */
	public java.lang.String getText() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>Account.accountMortenSpamTest.punished</code>.
	 */
	public void setPunished(java.lang.Integer value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>Account.accountMortenSpamTest.punished</code>.
	 */
	public java.lang.Integer getPunished() {
		return (java.lang.Integer) getValue(3);
	}

	/**
	 * Setter for <code>Account.accountMortenSpamTest.amountPunished</code>.
	 */
	public void setAmountPunished(java.lang.Integer value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>Account.accountMortenSpamTest.amountPunished</code>.
	 */
	public java.lang.Integer getAmountPunished() {
		return (java.lang.Integer) getValue(4);
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
	// Record5 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row5<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.Integer> fieldsRow() {
		return (org.jooq.Row5) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row5<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.Integer> valuesRow() {
		return (org.jooq.Row5) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return mineplex.database.tables.AccountMortenSpamTest.accountMortenSpamTest.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return mineplex.database.tables.AccountMortenSpamTest.accountMortenSpamTest.accountId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return mineplex.database.tables.AccountMortenSpamTest.accountMortenSpamTest.text;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field4() {
		return mineplex.database.tables.AccountMortenSpamTest.accountMortenSpamTest.punished;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field5() {
		return mineplex.database.tables.AccountMortenSpamTest.accountMortenSpamTest.amountPunished;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value2() {
		return getAccountId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getText();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value4() {
		return getPunished();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value5() {
		return getAmountPunished();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountMortenSpamTestRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountMortenSpamTestRecord value2(java.lang.Integer value) {
		setAccountId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountMortenSpamTestRecord value3(java.lang.String value) {
		setText(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountMortenSpamTestRecord value4(java.lang.Integer value) {
		setPunished(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountMortenSpamTestRecord value5(java.lang.Integer value) {
		setAmountPunished(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountMortenSpamTestRecord values(java.lang.Integer value1, java.lang.Integer value2, java.lang.String value3, java.lang.Integer value4, java.lang.Integer value5) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached AccountMortenSpamTestRecord
	 */
	public AccountMortenSpamTestRecord() {
		super(mineplex.database.tables.AccountMortenSpamTest.accountMortenSpamTest);
	}

	/**
	 * Create a detached, initialised AccountMortenSpamTestRecord
	 */
	public AccountMortenSpamTestRecord(java.lang.Integer id, java.lang.Integer accountId, java.lang.String text, java.lang.Integer punished, java.lang.Integer amountPunished) {
		super(mineplex.database.tables.AccountMortenSpamTest.accountMortenSpamTest);

		setValue(0, id);
		setValue(1, accountId);
		setValue(2, text);
		setValue(3, punished);
		setValue(4, amountPunished);
	}
}
