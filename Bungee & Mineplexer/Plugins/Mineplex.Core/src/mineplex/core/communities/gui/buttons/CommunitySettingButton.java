package mineplex.core.communities.gui.buttons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import mineplex.core.common.util.C;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.data.Community.PrivacySetting;
import mineplex.core.communities.data.CommunityRole;
import mineplex.core.communities.data.CommunitySetting;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemBuilder;

public class CommunitySettingButton extends CommunitiesGUIButton
{
	@SuppressWarnings("serial")
	private static final Map<ChatColor, DyeColor> COLOR_MAP = new HashMap<ChatColor, DyeColor>()
	{
		{
			put(ChatColor.AQUA, DyeColor.CYAN);
			put(ChatColor.BLACK, DyeColor.BLACK);
			put(ChatColor.BLUE, DyeColor.LIGHT_BLUE);
			put(ChatColor.DARK_AQUA, DyeColor.CYAN);
			put(ChatColor.DARK_BLUE, DyeColor.BLUE);
			put(ChatColor.DARK_GRAY, DyeColor.GRAY);
			put(ChatColor.DARK_GREEN, DyeColor.GREEN);
			put(ChatColor.DARK_PURPLE, DyeColor.PURPLE);
			put(ChatColor.DARK_RED, DyeColor.RED);
			put(ChatColor.GOLD, DyeColor.YELLOW);
			put(ChatColor.GRAY, DyeColor.SILVER);
			put(ChatColor.GREEN, DyeColor.LIME);
			put(ChatColor.LIGHT_PURPLE, DyeColor.PINK);
			put(ChatColor.RED, DyeColor.RED);
			put(ChatColor.WHITE, DyeColor.WHITE);
			put(ChatColor.YELLOW, DyeColor.YELLOW);
		}
	};
	@SuppressWarnings("serial")
	private static final Map<ChatColor, String> COLOR_NAME_MAP = new HashMap<ChatColor, String>()
	{
		{
			put(ChatColor.AQUA, "Aqua");
			put(ChatColor.BLACK, "Black");
			put(ChatColor.BLUE, "Blue");
			put(ChatColor.DARK_AQUA, "Cyan");
			put(ChatColor.DARK_BLUE, "Dark Blue");
			put(ChatColor.DARK_GRAY, "Dark Gray");
			put(ChatColor.DARK_GREEN, "Dark Green");
			put(ChatColor.DARK_PURPLE, "Purple");
			put(ChatColor.DARK_RED, "Dark Red");
			put(ChatColor.GOLD, "Gold");
			put(ChatColor.GRAY, "Gray");
			put(ChatColor.GREEN, "Green");
			put(ChatColor.LIGHT_PURPLE, "Pink");
			put(ChatColor.RED, "Red");
			put(ChatColor.WHITE, "White");
			put(ChatColor.YELLOW, "Yellow");
		}
	};
	
	private Player _viewer;
	private Community _community;
	private CommunitySetting _setting;
	
