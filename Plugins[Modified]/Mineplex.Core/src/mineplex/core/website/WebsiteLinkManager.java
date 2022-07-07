package mineplex.core.website;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import mineplex.core.Managers;
import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.event.GroupAddEvent;
import mineplex.core.account.event.GroupRemoveEvent;
import mineplex.core.account.event.PrimaryGroupUpdateEvent;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.wineffect.WinEffectLogo;
import mineplex.core.powerplayclub.PPCDataRequestEvent;
import mineplex.core.powerplayclub.PowerPlayData;
import mineplex.core.powerplayclub.SubscriptionAddEvent;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.TrackManager.TrackGiveResult;
import mineplex.core.titles.tracks.award.NewWebsiteTrack;
import mineplex.serverdata.database.DBPool;

@ReflectivelyCreateMiniPlugin
public class WebsiteLinkManager extends MiniDbClientPlugin<ForumUserData>
{
	public enum Perm implements Permission
	{
		LINK_COMMAND,
		UNLINK_COMMAND,
	}

	private static final JsonParser PARSER = new JsonParser();
	private static final String API_URL = "https://xen.mineplex.com/api.php";
	private static final String API_KEY = "dd412425-edb0-477c-abee-2d0b507c59ef";
	private static final int POWER_PLAY_TAG_ID = 17;
	private static final int LINKED_TAG_ID = 91;
	
