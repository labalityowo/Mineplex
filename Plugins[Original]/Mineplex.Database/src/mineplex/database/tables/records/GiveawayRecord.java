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
public class GiveawayRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.GiveawayRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record10<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Byte, java.lang.Integer, java.lang.Byte, java.lang.Byte> {

	private static final long serialVersionUID = -1809311307;

	/**
	 * Setter for <code>Account.giveaway.id</code>.
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.giveaway.id</code>.
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.giveaway.name</code>.
	 */
	public void setName(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.giveaway.name</code>.
	 */
	public java.lang.String getName() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>Account.giveaway.prettyName</code>.
	 */
	public void setPrettyName(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>Account.giveaway.prettyName</code>.
	 */
	public java.lang.String getPrettyName() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>Account.giveaway.header</code>.
	 */
	public void setHeader(java.lang.String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>Account.giveaway.header</code>.
	 */
	public java.lang.String getHeader() {
		return (java.lang.String) getValue(3);
	}

	/**
	 * Setter for <code>Account.giveaway.message</code>.
	 */
	public void setMessage(java.lang.String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>Account.giveaway.message</code>.
	 */
	public java.lang.String getMessage() {
		return (java.lang.String) getValue(4);
	}

	/**
	 * Setter for <code>Account.giveaway.max</code>.
	 */
	public void setMax(java.lang.Integer value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>Account.giveaway.max</code>.
	 */
	public java.lang.Integer getMax() {
		return (java.lang.Integer) getValue(5);
	}

	/**
	 * Setter for <code>Account.giveaway.notifyNetwork</code>.
	 */
	public void setNotifyNetwork(java.lang.Byte value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>Account.giveaway.notifyNetwork</code>.
	 */
	public java.lang.Byte getNotifyNetwork() {
		return (java.lang.Byte) getValue(6);
	}

	/**
	 * Setter for <code>Account.giveaway.notifyCooldown</code>.
	 */
	public void setNotifyCooldown(java.lang.Integer value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>Account.giveaway.notifyCooldown</code>.
	 */
	public java.lang.Integer getNotifyCooldown() {
		return (java.lang.Integer) getValue(7);
	}

	/**
	 * Setter for <code>Account.giveaway.canWinTwice</code>.
	 */
	public void setCanWinTwice(java.lang.Byte value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>Account.giveaway.canWinTwice</code>.
	 */
	public java.lang.Byte getCanWinTwice() {
		return (java.lang.Byte) getValue(8);
	}

	/**
	 * Setter for <code>Account.giveaway.enabled</code>.
	 */
	public void setEnabled(java.lang.Byte value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>Account.giveaway.enabled</code>.
	 */
	public java.lang.Byte getEnabled() {
		return (java.lang.Byte) getValue(9);
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
	// Record10 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row10<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Byte, java.lang.Integer, java.lang.Byte, java.lang.Byte> fieldsRow() {
		return (org.jooq.Row10) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row10<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Byte, java.lang.Integer, java.lang.Byte, java.lang.Byte> valuesRow() {
		return (org.jooq.Row10) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return mineplex.database.tables.Giveaway.giveaway.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return mineplex.database.tables.Giveaway.giveaway.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return mineplex.database.tables.Giveaway.giveaway.prettyName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field4() {
		return mineplex.database.tables.Giveaway.giveaway.header;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field5() {
		return mineplex.database.tables.Giveaway.giveaway.message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field6() {
		return mineplex.database.tables.Giveaway.giveaway.max;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Byte> field7() {
		return mineplex.database.tables.Giveaway.giveaway.notifyNetwork;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field8() {
		return mineplex.database.tables.Giveaway.giveaway.notifyCooldown;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Byte> field9() {
		return mineplex.database.tables.Giveaway.giveaway.canWinTwice;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Byte> field10() {
		return mineplex.database.tables.Giveaway.giveaway.enabled;
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
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getPrettyName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value4() {
		return getHeader();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value5() {
		return getMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value6() {
		return getMax();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Byte value7() {
		return getNotifyNetwork();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value8() {
		return getNotifyCooldown();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Byte value9() {
		return getCanWinTwice();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Byte value10() {
		return getEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value2(java.lang.String value) {
		setName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value3(java.lang.String value) {
		setPrettyName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value4(java.lang.String value) {
		setHeader(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value5(java.lang.String value) {
		setMessage(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value6(java.lang.Integer value) {
		setMax(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value7(java.lang.Byte value) {
		setNotifyNetwork(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value8(java.lang.Integer value) {
		setNotifyCooldown(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value9(java.lang.Byte value) {
		setCanWinTwice(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord value10(java.lang.Byte value) {
		setEnabled(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GiveawayRecord values(java.lang.Integer value1, java.lang.String value2, java.lang.String value3, java.lang.String value4, java.lang.String value5, java.lang.Integer value6, java.lang.Byte value7, java.lang.Integer value8, java.lang.Byte value9, java.lang.Byte value10) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached GiveawayRecord
	 */
	public GiveawayRecord() {
		super(mineplex.database.tables.Giveaway.giveaway);
	}

	/**
	 * Create a detached, initialised GiveawayRecord
	 */
	public GiveawayRecord(java.lang.Integer id, java.lang.String name, java.lang.String prettyName, java.lang.String header, java.lang.String message, java.lang.Integer max, java.lang.Byte notifyNetwork, java.lang.Integer notifyCooldown, java.lang.Byte canWinTwice, java.lang.Byte enabled) {
		super(mineplex.database.tables.Giveaway.giveaway);

		setValue(0, id);
		setValue(1, name);
		setValue(2, prettyName);
		setValue(3, header);
		setValue(4, message);
		setValue(5, max);
		setValue(6, notifyNetwork);
		setValue(7, notifyCooldown);
		setValue(8, canWinTwice);
		setValue(9, enabled);
	}
}