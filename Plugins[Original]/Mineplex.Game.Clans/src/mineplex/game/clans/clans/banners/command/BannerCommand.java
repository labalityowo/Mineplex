package mineplex.game.clans.clans.banners.command;

import java.util.LinkedList;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.banners.BannerManager;
import mineplex.game.clans.clans.banners.BannerPattern;
import mineplex.game.clans.clans.banners.ClanBanner;
import mineplex.game.clans.clans.banners.gui.nonedit.NonEditOverviewGUI;

/**
 * Main banner usage command
 */
public class BannerCommand extends CommandBase<BannerManager>
{
	public BannerCommand(BannerManager plugin)
	{
		super(plugin, BannerManager.Perm.BANNER_COMMAND, "banner");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (ClansManager.getInstance().getClan(caller) == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "You are not in a Clan!"));
			return;
		}
		ClanInfo clan = ClansManager.getInstance().getClan(caller);

		if (Plugin.getBannerUnlockLevel(caller) < 1)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "You have not purchased the ability to use Clan banners! Buy it at http://www.mineplex.com/shop!"));
			return;
		}
		if (!Plugin.LoadedBanners.containsKey(clan.getName()))
		{
			if (Plugin.getBannerUnlockLevel(caller) >= 2 && clan.getMembers().get(caller.getUniqueId()).getRole() == ClanRole.LEADER)
			{
				LinkedList<BannerPattern> patterns = new LinkedList<>();
				for (int i = 0; i < 12; i++)
				{
					patterns.add(new BannerPattern(i + 1));
				}
				ClanBanner banner = new ClanBanner(Plugin, clan, DyeColor.WHITE, patterns);
				Plugin.LoadedBanners.put(clan.getName(), banner);
				banner.save();
			}
			else
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Your Clan does not have a set banner!"));
				return;
			}
		}
		new NonEditOverviewGUI(caller, Plugin.LoadedBanners.get(clan.getName()));
	}
}