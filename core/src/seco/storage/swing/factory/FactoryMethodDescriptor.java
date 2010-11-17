package seco.storage.swing.factory;

public class FactoryMethodDescriptor extends MethodDescriptor
{
   String className;
   public FactoryMethodDescriptor()
   {
   }

	public FactoryMethodDescriptor(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
