package mineplex.core.progression;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.KitSelectorGadget;
import mineplex.core.progression.math.Calculations;

/**
 * The API class all kits with the new system must use
 */
public interface ProgressiveKit
{

	/**
	 * The name of the kit for displaying
	 *
	 * @return The kit's display name
	 */
	String getDisplayName();

	/**
	 * This is the name stored in the database and checked against
	 *
	 * @return The internal name of this kit
	 */
	String getInternalName();

	/**
	 * Get the Icon to display in the GUI
	 *
	 * @return This kit's icon
	 */
	Material getIcon();

	/**
	 * Get the description for this kit
	 *
	 * @return This kit's description
	 */
	String[] getDescription();

	/**
	 * This method gives the GUI information to display about the kit's upgrades and what they do
	 * This information is being directly displayed in a GUI, so make sure it looks good
	 *
	 * @return The map of upgrade level to a list of upgrade details
	 */
	Map<Integer, List<String>> getUpgradeDetails();

	/**
	 * Determines whether or not the kit UI is displayed with "Coming Soon" or actual upgrade details.
	 *
	 * @return <code>true</code> if the menu should populate from {@code getAbilityDetails}
	 */
	default boolean showUpgrades()
	{
		return false;
	}

	/**
	 * Retrieve the current level of this kit
	 *
	 * @param player The UUID of the player whose level you wish to check
	 * @return The kit's current level
	 */
	int getLevel(UUID player);

	//Should this be a double? I don't think so...

	/**
	 * Get the current amount of XP for this kit
	 * NOTE: XP Resets after each level up
	 *
	 * @param player The UUID of the player whose XP you wish to check
	 * @return The kit's current XP
	 */
	int getXp(UUID player);

	/**
	 * Get the current state of upgrades for this kit
	 * X/5 with 5 being the highest.
	 * Linearly activated upon meeting certain criteria
	 *
	 * @param player The UUID of the player whose upgrade level you wish to check
	 * @return The kit's current upgrade level out of 5
	 */
	int getUpgradeLevel(UUID player);

	/**
	 * Is this kit set as a default kit for this player?
	 *
	 * @param player The UUID of the player whose default type for this kit you wish to check
	 * @return Whether or not this kit is a default
	 */
	boolean isDefault(UUID player);

	/**
	 * Gets whether or not this kit has been selected by the player
	 *
	 * @param player The UUID of the player
	 * @return Whether or not this kit has been selected by the player
	 */
	boolean isSelected(UUID player);

	/**
	 * Upgrade the player's kit to the current level
	 *
	 * @param upgradeLevel The level to upgrade too
	 * @param player       The player who we want to upgrade for.
	 */
	void upgrade(int upgradeLevel, UUID player);

	/**
	 * Toggle whether or not this kit is default for the specified player
	 *
	 * @param defaultValue Whether or not this kit should be default
	 * @param player       The specific player for whom this kit's default staturs will be toggled
	 */
	void setDefault(boolean defaultValue, UUID player);

	/**
	 * Set the player's overall level for this kit
	 *
	 * @param level  The level to which you wish to set the player's kit
	 * @param player The UUID of the player
	 */
	void setLevel(int level, UUID player);

	/**
	 * Set the player's overall xp for this kit (relative to his current level, this is not TOTAL xp)
	 *
	 * @param xp     The xp to which you wish to set the player's kit
	 * @param player The UUID of the player
	 */
	void setXp(int xp, UUID player);

	/**
	 * Set the player's overall upgradeLevel for this kit
	 *
	 * @param upgradeLevel The upgradeLevel to which you wish to set the player's kit
	 * @param player       The UUID of the player
	 */
	void setUpgradeLevel(int upgradeLevel, UUID player);

	/**
	 * This is called when a player selects a kit in the GUI
	 * This is where you do your logic for storing player kits in relation to the game
	 *
	 * @param player The player who selects this kit.
	 */
	void onSelected(UUID player);

	/**
	 * This is called when a player clicks the default button in the gui
	 * It will change the state of default
	 *
	 * @param player The player who toggles this kit's default setting.
	 */
	void onSetDefault(UUID player);

