/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;


/** Request processor that is capable to execute requests in dedicated threads.
 * You can create your own instance or use the shared one.
 *
 * <P><A name="use_cases">There are several use cases for RequestProcessor:</A>
 *
 * <UL><LI>Having something done asynchronously in some other thread,
 *  not insisting on any kind of serialization of the requests:
 *  Use <CODE>RequestProcessor.{@link RequestProcessor#getDefault
 *  }.{@link #post(java.lang.Runnable) post(runnable)}</CODE>
 *  for this purpose.
 * <LI>Having something done later in some other thread:
 *  Use <CODE>RequestProcessor.{@link RequestProcessor#getDefault
 *  }.{@link #post(java.lang.Runnable,int) post(runnable,&nbsp;delay)}</CODE>
 * <LI>Having something done periodically in any thread: Use the
 *  {@link RequestProcessor.Task}'s ability to
 *  {@link RequestProcessor.Task#schedule schedule()}, like
 *  <PRE>
 *      static RequestProcessor.Task CLEANER = RequestProcessor.getDefault().post(runnable,DELAY);
 *      public void run() {
 *          doTheWork();
 *          CLEANER.schedule(DELAY);
 *      }
 *  </PRE>
 *  <STRONG>Note:</STRONG> Please think twice before implementing some periodic
 *  background activity. It is generally considered evil if it will run
    regardless of user actions and the application state, even while the application
    is minimized / not currently used.
 * <LI>Having something done in some other thread but properly ordered:
 *  Create a private instance of the
 *  {@link RequestProcessor#RequestProcessor(java.lang.String) RequestProcessor(name)}</CODE>
 *  and use it from all places you'd like to have serialized. It works
 *  like a simple Mutex.
 * <LI>Having some entity that will do processing in a limited
 *  number of threads paralelly: Create a private instance of the
 *  {@link RequestProcessor#RequestProcessor(java.lang.String,int) RequestProcessor(name,throughput)}</CODE>
 *  set proper throughput and use it to schedule the work.
 *  It works like a queue of requests passing through a semafore with predefined
 *  number of <CODE>DOWN()</CODE>s.
 * </UL>
 *
 * <STRONG>Note:</STRONG> If you don't need to serialize your requests but
 * you're generating them in bursts, you should use your private
 * <CODE>RequestProcessor</CODE> instance with limited throughput (probably
 * set to 1), NetBeans would try to run all your requests in parallel otherwise.
 *
 * <P>
 * Since version 6.3 there is a conditional support for interruption of long running tasks.
 * There always was a way how to cancel not yet running task using {@link RequestProcessor.Task#cancel }
 * but if the task was already running, one was out of luck. Since version 6.3
 * the thread running the task is interrupted and the Runnable can check for that
 * and terminate its execution sooner. In the runnable one shall check for 
 * thread interruption (done from {@link RequestProcessor.Task#cancel }) and 
 * if true, return immediatelly as in this example:
 * <PRE>
 * public void run () {
 *     while (veryLongTimeLook) {
 *       doAPieceOfIt ();
 *
 *       if (Thread.interrupted ()) return;
 *     }
 * }
 * </PRE>
 * 
 * @author Petr Nejedly, Jaroslav Tulach
 */
public final class RequestProcessor {
    /** the static instance for users that do not want to have own processor */
    private static RequestProcessor DEFAULT = new RequestProcessor();

    // 50: a conservative value, just for case of misuse

    /** the static instance for users that do not want to have own processor */
    private static RequestProcessor UNLIMITED = new RequestProcessor("Default RequestProcessor", 50); // NOI18N

    /** A shared timer used to pass timeouted tasks to pending queue */
    private static Timer starterThread = new Timer(true);

    /** logger */
    private static Logger logger;

    /** The counter for automatic naming of unnamed RequestProcessors */
    private static int counter = 0;
    static final boolean SLOW = Boolean.getBoolean("org.openide.util.RequestProcessor.Item.SLOW");

    /** The name of the RequestProcessor instance */
    String name;

    /** If the RP was stopped, this variable will be set, every new post()
     * will throw an exception and no task will be processed any further */
    boolean stopped = false;

    /** The lock covering following four fields. They should be accessed
     * only while having this lock held. */
    private Object processorLock = new Object();

    /** The set holding all the Processors assigned to this RequestProcessor */
    private HashSet<Processor> processors = new HashSet<Processor>();

