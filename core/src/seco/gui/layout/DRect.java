package seco.gui.layout;


public class DRect
{
    public DValue x;
    public DValue y;
    public DValue width;
    public DValue height;

    public DRect(DValue x, DValue y, DValue width, DValue height)
    {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public DRect()
    {
        super();
    }
    
    public DValue getX()
    {
        return x;
    }
    public void setX(DValue x)
    {
        this.x = x;
    }
    public DValue getY()
    {
        return y;
    }
    public void setY(DValue y)
    {
        this.y = y;
    }
    public DValue getWidth()
    {
        return width;
    }
    public void setWidth(DValue width)
    {
        this.width = width;
    }
    public DValue getHeight()
    {
        return height;
    }
    public void setHeight(DValue height)
    {
        this.height = height;
    }
    
    @Override
    public String toString()
    {
        return "DualRect[" + x + "," + y + "," + width + "," + height + "]";
    }
}
