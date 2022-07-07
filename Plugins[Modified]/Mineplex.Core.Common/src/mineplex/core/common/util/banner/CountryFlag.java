package mineplex.core.common.util.banner;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.DyeColor.*;
import static org.bukkit.block.banner.PatternType.*;

/**
 * Banner patterns for country (and related) flags.
 */
public enum CountryFlag
{

	MINEPLEX("Mineplex", "Mineplex", BLACK,
			new Pattern(ORANGE, TRIANGLE_TOP),
			new Pattern(BLACK, TRIANGLES_TOP),
			new Pattern(ORANGE, STRIPE_LEFT),
			new Pattern(ORANGE, STRIPE_RIGHT),
			new Pattern(BLACK, BORDER),
			new Pattern(BLACK, STRIPE_BOTTOM)),

	RUDOLPH("Rudolph", "Rudolph", SILVER,
			new Pattern(BROWN, HALF_HORIZONTAL_MIRROR),
			new Pattern(RED, TRIANGLES_BOTTOM),
			new Pattern(BROWN, SQUARE_BOTTOM_RIGHT),
			new Pattern(GREEN, CURLY_BORDER),
			new Pattern(GREEN, CIRCLE_MIDDLE),
			new Pattern(GREEN, TRIANGLE_TOP)),

	CHRISTMAS_TREE("Christmas Tree", "Christmas Tree", GREEN,
			new Pattern(LIGHT_BLUE, BRICKS),
			new Pattern(BLUE, BRICKS),
			new Pattern(LIME, GRADIENT),
			new Pattern(BROWN, TRIANGLES_BOTTOM),
			new Pattern(YELLOW, TRIANGLES_TOP),
			new Pattern(RED, CURLY_BORDER)),

	PRESENT("Present", "Present", WHITE,
			new Pattern(BLACK, HALF_HORIZONTAL_MIRROR),
			new Pattern(BLACK, HALF_HORIZONTAL),
			new Pattern(WHITE, CREEPER),
			new Pattern(SILVER, STRIPE_MIDDLE),
			new Pattern(SILVER, STRAIGHT_CROSS),
			new Pattern(GRAY, FLOWER)),

	WREATH("Wreath", "Wreath", BROWN,
			new Pattern(BLACK, BORDER),
			new Pattern(BROWN, BORDER),
			new Pattern(YELLOW, CREEPER),
			new Pattern(RED, SKULL),
			new Pattern(GREEN, FLOWER),
			new Pattern(BROWN, CIRCLE_MIDDLE)),

	SNOW_FLAKE("Snow Flake", "Snow Flake", BLUE,
			new Pattern(WHITE, STRAIGHT_CROSS),
			new Pattern(PINK, CROSS),
			new Pattern(WHITE, CROSS),
			new Pattern(BLUE, CURLY_BORDER),
			new Pattern(WHITE, FLOWER)),

	HEART("Heart", "Heart", RED,
			new Pattern(BLACK, CURLY_BORDER),
			new Pattern(BLACK, STRIPE_BOTTOM),
			new Pattern(RED, RHOMBUS_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(BLACK, TRIANGLE_TOP),
			new Pattern(BLACK, STRIPE_TOP)),

	// Country
	AFGHANISTAN("Afghanistan", "Afghanistani", RED,
			new Pattern(BLACK, STRIPE_TOP),
			new Pattern(GREEN, STRIPE_BOTTOM),
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE)),

	ALBANIA("Albania", "Albanian", RED,
			new Pattern(BLACK, FLOWER),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(RED, BORDER),
			new Pattern(RED, STRIPE_SMALL),
			new Pattern(BLACK, CIRCLE_MIDDLE)),

	ALGERIA("Algeria", "Algerian", GREEN,
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(WHITE, TRIANGLE_BOTTOM)),

	ANGOLA("Angola", "Angolan", RED,
			new Pattern(BLACK, HALF_VERTICAL),
			new Pattern(YELLOW, FLOWER),
			new Pattern(BLACK, STRIPE_LEFT),
			new Pattern(RED, STRIPE_RIGHT)),

	ARGENTINA("Argentina", "Argentinian", WHITE,
			new Pattern(LIGHT_BLUE, STRIPE_LEFT),
			new Pattern(LIGHT_BLUE, STRIPE_RIGHT),
			new Pattern(YELLOW, CIRCLE_MIDDLE)),