    /** Actualy the first item is pending to be processed.
     * Can be accessed/trusted only under the above processorLock lock.
     * If null, nothing is scheduled and the processor is not running. */
    private List<Item> queue = new LinkedList<Item>();

    /** Number of currently running processors. If there is a new request
     * and this number is lower that the throughput, new Processor is asked
     * to carry over the request. */
    private int running = 0;

    /** The maximal number of processors that can perform the requests sent
     * to this RequestProcessors. If 1, all the requests are serialized. */
    private int throughput;
    
    /** support for interrupts or not? */
    private boolean interruptThread;

    /** Creates new RequestProcessor with automatically assigned unique name. */
    public RequestProcessor() {
        this(null, 1);
    }

    /** Creates a new named RequestProcessor with throughput 1.
     * @param name the name to use for the request processor thread */
    public RequestProcessor(String name) {
        this(name, 1);
    }

    /** Creates a new named RequestProcessor with defined throughput.
     * @param name the name to use for the request processor thread
     * @param throughput the maximal count of requests allowed to run in parallel
     *
     * @since OpenAPI version 2.12
     */
    public RequestProcessor(String name, int throughput) {
        this(name, throughput, false);
    }

    /** Creates a new named RequestProcessor with defined throughput which 
     * can support interruption of the thread the processor runs in.
     * There always was a way how to cancel not yet running task using {@link RequestProcessor.Task#cancel }
     * but if the task was already running, one was out of luck. With this
     * constructor one can create a {@link RequestProcessor} which threads
     * thread running tasks are interrupted and the Runnable can check for that
     * and terminate its execution sooner. In the runnable one shall check for 
     * thread interruption (done from {@link RequestProcessor.Task#cancel }) and 
     * if true, return immediatelly as in this example:
     * <PRE>
     * public void run () {
     *     while (veryLongTimeLook) {
     *       doAPieceOfIt ();
     *
     *       if (Thread.interrupted ()) return;
     *     }
     * }
     * </PRE>
     *
     * @param name the name to use for the request processor thread
     * @param throughput the maximal count of requests allowed to run in parallel
     * @param interruptThread true if {@link RequestProcessor.Task#cancel} shall interrupt the thread
     *
     * @since 6.3
     */
    public RequestProcessor(String name, int throughput, boolean interruptThread) {
        this.throughput = throughput;
        this.name = (name != null) ? name : ("OpenIDE-request-processor-" + (counter++));
        this.interruptThread = interruptThread;
    }
    
    
    /** The getter for the shared instance of the <CODE>RequestProcessor</CODE>.
     * This instance is shared by anybody who
     * needs a way of performing sporadic or repeated asynchronous work.
     * Tasks posted to this instance may be canceled until they start their
     * execution. If a there is a need to cancel a task while it is running
     * a seperate request processor needs to be created via 
     * {@link #RequestProcessor(String, int, boolean)} constructor.
     *
     * @return an instance of RequestProcessor that is capable of performing
     * "unlimited" (currently limited to 50, just for case of misuse) number
     * of requests in parallel. 
     *
     * @see #RequestProcessor(String, int, boolean)
     * @see RequestProcessor.Task#cancel
     *
     * @since version 2.12
     */
    public static RequestProcessor getDefault() {
        return UNLIMITED;
    }

    /** This methods asks the request processor to start given
     * runnable immediately. The default priority is {@link Thread#MIN_PRIORITY}.
     *
     * @param run class to run
     * @return the task to control the request
     */
    public Task post(Runnable run) {
        return post(run, 0, Thread.MIN_PRIORITY);
    }

    /** This methods asks the request processor to start given
    * runnable after <code>timeToWait</code> milliseconds. The default priority is {@link Thread#MIN_PRIORITY}.
    *
    * @param run class to run
    * @param timeToWait to wait before execution
    * @return the task to control the request
    */
    public Task post(final Runnable run, int timeToWait) {
        return post(run, timeToWait, Thread.MIN_PRIORITY);
    }