	private WebsiteLinkManager()
	{
		super("Website Link");
		
		addCommand(new LinkCommand(this));
		addCommand(new UnlinkCommand(this));
		
		Authenticator.setDefault(new MineplexAuthenticator("minexen", "!z#u5G8g"));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.LINK_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.UNLINK_COMMAND, true, true);
	}
	
	private String makeUsernameURL(String username)
	{
		return username.replaceAll(Pattern.quote(" "), "%20");
	}
	
	public void unlink(Player sender, String target)
	{
		getClientManager().getOrLoadClient(target, client ->
		{
			if (client != null)
			{
				Consumer<ForumUserData> dataCallback = data ->
				{
					if (data == null)
					{
						UtilPlayer.message(sender, F.main(getName(), "Could not find " + F.name(target) + "!"));
					}
					else
					{
						if (!data.Linked)
						{
							UtilPlayer.message(sender, F.main(getName(), F.name(target) + " is not linked to a forum account!"));
						}
						else
						{
							runAsync(() ->
							{
								loadXenforoAccount(data.LinkedForumId, user ->
								{
									List<Integer> remove = new ArrayList<>();
									remove.add(POWER_PLAY_TAG_ID);
									remove.add(LINKED_TAG_ID);
									for (PermissionGroup group : PermissionGroup.values())
									{
										if (group.getForumId() != -1)
										{
											remove.add(group.getForumId());
										}
									}
									String call = "action=editUser&user=" + makeUsernameURL(user.username) + "&custom_fields=mcAcctIdPC=";
									doAPICall(call, err ->
									{
										runSync(() -> UtilPlayer.message(sender, F.main(getName(), F.name(target) + " was not able to be unlinked at this time!")));
									}, () ->
									{
										runSync(() -> UtilPlayer.message(sender, F.main(getName(), F.name(target) + " was not able to be unlinked at this time!")));
									}, () ->
									{
										refreshSiteTags(data.LinkedForumId, remove, new ArrayList<>(), false, () ->
										{
											try (Connection c = DBPool.getAccount().getConnection())
											{
												c.prepareStatement("DELETE FROM forumLink WHERE accountId=" + client.getAccountId()).execute();
												runSync(() ->
												{
													UtilPlayer.message(sender, F.main(getName(), F.name(target) + " was successfully unlinked!"));
													data.Linked = false;
													data.LinkedForumId = -1;
													data.LastSyncedPowerPlayStatus = false;
												});
											}
											catch (SQLException e)
											{
												e.printStackTrace();
												runSync(() -> UtilPlayer.message(sender, F.main(getName(), F.name(target) + " was not able to be unlinked at this time!")));
											}
										}, false, () -> UtilPlayer.message(sender, F.main(getName(), F.name(target) + " was not able to be unlinked at this time!")), true);
									});
								});
							});
						}
					}
				};
				if (Bukkit.getPlayer(client.getUniqueId()) != null)
				{
					dataCallback.accept(Get(client.getUniqueId()));
				}
				else
				{
					runAsync(() ->
					{
						try (Connection c = DBPool.getAccount().getConnection())
						{
							ResultSet rs = c.prepareStatement("SELECT userId, powerPlayStatus FROM forumLink WHERE accountId=" + client.getAccountId() + ";").executeQuery();
							if (rs.next())
							{
								Integer userId = rs.getInt(1);
								Boolean powerPlay = rs.getBoolean(2);
								final ForumUserData data = new ForumUserData();
								data.Linked = true;
								data.LinkedForumId = userId;
								data.LastSyncedPowerPlayStatus = powerPlay;
								runSync(() -> dataCallback.accept(data));
							}
							else
							{
								runSync(() -> dataCallback.accept(new ForumUserData()));
							}
						}
						catch (SQLException e)
						{
							e.printStackTrace();
							runSync(() -> dataCallback.accept(new ForumUserData()));
						}
					});
				}
			}
			else
			{
				UtilPlayer.message(sender, F.main(getName(), "Could not find " + F.name(target) + "!"));
			}
		});
	}
	
	public void startLink(Player player, String code)
	{
		final int forumId = getForumId(code);
		final int accountId = getClientManager().getAccountId(player);
		final PermissionGroup group = getClientManager().Get(player).getPrimaryGroup();
		final Set<PermissionGroup> additional = getClientManager().Get(player).getAdditionalGroups();
		PowerPlayData d = UtilServer.CallEvent(new PPCDataRequestEvent(player)).getData();
		final boolean powerPlay = (d != null && d.isSubscribed());
		if (forumId == -1)
		{
			UtilPlayer.message(player, F.main(getName(), "That link code is invalid!"));
			return;
		}
		runAsync(() ->
		{
			loadXenforoAccount(forumId, data ->
			{
				if (data == null)
				{
					UtilPlayer.message(player, F.main(getName(), "That link code is invalid!"));
					return;
				}
				if (data.custom_fields.containsKey("mcAcctIdPC") && !data.custom_fields.get("mcAcctIdPC").isEmpty())
				{
					UtilPlayer.message(player, F.main(getName(), "That link code is invalid!"));
					return;
				}
				completeLink(player, data, accountId, group, additional, powerPlay);
			});
		});
	}
	
	private void completeLink(Player player, XenForoData data, int accountId, PermissionGroup group, Set<PermissionGroup> additional, boolean powerPlay)
	{
		try (Connection c = DBPool.getAccount().getConnection())
		{
			boolean success = c.prepareStatement("INSERT INTO forumLink (accountId, userId, powerPlayStatus) VALUES (" + accountId + ", " + data.user_id + ", " + powerPlay + ");").executeUpdate() > 0;
			if (success)
			{
				String call = "action=editUser&user=" + makeUsernameURL(data.username) + "&custom_fields=mcAcctIdPC=" + accountId;
				List<Integer> adding = new ArrayList<>();
				adding.add(91);
				if (group.getForumId() != -1)
				{
					adding.add(group.getForumId());
				}
				for (PermissionGroup addit : additional)
				{
					if (addit.getForumId() != -1)
					{
						adding.add(addit.getForumId());
					}
				}
				if (powerPlay)
				{
					adding.add(POWER_PLAY_TAG_ID);
				}
				doAPICall(call, err ->
				{
					runSync(() -> UtilPlayer.message(player, F.main(getName(), "The link failed! Please try again!")));
				}, () ->
				{
					runSync(() -> UtilPlayer.message(player, F.main(getName(), "The link failed! Please try again!")));
				}, () ->
				{
					refreshSiteTags(data, new ArrayList<>(), adding, false, () ->
					{
						UtilPlayer.message(player, F.main(getName(), "You have successfully linked your account!"));
						if (player.isOnline())
						{
							ForumUserData userData = Get(player);

							userData.LinkedForumId = data.user_id;
							userData.LastSyncedPowerPlayStatus = powerPlay;
							userData.Linked = true;
						}

						TrackManager trackManager = Managers.get(TrackManager.class);
						GadgetManager gadgetManager = Managers.get(GadgetManager.class);
						DonationManager donationManager = Managers.get(DonationManager.class);

						if (trackManager != null)
						{
							Track track = trackManager.getTrack(NewWebsiteTrack.class);

							trackManager.unlockTrack(player, track, result ->
							{
								if (result == TrackGiveResult.SUCCESS)
								{
									player.sendMessage(F.main(getName(), "You unlocked the " + F.name(track.getLongName()) + " title."));
								}
								else
								{
									player.sendMessage(F.main(getName(), "Was unable to reward you the " + F.name(track.getLongName()) + " title."));
								}
							});
						}

						if (gadgetManager != null && donationManager != null)
						{
							String gadget = gadgetManager.getGadget(WinEffectLogo.class).getName();

							donationManager.purchaseUnknownSalesPackage(player, gadgetManager.getGadget(WinEffectLogo.class).getName(), GlobalCurrency.TREASURE_SHARD, 0, true, response ->
							{
								if (response == TransactionResponse.Success)
								{
									player.sendMessage(F.main(getName(), "You unlocked the " + F.name(gadget) + " Win Effect."));
								}
								else if (response != TransactionResponse.AlreadyOwns)
								{
									player.sendMessage(F.main(getName(), "Was unable to reward you the " + F.name(gadget) + " Win Effect."));
								}
							});
						}
					}, true, () -> UtilPlayer.message(player, F.main(getName(), "The link failed! Please try again!")), true);
				});
			}
			else
			{
				runSync(() -> UtilPlayer.message(player, F.main(getName(), "The link failed! Please try again!")));
			}
		}
		catch (SQLException e)
		{
			runSync(() -> UtilPlayer.message(player, F.main(getName(), "The link failed! Please try again!")));
			e.printStackTrace();
		}
	}
	
	private void refreshSiteTags(int userId, List<Integer> removing, List<Integer> adding, boolean runAsync, Runnable after, boolean runAfterSync, Runnable onErr, boolean runErrSync)
	{
		Runnable r = () ->
		{
			loadXenforoAccount(userId, data ->
			{
				refreshSiteTags(data, removing, adding, false, after, runAfterSync, onErr, runErrSync);
			});
		};
		
		if (runAsync)
		{
			runAsync(r);
		}
		else
		{
			r.run();
		}
	}
	
	private void refreshSiteTags(XenForoData data, List<Integer> removing, List<Integer> adding, boolean runAsync, Runnable after, boolean runAfterSync, Runnable onErr, boolean runErrSync)
	{
		Runnable r = () ->
		{
			if (data == null)
			{
				return;
			}
			String callBase = "action=editUser&user=" + makeUsernameURL(data.username);
			StringBuilder groups = new StringBuilder();
			for (int groupId : data.secondary_group_ids)
			{
				if (!removing.contains(groupId) && !adding.contains(groupId))
				{
					groups.append(",").append(groupId);
				}
			}
			for (Integer groupId : adding)
			{
				groups.append(",").append(groupId);
			}
			if (groups.length() > 0)
			{
				groups = new StringBuilder(groups.substring(1));
			}
			final String addGroups = groups.toString();
			groups = new StringBuilder();
			for (int groupId : data.secondary_group_ids)
			{
				groups.append(",").append(groupId);
			}
			if (groups.length() > 0)
			{
				groups = new StringBuilder(groups.substring(1));
			}
			final String remGroups = groups.toString();
			if (!remGroups.isEmpty())
			{
				doAPICall(callBase + "&remove_groups=" + remGroups, err ->
				{
					if (runErrSync)
					{
						runSync(onErr);
					}
					else
					{
						onErr.run();
					}
				}, () ->
				{
					if (runErrSync)
					{
						runSync(onErr);
					}
					else
					{
						onErr.run();
					}
				}, () ->
				{
					if (!addGroups.isEmpty())
					{
						doAPICall(callBase + "&add_groups=" + addGroups, err ->
						{
							if (runErrSync)
							{
								runSync(onErr);
							}
							else
							{
								onErr.run();
							}
						}, () ->
						{
							if (runErrSync)
							{
								runSync(onErr);
							}
							else
							{
								onErr.run();
							}
						}, () ->
						{
							if (runAfterSync)
							{
								runSync(after);
							}
							else
							{
								after.run();
							}
						});
					}
					else
					{
						if (runAfterSync)
						{
							runSync(after);
						}
						else
						{
							after.run();
						}
					}
				});
			}
			else
			{
				if (!addGroups.isEmpty())
				{
					doAPICall(callBase + "&add_groups=" + addGroups, err ->
					{
						if (runErrSync)
						{
							runSync(onErr);
						}
						else
						{
							onErr.run();
						}
					}, () ->
					{
						if (runErrSync)
						{
							runSync(onErr);
						}
						else
						{
							onErr.run();
						}
					}, () ->
					{
						if (runAfterSync)
						{
							runSync(after);
						}
						else
						{
							after.run();
						}
					});
				}
				else
				{
					if (runAfterSync)
					{
						runSync(after);
					}
					else
					{
						after.run();
					}
				}
			}
		};

		if (runAsync)
		{
			runAsync(r);
		}
		else
		{
			r.run();
		}
	}
	
	private void loadXenforoAccount(int userId, Consumer<XenForoData> callback)
	{
		try
		{
			StringBuilder result = new StringBuilder();
			URL call = new URL(API_URL + "?hash=" + API_KEY + "&action=getUser&value=" + userId);
			BufferedReader br = new BufferedReader(new InputStreamReader(call.openStream()));
			br.lines().forEach(result::append);
			
			String json = result.toString().trim();
			
			JsonObject response;
			try
			{
				response = PARSER.parse(json).getAsJsonObject();
				if (response.has("error"))
				{
					callback.accept(null);
				}
				else
				{
					XenForoData data = new XenForoData();
					data.user_id = response.get("user_id").getAsInt();
					data.username = response.get("username").getAsString();
					data.email = response.get("email").getAsString();
					data.user_group_id = response.get("user_group_id").getAsInt();
					String groups = response.get("secondary_group_ids").getAsString();
					if (groups.isEmpty())
					{
						data.secondary_group_ids = new int[] {};
					}
					else
					{
						String[] groupIds = groups.split(",");
						data.secondary_group_ids = new int[groupIds.length];
						for (int index = 0; index < groupIds.length; index++)
						{
							data.secondary_group_ids[index] = Integer.parseInt(groupIds[index]);
						}
					}
					Map<String, String> fields = new HashMap<>();
					if (response.get("custom_fields") instanceof JsonObject)
					{
						JsonObject cFields = (JsonObject) response.get("custom_fields");
						for (Entry<String, JsonElement> entry : cFields.entrySet())
						{
							fields.put(entry.getKey(), entry.getValue().getAsString());
						}
					}
					data.custom_fields = fields;
					callback.accept(data);
				}
			}
			catch (JsonSyntaxException e)
			{
				callback.accept(null);
				e.printStackTrace();
				return;
			}
		}
		catch (IOException e)
		{
			callback.accept(null);
			e.printStackTrace();
		}
	}
	
	private void doAPICall(String call, Consumer<String> errorCallback, Runnable ioException, Runnable onComplete)
	{
		StringBuilder input = new StringBuilder();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(API_URL + "?hash=" + API_KEY + "&" + call).openStream())))
		{
			reader.lines().forEachOrdered(input::append);
        }
		catch (IOException e)
		{
			System.out.println("[XENFORO API] Could not connect to web server");
			e.printStackTrace();
			ioException.run();
			return;
        }
		
		JsonObject response = null;
		try
		{
			response = PARSER.parse(input.toString().trim()).getAsJsonObject();
		}
		catch (JsonSyntaxException e)
		{
			System.out.println("[XENFORO API] Could not parse JSON response data");
			e.printStackTrace();
			return;
		}
		
		if (response.has("error"))
		{
			if (response.get("error").getAsString().equals("7"))
			{
				if (response.has("user_error_id"))
				{
					String errorID = response.get("user_error_id").getAsString();
					
					errorCallback.accept(errorID);
					System.out.println("[XENFORO API] An error was found in the JSON response (id: " + errorID + ") from REST call: " + call);
        			return;
				}
				else
				{
					System.out.println("[XENFORO API] An error was found in the JSON response, but no error code was found from REST call: " + call);
				}
			}
			else
			{
				System.out.println("[XENFORO API] A non-user error was found in the JSON response (id: " + response.get("error").getAsString() + ") from REST call: " + call);
				return;
			}
		}
		else
		{
			onComplete.run();
		}
	}

	private int getForumId(String linkCode)
	{
		String given = linkCode.replace("-", "");
		if (given.length() < 9)
		{
			return -1;
		}
		if (!StringUtils.isNumeric(given))
		{
			return -1;
		}
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int offset = 100000000;
		StringBuilder sb = new StringBuilder(String.valueOf(cal.get(Calendar.DAY_OF_YEAR)));
		while (sb.length() < 3)
		{
			sb.insert(0, "0");
		}
		String reverse = sb.reverse().toString();
		int test = Integer.parseInt(reverse + reverse + reverse);
		test += offset;
		
		int input = Integer.parseInt(given);
		
		int abs = Math.abs(test - input);
		
//		Bukkit.broadcastMessage("REVERSE: " + reverse);
//		Bukkit.broadcastMessage("TEST: " + test);
//		Bukkit.broadcastMessage("INPUT: " + input);
//		Bukkit.broadcastMessage("ABS: " + abs);
//		Bukkit.broadcastMessage("MODULUS: " + abs % 11);
		
		if (abs % 11 != 0)
		{
			return -1;
		}
		
		return abs / 11;
	}
	
	private void checkAccountOnline(int accountId, BiConsumer<Boolean, UUID> consumer)
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (getClientManager().getAccountId(player) == accountId)
			{
				consumer.accept(true, player.getUniqueId());
				return;
			}
		}
		
		consumer.accept(false, null);
	}
	
	@EventHandler
	public void handleRankSave(PrimaryGroupUpdateEvent event)
	{
		BiConsumer<Integer, Set<PermissionGroup>> dataCallback = (userId, groups) ->
		{
			List<Integer> remove = new ArrayList<>();
			List<Integer> add = new ArrayList<>();
			for (PermissionGroup group : PermissionGroup.values())
			{
				if (group.getForumId() != -1 && event.getGroup() != group && !groups.contains(group))
				{
					remove.add(group.getForumId());
				}
			}
			PermissionGroup group = event.getGroup();
			if (group.getForumId() != -1)
			{
				add.add(group.getForumId());
			}
			
			refreshSiteTags(userId, remove, add, false, () -> {}, false, () -> {}, false);
		};
		runAsync(() ->
		{
			int id = -1;
			try (Connection c = DBPool.getAccount().getConnection();
				PreparedStatement s = c.prepareStatement("SELECT userId FROM forumLink WHERE accountId=" + event.getAccountId() + ";");
				ResultSet rs = s.executeQuery();
				)
			{
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			if (id == -1)
			{
				return;
			}
			final int userId = id;
			getClientManager().getRepository().fetchGroups(event.getAccountId(), (primaryName, additionalNames) ->
			{
				dataCallback.accept(userId, additionalNames);
			}, () -> {}, false);
		});
	}
	
	@EventHandler
	public void handleRankSave(GroupAddEvent event)
	{
		BiConsumer<Integer, Set<PermissionGroup>> dataCallback = (userId, groups) ->
		{
			List<Integer> remove = new ArrayList<>();
			List<Integer> add = new ArrayList<>();
			for (PermissionGroup group : PermissionGroup.values())
			{
				if (group.getForumId() != -1 && event.getGroup() != group && !groups.contains(group))
				{
					remove.add(group.getForumId());
				}
			}
			PermissionGroup group = event.getGroup();
			if (group.getForumId() != -1)
			{
				add.add(group.getForumId());
			}
			
			refreshSiteTags(userId, remove, add, false, () -> {}, false, () -> {}, false);
		};
		runAsync(() ->
		{
			int id = -1;
			try (Connection c = DBPool.getAccount().getConnection();
				PreparedStatement s = c.prepareStatement("SELECT userId FROM forumLink WHERE accountId=" + event.getAccountId() + ";");
				ResultSet rs = s.executeQuery();
				)
			{
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			if (id == -1)
			{
				return;
			}
			final int userId = id;
			getClientManager().getRepository().fetchGroups(event.getAccountId(), (primaryGroup, additionalGroups) ->
			{
				Set<PermissionGroup> groups = new HashSet<>(additionalGroups);
				groups.add(primaryGroup);
				groups.remove(event.getGroup());
				dataCallback.accept(userId, groups);
			}, () -> {}, false);
		});
	}
	
	@EventHandler
	public void handleRankSave(GroupRemoveEvent event)
	{
		BiConsumer<Integer, Set<PermissionGroup>> dataCallback = (userId, groups) ->
		{
			List<Integer> remove = new ArrayList<>();
			List<Integer> add = new ArrayList<>();
			for (PermissionGroup group : PermissionGroup.values())
			{
				if (group.getForumId() != -1 && !groups.contains(group))
				{
					remove.add(group.getForumId());
				}
			}
			
			refreshSiteTags(userId, remove, add, false, () -> {}, false, () -> {}, false);
		};
		runAsync(() ->
		{
			int id = -1;
			try (Connection c = DBPool.getAccount().getConnection();
				PreparedStatement s = c.prepareStatement("SELECT userId FROM forumLink WHERE accountId=" + event.getAccountId() + ";");
				ResultSet rs = s.executeQuery();
				)
			{
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			if (id == -1)
			{
				return;
			}
			final int userId = id;
			getClientManager().getRepository().fetchGroups(event.getAccountId(), (primaryGroup, additionalGroups) ->
			{
				Set<PermissionGroup> groups = Sets.newHashSet(additionalGroups);
				groups.add(primaryGroup);
				dataCallback.accept(userId, groups);
			}, () -> {}, false);
		});
	}
	
	@EventHandler
	public void handleSubscriptionAdd(SubscriptionAddEvent event)
	{
		checkAccountOnline(event.getAccountId(), (online, uuid) ->
		{
			if (online)
			{
				ForumUserData fd = Get(uuid);
				if (fd.Linked && !fd.LastSyncedPowerPlayStatus)
				{
					final int userId = fd.LinkedForumId;
					runAsync(() ->
					{
						List<Integer> add = new ArrayList<>();
						add.add(POWER_PLAY_TAG_ID);
						refreshSiteTags(userId, new ArrayList<>(), add, false, () ->
						{
							runSync(() ->
							{
								fd.LastSyncedPowerPlayStatus = true;
							});
							try (Connection c = DBPool.getAccount().getConnection())
							{
								c.prepareStatement("UPDATE forumLink SET powerPlayStatus=true WHERE accountId=" + event.getAccountId() + ";").execute();
							}
							catch (SQLException e)
							{
								e.printStackTrace();
							}
						}, false, () -> {}, false);
					});
				}
			}
			else
			{
				runAsync(() ->
				{
					try (Connection c = DBPool.getAccount().getConnection())
					{
						ResultSet rs = c.prepareStatement("SELECT userId FROM forumLink WHERE accountId=" + event.getAccountId() + ";").executeQuery();
						if (rs.next())
						{
							int userId = rs.getInt(1);
							List<Integer> add = new ArrayList<>();
							add.add(17);
							refreshSiteTags(userId, new ArrayList<>(), add, false, () ->
							{
								try
								{
									c.prepareStatement("UPDATE forumLink SET powerPlayStatus=true WHERE accountId=" + event.getAccountId() + ";").execute();
								}
								catch (SQLException e)
								{
									e.printStackTrace();
								}
							}, false, () -> {}, false);
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				});
			}
		});
	}
	
	@EventHandler
	public void updatePPCTag(PlayerJoinEvent event)
	{
		runSyncLater(() ->
		{
			if (event.getPlayer().isOnline())
			{
				boolean ppc;
				PowerPlayData d = UtilServer.CallEvent(new PPCDataRequestEvent(event.getPlayer())).getData();
				if (d != null)
				{
					ppc = d.isSubscribed();
					final boolean powerPlay = ppc;
					if (Get(event.getPlayer()).Linked && Get(event.getPlayer()).LastSyncedPowerPlayStatus != powerPlay)
					{
						final int userId = Get(event.getPlayer()).LinkedForumId;
						final int accountId = getClientManager().getAccountId(event.getPlayer());
						runAsync(() ->
						{
							List<Integer> remove = new ArrayList<>();
							List<Integer> add = new ArrayList<>();
							if (powerPlay)
							{
								add.add(POWER_PLAY_TAG_ID);
							}
							else
							{
								remove.add(POWER_PLAY_TAG_ID);
							}
							refreshSiteTags(userId, remove, add, false, () ->
							{
								runSync(() ->
								{
									if (event.getPlayer().isOnline())
									{
										Get(event.getPlayer()).LastSyncedPowerPlayStatus = powerPlay;
									}
								});
								try (Connection c = DBPool.getAccount().getConnection())
								{
									c.prepareStatement("UPDATE forumLink SET powerPlayStatus=" + powerPlay + " WHERE accountId=" + accountId + ";").execute();
								}
								catch (SQLException e)
								{
									e.printStackTrace();
								}
							}, false, () -> {}, false);
						});
					}
				}
			}
		}, 40L);
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT userId, powerPlayStatus FROM forumLink WHERE accountId=" + accountId + ";";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		if (resultSet.next())
		{
			ForumUserData data = new ForumUserData();
			data.Linked = true;
			data.LinkedForumId = resultSet.getInt(1);
			data.LastSyncedPowerPlayStatus = resultSet.getBoolean(2);
			Set(uuid, data);
		}
		else
		{
			Set(uuid, new ForumUserData());
		}
	}

	@Override
	protected ForumUserData addPlayer(UUID uuid)
	{
		return new ForumUserData();
	}
}
