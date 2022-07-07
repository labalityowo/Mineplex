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
public class ClanWarRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.ClanWarRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record8<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Byte, java.sql.Timestamp, java.sql.Timestamp, java.sql.Timestamp> {

	private static final long serialVersionUID = -2028180122;

	/**
	 * Setter for <code>Account.clanWar.id</code>.
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.clanWar.id</code>.
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.clanWar.initiatorId</code>.
	 */
	public void setInitiatorId(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.clanWar.initiatorId</code>.
	 */
	public java.lang.Integer getInitiatorId() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>Account.clanWar.clanId</code>.
	 */
	public void setClanId(java.lang.Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>Account.clanWar.clanId</code>.
	 */
	public java.lang.Integer getClanId() {
		return (java.lang.Integer) getValue(2);
	}

	/**
	 * Setter for <code>Account.clanWar.score</code>.
	 */
	public void setScore(java.lang.Integer value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>Account.clanWar.score</code>.
	 */
	public java.lang.Integer getScore() {
		return (java.lang.Integer) getValue(3);
	}

	/**
	 * Setter for <code>Account.clanWar.completed</code>.
	 */
	public void setCompleted(java.lang.Byte value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>Account.clanWar.completed</code>.
	 */
	public java.lang.Byte getCompleted() {
		return (java.lang.Byte) getValue(4);
	}

	/**
	 * Setter for <code>Account.clanWar.created</code>.
	 */
	public void setCreated(java.sql.Timestamp value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>Account.clanWar.created</code>.
	 */
	public java.sql.Timestamp getCreated() {
		return (java.sql.Timestamp) getValue(5);
	}

	/**
	 * Setter for <code>Account.clanWar.ended</code>.
	 */
	public void setEnded(java.sql.Timestamp value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>Account.clanWar.ended</code>.
	 */
	public java.sql.Timestamp getEnded() {
		return (java.sql.Timestamp) getValue(6);
	}

	/**
	 * Setter for <code>Account.clanWar.lastUpdated</code>.
	 */
	public void setLastUpdated(java.sql.Timestamp value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>Account.clanWar.lastUpdated</code>.
	 */
	public java.sql.Timestamp getLastUpdated() {
		return (java.sql.Timestamp) getValue(7);
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
	// Record8 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row8<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Byte, java.sql.Timestamp, java.sql.Timestamp, java.sql.Timestamp> fieldsRow() {
		return (org.jooq.Row8) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row8<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Byte, java.sql.Timestamp, java.sql.Timestamp, java.sql.Timestamp> valuesRow() {
		return (org.jooq.Row8) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return mineplex.database.tables.ClanWar.clanWar.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return mineplex.database.tables.ClanWar.clanWar.initiatorId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field3() {
		return mineplex.database.tables.ClanWar.clanWar.clanId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field4() {
		return mineplex.database.tables.ClanWar.clanWar.score;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Byte> field5() {
		return mineplex.database.tables.ClanWar.clanWar.completed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.sql.Timestamp> field6() {
		return mineplex.database.tables.ClanWar.clanWar.created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.sql.Timestamp> field7() {
		return mineplex.database.tables.ClanWar.clanWar.ended;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.sql.Timestamp> field8() {
		return mineplex.database.tables.ClanWar.clanWar.lastUpdated;
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
		return getInitiatorId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value3() {
		return getClanId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value4() {
		return getScore();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Byte value5() {
		return getCompleted();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.sql.Timestamp value6() {
		return getCreated();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.sql.Timestamp value7() {
		return getEnded();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.sql.Timestamp value8() {
		return getLastUpdated();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord value2(java.lang.Integer value) {
		setInitiatorId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord value3(java.lang.Integer value) {
		setClanId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord value4(java.lang.Integer value) {
		setScore(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord value5(java.lang.Byte value) {
		setCompleted(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord value6(java.sql.Timestamp value) {
		setCreated(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord value7(java.sql.Timestamp value) {
		setEnded(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord value8(java.sql.Timestamp value) {
		setLastUpdated(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClanWarRecord values(java.lang.Integer value1, java.lang.Integer value2, java.lang.Integer value3, java.lang.Integer value4, java.lang.Byte value5, java.sql.Timestamp value6, java.sql.Timestamp value7, java.sql.Timestamp value8) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached ClanWarRecord
	 */
	public ClanWarRecord() {
		super(mineplex.database.tables.ClanWar.clanWar);
	}

	/**
	 * Create a detached, initialised ClanWarRecord
	 */
	public ClanWarRecord(java.lang.Integer id, java.lang.Integer initiatorId, java.lang.Integer clanId, java.lang.Integer score, java.lang.Byte completed, java.sql.Timestamp created, java.sql.Timestamp ended, java.sql.Timestamp lastUpdated) {
		super(mineplex.database.tables.ClanWar.clanWar);

		setValue(0, id);
		setValue(1, initiatorId);
		setValue(2, clanId);
		setValue(3, score);
		setValue(4, completed);
		setValue(5, created);
		setValue(6, ended);
		setValue(7, lastUpdated);
	}
}
