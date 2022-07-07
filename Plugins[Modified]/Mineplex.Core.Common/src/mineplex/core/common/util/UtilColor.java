package mineplex.core.common.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

/**
 * Created by Shaun on 11/12/2014.
 */
public class UtilColor
{
	public static final RGBData RgbRed = hexToRgb(0xee0100);
	public static final RGBData RgbGold = hexToRgb(0xffd014);
	public static final RGBData RgbLightBlue = hexToRgb(0x61fff7);
	public static final RGBData RgbLightRed = hexToRgb(0xeb1c1c);
	public static final RGBData RgbPurple = hexToRgb(0x9c17a3);

	public static final Color DEFAULT_LEATHER_COLOR = Color.fromRGB(160, 101, 64);

	public static byte chatColorToWoolData(ChatColor chatColor)
	{
		switch (chatColor)
		{
			// 0: white
			// 1: orange
			// 2: magenta
			// 3: light blue
			// 4: yellow
			// 5: lime
			// 6: pink
			// 7: gray
			// 8: light gray
			// 9: cyan
			// 10: purple
			// 11: blue
			// 12: brown
			// 13: green
			// 14: red
			// 15: black
			case BLACK:
				return 1;
			case DARK_BLUE:
				return 11;
			case DARK_GREEN:
				return 13;
			case DARK_AQUA:
				return 9;
			case DARK_RED:
				return 12;
			case DARK_PURPLE:
				return 10;
			case GOLD:
				return 1;
			case GRAY:
				return 8;
			case DARK_GRAY:
				return 7;
			case BLUE:
				return 11;
			case GREEN:
				return 5;
			case AQUA:
				return 3;
			case RED:
				return 14;
			case LIGHT_PURPLE:
				return 2;
			case YELLOW:
				return 4;
			default:
				return 0;
		}
	}

	public static ChatColor woolDataToChatColor(byte data)
	{
		switch (data)
		{
			case 0:
				return ChatColor.WHITE;
			case 1:
				return ChatColor.GOLD;
			case 2:
				return ChatColor.DARK_PURPLE;
			case 3:
				return ChatColor.AQUA;
			case 4:
				return ChatColor.YELLOW;
			case 5:
				return ChatColor.GREEN;
			case 6:
				return ChatColor.LIGHT_PURPLE;
			case 7:
				return ChatColor.DARK_GRAY;
			case 8:
				return ChatColor.GRAY;
			case 9:
				return ChatColor.DARK_AQUA;
			case 10:
				return ChatColor.DARK_PURPLE;
			case 11:
				return ChatColor.DARK_BLUE;
			case 12:
				return ChatColor.DARK_RED;
			case 13:
				return ChatColor.DARK_GREEN;
			case 14:
				return ChatColor.RED;
			case 15:
				return ChatColor.BLACK;
			default:
				return ChatColor.WHITE;
		}
	}

	public static String chatColorToJsonColor(ChatColor chatColor)
	{
		return chatColor.name().toLowerCase();
	}
	
	public static Vector colorToVector(Color color)
	{
		return new Vector(Math.max(color.getRed()/255.0, 0.00001f), color.getGreen()/255.0, color.getBlue()/255.0);
	}
	
	public static RGBData hexToRgb(int hex)
	{
		return new RGBData(hex >> 16, hex >> 8 & 0xFF, hex & 0xFF);
	}
	
	public static int rgbToHex(RGBData rgb)
	{
		return (rgb.getFullRed() << 16 | rgb.getFullGreen() << 8 | rgb.getFullBlue());
	}
	
	public static int rgbToHex(int red, int green, int blue)
	{
		return (red << 16 | green << 8 | blue);
	}

	public static RGBData rgb(int r, int g, int b)
	{
		return new RGBData(r, g, b);
	}

	public static Color getNextColor(Color original, Color finalColor, int increment)
	{
		int red = original.getRed(), green = original.getGreen(), blue = original.getBlue();

		if (red > finalColor.getRed())
			red -= increment;
		else if (red < finalColor.getRed())
			red += increment;
		else if (green > finalColor.getGreen())
			green -= increment;
		else if (green < finalColor.getGreen())
			green += increment;
		else if (blue > finalColor.getBlue())
			blue -= increment;
		else if (blue < finalColor.getBlue())
			blue += increment;

		red = UtilMath.clamp(red, 0, 255);
		green = UtilMath.clamp(green, 0, 255);
		blue = UtilMath.clamp(blue, 0, 255);

		return Color.fromRGB(red, green, blue);
	}

    /**
     * Applies Color to a Leather armor
     * @param itemStack
     * @param color
     * @return ItemStack with color applied
     */
	public static ItemStack applyColor(@Nonnull ItemStack itemStack, Color color)
	{
        switch (itemStack.getType())
        {
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();

                leatherArmorMeta.setColor(color);
                itemStack.setItemMeta(leatherArmorMeta);
                return itemStack;
            default:
                return itemStack;
        }
	}

    /**
     * Gets color from Leather armor
     * @param itemStack
     * @return Color of the item
     */
	public static Color getItemColor(@Nonnull ItemStack itemStack)
	{
        switch (itemStack.getType())
        {
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();

                return leatherArmorMeta.getColor();
            default:
                return DEFAULT_LEATHER_COLOR;
        }
	}
}