    /** This methods asks the request processor to start given
    * runnable after <code>timeToWait</code> milliseconds. Given priority is assigned to the
    * request. <p>
    * For request relaying please consider:
    * <pre>
    *    post(run, timeToWait, Thread.currentThread().getPriority());
    * </pre>
    *
    * @param run class to run
    * @param timeToWait to wait before execution
    * @param priority the priority from {@link Thread#MIN_PRIORITY} to {@link Thread#MAX_PRIORITY}
    * @return the task to control the request
    */
    public Task post(final Runnable run, int timeToWait, int priority) {
        RequestProcessor.Task task = new Task(run, priority);
        task.schedule(timeToWait);

        return task;
    }

    /** Creates request that can be later started by setting its delay.
    * The request is not immediatelly put into the queue. It is planned after
    * setting its delay by schedule method. By default the initial state of 
    * the task is <code>!isFinished()</code> so doing waitFinished() will
    * block on and wait until the task is scheduled.
    *
    * @param run action to run in the process
    * @return the task to control execution of given action
    */
    public Task create(Runnable run) {
        return create(run, false);
    }
    
    /** Creates request that can be later started by setting its delay.
    * The request is not immediatelly put into the queue. It is planned after
    * setting its delay by schedule method.
    *
    * @param run action to run in the process
    * @param initiallyFinished should the task be marked initially finished? If 
    *   so the {@link Task#waitFinished} on the task will succeeded immediatelly even
    *   the task has not yet been {@link Task#schedule}d.
    * @return the task to control execution of given action
    * @since 6.8
    */
    public Task create(Runnable run, boolean initiallyFinished) {
        Task t = new Task(run);
        if (initiallyFinished) {
            t.notifyFinished();
        }
        return t;
    }
    

    /** Tests if the current thread is request processor thread.
    * This method could be used to prevent the deadlocks using
    * <CODE>waitFinished</CODE> method. Any two tasks created
    * by request processor must not wait for themself.
    *
    * @return <CODE>true</CODE> if the current thread is request processor
    *          thread, otherwise <CODE>false</CODE>
    */
    public boolean isRequestProcessorThread() {
        Thread c = Thread.currentThread();

        //        return c instanceof Processor && ((Processor)c).source == this;
        synchronized (processorLock) {
            return processors.contains(c);
        }
    }

    /** Stops processing of runnables processor.
    * The currently running runnable is finished and no new is started.
    */
    public void stop() {
        if ((this == UNLIMITED) || (this == DEFAULT)) {
            throw new IllegalArgumentException("Can't stop shared RP's"); // NOI18N
        }

        synchronized (processorLock) {
            stopped = true;

            Iterator it = processors.iterator();

            while (it.hasNext())
                ((Processor) it.next()).interrupt();
        }
    }

    //
    // Static methods communicating with default request processor
    //

    /** This methods asks the request processor to start given
     * runnable after <code>timeToWait</code> milliseconds. The default priority is {@link Thread#MIN_PRIORITY}.
     *
     * @param run class to run
     * @return the task to control the request
     *
     * @deprecated Sharing of one singlethreaded <CODE>RequestProcessor</CODE>
     * among different users and posting even blocking requests is inherently
     * deadlock-prone. See <A href="#use_cases">use cases</A>. */
    public static Task postRequest(Runnable run) {
        return DEFAULT.post(run);
    }

    /** This methods asks the request processor to start given
     * runnable after <code>timeToWait</code> milliseconds.
     * The default priority is {@link Thread#MIN_PRIORITY}.
     *
     * @param run class to run
     * @param timeToWait to wait before execution
     * @return the task to control the request
     *
     * @deprecated Sharing of one singlethreaded <CODE>RequestProcessor</CODE>
     * among different users and posting even blocking requests is inherently
     * deadlock-prone. See <A href="#use_cases">use cases</A>. */
    public static Task postRequest(final Runnable run, int timeToWait) {
        return DEFAULT.post(run, timeToWait);
    }

    /** This methods asks the request processor to start given
     * runnable after <code>timeToWait</code> milliseconds. Given priority is assigned to the
     * request.
     * @param run class to run
     * @param timeToWait to wait before execution
     * @param priority the priority from {@link Thread#MIN_PRIORITY} to {@link Thread#MAX_PRIORITY}
     * @return the task to control the request
     *
     * @deprecated Sharing of one singlethreaded <CODE>RequestProcessor</CODE>
     * among different users and posting even blocking requests is inherently
     * deadlock-prone. See <A href="#use_cases">use cases</A>. */
    public static Task postRequest(final Runnable run, int timeToWait, int priority) {
        return DEFAULT.post(run, timeToWait, priority);
    }

