package mineplex.core.book;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;

import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class BookBuilder
{
	public static BookBuilder newBuilder()
	{
		return new BookBuilder();
	}

	private final List<PageBuilder> _pageBuilders = new ArrayList<>();

	private String _title;
	private String _author = "";
	private int _generation = 0;
	private boolean _resolved = true;

	private BookBuilder()
	{

	}

	public PageBuilder newPage(int index)
	{
		PageBuilder pageBuilder = new PageBuilder(this);
		_pageBuilders.add(index, pageBuilder);
		return pageBuilder;
	}

	public PageBuilder newPage()
	{
		PageBuilder pageBuilder = new PageBuilder(this);
		_pageBuilders.add(pageBuilder);
		return pageBuilder;
	}

	public int getPageNumber(PageBuilder builder)
	{
		return _pageBuilders.indexOf(builder);
	}

	public BookBuilder title(String title)
	{
		Validate.notNull(title, "Title cannot be null");
		this._title = title;
		return this;
	}

	public BookBuilder author(String author)
	{
		Validate.notNull(author, "Author cannot be null");
		this._author = author;
		return this;
	}

	public BookBuilder resolved(boolean resolved)
	{
		this._resolved = resolved;
		return this;
	}

	public BookBuilder generation(int generation)
	{
		this._generation = generation;
		return this;
	}

	public ItemStack toItem()
	{
		net.minecraft.server.v1_8_R3.ItemStack itemStack = new net.minecraft.server.v1_8_R3.ItemStack(Items.WRITTEN_BOOK);
		itemStack.setTag(toCompound());
		return CraftItemStack.asCraftMirror(itemStack);
	}

	public NBTTagCompound toCompound()
	{
		NBTTagCompound compound = new NBTTagCompound();
		if (_author != null)
			compound.setString("author", _author);
		if (_title != null)
		{
			if (_title.length() < 32)
			{
				compound.setString("title", _title);
			}
			else
			{
				NBTTagCompound display = new NBTTagCompound();

				display.set("Name", new NBTTagString(ChatColor.RESET + _title));
				compound.set("display", display);
			}
		}
		compound.setInt("generation", _generation);
		compound.setBoolean("resolved", _resolved);

		NBTTagList pages = new NBTTagList();
		for (PageBuilder pageBuilder : _pageBuilders)
		{
			pages.add(new NBTTagString(pageBuilder.build()));
		}

		compound.set("pages", pages);

		return compound;
	}
}
