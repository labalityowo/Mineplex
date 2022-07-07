package mineplex.core.chatsnap;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mineplex.core.report.data.Report;

/**
 * Handles temporary storage of {@link SnapshotMessage} instances.
 */
public class SnapshotManager
{
	public static final int MAX_SNAPSHOTS = 5;

	// There aren't any List or Set caching implementations
	// For an easy work around, we store values as the Key
	// For the value we just use some dummy object
	// I went with Boolean as it's the smallest data type
	private final Cache<SnapshotMessage, Boolean> _messages = CacheBuilder.newBuilder()
			.concurrencyLevel(4)
			.expireAfterWrite(3, TimeUnit.MINUTES)
			.build();

	private final JavaPlugin _javaPlugin;
	private final SnapshotRepository _snapshotRepository;

	public SnapshotManager(JavaPlugin javaPlugin, SnapshotRepository snapshotRepository)
	{
		_javaPlugin = javaPlugin;
		_snapshotRepository = snapshotRepository;
	}

	public SnapshotRepository getRepository()
	{
		return _snapshotRepository;
	}

	/**
	 * Keeps a message in memory temporarily (30 minutes) and then discards it.
	 * During this time, other modules (such as the Report module) can access it for their own use.
	 *
	 * @param message the message to temporarily store
	 */
	public void cacheMessage(SnapshotMessage message)
	{
		_messages.put(message, true);
	}

	/**
	 * Gets all currently stored snapshots.
	 * The set is in chronological order of the time the message was sent.
	 *
	 * @return a set containing all snapshots
	 */
	public Set<SnapshotMessage> getMessages()
	{
		// The compareTo method in SnapshotMessage will ensure this in chronological order
		Set<SnapshotMessage> messages = new TreeSet<>();
		messages.addAll(_messages.asMap().keySet());
		return messages;
	}

	/**
	 * Gets all messages an account is involved in.
	 * The user may be the sender or recipient of a message.
	 * Does not include PMs unless sender or receiver is in the exclusions collection.
	 *
	 * @param accountId the account to search for messages involved in
	 * @param pmWhitelistIds a list of account ids of which to include PMs of
	 * @return the messages that the account is involved in
	 */
	public Set<SnapshotMessage> getMessagesInvolving(int accountId, Collection<Integer> pmWhitelistIds)
	{
		return getMessagesInvolving(accountId).stream()
				.filter(message -> includeMessage(message, pmWhitelistIds))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	private boolean includeMessage(SnapshotMessage message, Collection<Integer> pmWhitelistIds)
	{
		return message.getType() != MessageType.PM ||
				pmWhitelistIds.contains(message.getSenderId()) ||
				!Collections.disjoint(message.getRecipientIds(), pmWhitelistIds);
	}

	/**
	 * Gets all messages an account is involved in.
	 * The user may be the sender or recipient of a message.
	 *
	 * @param accountId the account to search for messages involved in
	 * @return the messages that the account is involved in
	 */
	public Set<SnapshotMessage> getMessagesInvolving(int accountId)
	{
		Set<SnapshotMessage> messagesInvolved = new TreeSet<>();
		messagesInvolved.addAll(getMessagesFrom(accountId));
		messagesInvolved.addAll(getMessagesReceived(accountId));
		return messagesInvolved;
	}

	/**
	 * Gets all messages sent by an account.
	 *
	 * @param senderId the account to search for messages involved in
	 * @return the messages that the account is involved in
	 */
	public Set<SnapshotMessage> getMessagesFrom(int senderId)
	{
		return _messages.asMap().keySet().stream()
				.filter(message -> message.getSenderId() == senderId)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	/**
	 * Gets all messages received by an account.
	 *
	 * @param recipientId the account to search for messages received
	 * @return the messages that the account is involved in
	 */
	public Set<SnapshotMessage> getMessagesReceived(int recipientId)
	{
		return _messages.asMap().keySet().stream()
				.filter(message -> message.getRecipientIds().contains(recipientId))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public CompletableFuture<SnapshotMetadata> saveReportSnapshot(Report report, Collection<SnapshotMessage> messages)
	{
		SnapshotMetadata snapshotMetadata = report.getSnapshotMetadata().orElseThrow(() ->
				new IllegalStateException("Report does not have associated snapshot."));

		return _snapshotRepository.insertMessages(snapshotMetadata.getId(), messages).whenComplete(((aVoid, throwable) ->
		{
			if (throwable == null)
			{
				report.setSnapshotMetadata(snapshotMetadata);
			}
			else
			{
				_javaPlugin.getLogger().log(Level.SEVERE, "Error whilst saving snapshot.", throwable);
			}
		})).thenApply(aVoid -> snapshotMetadata);
	}
}
