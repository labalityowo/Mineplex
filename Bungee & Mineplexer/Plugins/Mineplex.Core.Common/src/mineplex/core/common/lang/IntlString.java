package mineplex.core.common.lang;

import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class IntlString
{
	
	public static IntlString[] toIntl(String... strings)
	{
		IntlString[] intl = new IntlString[strings.length];
		
		for (int i = 0; i < strings.length; i++) {
			final String string = strings[i];
			intl[i] = new IntlString("")
			{
				public String tr(Locale locale)
				{
					return string;
				}
			};
		}
		
		return intl;
	}
	
	/**
	 * An empty {@link IntlString}.
	 */
	public static final IntlString EMPTY = toIntl("")[0];
	
	private final Argument<String> key;
	private final List<Argument<Object>> arguments = new ArrayList<>();

	public IntlString(String key, ChatColor... styles)
	{
		this.key = new Argument<>(key, styles);
	}

	IntlString(String key, String style)
	{
		this.key = new Argument<>(key, style);
	}

	private IntlString arg(Argument<Object> argument)
	{
		IntlString result = new IntlString(getKey().getArgument(), getKey().getStyle());
		result.arguments.addAll(getArguments());
		result.arguments.add(argument);

		return result;
	}

	public Argument<String> getKey()
	{
		return key;
	}

	public List<Argument<Object>> getArguments()
	{
		return Collections.unmodifiableList(arguments);
	}

	public IntlString arg(Object value, ChatColor... styles)
	{
		return arg(new Argument<>(value, styles));
	}

	public IntlString arg(Object value, String style)
	{
		return arg(new Argument<>(value, style));
	}

	public String tr()
	{
		return tr(Locale.getDefault());
	}
	
	public String tr(Entity entity)
	{
		if (entity instanceof Player)
			return tr((Player) entity);
		else
			return tr();
	}

	public String tr(Player player)
	{
		return tr(Lang.getPlayerLocale(player));
	}

	public String tr(Locale locale)
	{
		if (locale == null)
			locale = Locale.getDefault();

		String formatString = Lang.get(getKey().getArgument(), locale);

		if (getKey().getArgument().equals("stats.achievements.disabled.requires.0.players"))
		{
			int x = 8;
		}

		if (getArguments().isEmpty())
			return getKey().getStyle() + formatString;
		else
		{
			MessageFormat format = new MessageFormat(formatString, locale);

			Format[] formats = format.getFormatsByArgumentIndex();
			Object[] argArray = new Object[getArguments().size()];
			for (int i = 0; i < formats.length; i++)
			{
				argArray[i] = getArguments().get(i);
				if (argArray[i] instanceof IntlString)
					argArray[i] = ((IntlString) argArray[i]).tr(locale);

				if (formats[i] != null)
				{
					argArray[i] = formats[i].format(argArray[i]);
					format.setFormatByArgumentIndex(i, null);
				}

				String style = getArguments().get(i).getStyle();
				if (!style.isEmpty())
					argArray[i] = style + argArray[i] + ChatColor.RESET;

				argArray[i] = argArray[i] + getKey().getStyle();
			}

			return getKey().getStyle() + format.format(argArray, new StringBuffer(), null).toString();
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof IntlString))
			return false;

		IntlString s = (IntlString) o;

		return getKey().equals(s.getKey()) && getArguments().equals(s.getArguments());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getKey(), getArguments());
	}

	@Override
	public String toString()
	{
		return toEnglishString();
	}
	
	public String toEnglishString()
	{
		return tr(Locale.ENGLISH);
	}

	private static class Argument<T>
	{
		private final T argument;
		private final String style;

		public Argument(T value, ChatColor... styles)
		{
			this.argument = value;

			String s = "";
			ChatColor color = null;
			for (ChatColor style : styles)
			{
				if (style.isColor())
					color = style;
				else if (style.isFormat())
					s += style;
			}

			this.style = ChatColor.getLastColors((color == null ? "" : color) + s);
		}

		public Argument(T value, String style)
		{
			this.argument = value;
			this.style = style == null ? "" : ChatColor.getLastColors(style);
		}

		public T getArgument()
		{
			return argument;
		}

		public String getStyle()
		{
			return style;
		}

		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof Argument))
				return false;

			Argument<?> p = (Argument<?>) o;

			return getArgument().equals(p.getArgument()) && getStyle().equals(p.getStyle());
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(getArgument(), getStyle());
		}

		@Override
		public String toString()
		{
			return getStyle() + getArgument();
		}
	}
}