	public CommunitySettingButton(Player viewer, Community community, CommunitySetting setting)
	{
		super(new ItemBuilder(Material.BARRIER).build());
		
		_viewer = viewer;
		_community = community;
		_setting = setting;
		update();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update()
	{
		if (_setting == CommunitySetting.FAVORITE_GAME)
		{
			Button = new ItemBuilder(new ItemStack(_community.getFavoriteGame().getMaterial(), 1, _community.getFavoriteGame().getMaterialData(), null)).setTitle(C.cGreenB + "Favorite Game").addLore(C.cWhite + _community.getFavoriteGame().getName(), C.cRed, C.cYellow + "Left Click " + C.cWhite + "Next Game", C.cYellow + "Right Click " + C.cWhite + "Previous Game").build();
		}
		else if (_setting == CommunitySetting.PRIVACY)
		{
			Button = new ItemBuilder(Material.DARK_OAK_DOOR_ITEM).setTitle(C.cGreenB + "Privacy").addLore(C.cWhite + _community.getPrivacySetting().getDisplayText(), C.cRed, C.cYellow + "Left Click " + C.cWhite + "Next Privacy Setting", C.cYellow + "Right Click " + C.cWhite + "Previous Privacy Setting").build();
		}
		else if (_setting == CommunitySetting.CHAT_DELAY)
		{
			Button = new ItemBuilder(Material.PAPER).setTitle(C.cGreenB + "Chat Delay").addLore(C.cWhite + (_community.getChatDelay() == 0 ? "No Delay" : _community.getChatDelay() / 1000 + " Second(s)"), C.cRed, C.cYellow + "Left Click " + C.cWhite + "Next Delay Setting", C.cYellow + "Right Click " + C.cWhite + "Previous Delay Setting").build();
		}
		else if (_setting == CommunitySetting.CHAT_NAME_COLOR)
		{
			ItemStack base = new MaterialData(Material.WOOL, COLOR_MAP.get(_community.getChatFormatting()[0]).getWoolData()).toItemStack(1);
			Button = new ItemBuilder(base).setTitle(C.cGreenB + "Chat Community Color").addLore(C.cWhite + COLOR_NAME_MAP.get(_community.getChatFormatting()[0]), C.cRed, C.cYellow + "Left Click " + C.cWhite + "Next Color", C.cYellow + "Right Click " + C.cWhite + "Previous Color").build();
		}
		else if (_setting == CommunitySetting.CHAT_PLAYER_COLOR)
		{
			ItemStack base = new MaterialData(Material.WOOL, COLOR_MAP.get(_community.getChatFormatting()[1]).getWoolData()).toItemStack(1);
			Button = new ItemBuilder(base).setTitle(C.cGreenB + "Chat Player Color").addLore(C.cWhite + COLOR_NAME_MAP.get(_community.getChatFormatting()[1]), C.cRed, C.cYellow + "Left Click " + C.cWhite + "Next Color", C.cYellow + "Right Click " + C.cWhite + "Previous Color").build();
		}
		else if (_setting == CommunitySetting.CHAT_MESSAGE_COLOR)
		{
			ItemStack base = new MaterialData(Material.WOOL, COLOR_MAP.get(_community.getChatFormatting()[2]).getWoolData()).toItemStack(1);
			Button = new ItemBuilder(base).setTitle(C.cGreenB + "Chat Message Color").addLore(C.cWhite + COLOR_NAME_MAP.get(_community.getChatFormatting()[2]), C.cRed, C.cYellow + "Left Click " + C.cWhite + "Next Color", C.cYellow + "Right Click " + C.cWhite + "Previous Color").build();
		}
	}

	@Override
	public void handleClick(ClickType type)
	{
		if (type == ClickType.LEFT)
		{
			if (getCommunityManager().Get(_viewer).getRoleIn(_community) != null && getCommunityManager().Get(_viewer).getRoleIn(_community).ordinal() <= CommunityRole.COLEADER.ordinal())
			{
				String[] valueArray = new String[] {};
				int index = 0;
				
				if (_setting == CommunitySetting.FAVORITE_GAME)
				{
					GameDisplay[] games = Arrays.asList(GameDisplay.values()).stream().filter(display -> display.isCommunityFavoriteOption()).toArray(size -> new GameDisplay[size]);
					valueArray = new String[games.length];
					for (int i = 0; i < games.length; i++)
					{
						valueArray[i] = games[i].getName();
					}
					
					index = Arrays.asList(valueArray).indexOf(_community.getFavoriteGame().getName());
				}
				else if (_setting == CommunitySetting.PRIVACY)
				{
					valueArray = new String[] {PrivacySetting.OPEN.toString(), PrivacySetting.RECRUITING.toString(), PrivacySetting.PRIVATE.toString()};
					
					index = Arrays.asList(valueArray).indexOf(_community.getPrivacySetting().toString());
				}
				else if (_setting == CommunitySetting.CHAT_DELAY)
				{
					valueArray = new String[] {1000L + "", 3000L + "", 5000L + ""};
					
					index = Arrays.asList(valueArray).indexOf(_community.getChatDelay().toString());
				}
				else if (_setting == CommunitySetting.CHAT_NAME_COLOR)
				{
					ChatColor[] colors = COLOR_MAP.keySet().toArray(new ChatColor[COLOR_MAP.size()]);
					valueArray = new String[colors.length];
					for (int i = 0; i < colors.length; i++)
					{
						valueArray[i] = colors[i].name();
					}
					
					index = Arrays.asList(valueArray).indexOf(_community.getChatFormatting()[0].name());
				}
				else if (_setting == CommunitySetting.CHAT_PLAYER_COLOR)
				{
					ChatColor[] colors = COLOR_MAP.keySet().toArray(new ChatColor[COLOR_MAP.size()]);
					valueArray = new String[colors.length];
					for (int i = 0; i < colors.length; i++)
					{
						valueArray[i] = colors[i].name();
					}
					
					index = Arrays.asList(valueArray).indexOf(_community.getChatFormatting()[1].name());
				}
				else if (_setting == CommunitySetting.CHAT_MESSAGE_COLOR)
				{
					ChatColor[] colors = COLOR_MAP.keySet().toArray(new ChatColor[COLOR_MAP.size()]);
					valueArray = new String[colors.length];
					for (int i = 0; i < colors.length; i++)
					{
						valueArray[i] = colors[i].name();
					}
					
					index = Arrays.asList(valueArray).indexOf(_community.getChatFormatting()[2].name());
				}
				
				int newIndex = (index + 1 >= valueArray.length) ? 0 : (index + 1);
				getCommunityManager().handleSettingUpdate(_viewer, _community, _setting, valueArray[newIndex]);
			}
		}
		if (type == ClickType.RIGHT)
		{
			if (getCommunityManager().Get(_viewer).getRoleIn(_community) != null && getCommunityManager().Get(_viewer).getRoleIn(_community).ordinal() <= CommunityRole.COLEADER.ordinal())
			{
				String[] valueArray = new String[] {};
				int index = 0;
				
				if (_setting == CommunitySetting.FAVORITE_GAME)
				{
					GameDisplay[] games = Arrays.asList(GameDisplay.values()).stream().filter(display -> display.isCommunityFavoriteOption()).toArray(size -> new GameDisplay[size]);
					valueArray = new String[games.length];
					for (int i = 0; i < games.length; i++)
					{
						valueArray[i] = games[i].getName();
					}
					
					index = Arrays.asList(valueArray).indexOf(_community.getFavoriteGame().getName());
				}
				else if (_setting == CommunitySetting.PRIVACY)
				{
					valueArray = new String[] {PrivacySetting.OPEN.toString(), PrivacySetting.RECRUITING.toString(), PrivacySetting.PRIVATE.toString()};
					
					index = Arrays.asList(valueArray).indexOf(_community.getPrivacySetting().toString());
				}
				else if (_setting == CommunitySetting.CHAT_DELAY)
				{
					valueArray = new String[] {1000L + "", 3000L + "", 5000L + ""};
					
					index = Arrays.asList(valueArray).indexOf(_community.getChatDelay().toString());
				}
				else if (_setting == CommunitySetting.CHAT_NAME_COLOR)
				{
					ChatColor[] colors = COLOR_MAP.keySet().toArray(new ChatColor[COLOR_MAP.size()]);
					valueArray = new String[colors.length];
					for (int i = 0; i < colors.length; i++)
					{
						valueArray[i] = colors[i].name();
					}
					
					index = Arrays.asList(valueArray).indexOf(_community.getChatFormatting()[0].name());
				}
				else if (_setting == CommunitySetting.CHAT_PLAYER_COLOR)
				{
					ChatColor[] colors = COLOR_MAP.keySet().toArray(new ChatColor[COLOR_MAP.size()]);
					valueArray = new String[colors.length];
					for (int i = 0; i < colors.length; i++)
					{
						valueArray[i] = colors[i].name();
					}
					
					index = Arrays.asList(valueArray).indexOf(_community.getChatFormatting()[1].name());
				}
				else if (_setting == CommunitySetting.CHAT_MESSAGE_COLOR)
				{
					ChatColor[] colors = COLOR_MAP.keySet().toArray(new ChatColor[COLOR_MAP.size()]);
					valueArray = new String[colors.length];
					for (int i = 0; i < colors.length; i++)
					{
						valueArray[i] = colors[i].name();
					}
					
					index = Arrays.asList(valueArray).indexOf(_community.getChatFormatting()[2].name());
				}
				
				int newIndex = (index - 1 < 0) ? (valueArray.length - 1) : (index - 1);
				getCommunityManager().handleSettingUpdate(_viewer, _community, _setting, valueArray[newIndex]);
			}
		}
	}
}