	ARMENIA("Armenia", "Armenian", RED,
			new Pattern(ORANGE, HALF_VERTICAL),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER)),

	AUSTRALIA("Australia", "Australian", WHITE,
			new Pattern(BLUE, CROSS),
			new Pattern(BLUE, STRAIGHT_CROSS),
			new Pattern(BLUE, CURLY_BORDER),
			new Pattern(RED, SQUARE_TOP_RIGHT)),

	AUSTRIA("Austria", "Austrian", RED,
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER)),

	AZERBAIJAN("Azerbaijan", "Azerbaijani", RED,
			new Pattern(WHITE, FLOWER),
			new Pattern(WHITE, FLOWER),
			new Pattern(WHITE, FLOWER),
			new Pattern(RED, DIAGONAL_LEFT_MIRROR),
			new Pattern(RED, DIAGONAL_RIGHT_MIRROR),
			new Pattern(LIGHT_BLUE, STRIPE_RIGHT),
			new Pattern(GREEN, STRIPE_LEFT)),

	BAHRAIN("Bahrain", "Bahraini", RED,
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(WHITE, TRIANGLES_TOP)),

	BANGLADESH("Bangladesh", "Bangladeshi", GREEN,
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE)),

	BELARUS("Belarus", "Belarusian", RED,
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(WHITE, TRIANGLES_TOP)),

	BELGIUM("Belgium", "Belgian", YELLOW,
			new Pattern(BLACK, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	BERMUDA("Bermuda", "Bermudian", BLUE,
			new Pattern(WHITE, BRICKS),
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(RED, HALF_HORIZONTAL_MIRROR),
			new Pattern(RED, STRIPE_MIDDLE)),

	BHUTAN("Bhutan", "Bhutanese", YELLOW,
			new Pattern(ORANGE, DIAGONAL_LEFT_MIRROR),
			new Pattern(WHITE, RHOMBUS_MIDDLE)),

	BOLIVIA("Bolivia", "Bolivian", RED,
			new Pattern(GREEN, HALF_VERTICAL),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(LIGHT_BLUE, CIRCLE_MIDDLE),
			new Pattern(LIGHT_BLUE, CIRCLE_MIDDLE),
			new Pattern(LIGHT_BLUE, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE)),

	BOSNIA_AND_HERZEGOVINA("Bosnia and Herzegovina", "Bosnia and Herzegovinian", BLUE,
			new Pattern(WHITE, STRIPE_DOWNLEFT),
			new Pattern(BLUE, BRICKS),
			new Pattern(YELLOW, DIAGONAL_RIGHT),
			new Pattern(BLUE, STRIPE_BOTTOM)),

	BRAZIL("Brazil", "Brazilian", LIME,
			new Pattern(YELLOW, RHOMBUS_MIDDLE),
			new Pattern(YELLOW, RHOMBUS_MIDDLE),
			new Pattern(YELLOW, RHOMBUS_MIDDLE),
			new Pattern(BLUE, CIRCLE_MIDDLE),
			new Pattern(BLUE, CIRCLE_MIDDLE)),

	BRUNEI("Brunei", "Bruneian", YELLOW,
			new Pattern(WHITE, STRIPE_DOWNLEFT),
			new Pattern(WHITE, STRIPE_DOWNLEFT),
			new Pattern(WHITE, STRIPE_DOWNLEFT),
			new Pattern(BLACK, CROSS),
			new Pattern(YELLOW, DIAGONAL_LEFT),
			new Pattern(YELLOW, SQUARE_BOTTOM_RIGHT),
			new Pattern(RED, CIRCLE_MIDDLE)),

	BULGARIA("Bulgaria", "Bulgarian", WHITE,
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(GREEN, STRIPE_CENTER),
			new Pattern(GREEN, STRIPE_CENTER),
			new Pattern(GREEN, STRIPE_CENTER)),

	CAMBODIA("Cambodia", "Cambodian", RED,
			new Pattern(WHITE, FLOWER),
			new Pattern(BLUE, STRIPE_LEFT),
			new Pattern(BLUE, STRIPE_RIGHT)),

	CAMEROON("Cameroon", "Cameroonian", RED,
			new Pattern(YELLOW, CIRCLE_MIDDLE),
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(YELLOW, STRIPE_BOTTOM)),

	CANADA("Canada", "Canadian", WHITE,
			new Pattern(RED, CROSS),
			new Pattern(WHITE, STRIPE_LEFT),
			new Pattern(RED, STRIPE_MIDDLE),
			new Pattern(WHITE, BORDER),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	CHILE("Chile", "Chilean", BLUE,
			new Pattern(WHITE, CROSS),
			new Pattern(BLUE, TRIANGLES_TOP),
			new Pattern(BLUE, RHOMBUS_MIDDLE),
			new Pattern(BLUE, BORDER),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(RED, HALF_VERTICAL)),

	CHINA("China", "Chinese", RED,
			new Pattern(YELLOW, SQUARE_TOP_RIGHT),
			new Pattern(RED, PatternType.STRIPE_SMALL),
			new Pattern(RED, BORDER),
			new Pattern(RED, BRICKS)),

	COLOMBIA("Colombia", "Colombian", RED,
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(YELLOW, HALF_VERTICAL_MIRROR)),

	COSTA_RICA("Costa Rica", "Costa Rican", WHITE,
			new Pattern(BLUE, STRIPE_SMALL),
			new Pattern(BLUE, STRIPE_SMALL),
			new Pattern(BLUE, STRIPE_SMALL),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(RED, STRIPE_CENTER)),

	CROATIA("Croatia", "Croatian", RED,
			new Pattern(WHITE, FLOWER),
			new Pattern(BLUE, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(RED, CIRCLE_MIDDLE)),

	CUBA("Cuba", "Cuban", BLUE,
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(RED, TRIANGLE_TOP)),

	CYPRUS("Cyprus", "Cyprus", WHITE,
			new Pattern(ORANGE, FLOWER),
			new Pattern(ORANGE, FLOWER),
			new Pattern(ORANGE, FLOWER),
			new Pattern(ORANGE, CIRCLE_MIDDLE),
			new Pattern(WHITE, DIAGONAL_LEFT_MIRROR),
			new Pattern(WHITE, CURLY_BORDER),
			new Pattern(WHITE, CURLY_BORDER),
			new Pattern(WHITE, CURLY_BORDER)),

	CZECH_REPUBLIC("Czech Republic", "Czech Republic", true, WHITE,
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(BLUE, TRIANGLE_TOP)),

	DENMARK("Denmark", "Danish", WHITE,
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, STRIPE_CENTER)),

	DOMINICAN_REPUBLIC("Dominican Republic", "Dominican", true, WHITE,
			new Pattern(BLUE, SQUARE_TOP_RIGHT),
			new Pattern(BLUE, SQUARE_BOTTOM_LEFT),
			new Pattern(RED, SQUARE_TOP_LEFT),
			new Pattern(RED, SQUARE_BOTTOM_RIGHT),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(GREEN, CIRCLE_MIDDLE)),

	ECUADOR("Ecuador", "Ecuadorian", RED,
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(YELLOW, HALF_VERTICAL_MIRROR),
			new Pattern(LIGHT_BLUE, CIRCLE_MIDDLE)),

	EGYPT("Egypt", "Egyptian", WHITE,
			new Pattern(YELLOW, CIRCLE_MIDDLE),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(BLACK, STRIPE_LEFT)),

	EL_SALVADOR("El Salvador", "El Salvadorian", BLUE,
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(GREEN, CIRCLE_MIDDLE)),

	ENGLAND("England", "English", WHITE,
			new Pattern(RED, STRAIGHT_CROSS),
			new Pattern(RED, STRAIGHT_CROSS),
			new Pattern(RED, STRAIGHT_CROSS)),

	ESTONIA("Estonia", "Estonian", BLUE,
			new Pattern(WHITE, HALF_VERTICAL),
			new Pattern(BLACK, STRIPE_CENTER),
			new Pattern(BLACK, STRIPE_CENTER),
			new Pattern(BLACK, STRIPE_CENTER)),

	ETHIOPIA("Ethiopia", "Ethiopian", GREEN,
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(BLUE, CIRCLE_MIDDLE),
			new Pattern(BLUE, CIRCLE_MIDDLE),
			new Pattern(BLUE, CIRCLE_MIDDLE)),

	EU("European Union", "European Union", true, BLUE,
			new Pattern(YELLOW, CROSS),
			new Pattern(YELLOW, STRIPE_MIDDLE),
			new Pattern(BLUE, BORDER),
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(BLUE, STRIPE_BOTTOM),
			new Pattern(BLUE, CIRCLE_MIDDLE)),

	FAROE_ISLANDS("Faroe Islands", "Faroese", true, RED,
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(RED, STRIPE_CENTER)),

	FINLAND("Finland", "Finnish", BLUE,
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(BLUE, STRIPE_CENTER)),

	FRANCE("France", "French", WHITE,
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	GABON("Gabon", "Gabonese", GREEN,
			new Pattern(BLUE, HALF_VERTICAL),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(YELLOW, STRIPE_CENTER)),

	GEORGIA("Georgia", "Georgian", WHITE,
			new Pattern(RED, TRIANGLES_TOP),
			new Pattern(RED, TRIANGLES_TOP),
			new Pattern(RED, TRIANGLES_TOP),
			new Pattern(RED, TRIANGLES_BOTTOM),
			new Pattern(RED, TRIANGLES_BOTTOM),
			new Pattern(RED, TRIANGLES_BOTTOM),
			new Pattern(WHITE, BORDER),
			new Pattern(WHITE, BORDER),
			new Pattern(WHITE, BORDER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(RED, STRAIGHT_CROSS),
			new Pattern(RED, STRAIGHT_CROSS),
			new Pattern(RED, STRAIGHT_CROSS)),

	GERMANY("Germany", "German", BLACK,
			new Pattern(YELLOW, HALF_VERTICAL),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(RED, STRIPE_CENTER)),

	GHANA("Ghana", "Ghanaian", YELLOW,
			new Pattern(BLACK, CROSS),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(GREEN, STRIPE_LEFT)),

	GREECE("Greece", "Greek", BLUE,
			new Pattern(BLUE, HALF_VERTICAL),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(BLUE, SQUARE_TOP_RIGHT)),

	GUATEMALA("Guatemala", "Guatemalan", WHITE,
			new Pattern(LIGHT_BLUE, STRIPE_TOP),
			new Pattern(LIGHT_BLUE, STRIPE_BOTTOM),
			new Pattern(GREEN, CIRCLE_MIDDLE)),

	HONDURAS("Honduras", "Honduran", WHITE,
			new Pattern(BLUE, FLOWER),
			new Pattern(WHITE, STRIPE_LEFT),
			new Pattern(WHITE, STRIPE_LEFT),
			new Pattern(WHITE, STRIPE_LEFT),
			new Pattern(WHITE, STRIPE_RIGHT),
			new Pattern(WHITE, STRIPE_RIGHT),
			new Pattern(WHITE, STRIPE_RIGHT),
			new Pattern(WHITE, STRAIGHT_CROSS),
			new Pattern(WHITE, STRAIGHT_CROSS),
			new Pattern(WHITE, STRAIGHT_CROSS),
			new Pattern(BLUE, STRIPE_LEFT),
			new Pattern(BLUE, STRIPE_RIGHT)),

	HONG_KONG("Hong Kong", "Hong Kongese", RED,
			new Pattern(WHITE, FLOWER)),

	HUNGARY("Hungary", "Hungarian", RED,
			new Pattern(GREEN, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER)),

	ICELAND("Iceland", "Icelandic", RED,
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(RED, STRIPE_CENTER)),

	INDIA("India", "Indian", WHITE,
			new Pattern(ORANGE, STRIPE_LEFT),
			new Pattern(GREEN, STRIPE_RIGHT),
			new Pattern(BLUE, CIRCLE_MIDDLE)),

	INDONESIA("Indonesia", "Indonesian", WHITE,
			new Pattern(RED, HALF_VERTICAL_MIRROR)),

	IRAN("Iran", "Iranian", WHITE,
			new Pattern(GREEN, STRIPE_RIGHT),
			new Pattern(RED, STRIPE_LEFT),
			new Pattern(BLACK, CIRCLE_MIDDLE),
			new Pattern(BLACK, CIRCLE_MIDDLE),
			new Pattern(BLACK, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE)),

	IRAQ("Iraq", "Iraqi", WHITE,
			new Pattern(GREEN, FLOWER),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(BLACK, STRIPE_LEFT)),

	IRELAND("Ireland", "Irish", WHITE,
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(ORANGE, STRIPE_BOTTOM)),

	ISLE_OF_MAN("Isle of Man", "Isle of Man", true, RED,
			new Pattern(WHITE, SKULL),
			new Pattern(WHITE, FLOWER),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(RED, STRIPE_LEFT),
			new Pattern(RED, STRIPE_RIGHT)),

	ISRAEL("Israel", "Israeli", WHITE,
			new Pattern(BLUE, STRIPE_LEFT),
			new Pattern(BLUE, STRIPE_RIGHT),
			new Pattern(BLUE, FLOWER)),

	ITALY("Italy", "Italian", WHITE,
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	IVORY_COAST("Ivory Coast", "Ivory Coast", true, WHITE,
			new Pattern(ORANGE, STRIPE_TOP),
			new Pattern(ORANGE, STRIPE_TOP),
			new Pattern(ORANGE, STRIPE_TOP),
			new Pattern(LIME, STRIPE_BOTTOM),
			new Pattern(LIME, STRIPE_BOTTOM),
			new Pattern(LIME, STRIPE_BOTTOM)),

	JAMAICA("Jamaica", "Jamaican", GREEN,
			new Pattern(YELLOW, STRIPE_DOWNLEFT),
			new Pattern(YELLOW, STRIPE_DOWNLEFT),
			new Pattern(YELLOW, STRIPE_DOWNLEFT),
			new Pattern(YELLOW, STRIPE_DOWNRIGHT),
			new Pattern(YELLOW, STRIPE_DOWNRIGHT),
			new Pattern(YELLOW, STRIPE_DOWNRIGHT),
			new Pattern(BLACK, TRIANGLE_TOP),
			new Pattern(BLACK, TRIANGLE_BOTTOM)),

	JAPAN("Japan", "Japanese", WHITE,
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE)),

	JORDAN("Jordan", "Jordanian", BLACK,
			new Pattern(GREEN, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(RED, TRIANGLE_TOP),
			new Pattern(RED, TRIANGLE_TOP),
			new Pattern(RED, TRIANGLE_TOP)),

	KAZAKHSTAN("Kazakhstan", "Kazakhstani", LIGHT_BLUE,
			new Pattern(YELLOW, CREEPER),
			new Pattern(LIGHT_BLUE, HALF_VERTICAL),
			new Pattern(YELLOW, TRIANGLES_TOP)),

	KENYA("Kenya", "Kenyan", WHITE,
			new Pattern(BLACK, STRIPE_RIGHT),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(WHITE, FLOWER),
			new Pattern(BLACK, CIRCLE_MIDDLE),
			new Pattern(BLACK, CIRCLE_MIDDLE),
			new Pattern(BLACK, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE)),

	KUWAIT("Kuwait", "Kuwaiti", GREEN,
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(BLACK, TRIANGLE_TOP),
			new Pattern(WHITE, CIRCLE_MIDDLE)),

	KYRGYZSTAN("Kyrgyzstan", "Kyrgyzstani", RED,
			new Pattern(YELLOW, CIRCLE_MIDDLE)),

	LATVIA("Latvia", "Latvian", RED,
			new Pattern(WHITE, STRIPE_CENTER)),

	LEBANON("Lebanon", "Lebanese", WHITE,
			new Pattern(GREEN, FLOWER),
			new Pattern(GREEN, FLOWER),
			new Pattern(GREEN, FLOWER),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(RED, STRIPE_LEFT),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(GREEN, CIRCLE_MIDDLE)),

	LIBYA("Libya", "Libyan", BLACK,
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(BLACK, FLOWER),
			new Pattern(BLACK, TRIANGLE_TOP),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(GREEN, STRIPE_LEFT)),

	LITHUANIA("Lithuania", "Lithuanian", YELLOW,
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(GREEN, STRIPE_CENTER),
			new Pattern(GREEN, STRIPE_CENTER),
			new Pattern(GREEN, STRIPE_CENTER)),

	LUXEMBOURG("Luxembourg", "Luxembourg", RED,
			new Pattern(LIGHT_BLUE, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER)),

	MACAU("Macau", "Macau", GREEN,
			new Pattern(YELLOW, BRICKS),
			new Pattern(GREEN, HALF_VERTICAL),
			new Pattern(WHITE, FLOWER),
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(GREEN, STRIPE_RIGHT),
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(GREEN, STRIPE_BOTTOM)),

	MACEDONIA("Macedonia", "Macedonian", RED,
			new Pattern(YELLOW, CROSS),
			new Pattern(YELLOW, STRAIGHT_CROSS),
			new Pattern(RED, RHOMBUS_MIDDLE),
			new Pattern(YELLOW, CIRCLE_MIDDLE)),

	MALAYSIA("Malaysia", "Malaysian", RED,
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(BLUE, SQUARE_TOP_RIGHT),
			new Pattern(YELLOW, TRIANGLES_TOP)),

	MALTA("Malta", "Maltese", WHITE,
			new Pattern(RED, HALF_HORIZONTAL_MIRROR),
			new Pattern(GRAY, SQUARE_TOP_RIGHT),
			new Pattern(WHITE, SQUARE_TOP_RIGHT)),

	MEXICO("Mexico", "Mexican", WHITE,
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(BROWN, CIRCLE_MIDDLE),
			new Pattern(BROWN, CIRCLE_MIDDLE),
			new Pattern(YELLOW, CIRCLE_MIDDLE)),

	MOLDOVA("Moldova", "Moldovan", YELLOW,
			new Pattern(BROWN, CIRCLE_MIDDLE),
			new Pattern(YELLOW, STRIPE_LEFT),
			new Pattern(YELLOW, STRIPE_RIGHT),
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	MONGOLIA("Mongolia", "Mongolian", BLUE,
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(YELLOW, TRIANGLES_TOP)),

	MONTENEGRO("Montenegro", "Montenegrin", RED,
			new Pattern(YELLOW, FLOWER),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(RED, STRIPE_LEFT),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(YELLOW, BORDER)),

	MOROCCO("Morocco", "Moroccan", RED,
			new Pattern(GREEN, CROSS),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	MOZAMBIQUE("Mozambique", "Mozambique", WHITE,
			new Pattern(CYAN, STRIPE_RIGHT),
			new Pattern(YELLOW, STRIPE_LEFT),
			new Pattern(BLACK, STRIPE_CENTER),
			new Pattern(RED, TRIANGLE_TOP)),

	NEPAL("Nepal", "Nepali", RED,
			new Pattern(BLUE, BORDER),
			new Pattern(BLUE, BORDER),
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(RED, FLOWER)),

	NETHERLANDS("Netherlands", "Dutch", true, WHITE,
			new Pattern(RED, PatternType.STRIPE_RIGHT),
			new Pattern(RED, PatternType.STRIPE_RIGHT),
			new Pattern(BLUE, PatternType.STRIPE_LEFT),
			new Pattern(BLUE, PatternType.STRIPE_LEFT)),

	NEW_ZEALAND("New Zealand", "New Zealand", RED,
			new Pattern(BLUE, CROSS),
			new Pattern(BLUE, STRAIGHT_CROSS),
			new Pattern(BLUE, CURLY_BORDER),
			new Pattern(WHITE, SQUARE_TOP_RIGHT)),

	NIGERIA("Nigeria", "Nigerian", WHITE,
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(GREEN, STRIPE_BOTTOM)),

	NORWAY("Norway", "Norwegian", BLUE,
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(RED, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER)),

	OMAN("Oman", "Omani", WHITE,
			new Pattern(GREEN, HALF_VERTICAL),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(RED, TRIANGLES_TOP),
			new Pattern(RED, SQUARE_TOP_LEFT)),

	PAKISTAN("Pakistan", "Pakistani", GREEN,
			new Pattern(WHITE, FLOWER),
			new Pattern(GREEN, TRIANGLE_BOTTOM),
			new Pattern(GREEN, TRIANGLE_BOTTOM),
			new Pattern(GREEN, TRIANGLE_BOTTOM),
			new Pattern(GREEN, BORDER),
			new Pattern(GREEN, BORDER),
			new Pattern(GREEN, BORDER),
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(GREEN, STRIPE_BOTTOM),
			new Pattern(GREEN, STRIPE_BOTTOM),
			new Pattern(GREEN, STRIPE_BOTTOM),
			new Pattern(WHITE, STRIPE_TOP)),

	PANAMA("Panama", "Panamanian", WHITE,
			new Pattern(BLUE, SQUARE_TOP_RIGHT),
			new Pattern(RED, SQUARE_BOTTOM_LEFT),
			new Pattern(WHITE, BORDER),
			new Pattern(WHITE, BORDER),
			new Pattern(WHITE, BORDER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, CURLY_BORDER),
			new Pattern(RED, SQUARE_BOTTOM_RIGHT),
			new Pattern(BLUE, SQUARE_TOP_LEFT)),

	PARAGUAY("Paraguay", "Paraguayan", RED,
			new Pattern(BLUE, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE)),

	PERU("Peru", "Peruvian", WHITE,
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(YELLOW, CIRCLE_MIDDLE),
			new Pattern(YELLOW, CIRCLE_MIDDLE),
			new Pattern(YELLOW, CIRCLE_MIDDLE),
			new Pattern(GREEN, CIRCLE_MIDDLE)),

	PHILIPPINES("Philippines", "Philippine", true, BLUE,
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(WHITE, TRIANGLE_TOP)),

	POLAND("Poland", "Polish", WHITE,
			new Pattern(RED, HALF_VERTICAL)),

	PORTUGAL("Portugal", "Portuguese", RED,
			new Pattern(GREEN, STRIPE_BOTTOM),
			new Pattern(YELLOW, CREEPER),
			new Pattern(YELLOW, SKULL),
			new Pattern(RED, HALF_HORIZONTAL)),

	PUERTO_RICO("Puerto Rico", "Puerto Rican", RED,
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(BLUE, TRIANGLE_TOP)),

	QATAR("Qatar", "Qatari", RED,
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(WHITE, TRIANGLES_TOP)),

	ROMANIA("Romania", "Romanian", YELLOW,
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	RUSSIA("Germany", "German", WHITE,
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER)),

	RWANDA("Rwanda", "Rwandan", LIGHT_BLUE,
			new Pattern(YELLOW, TRIANGLES_TOP),
			new Pattern(LIGHT_BLUE, BORDER),
			new Pattern(GREEN, HALF_VERTICAL),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(YELLOW, STRIPE_CENTER),
			new Pattern(YELLOW, STRIPE_CENTER)),

	SAUDI_ARABIA("Saudi Arabia", "Saudi Arabian", GREEN,
			new Pattern(WHITE, SKULL),
			new Pattern(WHITE, MOJANG),
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(GREEN, STRIPE_CENTER),
			new Pattern(GREEN, BORDER)),

	SCOTLAND("Scotland", "Scottish", BLUE,
			new Pattern(WHITE, CROSS),
			new Pattern(WHITE, CROSS)),

	SENEGAL("Senegal", "Senegalese", YELLOW,
			new Pattern(GREEN, CROSS),
			new Pattern(YELLOW, STRIPE_BOTTOM),
			new Pattern(YELLOW, STRIPE_BOTTOM),
			new Pattern(YELLOW, STRIPE_TOP),
			new Pattern(YELLOW, STRIPE_TOP),
			new Pattern(YELLOW, STRIPE_TOP),
			new Pattern(GREEN, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	SERBIA("Serbia", "Serbian", RED,
			new Pattern(WHITE, HALF_VERTICAL),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(RED, CIRCLE_MIDDLE),
			new Pattern(WHITE, CIRCLE_MIDDLE)),

	SINGAPORE("Singapore", "Singaporean", RED,
			new Pattern(WHITE, FLOWER),
			new Pattern(RED, HALF_HORIZONTAL_MIRROR),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(WHITE, HALF_VERTICAL)),

	SLOVAKIA("Slovakia", "Slovakian", BLUE,
			new Pattern(RED, SKULL),
			new Pattern(WHITE, BRICKS),
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(WHITE, STRIPE_RIGHT),
			new Pattern(WHITE, STRIPE_RIGHT),
			new Pattern(WHITE, STRIPE_RIGHT),
			new Pattern(RED, STRIPE_LEFT),
			new Pattern(RED, STRIPE_LEFT),
			new Pattern(RED, STRIPE_LEFT)),

	SLOVENIA("Slovenia", "Slovenian", WHITE,
			new Pattern(BLUE, CROSS),
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(WHITE, HALF_HORIZONTAL_MIRROR),
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER)),

	SOLOMON_ISLANDS("Solomon Islands", "Solomon Islands", true, BLUE,
			new Pattern(WHITE, BRICKS),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(BLUE, BORDER),
			new Pattern(BLUE, CURLY_BORDER),
			new Pattern(BLUE, RHOMBUS_MIDDLE),
			new Pattern(BLUE, HALF_VERTICAL),
			new Pattern(GREEN, DIAGONAL_LEFT_MIRROR),
			new Pattern(BLACK, STRIPE_DOWNRIGHT),
			new Pattern(BLACK, STRIPE_DOWNRIGHT)),

	SOMALIA("Somalia", "Somalian", LIGHT_BLUE,
			new Pattern(WHITE, CROSS),
			new Pattern(LIGHT_BLUE, STRIPE_TOP),
			new Pattern(LIGHT_BLUE, STRIPE_BOTTOM)),

	SOUTH_AFRICA("South Africa", "South African", WHITE,
			new Pattern(GREEN, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_LEFT),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(YELLOW, TRIANGLE_TOP),
			new Pattern(YELLOW, TRIANGLE_TOP),
			new Pattern(YELLOW, TRIANGLE_TOP),
			new Pattern(BLACK, TRIANGLE_TOP)),

	SOUTH_KOREA("South Korea", "South Korean", RED,
			new Pattern(BLUE, DIAGONAL_LEFT_MIRROR),
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, CURLY_BORDER),
			new Pattern(WHITE, STRIPE_BOTTOM),
			new Pattern(BLACK, TRIANGLES_TOP),
			new Pattern(BLACK, TRIANGLES_TOP),
			new Pattern(BLACK, TRIANGLES_TOP),
			new Pattern(BLACK, TRIANGLES_BOTTOM),
			new Pattern(BLACK, TRIANGLES_BOTTOM),
			new Pattern(BLACK, TRIANGLES_BOTTOM),
			new Pattern(WHITE, TRIANGLES_BOTTOM),
			new Pattern(WHITE, TRIANGLES_TOP),
			new Pattern(WHITE, BORDER),
			new Pattern(WHITE, BORDER)),

	SOUTH_SUDAN("South Sudan", "South Sudanese", WHITE,
			new Pattern(BLACK, STRIPE_RIGHT),
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(RED, STRIPE_CENTER),
			new Pattern(BLUE, TRIANGLE_TOP)),

	SPAIN("Spain", "Spanish", YELLOW,
			new Pattern(BROWN, FLOWER),
			new Pattern(YELLOW, HALF_HORIZONTAL_MIRROR)),

	SRI_LANKA("Sri Lanka", "Sri Lankan", RED,
			new Pattern(YELLOW, STRIPE_MIDDLE),
			new Pattern(YELLOW, CREEPER),
			new Pattern(ORANGE, HALF_HORIZONTAL),
			new Pattern(LIME, STRIPE_TOP),
			new Pattern(YELLOW, BORDER)),

	SUDAN("Sudan", "Sudanese", RED,
			new Pattern(BLACK, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(GREEN, TRIANGLE_TOP)),

	SURINAME("Suriname", "Surinamese", WHITE,
			new Pattern(RED, STRAIGHT_CROSS),
			new Pattern(YELLOW, FLOWER),
			new Pattern(YELLOW, SKULL),
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(GREEN, STRIPE_RIGHT)),

	SWEDEN("Sweden", "Swedish", YELLOW,
			new Pattern(BLUE, STRIPE_TOP),
			new Pattern(BLUE, HALF_HORIZONTAL_MIRROR),
			new Pattern(YELLOW, STRIPE_CENTER)),

	SWITZERLAND("Switzerland", "Swiss", RED,
			new Pattern(WHITE, STRAIGHT_CROSS),
			new Pattern(WHITE, STRAIGHT_CROSS),
			new Pattern(WHITE, STRAIGHT_CROSS),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(RED, STRIPE_BOTTOM),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, BORDER),
			new Pattern(RED, BORDER),
			new Pattern(RED, BORDER)),

	SYRIA("Syria", "Syrian", WHITE,
			new Pattern(GREEN, FLOWER),
			new Pattern(GREEN, FLOWER),
			new Pattern(GREEN, FLOWER),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(RED, STRIPE_RIGHT),
			new Pattern(BLACK, STRIPE_LEFT),
			new Pattern(BLACK, STRIPE_LEFT),
			new Pattern(BLACK, STRIPE_LEFT)),

	TAIWAN("Taiwan", "Taiwanese", BLUE,
			new Pattern(WHITE, CROSS),
			new Pattern(BLUE, CURLY_BORDER),
			new Pattern(BLUE, FLOWER),
			new Pattern(BLUE, CIRCLE_MIDDLE),
			new Pattern(BLUE, RHOMBUS_MIDDLE),
			new Pattern(RED, HALF_VERTICAL),
			new Pattern(RED, HALF_HORIZONTAL_MIRROR),
			new Pattern(RED, HALF_HORIZONTAL_MIRROR),
			new Pattern(RED, HALF_HORIZONTAL_MIRROR)),

	TAJIKISTAN("Tajikistan", "Tajikistani", WHITE,
			new Pattern(YELLOW, CROSS),
			new Pattern(WHITE, STRIPE_TOP),
			new Pattern(WHITE, STRIPE_BOTTOM),
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(RED, STRIPE_RIGHT)),

	TANZANIA("Tanzania", "Tanzanian", LIGHT_BLUE,
			new Pattern(LIME, DIAGONAL_RIGHT),
			new Pattern(YELLOW, STRIPE_DOWNRIGHT),
			new Pattern(YELLOW, STRIPE_DOWNRIGHT),
			new Pattern(YELLOW, STRIPE_DOWNRIGHT)),

	THAILAND("Thailand", "Thai", WHITE,
			new Pattern(RED, STRIPE_SMALL),
			new Pattern(RED, STRIPE_SMALL),
			new Pattern(RED, STRIPE_SMALL),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER),
			new Pattern(BLUE, STRIPE_CENTER)),

	TUNISIA("Tunisia", "Tunisian", RED,
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(RED, FLOWER),
			new Pattern(RED, TRIANGLE_BOTTOM)),

	TURKEY("Turkey", "Turkish", RED,
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(RED, FLOWER),
			new Pattern(RED, TRIANGLE_BOTTOM)),

	UAE("United Arab Emirates", "United Arab Emirates", true, GREEN,
			new Pattern(BLACK, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_TOP)),

	UGANDA("Uganda", "Ugandan", RED,
			new Pattern(BLACK, STRIPE_LEFT),
			new Pattern(BLACK, STRIPE_RIGHT),
			new Pattern(YELLOW, STRIPE_SMALL),
			new Pattern(RED, BORDER)),

	UK("United Kingdom", "British", true, BLUE,
			new Pattern(WHITE, STRIPE_DOWNLEFT),
			new Pattern(WHITE, STRIPE_DOWNLEFT),
			new Pattern(WHITE, STRIPE_DOWNRIGHT),
			new Pattern(WHITE, STRIPE_DOWNRIGHT),
			new Pattern(RED, CROSS),
			new Pattern(RED, CROSS),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(WHITE, STRIPE_MIDDLE),
			new Pattern(RED, STRAIGHT_CROSS),
			new Pattern(RED, STRAIGHT_CROSS),
			new Pattern(RED, STRAIGHT_CROSS)),

	UKRAINE("Ukraine", "Ukrainian", BLUE,
			new Pattern(YELLOW, HALF_VERTICAL)),

	URUGUAY("Uruguay", "Uruguayan", WHITE,
			new Pattern(BLUE, STRIPE_SMALL),
			new Pattern(YELLOW, SQUARE_TOP_RIGHT)),

	USA("United States of America", "American", true, RED,
			new Pattern(WHITE, STRIPE_SMALL),
			new Pattern(BLUE, SQUARE_TOP_RIGHT),
			new Pattern(BLUE, SQUARE_TOP_RIGHT),
			new Pattern(BLUE, SQUARE_TOP_RIGHT)),

	UZBEKISTAN("Uzbekistan", "Uzbekistani", RED,
			new Pattern(GREEN, STRIPE_LEFT),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(LIGHT_BLUE, STRIPE_RIGHT)),

	VENEZUELA("Venezuela", "Venezuelan", BLUE,
			new Pattern(WHITE, CIRCLE_MIDDLE),
			new Pattern(BLUE, STRAIGHT_CROSS),
			new Pattern(YELLOW, STRIPE_RIGHT),
			new Pattern(YELLOW, STRIPE_RIGHT),
			new Pattern(YELLOW, STRIPE_RIGHT),
			new Pattern(RED, STRIPE_LEFT),
			new Pattern(RED, STRIPE_LEFT),
			new Pattern(RED, STRIPE_LEFT)),

	VIETNAM("Vietnam", "Vietnamese", RED,
			new Pattern(YELLOW, CROSS),
			new Pattern(RED, STRIPE_TOP),
			new Pattern(RED, STRIPE_BOTTOM)),

	WALES("Wales", "Welsh", WHITE,
			new Pattern(GREEN, HALF_VERTICAL),
			new Pattern(RED, MOJANG),
			new Pattern(RED, MOJANG),
			new Pattern(RED, SKULL)),

	YEMEN("Yemen", "Yemeni", RED,
			new Pattern(BLACK, HALF_VERTICAL),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER),
			new Pattern(WHITE, STRIPE_CENTER)),

	ZAMBIA("Zambia", "Zambian", LIME,
			new Pattern(YELLOW, FLOWER),
			new Pattern(RED, STRIPE_MIDDLE),
			new Pattern(BLACK, HALF_HORIZONTAL_MIRROR),
			new Pattern(ORANGE, STRIPE_BOTTOM)),

	ZIMBABWE("Zimbabwe", "Zimbabwean", RED,
			new Pattern(BLACK, STRAIGHT_CROSS),
			new Pattern(YELLOW, STRIPE_LEFT),
			new Pattern(YELLOW, STRIPE_RIGHT),
			new Pattern(GREEN, BORDER),
			new Pattern(WHITE, TRIANGLE_TOP)),

	;

	private final boolean _article;
	private final String _country;
	private final String _adjective;
	private final DyeColor _baseColor;
	private final Pattern[] _patterns;

	/**
	 * Create a country flag banner.
	 *
	 * @param country    Country name.
	 * @param adjective  Country name as an adjective.
	 * @param baseColor  base color of the banner.
	 * @param patterns   List of patterns to apply to the banner.
	 */
	CountryFlag(String country, String adjective, DyeColor baseColor, Pattern... patterns)
	{
		this(country, adjective, false, baseColor, patterns);
	}

	/**
	 * Create a country flag banner for a country with a name often preceded with "The".
	 *
	 * @param country    Country name.
	 * @param adjective  Country name as an adjective.
	 * @param article    Whether the country name should be preceded with "The".
	 * @param baseColor  base color of the banner.
	 * @param patterns   List of patterns to apply to the banner.
	 */
	CountryFlag(String country, String adjective, boolean article, DyeColor baseColor, Pattern... patterns)
	{
		_country = country;
		_adjective = adjective;
		_article = article;
		_baseColor = baseColor;
		_patterns = patterns;
	}

	/**
	 * @return a flag banner item.
	 */
	public ItemStack getBanner()
	{
		ItemStack banner = new ItemStack(Material.BANNER);
		BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();
		bannerMeta.setBaseColor(_baseColor);

		for (Pattern pattern : _patterns)
		{
			bannerMeta.addPattern(pattern);
		}

		banner.setItemMeta(bannerMeta);
		return banner;
	}

	/**
	 * @return the name of the country.
	 */
	public String getCountryName()
	{
		return (_article ? "The " : "") + _country;
	}

	/**
	 * @return the name of the country without a possible article before the country name.
	 */
	public String getCountryNameRaw()
	{
		return _country;
	}

	/**
	 * @return the name of the country written in an adjectival manner.
	 */
	public String getCountryAdjective()
	{
		return _adjective;
	}

	/**
	 * @return the banner color.
	 */
	public DyeColor getBaseColor()
	{
		return _baseColor;
	}

	/**
	 * @return the patterns applied to the banner.
	 */
	public List<Pattern> getPatterns()
	{
		return Arrays.asList(_patterns);
	}

}
