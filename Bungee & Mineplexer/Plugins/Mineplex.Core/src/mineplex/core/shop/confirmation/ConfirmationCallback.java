package mineplex.core.shop.confirmation;

/**
 * @author Shaun Bennett
 */
public interface ConfirmationCallback
{
	public void resolve(String message);

	public void reject(String message);
}
