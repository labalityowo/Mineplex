package mineplex.game.clans.items.attributes;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansUtility;
import mineplex.game.clans.items.CustomItem;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

/**
 * Represents an attribute that can be attached to {@link CustomItem} to augment their abilities 
 * and special effects on trigger.
 * @author MrTwiggy
 *
 */
public abstract class ItemAttribute
{

	private AttributeType _type;
	public AttributeType getType() { return _type; }
	public boolean matchesType(AttributeType type) { return _type == type; }
	
	public ItemAttribute(AttributeType type)
	{
		_type = type;
	}
	
	/**
	 * @return the attribute name display to players.
	 */
	public abstract String getDisplayName();
	
	/**
	 * @return a user-friendly description of this attribute, entailing it's effects
	 * and current associated values.
	 */
	public String getDescription() { return "???IMPLEMENT???"; }

	public void onInteract(PlayerInteractEvent event)
	{
		// Implementation left to subclasses.
	}
	
	public void onAttack(CustomDamageEvent event)
	{
		// Implementation left to subclasses.
	}
	
	public void onAttacked(CustomDamageEvent event)
	{
		// Implementation left to subclasses.
	}
	
	/**
	 * @param minValue - the minimum value for attribute value range
	 * @param maxValue - the maximum value for attribute value range
	 * @return newly instantiated {@link ValueDistribution} for attribute values in range [{@code minValue}. {@code maxValue}].
	 */
	public static ValueDistribution generateDistribution(double minValue, double maxValue)
	{
		return new ValueDistribution(minValue, maxValue);
	}
	
	/**
	 * @param amplifier - the amplifier for a potion effect intensity
	 * @return the roman-numeral properly representing the amplifier
	 */
	public static String amplifierToRoman(int amplifier)
	{
		return integerToRomanNumeral(amplifier + 1);	// Add one because amplifiers are zero-based
	}
	
	// Ugly int-to-roman numeral conversion found online. Don't judge!
	public static String integerToRomanNumeral(int input) {
	    if (input < 1 || input > 3999)
	        return "???";
	    String s = "";
	    while (input >= 1000) {
	        s += "M";
	        input -= 1000;        }
	    while (input >= 900) {
	        s += "CM";
	        input -= 900;
	    }
	    while (input >= 500) {
	        s += "D";
	        input -= 500;
	    }
	    while (input >= 400) {
	        s += "CD";
	        input -= 400;
	    }
	    while (input >= 100) {
	        s += "C";
	        input -= 100;
	    }
	    while (input >= 90) {
	        s += "XC";
	        input -= 90;
	    }
	    while (input >= 50) {
	        s += "L";
	        input -= 50;
	    }
	    while (input >= 40) {
	        s += "XL";
	        input -= 40;
	    }
	    while (input >= 10) {
	        s += "X";
	        input -= 10;
	    }
	    while (input >= 9) {
	        s += "IX";
	        input -= 9;
	    }
	    while (input >= 5) {
	        s += "V";
	        input -= 5;
	    }
	    while (input >= 4) {
	        s += "IV";
	        input -= 4;
	    }
	    while (input >= 1) {
	        s += "I";
	        input -= 1;
	    }    
	    return s;
	}

	protected boolean isTeammate(Entity attacker, Entity defender)
	{
		if (attacker == null || defender == null) return false;
		// Don't count attacks towards teammates
		if (attacker instanceof Player && defender instanceof Player)
		{
			ClansUtility.ClanRelation relation = ClansManager.getInstance().getRelation((Player) attacker, (Player) defender);
			if (relation == ClansUtility.ClanRelation.ALLY
					|| relation == ClansUtility.ClanRelation.SAFE
					|| relation == ClansUtility.ClanRelation.SELF)
			{
				return true;
			}
		}
		return false;
	}
}