    /** Creates request that can be later started by setting its delay.
     * The request is not immediatelly put into the queue. It is planned after
     * setting its delay by setDelay method.
     * @param run action to run in the process
     * @return the task to control execution of given action
     *
     * @deprecated Sharing of one singlethreaded <CODE>RequestProcessor</CODE>
     * among different users and posting even blocking requests is inherently
     * deadlock-prone. See <A href="#use_cases">use cases</A>. */
    public static Task createRequest(Runnable run) {
        return DEFAULT.create(run);
    }

    /** Logger for the error manager.
     */
    static Logger logger() {
        synchronized (starterThread) {
            if (logger == null) {
                logger = Logger.getLogger("org.openide.util.RequestProcessor"); // NOI18N
            }

            return logger;
        }
    }

    //------------------------------------------------------------------------------
    // The pending queue management implementation
    //------------------------------------------------------------------------------

    /** Place the Task to the queue of pending tasks for immediate processing.
     * If there is no other Task planned, this task is immediatelly processed
     * in the Processor.
     */
    void enqueue(Item item) {
        Logger em = logger();
        boolean loggable = em.isLoggable(Level.FINE);
        
        synchronized (processorLock) {
            if (item.getTask() == null) {
                if (loggable) {
                    em.fine("Null task for item " + item); // NOI18N
                }
                return;
            }

            prioritizedEnqueue(item);

            if (running < throughput) {
                running++;

                Processor proc = Processor.get();
                processors.add(proc);
                proc.setName(name);
                proc.attachTo(this);
            }
        }
        if (loggable) {
            em.fine("Item enqueued: " + item.action + " status: " + item.enqueued); // NOI18N
        }
    }

    // call it under queue lock i.e. processorLock
    private void prioritizedEnqueue(Item item) {
        int iprio = item.getPriority();

        if (queue.isEmpty()) {
            queue.add(item);
            item.enqueued = true;

            return;
        } else if (iprio <= ((Item) queue.get(queue.size() - 1)).getPriority()) {
            queue.add(item);
            item.enqueued = true;
        } else {
            for (ListIterator<Item> it = queue.listIterator(); it.hasNext();) {
                Item next = (Item) it.next();

                if (iprio > next.getPriority()) {
                    it.set(item);
                    it.add(next);
                    item.enqueued = true;

                    return;
                }
            }

            throw new IllegalStateException("Prioritized enqueue failed!");
        }
    }

    Task askForWork(Processor worker, String debug) {
        if (stopped || queue.isEmpty()) { // no more work in this burst, return him
            processors.remove(worker);
            Processor.put(worker, debug);
            running--;

            return null;
        } else { // we have some work for the worker, pass it

            Item i = (Item) queue.remove(0);
            Task t = i.getTask();
            i.clear(worker);

            return t;
        }
    }

    /**
     * The task describing the request sent to the processor.
     * Cancellable since 4.1.
     */
    public final class Task extends seco.notebook.util.Task //implements Cancellable 
    {
        private Item item;
        private int priority = Thread.MIN_PRIORITY;
        private long time = 0;
        private Thread lastThread = null;

        /** @param run runnable to start
        * @param delay amount of millis to wait
        * @param priority the priorty of the task
        */
        Task(Runnable run) {
            super(run);
        }

        Task(Runnable run, int priority) {
            super(run);

            if (priority < Thread.MIN_PRIORITY) {
                priority = Thread.MIN_PRIORITY;
            }

            if (priority > Thread.MAX_PRIORITY) {
                priority = Thread.MAX_PRIORITY;
            }

            this.priority = priority;
        }

        public void run() {
            try {
                notifyRunning();
                lastThread = Thread.currentThread();
                run.run();
            } finally {
                Item scheduled = this.item;
                if (scheduled != null && scheduled.getTask() == this) {
                    // do not mark as finished, we are scheduled for future
                } else {
                    notifyFinished();
                }
                lastThread = null;
            }
        }

