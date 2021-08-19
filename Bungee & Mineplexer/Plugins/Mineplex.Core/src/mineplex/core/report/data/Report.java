package mineplex.core.report.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import mineplex.core.chatsnap.SnapshotMetadata;
import mineplex.core.report.ReportCategory;
import mineplex.core.report.ReportHandlerTask;
import mineplex.core.report.ReportResult;
import mineplex.core.report.ReportTeam;
import mineplex.serverdata.Region;

/**
 * Holds data for a Report.
 */
public class Report
{
	protected Long _reportId;
	private final int _suspectId;
	private final ReportCategory _category;
	private final Region _region;
	// set of player account ids and the reason they reported this player
	private final Map<Integer, ReportMessage> _reportMessages = new HashMap<>();
	private Integer _handlerId = null;
	private SnapshotMetadata _snapshotMetadata = null;
	private ReportResult _reportResult = null;
	private ReportTeam _assignedTeam = null;

	private ReportHandlerTask _handlerTask = null;

	public Report(int suspectId, ReportCategory category, Region region)
	{
		this(null, suspectId, category, region);
	}

	protected Report(Long reportId, int suspectId, ReportCategory category, Region region)
	{
		_reportId = reportId;
		_suspectId = suspectId;
		_category = category;
		_region = region;
	}

	public Optional<Long> getId()
	{
		return Optional.ofNullable(_reportId);
	}

	public int getSuspectId()
	{
		return _suspectId;
	}

	public ReportCategory getCategory()
	{
		return _category;
	}

	public Optional<Region> getRegion()
	{
		return Optional.ofNullable(_region);
	}

	public Map<Integer, ReportMessage> getMessages()
	{
		return _reportMessages;
	}

	public void addReportReason(ReportMessage reportMessage)
	{
		_reportMessages.put(reportMessage.getReporterId(), reportMessage);
	}

	public Map<Integer, ReportMessage> getReportMessages()
	{
		return _reportMessages;
	}

	public ReportMessage getReportMessage(int accountId)
	{
		return _reportMessages.get(accountId);
	}

	public Set<Integer> getReporterIds()
	{
		return _reportMessages.keySet();
	}

	public Optional<Integer> getHandlerId()
	{
		return Optional.ofNullable(_handlerId);
	}

	public void setHandlerId(Integer handlerId)
	{
		_handlerId = handlerId;
	}

	public Optional<SnapshotMetadata> getSnapshotMetadata()
	{
		return Optional.ofNullable(_snapshotMetadata);
	}

	public void setSnapshotMetadata(SnapshotMetadata snapshotMetadata)
	{
		_snapshotMetadata = snapshotMetadata;
	}

	public Optional<ReportResult> getResult()
	{
		return Optional.ofNullable(_reportResult);
	}

	public void setReportResult(ReportResult reportResult)
	{
		_reportResult = reportResult;
	}

	public Optional<ReportTeam> getAssignedTeam()
	{
		return Optional.ofNullable(_assignedTeam);
	}

	public void setAssignedTeam(ReportTeam assignedTeam)
	{
		_assignedTeam = assignedTeam;
	}

	public ReportMessage getLatestMessage()
	{
		ReportMessage latest = null;

		for (ReportMessage reportMessage : _reportMessages.values())
		{
			if (latest == null || reportMessage.getTimeCreated().isAfter(latest.getTimeCreated()))
			{
				latest = reportMessage;
			}
		}

		return latest;
	}

	public Optional<ReportHandlerTask> getHandlerTask()
	{
		return Optional.ofNullable(_handlerTask);
	}

	public void setHandlerTask(ReportHandlerTask handlerTask)
	{
		_handlerTask = handlerTask;
	}

	public void cancelHandlerTask()
	{
		if (_handlerTask != null)
		{
			_handlerTask.cancel();
			_handlerTask = null;
		}
	}
}
