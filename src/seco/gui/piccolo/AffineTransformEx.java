package seco.gui.piccolo;

import edu.umd.cs.piccolo.util.PAffineTransform;

//Helper class used as a bean instead of PAffineTransform,
//which doesn't persist appropriately
public class AffineTransformEx 
{
    private PAffineTransform transform;
    private double m00;
    private double m10;
    private double m01;
    private double m11;
    private double m02;
    private double m12;

    public AffineTransformEx(){}
    
    public AffineTransformEx(PAffineTransform tr)
    {
        storeTransform(tr);
    }
    
    public PAffineTransform getTransform()
    {
        if(transform == null)
            transform = new PAffineTransform(m00, m10, m01, m11, m02, m12);
        return transform;
    }
    
    private void storeTransform(PAffineTransform tr)
    {
        transform = tr;
        this.m00 = tr.getScaleX();
        this.m01 = tr.getShearX();
        this.m02 = tr.getTranslateX();
        this.m11 = tr.getScaleY();
        this.m10 = tr.getShearY();
        this.m12 = tr.getTranslateY();
        
    }
    

    public String toString()
    {
        return ("PAffineTransformEx[[" + _matround(m00) + ", " + _matround(m01)
                + ", " + _matround(m02) + "], [" + _matround(m10) + ", "
                + _matround(m11) + ", " + _matround(m12) + "]]");
    }

    private static double _matround(double matval)
    {
        return Math.rint(matval * 1E15) / 1E15;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(m00);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m01);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m02);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m10);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m11);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m12);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AffineTransformEx other = (AffineTransformEx) obj;
        if (Double.doubleToLongBits(m00) != Double.doubleToLongBits(other.m00))
            return false;
        if (Double.doubleToLongBits(m01) != Double.doubleToLongBits(other.m01))
            return false;
        if (Double.doubleToLongBits(m02) != Double.doubleToLongBits(other.m02))
            return false;
        if (Double.doubleToLongBits(m10) != Double.doubleToLongBits(other.m10))
            return false;
        if (Double.doubleToLongBits(m11) != Double.doubleToLongBits(other.m11))
            return false;
        if (Double.doubleToLongBits(m12) != Double.doubleToLongBits(other.m12))
            return false;
        return true;
    }

    public double getM00()
    {
        return m00;
    }

    public void setM00(double m00)
    {
        this.m00 = m00;
    }

    public double getM10()
    {
        return m10;
    }

    public void setM10(double m10)
    {
        this.m10 = m10;
    }

    public double getM01()
    {
        return m01;
    }

    public void setM01(double m01)
    {
        this.m01 = m01;
    }

    public double getM11()
    {
        return m11;
    }

    public void setM11(double m11)
    {
        this.m11 = m11;
    }

    public double getM02()
    {
        return m02;
    }

    public void setM02(double m02)
    {
        this.m02 = m02;
    }

    public double getM12()
    {
        return m12;
    }

    public void setM12(double m12)
    {
        this.m12 = m12;
    }
    
    
}
