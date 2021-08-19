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
public class ServerPasswordRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.ServerPasswordRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record3<java.lang.Integer, java.lang.String, java.lang.String> {

	private static final long serialVersionUID = -1975811256;

	/**
	 * Setter for <code>Account.serverPassword.id</code>.
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.serverPassword.id</code>.
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.serverPassword.server</code>.
	 */
	public void setServer(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.serverPassword.server</code>.
	 */
	public java.lang.String getServer() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>Account.serverPassword.password</code>.
	 */
	public void setPassword(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>Account.serverPassword.password</code>.
	 */
	public java.lang.String getPassword() {
		return (java.lang.String) getValue(2);
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
	// Record3 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.Integer, java.lang.String, java.lang.String> fieldsRow() {
		return (org.jooq.Row3) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.Integer, java.lang.String, java.lang.String> valuesRow() {
		return (org.jooq.Row3) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return mineplex.database.tables.ServerPassword.serverPassword.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return mineplex.database.tables.ServerPassword.serverPassword.server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return mineplex.database.tables.ServerPassword.serverPassword.password;
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
		return getServer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getPassword();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerPasswordRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerPasswordRecord value2(java.lang.String value) {
		setServer(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerPasswordRecord value3(java.lang.String value) {
		setPassword(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerPasswordRecord values(java.lang.Integer value1, java.lang.String value2, java.lang.String value3) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached ServerPasswordRecord
	 */
	public ServerPasswordRecord() {
		super(mineplex.database.tables.ServerPassword.serverPassword);
	}

	/**
	 * Create a detached, initialised ServerPasswordRecord
	 */
	public ServerPasswordRecord(java.lang.Integer id, java.lang.String server, java.lang.String password) {
		super(mineplex.database.tables.ServerPassword.serverPassword);

		setValue(0, id);
		setValue(1, server);
		setValue(2, password);
	}
}
