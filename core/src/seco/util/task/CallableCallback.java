package seco.util.task;

import java.util.concurrent.Callable;

public interface CallableCallback<V> extends Callable<V>, CompletionCallback<V>
{
}