	/**
	 * This method is called when a player level ups
	 * If you have special effects or messages, here is the place to do it
	 *
	 * @param player The player who leveled up
	 */
	void onLevelUp(UUID player);

	/**
	 * Gets whether or not the player can purchase the upgrade level
	 *
	 * @param upgradeLevel The level of the upgrade you wish to check.
	 * @param player       The UUID of the player
	 * @return If the player can purchase the upgrade
	 */
	boolean canPurchaseUpgrade(UUID player, int upgradeLevel);

	/* ======================================================================================================
	 *
	 *
	 * Below here are utility methods that I believe people will be using frequently.
	 * If you need to override them, feel free to do so.
	 *
	 *
	 * ====================================================================================================== */

	/**
	 * Gets if a player has already leveled up
	 * Players are only allowed 1 level up per game.
	 *
	 * @param player The player's UUID we want to check against
	 * @return Whether or not the player has already leveled up in this game
	 */
	boolean alreadyLeveledUp(UUID player);

	/**
	 * Increment the players current level for this kit, and reset his XP
	 *
	 * @param player The player whose level we want to increase
	 */
	default void levelUp(UUID player)
	{
		setXp(1, player);

		setLevel(getLevel(player) + 1, player);

		onLevelUp(player);
	}

	/**
	 * Check to see if this player is eligible for level up
	 *
	 * @param player The player who we want to check
	 * @return True if he is able to level up, or false if he's not
	 */
	default boolean isLevelUpReady(UUID player)
	{
		int currentLevel = getLevel(player);

		int xp = getXp(player);
		int nextXP = Calculations.getXpForNextLevel(currentLevel);

		if (nextXP < xp)
		{
			//We don't want people to continue earning XP after the level up, so cancel this.
			//Makes sure that they only get the exact amount required.
			setXp(nextXP, player);
			return true;
		}

		return xp >= nextXP;
	}

	/**
	 * Get the difference (the amount needed) between the players current XP and his next levels XP
	 *
	 * @param player The player who we want to check against
	 * @return The integer difference between his current XP and his next level's required XP
	 */
	default int getXpDifference(UUID player)
	{
		int currentLevel = getLevel(player);

		int xp = getXp(player);
		int nextXP = Calculations.getXpForNextLevel(currentLevel);

		return nextXP - xp;
	}

	/**
	 * Retrieves whether or not the player currently owns this upgrade.
	 * This is the upgrade level, not the player level which the player must be to upgrade
	 * So it should be between 1 and 5
	 *
	 * @param player       The UUID of the player you wish to check
	 * @param upgradeLevel The level to check and see if the player owns
	 * @return If the player has the current upgrade
	 */
	default boolean ownsUpgrade(UUID player, int upgradeLevel)
	{
		return getUpgradeLevel(player) >= upgradeLevel;
	}

	default void displaySelectedEffect(Entity kitHost, GadgetManager gadgetManager, Player displayTo)
	{
		Gadget gadget = gadgetManager.getActive(displayTo, GadgetType.KIT_SELECTOR);
		if (gadget != null)
		{
			KitSelectorGadget kitSelectorGadget = (KitSelectorGadget) gadget;
			kitSelectorGadget.playParticle(kitHost, displayTo);
			return;
		}
		for (int i = 0; i < 1; i++)
		{
			double lead = i * ((2d * Math.PI) / 2);
			float x = (float) (Math.cos(kitHost.getTicksLived() / 5d + lead) * 1f);
			float z1 = (float) (Math.sin(kitHost.getTicksLived() / 5d + lead) * 1f);
			float z2 = (float) -(Math.sin(kitHost.getTicksLived() / 5d + lead) * 1f);
			float y = (float) (Math.sin(kitHost.getTicksLived() / 5d + lead) + 1f);

			UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, kitHost.getLocation().add(x, y, z1), 0f, 0f, 0f, 0, 1,
			  ViewDist.NORMAL, displayTo);
			UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, kitHost.getLocation().add(x, y, z2), 0f, 0f, 0f, 0, 1,
			  ViewDist.NORMAL, displayTo);
		}
	}
	
	default boolean usesXp()
	{
		return true;
	}
	
	default boolean crownsEnabled()
	{
		return false;
	}
}