        /** Getter for amount of millis till this task
        * is started.
        * @return amount of millis
        */
        public int getDelay() {
            long delay = time - System.currentTimeMillis();

            if (delay < 0L) {
                return 0;
            }

            if (delay > (long) Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            return (int) delay;
        }

        /** (Re-)schedules a task to run in the future.
        * If the task has not been run yet, it is postponed to
        * the new time. If it has already run and finished, it is scheduled
        * to be started again. If it is currently running, it is nevertheless
        * left to finish, and also scheduled to run again.
        * @param delay time in milliseconds to wait (starting from now)
        */
        public void schedule(int delay) {
            if (stopped) {
                throw new IllegalStateException("RequestProcessor already stopped!"); // NOI18N
            }

            time = System.currentTimeMillis() + delay;

            final Item localItem;

            synchronized (processorLock) {
                notifyRunning();

                if (item != null) {
                    item.clear(null);
                }

                item = new Item(this, RequestProcessor.this);
                localItem = item;
            }

            if (delay == 0) { // Place it to pending queue immediatelly
                enqueue(localItem);
            } else { // Post the starter
                starterThread.schedule(
                    new TimerTask() {
                        public void run() { // enqueue the created

                            try {
                                enqueue(localItem); // it may be already neutralized
                            } catch (RuntimeException e) {
                            	e.printStackTrace();
                                //ErrorManager.getDefault().notify(e);
                            }
                        }
                    }, delay
                );
            }
        }

        /** Removes the task from the queue.
        *
        * @return true if the task has been removed from the queue,
        *   false it the task has already been processed
        */
        public boolean cancel() {
            synchronized (processorLock) {
                boolean success;

                if (item == null) {
                    success = false;
                } else {
                    Processor p = item.getProcessor();
                    success = item.clear(null);

                    if (p != null) {
                        p.interruptTask(this, RequestProcessor.this);
                        item = null;
                    }
                }

                if (success) {
                    notifyFinished(); // mark it as finished
                }

                return success;
            }
        }

        /** Current priority of the task.
        */
        public int getPriority() {
            return priority;
        }

        /** Changes the priority the task will be performed with. */
        public void setPriority(int priority) {
            if (this.priority == priority) {
                return;
            }

            if (priority < Thread.MIN_PRIORITY) {
                priority = Thread.MIN_PRIORITY;
            }

            if (priority > Thread.MAX_PRIORITY) {
                priority = Thread.MAX_PRIORITY;
            }

            this.priority = priority;

            // update queue position accordingly
            synchronized (processorLock) {
                if (item == null) {
                    return;
                }

                if (queue.remove(item)) {
                    prioritizedEnqueue(item);
                }
            }
        }

        /** This method is an implementation of the waitFinished method
        * in the RequestProcessor.Task. It check the current thread if it is
        * request processor thread and in such case runs the task immediatelly
        * to prevent deadlocks.
        */
        public void waitFinished() {
            if (isRequestProcessorThread()) { //System.err.println(
                boolean toRun;
                
                Logger em = logger();
                boolean loggable = em.isLoggable(Level.FINE);
                
                if (loggable) {
                    em.fine("Task.waitFinished on " + this + " from other task in RP: " + Thread.currentThread().getName()); // NOI18N
                }
                

                synchronized (processorLock) {
                    // correct line:    toRun = (item == null) ? !isFinished (): (item.clear() && !isFinished ());
                    // the same:        toRun = !isFinished () && (item == null ? true : item.clear ());
                    toRun = !isFinished() && ((item == null) || item.clear(null));
                    if (loggable) {
                        em.fine("    ## finished: " + isFinished()); // NOI18N
                        em.fine("    ## item: " + item); // NOI18N
                    }
                }

                if (toRun) { 
                    if (loggable) {
                        em.fine("    ## running it synchronously"); // NOI18N
                    }
                    Processor processor = (Processor)Thread.currentThread();
                    processor.doEvaluate (this, processorLock, RequestProcessor.this);
                } else { // it is already running in other thread of this RP
                    if (loggable) {
                        em.fine("    ## not running it synchronously"); // NOI18N
                    }

                    if (lastThread != Thread.currentThread()) {
                        if (loggable) {
                            em.fine("    ## waiting for it to be finished"); // NOI18N
                        }
                        super.waitFinished();
                    }

                    //                    else {
                    //System.err.println("Thread waiting for itself!!!!! - semantics broken!!!");
                    //Thread.dumpStack();
                    //                    }
                }
                if (loggable) {
                    em.fine("    ## exiting waitFinished"); // NOI18N
                }
            } else {
                super.waitFinished();
            }
        }

        /** Enhanced reimplementation of the {@link Task#waitFinished(long)}
        * method. The added semantic is that if one calls this method from
        * another task of the same processor, and the task has not yet been
        * executed, the method will immediatelly detect that and throw
        * <code>InterruptedException</code> to signal that state.
        *
        * @param timeout the amount of time to wait
        * @exception InterruptedException if waiting has been interrupted or if
        *    the wait cannot succeed due to possible deadlock collision
        * @return true if the task was finished successfully during the
        *    timeout period, false otherwise
        *  @since 5.0
        */
        public boolean waitFinished(long timeout) throws InterruptedException {
            if (isRequestProcessorThread()) {
                boolean toRun;

                synchronized (processorLock) {
                    toRun = !isFinished() && ((item == null) || item.clear(null));
                }

                if (toRun) {
                    throw new InterruptedException(
                        "Cannot wait with timeout " + timeout + " from the RequestProcessor thread for task: " + this
                    ); // NOI18N
                } else { // it is already running in other thread of this RP

                    if (lastThread != Thread.currentThread()) {
                        return super.waitFinished(timeout);
                    } else {
                        return true;
                    }
                }
            } else {
                return super.waitFinished(timeout);
            }
        }

        public String toString() {
            return "RequestProcessor.Task [" + name + ", " + priority + "] for " + super.toString(); // NOI18N
        }
    }

