package mineplex.core.mavericks;

import java.util.UUID;

/**
 * A wrapper class to SQL data in the mavericksMasterBuildersApproved SQL table
 */
public class MavericksApprovedWrapper
{
	
	private final MavericksBuildWrapper _build;
	private final long _approvedDate;
	private final UUID _approvedBy;
	private final boolean _display;
	private Long _firstDisplayed;

	public MavericksApprovedWrapper(MavericksBuildWrapper build, long approvedDate, UUID approvedBy, boolean display, Long firstDisplayed)
	{
		_build = build;
		_approvedDate = approvedDate;
		_approvedBy = approvedBy;
		_display = display;
		_firstDisplayed = firstDisplayed;
	}
	
	
	public MavericksBuildWrapper getBuild()
	{
		return _build;
	}
	
	public long getApprovedDate()
	{
		return _approvedDate;
	}
	
	public UUID getApprovedBy()
	{
		return _approvedBy;
	}
	
	public Long getFirstDisplayed()
	{
		return _firstDisplayed;
	}
	
	public void setFirstDisplayed(Long firstDisplayed)
	{
		_firstDisplayed = firstDisplayed;
	}
	
	public boolean isDisplay()
	{
		return _display;
	}

}
