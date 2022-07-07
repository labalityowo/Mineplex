package mineplex.game.clans.clans.mounts;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.mounts.Mount.MountType;

public class MountClaimToken
{
	private final String STAR = "✩";
	
	public final int JumpStars;
	public final int SpeedStars;
	public final int StrengthStars;
	public final MountType Type;
	
	public MountClaimToken(int jumpStars, int speedStars, int strengthStars, MountType type)
	{
		JumpStars = jumpStars;
		SpeedStars = speedStars;
		StrengthStars = strengthStars;
		Type = type;
	}
	
	public ItemStack toItem()
	{
		ItemBuilder builder = new ItemBuilder(Type.getDisplayType());
		builder.setTitle(Type.getDisplayName() + " Mount Token");
		String strength = C.cYellow;
		for (int i = 0; i < StrengthStars; i++)
		{
			strength += STAR;
		}
		builder.addLore(C.cPurple + "Strength: " + strength);
		String speed = C.cYellow;
		for (int i = 0; i < SpeedStars; i++)
		{
			speed += STAR;
		}
		builder.addLore(C.cPurple + "Speed: " + speed);
		String jump = C.cYellow;
		for (int i = 0; i < JumpStars; i++)
		{
			jump += STAR;
		}
		builder.addLore(C.cPurple + "Jump: " + jump);
		builder.addLore(C.cRed);
		builder.addLore(C.cDGreen + "Right-Click While Holding to Consume");
		
		return builder.build();
	}
	
	public static MountClaimToken fromItem(ItemStack item)
	{
		if (!item.hasItemMeta() || !item.getItemMeta().hasLore())
		{
			return null;
		}
		
		MountType type = null;
		for (MountType check : MountType.values())
		{
			if (check.getDisplayType() == item.getType())
			{
				type = check;
				break;
			}
		}
		if (type == null)
		{
			return null;
		}
		
		int strength = -1;
		int speed = -1;
		int jump = -1;
		
		for (String lore : item.getItemMeta().getLore())
		{
			if (ChatColor.stripColor(lore).startsWith("Strength: "))
			{
				strength = ChatColor.stripColor(lore).replace("Strength: ", "").length();
			}
			if (ChatColor.stripColor(lore).startsWith("Speed: "))
			{
				speed = ChatColor.stripColor(lore).replace("Speed: ", "").length();
			}
			if (ChatColor.stripColor(lore).startsWith("Jump: "))
			{
				jump = ChatColor.stripColor(lore).replace("Jump: ", "").length();
			}
		}
		
		if (strength <= 0 || speed <= 0 || jump <= 0)
		{
			return null;
		}
		
		return new MountClaimToken(jump, speed, strength, type);
	}
}