    /* One item representing the task pending in the pending queue */
    private static class Item extends Exception {
        private final RequestProcessor owner;
        private Object action;
        private boolean enqueued;

        Item(Task task, RequestProcessor rp) {
            super("Posted StackTrace"); // NOI18N
            action = task;
            owner = rp;
        }

        Task getTask() {
            Object a = action;

            return (a instanceof Task) ? (Task) a : null;
        }

        /** Annulate this request iff still possible.
         * @returns true if it was possible to skip this item, false
         * if the item was/is already processed */
        boolean clear(Processor processor) {
            synchronized (owner.processorLock) {
                action = processor;

                return enqueued ? owner.queue.remove(this) : true;
            }
        }

        Processor getProcessor() {
            Object a = action;

            return (a instanceof Processor) ? (Processor) a : null;
        }

        int getPriority() {
            return getTask().getPriority();
        }

        public Throwable fillInStackTrace() {
            return SLOW ? super.fillInStackTrace() : this;
        }
    }

    //------------------------------------------------------------------------------
    // The Processor management implementation
    //------------------------------------------------------------------------------

    /**
    /** A special thread that processes timouted Tasks from a RequestProcessor.
     * It uses the RequestProcessor as a synchronized queue (a Channel),
     * so it is possible to run more Processors in paralel for one RequestProcessor
     */
    private static class Processor extends Thread {
        /** A stack containing all the inactive Processors */
        private static Stack<Processor> pool = new Stack<Processor>();

        /* One minute of inactivity and the Thread will die if not assigned */
        private static final int INACTIVE_TIMEOUT = 60000;

        /** Internal variable holding the Runnable to be run.
         * Used for passing Runnable through Thread boundaries.
         */

        //private Item task;
        private RequestProcessor source;

        /** task we are working on */
        private RequestProcessor.Task todo;
        private boolean idle = true;

        /** Waiting lock */
        private Object lock = new Object();

        public Processor() {
            super(getTopLevelThreadGroup(), "Inactive RequestProcessor thread"); // NOI18N
            setDaemon(true);
        }

        /** Provide an inactive Processor instance. It will return either
         * existing inactive processor from the pool or will create a new instance
         * if no instance is in the pool.
         *
         * @return inactive Processor
         */
        static Processor get() {
            synchronized (pool) {
                if (pool.isEmpty()) {
                    Processor proc = new Processor();
                    proc.idle = false;
                    proc.start();

                    return proc;
                } else {
                    Processor proc = (Processor) pool.pop();
                    proc.idle = false;

                    return proc;
                }
            }
        }

        /** A way of returning a Processor to the inactive pool.
         *
         * @param proc the Processor to return to the pool. It shall be inactive.
         * @param last the debugging string identifying the last client.
         */
        static void put(Processor proc, String last) {
            synchronized (pool) {
                proc.setName("Inactive RequestProcessor thread [Was:" + proc.getName() + "/" + last + "]"); // NOI18N
                proc.idle = true;
                pool.push(proc);
            }
        }

