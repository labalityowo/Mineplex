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
public class ReportTicketsRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.ReportTicketsRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record8<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String> {

	private static final long serialVersionUID = 1659005000;

	/**
	 * Setter for <code>Account.reportTickets.reportId</code>.
	 */
	public void setReportId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.reportTickets.reportId</code>.
	 */
	public java.lang.Integer getReportId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.reportTickets.date</code>.
	 */
	public void setDate(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.reportTickets.date</code>.
	 */
	public java.lang.String getDate() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>Account.reportTickets.eventDate</code>.
	 */
	public void setEventDate(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>Account.reportTickets.eventDate</code>.
	 */
	public java.lang.String getEventDate() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>Account.reportTickets.playerId</code>.
	 */
	public void setPlayerId(java.lang.Integer value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>Account.reportTickets.playerId</code>.
	 */
	public java.lang.Integer getPlayerId() {
		return (java.lang.Integer) getValue(3);
	}

	/**
	 * Setter for <code>Account.reportTickets.server</code>.
	 */
	public void setServer(java.lang.String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>Account.reportTickets.server</code>.
	 */
	public java.lang.String getServer() {
		return (java.lang.String) getValue(4);
	}

	/**
	 * Setter for <code>Account.reportTickets.closerId</code>.
	 */
	public void setCloserId(java.lang.Integer value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>Account.reportTickets.closerId</code>.
	 */
	public java.lang.Integer getCloserId() {
		return (java.lang.Integer) getValue(5);
	}

	/**
	 * Setter for <code>Account.reportTickets.result</code>.
	 */
	public void setResult(java.lang.String value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>Account.reportTickets.result</code>.
	 */
	public java.lang.String getResult() {
		return (java.lang.String) getValue(6);
	}

	/**
	 * Setter for <code>Account.reportTickets.reason</code>.
	 */
	public void setReason(java.lang.String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>Account.reportTickets.reason</code>.
	 */
	public java.lang.String getReason() {
		return (java.lang.String) getValue(7);
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
	public org.jooq.Row8<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String> fieldsRow() {
		return (org.jooq.Row8) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row8<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String> valuesRow() {
		return (org.jooq.Row8) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return mineplex.database.tables.ReportTickets.reportTickets.reportId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return mineplex.database.tables.ReportTickets.reportTickets.date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return mineplex.database.tables.ReportTickets.reportTickets.eventDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field4() {
		return mineplex.database.tables.ReportTickets.reportTickets.playerId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field5() {
		return mineplex.database.tables.ReportTickets.reportTickets.server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field6() {
		return mineplex.database.tables.ReportTickets.reportTickets.closerId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field7() {
		return mineplex.database.tables.ReportTickets.reportTickets.result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field8() {
		return mineplex.database.tables.ReportTickets.reportTickets.reason;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getReportId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getDate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getEventDate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value4() {
		return getPlayerId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value5() {
		return getServer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value6() {
		return getCloserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value7() {
		return getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value8() {
		return getReason();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord value1(java.lang.Integer value) {
		setReportId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord value2(java.lang.String value) {
		setDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord value3(java.lang.String value) {
		setEventDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord value4(java.lang.Integer value) {
		setPlayerId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord value5(java.lang.String value) {
		setServer(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord value6(java.lang.Integer value) {
		setCloserId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord value7(java.lang.String value) {
		setResult(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord value8(java.lang.String value) {
		setReason(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReportTicketsRecord values(java.lang.Integer value1, java.lang.String value2, java.lang.String value3, java.lang.Integer value4, java.lang.String value5, java.lang.Integer value6, java.lang.String value7, java.lang.String value8) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached ReportTicketsRecord
	 */
	public ReportTicketsRecord() {
		super(mineplex.database.tables.ReportTickets.reportTickets);
	}

	/**
	 * Create a detached, initialised ReportTicketsRecord
	 */
	public ReportTicketsRecord(java.lang.Integer reportId, java.lang.String date, java.lang.String eventDate, java.lang.Integer playerId, java.lang.String server, java.lang.Integer closerId, java.lang.String result, java.lang.String reason) {
		super(mineplex.database.tables.ReportTickets.reportTickets);

		setValue(0, reportId);
		setValue(1, date);
		setValue(2, eventDate);
		setValue(3, playerId);
		setValue(4, server);
		setValue(5, closerId);
		setValue(6, result);
		setValue(7, reason);
	}
}