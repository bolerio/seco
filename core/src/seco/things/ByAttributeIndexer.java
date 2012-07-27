package seco.things;

import java.util.Comparator;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.indexing.HGKeyIndexer;
import org.hypergraphdb.storage.ByteArrayConverter;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.HGPrimitiveType;

public class ByAttributeIndexer<KeyType, ValueType> extends HGKeyIndexer<KeyType, ValueType>
{
	private String attributeName;
	private HGHandle attributeType;
	
	public ByAttributeIndexer() { }
	public ByAttributeIndexer(HGHandle atomType, String attributeName, HGHandle attributeType) 
	{ 
		super("attribute_" + attributeName, atomType);
		this.attributeName = attributeName; 
		this.attributeType = attributeType;
	}
	
	@Override
	public ByteArrayConverter<KeyType> getConverter(HyperGraph graph)
	{
		HGAtomType type = graph.get(attributeType);
		return (ByteArrayConverter<KeyType>)type;
	}

	@Override
	public Comparator<byte[]> getComparator(HyperGraph graph)
	{
		HGAtomType type = graph.get(attributeType);
		if (type instanceof HGPrimitiveType)
			return ((HGPrimitiveType<?>)type).getComparator();
		else if (type instanceof Comparator)
			return (Comparator<byte[]>)type;
		else
			return null;	
	}

	@Override
	public KeyType getKey(HyperGraph graph, Object atom)
	{
		WithAttributes x = (WithAttributes)atom;
		return (KeyType)x.getAttribute(attributeName);
	}
	
	public String getAttributeName()
	{
		return attributeName;
	}
	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}
	public HGHandle getAttributeType()
	{
		return attributeType;
	}
	public void setAttributeType(HGHandle attributeType)
	{
		this.attributeType = attributeType;
	}
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result
				+ ((attributeType == null) ? 0 : attributeType.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ByAttributeIndexer other = (ByAttributeIndexer) obj;
		if (attributeName == null)
		{
			if (other.attributeName != null)
				return false;
		}
		else if (!attributeName.equals(other.attributeName))
			return false;
		if (attributeType == null)
		{
			if (other.attributeType != null)
				return false;
		}
		else if (!attributeType.equals(other.attributeType))
			return false;
		return true;
	}		
}