package mineplex.core.lifetimes;

public interface PhasedComponent<T> extends Component
{

	void setPhase(T phase);
}
