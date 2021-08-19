package nautilus.game.arcade.managers.voting.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.managers.voting.Vote;
import nautilus.game.arcade.managers.voting.Vote.VoteData;
import nautilus.game.arcade.managers.voting.Voteable;
import nautilus.game.arcade.managers.voting.types.VotableMap;

public class VotingPage<T extends Voteable> extends ShopPageBase<ArcadeManager, VotingShop>
{

	private final Vote<T> _vote;

	public VotingPage(ArcadeManager plugin, VotingShop shop, Player player, Vote<T> vote)
	{
		super(plugin, shop, plugin.GetClients(), plugin.GetDonation(), "Pick The Next " + vote.getName() + "!", player, 27);

		_vote = vote;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int slot = 12;
		VoteData<T> playerVote = _vote.getPlayerVote(getPlayer());

		addButton(10, new ItemBuilder(Material.COAL)
				.setTitle(C.cPurpleB + "Random " + _vote.getName())
				.addLore("Leave " + _vote.getName() + " voting up to chance", "with a random selection.")
				.setGlow(playerVote == null)
				.build(), (player, clickType) ->
		{
			_vote.removeVote(player);
			playRemoveSound(player);
			player.sendMessage(F.main("Game", "You voted for " + F.name("Random") + "."));
		});

		String lowerName = _vote.getName().toLowerCase();

		for (T voteable : _vote.getValues())
		{
			String name = voteable.getName();

			if (voteable instanceof VotableMap)
			{
				name = ((VotableMap) voteable).getDisplayName();
			}

			ItemBuilder builder = new ItemBuilder(voteable.getItemStack())
					.setTitle(C.cGreenB + name)
					.addLore("Click to vote for " + name, "to be the next " + lowerName + "!")
					.setGlow(playerVote != null && playerVote.getValue().equals(voteable));

			String fName = name;

			addButton(slot++, builder.build(), (player, clickType) ->
			{
				_vote.vote(player, voteable);
				playAcceptSound(player);
				player.sendMessage(F.main("Game", "You voted for the next " + lowerName + ", " + F.name(fName) + "."));
			});
		}
	}
}
