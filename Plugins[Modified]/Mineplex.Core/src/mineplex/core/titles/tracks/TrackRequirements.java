package mineplex.core.titles.tracks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.GadgetSet;

public class TrackRequirements
{
	private final Track _track;
	private final List<TrackTier> _tierRequirements = new ArrayList<>();

	private List<PointWrapper> _lore = new ArrayList<>();
	private List<PointWrapper> _bonuses = new ArrayList<>();

	private final GadgetManager _gadgetManager = Managers.require(GadgetManager.class);

	public TrackRequirements(Track track)
	{
		this._track = track;
	}

	public TrackRequirements addTier(TrackTier tier)
	{
		this._tierRequirements.add(tier);
		return this;
	}

	public TrackTier getTier(int tier)
	{
		return this._tierRequirements.get(tier);
	}

	public TrackTier getTier(Player player)
	{
		for (int i = _tierRequirements.size() - 1; i >= 0; i--)
		{
			if (_tierRequirements.get(i).test(player))
			{
				return _tierRequirements.get(i);
			}
		}
		return null;
	}

	public TrackTier getNextTier(Player player)
	{
		for (int i = _tierRequirements.size() - 1; i >= 0; i--)
		{
			if (_tierRequirements.get(i).test(player))
			{
				if (i == _tierRequirements.size() - 1)
				{
					return null;
				}
				else
				{
					return _tierRequirements.get(i + 1);
				}
			}
		}
		return _tierRequirements.get(0);
	}

	public List<TrackTier> getTiers()
	{
		return Collections.unmodifiableList(this._tierRequirements);
	}

	public int getRank(TrackTier tier)
	{
		return _tierRequirements.indexOf(tier) + 1;
	}

	public TrackRequirements withRequirement(int points, String perName)
	{
		return withRequirement(points, "per", perName);
	}

	public TrackRequirements withRequirement(int points, String verb, String name)
	{
		_lore.add(new PointWrapper(points, name, builder ->
		{
			builder.append("+ ", ComponentBuilder.FormatRetention.NONE)
					.append(points + " " + UtilText.plural("point", points) + " ", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.GREEN)
					.append(verb, ComponentBuilder.FormatRetention.NONE)
					.append(" ", ComponentBuilder.FormatRetention.NONE)
					.append(name)
					.color(ChatColor.GREEN)
					.append("\n", ComponentBuilder.FormatRetention.NONE);
		}));
		_lore.sort((a, b) ->
		{
			int result = Integer.compare(b._pts, a._pts);
			return result == 0 ? ChatColor.stripColor(b._name).compareTo(ChatColor.stripColor(a._name)) : result;
		});
		return this;
	}

	public TrackRequirements withSetBonus(Class<? extends GadgetSet> clazz, int bonusMultiplier)
	{
		return withBonus(_gadgetManager.getGadgetSet(clazz).getName() + " Set", "with", bonusMultiplier);
	}

	public TrackRequirements withBonus(String name, String verb, int multiplier)
	{
		_bonuses.add(new PointWrapper(multiplier, name, builder ->
		{
			builder.append(multiplier + "x", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.GREEN)
					.append(" points ", ComponentBuilder.FormatRetention.NONE)
					.append(verb, ComponentBuilder.FormatRetention.NONE)
					.append(" ", ComponentBuilder.FormatRetention.NONE)
					.append(name)
					.color(ChatColor.GREEN)
					.append("\n", ComponentBuilder.FormatRetention.NONE);
		}));
		_bonuses.sort((a, b) ->
		{
			int result = Integer.compare(b._pts, a._pts);
			return result == 0 ? ChatColor.stripColor(b._name).compareTo(ChatColor.stripColor(a._name)) : result;
		});
		return this;
	}

	public void appendLore(ComponentBuilder builder)
	{
		_lore.forEach(wrapper -> wrapper._consumer.accept(builder));
		_bonuses.forEach(wrapper -> wrapper._consumer.accept(builder));

		if (!_bonuses.isEmpty() || !_lore.isEmpty())
			builder.append("\n");
	}

	private class PointWrapper
	{
		private int _pts;
		private String _name;
		private Consumer<ComponentBuilder> _consumer;

		public PointWrapper(int pts, String name, Consumer<ComponentBuilder> consumer)
		{
			_pts = pts;
			_name = name;
			_consumer = consumer;
		}
	}
}
