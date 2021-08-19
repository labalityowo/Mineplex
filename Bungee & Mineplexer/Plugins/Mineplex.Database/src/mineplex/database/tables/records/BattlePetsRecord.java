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
public class BattlePetsRecord extends org.jooq.impl.UpdatableRecordImpl<mineplex.database.tables.records.BattlePetsRecord> implements java.io.Serializable, java.lang.Cloneable, org.jooq.Record12<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, org.jooq.types.UInteger, java.lang.Integer, java.lang.Byte, java.lang.Double, java.lang.Integer, java.lang.Integer, java.lang.Double, java.lang.Double> {

	private static final long serialVersionUID = 2004238101;

	/**
	 * Setter for <code>Account.battlePets.petId</code>.
	 */
	public void setPetId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>Account.battlePets.petId</code>.
	 */
	public java.lang.Integer getPetId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>Account.battlePets.accountId</code>.
	 */
	public void setAccountId(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>Account.battlePets.accountId</code>.
	 */
	public java.lang.Integer getAccountId() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>Account.battlePets.petType</code>.
	 */
	public void setPetType(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>Account.battlePets.petType</code>.
	 */
	public java.lang.String getPetType() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>Account.battlePets.petName</code>.
	 */
	public void setPetName(java.lang.String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>Account.battlePets.petName</code>.
	 */
	public java.lang.String getPetName() {
		return (java.lang.String) getValue(3);
	}

	/**
	 * Setter for <code>Account.battlePets.ageStamp</code>.
	 */
	public void setAgeStamp(org.jooq.types.UInteger value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>Account.battlePets.ageStamp</code>.
	 */
	public org.jooq.types.UInteger getAgeStamp() {
		return (org.jooq.types.UInteger) getValue(4);
	}

	/**
	 * Setter for <code>Account.battlePets.lastActive</code>.
	 */
	public void setLastActive(java.lang.Integer value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>Account.battlePets.lastActive</code>.
	 */
	public java.lang.Integer getLastActive() {
		return (java.lang.Integer) getValue(5);
	}

	/**
	 * Setter for <code>Account.battlePets.active</code>.
	 */
	public void setActive(java.lang.Byte value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>Account.battlePets.active</code>.
	 */
	public java.lang.Byte getActive() {
		return (java.lang.Byte) getValue(6);
	}

	/**
	 * Setter for <code>Account.battlePets.level</code>.
	 */
	public void setLevel(java.lang.Double value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>Account.battlePets.level</code>.
	 */
	public java.lang.Double getLevel() {
		return (java.lang.Double) getValue(7);
	}

	/**
	 * Setter for <code>Account.battlePets.battles</code>.
	 */
	public void setBattles(java.lang.Integer value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>Account.battlePets.battles</code>.
	 */
	public java.lang.Integer getBattles() {
		return (java.lang.Integer) getValue(8);
	}

	/**
	 * Setter for <code>Account.battlePets.wins</code>.
	 */
	public void setWins(java.lang.Integer value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>Account.battlePets.wins</code>.
	 */
	public java.lang.Integer getWins() {
		return (java.lang.Integer) getValue(9);
	}

	/**
	 * Setter for <code>Account.battlePets.hunger</code>.
	 */
	public void setHunger(java.lang.Double value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>Account.battlePets.hunger</code>.
	 */
	public java.lang.Double getHunger() {
		return (java.lang.Double) getValue(10);
	}

	/**
	 * Setter for <code>Account.battlePets.tiredness</code>.
	 */
	public void setTiredness(java.lang.Double value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>Account.battlePets.tiredness</code>.
	 */
	public java.lang.Double getTiredness() {
		return (java.lang.Double) getValue(11);
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
	// Record12 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row12<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, org.jooq.types.UInteger, java.lang.Integer, java.lang.Byte, java.lang.Double, java.lang.Integer, java.lang.Integer, java.lang.Double, java.lang.Double> fieldsRow() {
		return (org.jooq.Row12) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row12<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, org.jooq.types.UInteger, java.lang.Integer, java.lang.Byte, java.lang.Double, java.lang.Integer, java.lang.Integer, java.lang.Double, java.lang.Double> valuesRow() {
		return (org.jooq.Row12) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return mineplex.database.tables.BattlePets.battlePets.petId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return mineplex.database.tables.BattlePets.battlePets.accountId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return mineplex.database.tables.BattlePets.battlePets.petType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field4() {
		return mineplex.database.tables.BattlePets.battlePets.petName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<org.jooq.types.UInteger> field5() {
		return mineplex.database.tables.BattlePets.battlePets.ageStamp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field6() {
		return mineplex.database.tables.BattlePets.battlePets.lastActive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Byte> field7() {
		return mineplex.database.tables.BattlePets.battlePets.active;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Double> field8() {
		return mineplex.database.tables.BattlePets.battlePets.level;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field9() {
		return mineplex.database.tables.BattlePets.battlePets.battles;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field10() {
		return mineplex.database.tables.BattlePets.battlePets.wins;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Double> field11() {
		return mineplex.database.tables.BattlePets.battlePets.hunger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Double> field12() {
		return mineplex.database.tables.BattlePets.battlePets.tiredness;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getPetId();
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
		return getPetType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value4() {
		return getPetName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.types.UInteger value5() {
		return getAgeStamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value6() {
		return getLastActive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Byte value7() {
		return getActive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Double value8() {
		return getLevel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value9() {
		return getBattles();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value10() {
		return getWins();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Double value11() {
		return getHunger();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Double value12() {
		return getTiredness();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value1(java.lang.Integer value) {
		setPetId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value2(java.lang.Integer value) {
		setAccountId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value3(java.lang.String value) {
		setPetType(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value4(java.lang.String value) {
		setPetName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value5(org.jooq.types.UInteger value) {
		setAgeStamp(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value6(java.lang.Integer value) {
		setLastActive(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value7(java.lang.Byte value) {
		setActive(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value8(java.lang.Double value) {
		setLevel(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value9(java.lang.Integer value) {
		setBattles(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value10(java.lang.Integer value) {
		setWins(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value11(java.lang.Double value) {
		setHunger(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord value12(java.lang.Double value) {
		setTiredness(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattlePetsRecord values(java.lang.Integer value1, java.lang.Integer value2, java.lang.String value3, java.lang.String value4, org.jooq.types.UInteger value5, java.lang.Integer value6, java.lang.Byte value7, java.lang.Double value8, java.lang.Integer value9, java.lang.Integer value10, java.lang.Double value11, java.lang.Double value12) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached BattlePetsRecord
	 */
	public BattlePetsRecord() {
		super(mineplex.database.tables.BattlePets.battlePets);
	}

	/**
	 * Create a detached, initialised BattlePetsRecord
	 */
	public BattlePetsRecord(java.lang.Integer petId, java.lang.Integer accountId, java.lang.String petType, java.lang.String petName, org.jooq.types.UInteger ageStamp, java.lang.Integer lastActive, java.lang.Byte active, java.lang.Double level, java.lang.Integer battles, java.lang.Integer wins, java.lang.Double hunger, java.lang.Double tiredness) {
		super(mineplex.database.tables.BattlePets.battlePets);

		setValue(0, petId);
		setValue(1, accountId);
		setValue(2, petType);
		setValue(3, petName);
		setValue(4, ageStamp);
		setValue(5, lastActive);
		setValue(6, active);
		setValue(7, level);
		setValue(8, battles);
		setValue(9, wins);
		setValue(10, hunger);
		setValue(11, tiredness);
	}
}
