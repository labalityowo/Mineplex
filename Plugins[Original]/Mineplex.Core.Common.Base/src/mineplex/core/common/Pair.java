package mineplex.core.common;

import java.io.Serializable;

public class Pair<L, R> implements Serializable {

	private static final long serialVersionUID = -7631968541502978704L;

	private L left;
	private R right;
	
	public static <L, R> Pair<L, R> create(L left, R right)
	{
		return new Pair<L, R>(left, right);
	}

	private Pair(L left, R right) {
		this.setLeft(left);
		this.setRight(right);
	}

	public L getLeft()
	{
		return left;
	}

	public void setLeft(L left)
	{
		this.left = left;
	}

	public R getRight()
	{
		return right;
	}

	public void setRight(R right)
	{
		this.right = right;
	}

	@Override
	public String toString()
	{
		return getLeft().toString() + ":" + getRight().toString();
	}


	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Pair<?, ?> pair = (Pair<?, ?>) o;

		if (left != null ? !left.equals(pair.left) : pair.left != null) return false;
		return right != null ? right.equals(pair.right) : pair.right == null;

	}

	@Override
	public int hashCode()
	{
		int result = left != null ? left.hashCode() : 0;
		result = 31 * result + (right != null ? right.hashCode() : 0);
		return result;
	}
}