        /** setPriority wrapper that skips setting the same priority
         * we'return already running at */
        void setPrio(int priority) {
            if (priority != getPriority()) {
                setPriority(priority);
            }
        }

        /**
         * Sets an Item to be performed and notifies the performing Thread
         * to start the processing.
         *
         * @param r the Item to run.
         */
        public void attachTo(RequestProcessor src) {
            synchronized (lock) {
                //assert(source == null);
                source = src;
                lock.notify();
            }
        }

        /**
         * The method that will repeatedly wait for a request and perform it.
         */
        public void run() {
            for (;;) {
                RequestProcessor current = null;

                synchronized (lock) {
                    try {
                        if (source == null) {
                            lock.wait(INACTIVE_TIMEOUT); // wait for the job
                        }
                    } catch (InterruptedException e) {
                    }
                     // not interesting

                    current = source;
                    source = null;

                    if (current == null) { // We've timeouted

                        synchronized (pool) {
                            if (idle) { // and we're idle
                                pool.remove(this);

                                break; // exit the thread
                            } else { // this will happen if we've been just

                                continue; // before timeout when we were assigned
                            }
                        }
                    }
                }

                String debug = null;

                Logger em = logger();
                boolean loggable = em.isLoggable(Level.INFO);

                if (loggable) {
                    em.fine("Begining work " + getName()); // NOI18N
                }

                // while we have something to do
                for (;;) {
                    // need the same sync as interruptTask
                    synchronized (current.processorLock) {
                        todo = current.askForWork(this, debug);
                        if (todo == null) break;
                    }
                    setPrio(todo.getPriority());

                    try {
                        if (loggable) {
                            em.fine("  Executing " + todo); // NOI18N
                        }

                        todo.run();

                        if (loggable) {
                            em.fine("  Execution finished in" + getName()); // NOI18N
                        }

                        debug = todo.debug();
                    } catch (OutOfMemoryError oome) {
                        // direct notification, there may be no room for
                        // annotations and we need OOME to be processed
                        // for debugging hooks
                        em.log(Level.SEVERE, null, oome);
                    } catch (StackOverflowError e) {
                        // Try as hard as possible to get a real stack trace
                        e.printStackTrace();

                        // recoverable too
                        doNotify(todo, e);
                    } catch (Throwable t) {
                        doNotify(todo, t);
                    }

                    // need the same sync as interruptTask
                    synchronized (current.processorLock) {
                        // to improve GC
                        todo = null;
                        // and to clear any possible interrupted state
                        // set by calling Task.cancel ()
                        Thread.interrupted();
                    }
                }

                if (loggable) {
                    em.fine("Work finished " + getName()); // NOI18N
                }
            }
        }
        
        /** Evaluates given task directly.
         */
        final void doEvaluate (Task t, Object processorLock, RequestProcessor src) {
            Task previous = todo;
            boolean interrupted = Thread.interrupted();
            try {
                todo = t;
                t.run ();
            } finally {
                synchronized (processorLock) {
                    todo = previous;
                    if (interrupted || todo.item == null) {
                        if (src.interruptThread) {
                            // reinterrupt the thread if it was interrupted and
                            // we support interrupts
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }

        /** Called under the processorLock */
        public void interruptTask(Task t, RequestProcessor src) {
            if (t != todo) {
                // not running this task so
                return;
            }
            
            if (src.interruptThread) {
                // otherwise interrupt this thread
                interrupt();
            }
        }

        /** @see "#20467" */
        private static void doNotify(RequestProcessor.Task todo, Throwable ex) {
            logger().log(Level.SEVERE, null, ex);
            if (SLOW) {
                logger.log(Level.SEVERE, null, todo.item);
            }
        }

        /**
         * @return a top level ThreadGroup. The method ensures that even
         * Processors created by internal execution will survive the
         * end of the task.
         */
        static ThreadGroup getTopLevelThreadGroup() {
            java.security.PrivilegedAction<ThreadGroup> run = new java.security.PrivilegedAction<ThreadGroup>() {
                    public ThreadGroup run() {
                        ThreadGroup current = Thread.currentThread().getThreadGroup();

                        while (current.getParent() != null) {
                            current = current.getParent();
                        }

                        return current;
                    }
                };

            return java.security.AccessController.doPrivileged(run);
        }
    }
}

