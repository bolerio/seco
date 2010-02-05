package seco.notebook.syntax.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import seco.notebook.syntax.java.JavaResultItem;
import seco.notebook.syntax.util.JMIUtils;

//Base class with common functionality
public abstract class BaseAsyncCompletionQuery extends AsyncCompletionQuery
{
    protected JTextComponent component;
    protected CompletionResultSet queryResult;
    protected int creationCaretOffset;
    protected int queryCaretOffset;
    protected int queryAnchorOffset;
    protected String filterPrefix;

    protected BaseAsyncCompletionQuery(int caretOffset)
    {
        this.creationCaretOffset = caretOffset;
    }

    protected void preQueryUpdate(JTextComponent component)
    {
        int caretOffset = component.getCaretPosition();
        if (caretOffset >= creationCaretOffset)
                return;
        Completion.get().hideCompletion();
    }

    protected void prepareQuery(JTextComponent component)
    {
        this.component = component;
    }

    protected boolean canFilter(JTextComponent component)
    {
        int caretOffset = component.getCaretPosition();
        Document doc = component.getDocument();
        filterPrefix = null;
        if (caretOffset >= queryCaretOffset)
        {
            if (queryAnchorOffset > -1)
            {
                try
                {
                    filterPrefix = doc.getText(queryAnchorOffset,
                            caretOffset - queryAnchorOffset);
                    if (!isJavaIdentifierPart(filterPrefix))
                    {
                        filterPrefix = null;
                    }
                }
                catch (BadLocationException e)
                {
                    // filterPrefix stays null -> no filtering
                }
            }
        }
        return (filterPrefix != null);
    }

    protected void filter(CompletionResultSet resultSet)
    {
        if (filterPrefix != null && queryResult != null)
        {
            //resultSet.setTitle(
            //      getFilteredTitle(queryResult.getTitle(),filterPrefix));
            resultSet.setAnchorOffset(queryAnchorOffset);
            resultSet.addAllItems(getFilteredData(queryResult.getData(),
                    filterPrefix));
        }
        resultSet.finish();
    }

    private boolean isJavaIdentifierPart(CharSequence text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if (!(Character.isJavaIdentifierPart(text.charAt(i))))
            {
                return false;
            }
        }
        return true;
    }

    private Collection<CompletionItem> getFilteredData(Collection<CompletionItem> data, String prefix)
    {
        List<CompletionItem> ret = new ArrayList<CompletionItem>();
        boolean camelCase = prefix.length() > 1
                && prefix.equals(prefix.toUpperCase());
        for (CompletionItem itm : data)
        {
            if (JMIUtils.startsWith(itm.getItemText(), prefix)
                    || (camelCase
                            && (itm instanceof JavaResultItem.ClassResultItem) && JMIUtils
                            .matchesCamelCase(itm.getItemText(), prefix)))
            {
                ret.add(itm);
            }
        }
         return ret;
    }

//    private String getFilteredTitle(String title, String prefix)
//    {
//        int lastIdx = title.lastIndexOf('.');
//        String ret = lastIdx == -1 ? prefix : title.substring(0,
//                lastIdx + 1)
//                + prefix;
//        if (title.endsWith("*")) // NOI18N
//            ret += "*"; // NOI18N
//        return ret;
//    }
}
