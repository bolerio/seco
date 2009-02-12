package seco.gui.layout;

public class DValue
{
    private double value;
    private boolean percent;
    
    public DValue()
    {
    }
    
    public DValue(double value)
    {
        this(value, false);
    }
    public DValue(double value, boolean percent)
    {
        this.value = value;
        this.percent = percent;
    }

    public double getValue()
    {
        return value;
    }
    public void setValue(double value)
    {
        this.value = value;
    }
    public boolean isPercent()
    {
        return percent;
    }
    public void setPercent(boolean percent)
    {
        this.percent = percent;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final DValue other = (DValue) obj;
        if (percent != other.percent) return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value)) return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "" + value + ((percent) ? "%" : "") ;
    }
    
    
}
