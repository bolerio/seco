package seco.things;

/**
 * 
 * <p>
 * Compiled class files are recorded in HyperGraphDB as instances
 * of <code>ManagedClass</code>.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class ManagedClass
{
	private byte [] code;
	private String name;
	
	public byte[] getCode()
	{
		return code;
	}
	public void setCode(byte[] code)
	{
		this.code = code;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
}