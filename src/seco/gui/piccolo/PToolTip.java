package seco.gui.piccolo;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import java.awt.Font;
import java.awt.BasicStroke;

public class PToolTip extends PPath
{
    PText text;
    public PToolTip()
    {
        super();
        text = new PText();
        text.setFont(new Font("Arial-bold", Font.PLAIN, 12));
        text.setWidth(30);
        text.setConstrainWidthToTextWidth(true);
        addChild(text);
        // setPaint( new java.awt.Color( 255, 255, 191 ) );
        setPaint(java.awt.Color.lightGray);
        setStroke(new BasicStroke(1.2f));
        setStrokePaint(java.awt.Color.black);
        setText("");
    }

    public void setText(String new_text)
    {
        text.setText(new_text);
        if (new_text.equals(""))
        {
            setPathToRectangle(0, 0, 0, 0);
        }
        else
        {
            PBounds b = text.getFullBounds();
            setPathToRectangle((float) b.getX() - 2, (float) b.getY() - 2,
                    (float) b.getWidth() + 4, (float) b.getHeight() + 4);
        }
    }

    public void setBackground(int color)
    {
        switch (color)
        {
        case 1:
            setPaint(java.awt.Color.yellow);
            break;
        }
    }

    public void setForeground(int color)
    {
        switch (color)
        {
        case 1:
            setPaint(java.awt.Color.yellow);
            break;
        }
    }
}
