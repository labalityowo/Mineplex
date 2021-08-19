package mineplex.core.report;

import org.apache.commons.lang3.text.WordUtils;

/**
 * All possible roles a user can have in a report.
 */
public enum ReportRole
{
	SUSPECT,
	REPORTER,
	HANDLER;

	private final String _humanName;

	ReportRole()
	{
		_humanName = WordUtils.capitalize(name().toLowerCase().replace('_', ' '));
	}

	public String getHumanName()
	{
		return _humanName;
	}
}
