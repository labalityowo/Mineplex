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
public class AccountFriendRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.AccountFriendRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record7<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.sql.Timestamp, java.lang.Integer, java.lang.Integer> {

	private static final long serialVersionUID = -1039002159;

	/**
	 * Setter for <code>Account.accountFriend.id</code>.
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.accountFriend.id</code>.
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.accountFriend.uuidSource</code>.
	 */
	public void setUuidSource(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.accountFriend.uuidSource</code>.
	 */
	public java.lang.String getUuidSource() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>Account.accountFriend.uuidTarget</code>.
	 */
	public void setUuidTarget(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>Account.accountFriend.uuidTarget</code>.
	 */
	public java.lang.String getUuidTarget() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>Account.accountFriend.status</code>.
	 */
	public void setStatus(java.lang.String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>Account.accountFriend.status</code>.
	 */
	public java.lang.String getStatus() {
		return (java.lang.String) getValue(3);
	}

	/**
	 * Setter for <code>Account.accountFriend.created</code>.
	 */
	public void setCreated(java.sql.Timestamp value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>Account.accountFriend.created</code>.
	 */
	public java.sql.Timestamp getCreated() {
		return (java.sql.Timestamp) getValue(4);
	}

	/**
	 * Setter for <code>Account.accountFriend.sourceId</code>.
	 */
	public void setSourceId(java.lang.Integer value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>Account.accountFriend.sourceId</code>.
	 */
	public java.lang.Integer getSourceId() {
		return (java.lang.Integer) getValue(5);
	}

	/**
	 * Setter for <code>Account.accountFriend.targetId</code>.
	 */
	public void setTargetId(java.lang.Integer value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>Account.accountFriend.targetId</code>.
	 */
	public java.lang.Integer getTargetId() {
		return (java.lang.Integer) getValue(6);
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
	// Record7 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row7<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.sql.Timestamp, java.lang.Integer, java.lang.Integer> fieldsRow() {
		return (org.jooq.Row7) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row7<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.sql.Timestamp, java.lang.Integer, java.lang.Integer> valuesRow() {
		return (org.jooq.Row7) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return mineplex.database.tables.AccountFriend.accountFriend.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return mineplex.database.tables.AccountFriend.accountFriend.uuidSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return mineplex.database.tables.AccountFriend.accountFriend.uuidTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field4() {
		return mineplex.database.tables.AccountFriend.accountFriend.status;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.sql.Timestamp> field5() {
		return mineplex.database.tables.AccountFriend.accountFriend.created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field6() {
		return mineplex.database.tables.AccountFriend.accountFriend.sourceId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field7() {
		return mineplex.database.tables.AccountFriend.accountFriend.targetId;
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
	public java.lang.String value2() {
		return getUuidSource();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getUuidTarget();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value4() {
		return getStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.sql.Timestamp value5() {
		return getCreated();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value6() {
		return getSourceId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value7() {
		return getTargetId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountFriendRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountFriendRecord value2(java.lang.String value) {
		setUuidSource(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountFriendRecord value3(java.lang.String value) {
		setUuidTarget(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountFriendRecord value4(java.lang.String value) {
		setStatus(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountFriendRecord value5(java.sql.Timestamp value) {
		setCreated(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountFriendRecord value6(java.lang.Integer value) {
		setSourceId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountFriendRecord value7(java.lang.Integer value) {
		setTargetId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountFriendRecord values(java.lang.Integer value1, java.lang.String value2, java.lang.String value3, java.lang.String value4, java.sql.Timestamp value5, java.lang.Integer value6, java.lang.Integer value7) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached AccountFriendRecord
	 */
	public AccountFriendRecord() {
		super(mineplex.database.tables.AccountFriend.accountFriend);
	}

	/**
	 * Create a detached, initialised AccountFriendRecord
	 */
	public AccountFriendRecord(java.lang.Integer id, java.lang.String uuidSource, java.lang.String uuidTarget, java.lang.String status, java.sql.Timestamp created, java.lang.Integer sourceId, java.lang.Integer targetId) {
		super(mineplex.database.tables.AccountFriend.accountFriend);

		setValue(0, id);
		setValue(1, uuidSource);
		setValue(2, uuidTarget);
		setValue(3, status);
		setValue(4, created);
		setValue(5, sourceId);
		setValue(6, targetId);
	}
}
