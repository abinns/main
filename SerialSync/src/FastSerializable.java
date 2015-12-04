public interface FastSerializable
{
	public int getSize();

	public void stateGet(UnsafeMemory dst);

	public void stateSet(UnsafeMemory src);
}
