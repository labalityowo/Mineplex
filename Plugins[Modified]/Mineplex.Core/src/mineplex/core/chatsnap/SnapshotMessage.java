package mineplex.core.chatsnap;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.bukkit.ChatColor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a message sent by a player.
 */
public class SnapshotMessage implements Comparable<SnapshotMessage>
{
	protected Long _id = null;
	private final MessageType _messageType;
	private final int _senderId;
	private final Collection<Integer> _recipientIds;
	private final String _message;
	private final LocalDateTime _time;
	private final Set<Integer> linkedSnapshots = new HashSet<>();

	public SnapshotMessage(int senderId, int recipientId, String message)
	{
		this(MessageType.PM, senderId, Collections.singletonList(recipientId), message, LocalDateTime.now());
	}

	public SnapshotMessage(MessageType messageType, int senderId, Collection<Integer> recipientIds, String message)
	{
		this(messageType, senderId, recipientIds, message, LocalDateTime.now());
	}

	public SnapshotMessage(MessageType messageType, int senderId, Collection<Integer> recipientIds, String message, LocalDateTime time)
	{
		_messageType = messageType;
		_senderId = checkNotNull(senderId);
		_recipientIds = checkNotNull(recipientIds);
		_message = checkNotNull(message);
		_time = checkNotNull(time);

		if (messageType == MessageType.PM && recipientIds.size() > 1)
		{
			throw new IllegalArgumentException("SnapshotMessage type PM may not have more than 1 recipient.");
		}
	}

	public Optional<Long> getId()
	{
		return Optional.ofNullable(_id);
	}

	public MessageType getType()
	{
		return _messageType;
	}

	public int getSenderId()
	{
		return _senderId;
	}

	public String getMessage()
	{
		return _message;
	}

	public Set<Integer> getRecipientIds()
	{
		return new HashSet<>(_recipientIds);
	}

	public LocalDateTime getSentTime()
	{
		return _time;
	}

	public Set<Integer> getLinkedSnapshots()
	{
		return linkedSnapshots;
	}

	public void addLinkedSnapshot(int snapshotId)
	{
		linkedSnapshots.add(snapshotId);
	}

	@Override
	public int compareTo(SnapshotMessage o)
	{
		return getSentTime().compareTo(o.getSentTime());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SnapshotMessage that = (SnapshotMessage) o;
		return _time == that._time &&
				Objects.equals(_senderId, that._senderId) &&
				Objects.equals(_recipientIds, that._recipientIds) &&
				Objects.equals(_message, that._message);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_senderId, _recipientIds, _message, _time);
	}

	@Override
	public String toString()
	{
		return "SnapshotMessage{" +
				"sender=" + _senderId +
				", recipients=" + _recipientIds +
				", message='" + ChatColor.stripColor(_message) + '\'' +
				", created=" + _time +
				'}';
	}
}
