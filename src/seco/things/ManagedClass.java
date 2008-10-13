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
	private String packageName;
	
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
	public String getPackageName()
	{
		return packageName;
	}
	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}	
}