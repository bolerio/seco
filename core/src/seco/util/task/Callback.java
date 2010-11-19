package seco.util.task;

/**
 * <p>
 * A simple, generic callback interface.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 * @param <T>
 */
public interface Callback<T>
{
    void callback(T arg);
}