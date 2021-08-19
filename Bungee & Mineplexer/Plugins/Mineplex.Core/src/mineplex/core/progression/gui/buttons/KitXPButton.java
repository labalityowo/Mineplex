package mineplex.core.progression.gui.buttons;

import com.google.common.collect.Lists;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.ProgressiveKit;
import mineplex.core.progression.gui.guis.KitInformationTrackerMenu;
import mineplex.core.progression.gui.guis.KitMenu;
import mineplex.core.progression.math.Calculations;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This the button which takes the player to {@link KitInformationTrackerMenu}
 * Display's information regarding this kits level and experience
 */
public class KitXPButton extends KitButton
{

	private ItemStack _item;

	public KitXPButton(ProgressiveKit kit, Player player, KitProgressionManager plugin)
	{
		super(kit, null, plugin);

		ItemBuilder builder = new ItemBuilder(Material.EXP_BOTTLE);
		builder.setTitle(ChatColor.YELLOW + "XP and Level");

		List<String> lore = Lists.newArrayList(" ");

		StringBuilder levelStringBuilder = new StringBuilder();
		StringBuilder xpStringBuilder = new StringBuilder();

		int level = kit.getLevel(player.getUniqueId());

		levelStringBuilder.append(level).append(" out of 100 ")
		  .append(ChatColor.GRAY).append("(")
		  .append(getColor(level, 100)).append(100 - level)
		  .append(ChatColor.GRAY).append(")");

		int xp = kit.getXp(player.getUniqueId());
		int nextXp = Calculations.getXpForNextLevel(kit.getLevel(player.getUniqueId()));
		int diff = kit.getXpDifference(player.getUniqueId());

		xpStringBuilder.append(xp).append(" out of ").append(nextXp)
		  .append(ChatColor.GRAY).append(" (")
		  .append(getColor(xp, nextXp)).append(diff)
		  .append(ChatColor.GRAY).append(")");

		lore.add(addLoreLine("Level", levelStringBuilder.toString()));

		lore.add(addLoreLine("XP", xpStringBuilder.toString()));

		lore.add(" ");

		lore.add(ChatColor.WHITE + "Click to view Upgrade and XP tracking");

		builder.setLore(lore.toArray(new String[lore.size()]));

		_item = builder.build();
	}

	private ChatColor getColor(int i, int n)
	{
		return Calculations.getColor(i, n);
	}

	private String addLoreLine(String key, Object value)
	{
		return ChatColor.WHITE + key + ": " + ChatColor.GREEN + value;
	}

	@Override
	public ItemStack getItemStack()
	{
		return _item;
	}

	@Override
	public void setItemStack(ItemStack item)
	{
		this._item = item;
	}


	@Override
	public void onClick(Player player, ClickType clickType)
	{
		KitMenu menu = new KitInformationTrackerMenu(getKit(), getPlugin());
		menu.open(player);
	}
}
