package mineplex.core.report.ui;

/**
 * Allows re-use of the {@link ReportCategoryButton} class.
 */
public interface ReportCategoryCallback
{
	/**
	 * Invoked when a category button is clicked.
	 * @param button The button that was clicked
	 */
	void click(ReportCategoryButton button);
}
