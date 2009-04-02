package seco.api;

/**
 * <p>
 * Used in conjunction with niche global executor service - lets users
 * provide a callback for when a task submitted to the executor completes.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public interface CompletionCallback<V>
{
    /**
     * <p>
     * Called upon task completion.
     * </p>
     * 
     * @param result The result of task if it was a <code>Callable</code> instance
     * or <code>null</code> if it was a <code>Runnable</code> instance and must be
     * ignored.
     * @param ex In case an exception was thrown and the task did not complete because
     * of that, this is the exception. Otherwise this parameter is <code>null</code>.
     */
    void onCompletion(V result, Throwable ex);
}