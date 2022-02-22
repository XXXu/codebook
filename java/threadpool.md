# java 线程池解析
## ThreadPoolExecutor
ThreadPoolExecutor以内部线程池的形式对外提供管理任务执行，线程调度，线程池管理等等服务。ThreadPoolExecutor构造方法解析:
```
public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {}
```
| 参数名             |   作用 |
|:----------------|:------|
| corePoolSize    | 核心线程池大小 |
| maximumPoolSize | 最大线程池大小 |
| keepAliveTime | 线程池中超过corePoolSize数目的空闲线程最大存活时间 |
| TimeUnit | keepAliveTime时间单位 |
| workQueue | 阻塞任务队列 |
| threadFactory | 新建线程工厂 |
| RejectedExecutionHandler | 当提交任务数超过maximumPoolSize+workQueue之和时，任务会交给RejectedExecutionHandler来处理 |

**重点讲解：**
1. 当线程数小于corePoolSize时，新提交任务将创建一个新线程执行任务，即使此时线程池中存在空闲线程
2. 当线程数达到corePoolSize时，新提交任务将被放入workQueue中，等待线程池中任务调度执行
3. 当workQueue已满，且maximumPoolSize>corePoolSize时，新提交任务会创建新线程执行任务，但是线程数不能超过maximumPoolSize
4. 如果提交的线程数超过maximumPoolSize，则放进workQueue
5. 当提交任务数超过maximumPoolSize+workQueue时，新提交任务由RejectedExecutionHandler处理
6. 当线程池中超过corePoolSize线程，空闲时间达到keepAliveTime时，关闭空闲线程
7. 当设置allowCoreThreadTimeOut(true)时，线程池中corePoolSize线程空闲时间达到keepAliveTime也将关闭

## Executors
Executors提供静态方法方便我们构造出线程池，不建议使用Executors，而直接使用ThreadPoolExecutor，更能加深这些参数是什么意思，也能更好的构造出我们想要的线程池。
```
public static ExecutorService newFixedThreadPool(int nThreads) {  
        return new ThreadPoolExecutor(nThreads, nThreads,  
                                      0L, TimeUnit.MILLISECONDS,  
                                      new LinkedBlockingQueue<Runnable>());  
    }
```
> 构造一个固定线程数目的线程池，配置的corePoolSize与maximumPoolSize大小相同，同时使用了一个无界LinkedBlockingQueue存放阻塞任务，这个时候多余的任务将存在在阻塞队列，不会由RejectedExecutionHandler处理

```
public static ExecutorService newCachedThreadPool() {  
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,  
                                      60L, TimeUnit.SECONDS,  
                                      new SynchronousQueue<Runnable>());  
    }
```
构造一个缓冲功能的线程池，配置corePoolSize=0，maximumPoolSize=Integer.MAX_VALUE，keepAliveTime=60s,以及一个无容量的阻塞队列 SynchronousQueue，因此任务提交之后，将会创建新的线程执行；线程空闲超过60s将会销毁

```
public static ExecutorService newSingleThreadExecutor() {  
        return new FinalizableDelegatedExecutorService  
            (new ThreadPoolExecutor(1, 1,  
                                    0L, TimeUnit.MILLISECONDS,  
                                    new LinkedBlockingQueue<Runnable>()));  
    }
```
构造一个只支持一个线程的线程池，配置corePoolSize=maximumPoolSize=1，无界阻塞队列LinkedBlockingQueue，保证任务由一个线程串行执行

```
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {  
        return new ScheduledThreadPoolExecutor(corePoolSize);  
    }  

public static ScheduledExecutorService newScheduledThreadPool(  
            int corePoolSize, ThreadFactory threadFactory) {  
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);  
    }  

public ScheduledThreadPoolExecutor(int corePoolSize,  
                             ThreadFactory threadFactory) {  
        super(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS,  
              new DelayedWorkQueue(), threadFactory);  
    }
```

构造有定时功能的线程池，配置corePoolSize，无界延迟阻塞队列DelayedWorkQueue；有意思的是：maximumPoolSize=Integer.MAX_VALUE，由于DelayedWorkQueue是无界队列，所以这个值是没有意义的。说ScheduledThreadPoolExecutor之前，先来看看Timer。

## Timer
### 例子：
```
public class OutOfTime {
    public static void main(String[] args) throws InterruptedException {
        Timer timer=new Timer();
        timer.schedule(new ThrowTask(), 1);
        Thread.sleep(1000L);
        timer.schedule(new ThrowTask(), 1);
        Thread.sleep(5000L);
    }
}
class ThrowTask extends TimerTask{

    @Override
    public void run() {
        throw new RuntimeException();
    }

}
```
结果：
```
Exception in thread "Timer-0" java.lang.RuntimeException
    at com.koma.demo.ThrowTask.run(OutOfTime.java:24)
    at java.util.TimerThread.mainLoop(Unknown Source)
    at java.util.TimerThread.run(Unknown Source)
Exception in thread "main" java.lang.IllegalStateException: Timer already cancelled.
    at java.util.Timer.sched(Unknown Source)
    at java.util.Timer.schedule(Unknown Source)
```
你可能认为程序运行6秒后退出，实际上并不是，并且抛出异常消息：Timer already cancelled。

```
public class TimerTest {

    public static void main(String[] args) throws Exception {

        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                System.out.println("task1: " + System.currentTimeMillis());
            }
        };
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                System.out.println("task2: " + System.currentTimeMillis());
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        // 一秒钟执行一次
        timer.schedule(task1, 1, 1000);
        timer.schedule(task2, 1);

    }
}
```
结果：
```
task1: 1512714371122
task2: 1512714371122
task1: 1512714375122
task1: 1512714376122
task1: 1512714377122
task1: 1512714378122
```
从结果可看，task1本意是一秒钟一次，但是第二次在四秒后才运行，丢失了三次。

**Timer缺点：**
1. TimerTask抛出了一个未检查的异常，Timer线程并不会捕获异常，并且终止了定时线程。这种情况下，Timer也不会恢复线程的执行，而是错误的认为整个Timer都被取消了。 
2. 某个周期TimerTask需要没1000ms执行一次，而另一个Timertask需要执行4000ms，那么周期任务或者在40ms任务执行完成后快速连续的调用3次，或者彻底丢失4次调用,取决于它是基于固定速率还是固定延时来调度。

## ScheduledThreadPoolExecutor
### 例子：
```
public class ScheduledThreadPoolExecutorTest {
    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("task1: " + System.currentTimeMillis());

            }
        }, 1, 1000, TimeUnit.MILLISECONDS);

        service.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("task2: " + System.currentTimeMillis());
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 1, TimeUnit.MILLISECONDS);

    }
}
```
结果：
```
task1: 1512715487934
task2: 1512715487934
task1: 1512715488934
task1: 1512715489934
task1: 1512715490934
task1: 1512715491934
```
由此可见task1就是1秒执行一次。
```
public class ScheduledThreadPoolExecutorTest {
    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
        ScheduledThreadDemo t=new ScheduledThreadDemo();
        Thread.sleep(1000L);
        System.out.println(System.currentTimeMillis());
        service.schedule(t, 1, TimeUnit.MILLISECONDS);
        Thread.sleep(5000L);
        service.schedule(t, 1, TimeUnit.MILLISECONDS);
        System.out.println(System.currentTimeMillis());
        service.shutdown();
    }
}
class ScheduledThreadDemo implements Runnable{

    @Override
    public void run() {
        throw new RuntimeException();
    }
}
```
结果：
```
1512715983935
1512715988938
```
并没有抛出异常。

**总结：**
不要使用Timer，要用ScheduledThreadPoolExecutor

## UncaughtExceptionHandler

单线程的程序发生一个未捕获的异常时我们可以采用try….catch进行异常的捕获，但是在多线程环境中，线程抛出的异常是不能用try….catch捕获的，这样就有可能导致一些问题的出现，比如异常的时候无法回收一些系统资源，或者没有关闭当前的连接等。
```
public class ThreadDemo {
    public static void main(String[] args) {
        try {
            Thread thread = new Thread(new Task());
            thread.start();
        } catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
        }
    }
}

class Task implements Runnable {
    @Override
    public void run() {
        System.out.println(10 / 2);
        System.out.println(10 / 0);
        System.out.println(10 / 1);
    }
}
```
结果：
```
5
Exception in thread "Thread-0" java.lang.ArithmeticException: / by zero
    at com.koma.demo.Task.run(ThreadDemo.java:22)
    at java.lang.Thread.run(Unknown Source)
```
这个时候api提供的UncaughtExceptionHandler接口就派上用场了。

```
public class CaughtThread {
    public static void main(String args[]) {
        Thread thread = new Thread(new Task());
        // 设置异常处理的hanlder
        thread.setUncaughtExceptionHandler(new ExceptionHandler());
        thread.start();
    }
}

class ExceptionHandler implements UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("exception: " + e.getMessage());
    }
}
```

Thread还有一个默认的静态方法setDefaultUncaughtExceptionHandler，所有的线程默认的异常处理hanlder。
```
import java.lang.Thread.UncaughtExceptionHandler;

public class CaughtThread {
    public static void main(String args[]) {
        // Thread提供的静态方法，为所有的Thread提供默认的异常处理
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        Thread thread = new Thread(new Task());
        // 设置异常处理的hanlder
        // thread.setUncaughtExceptionHandler(new ExceptionHandler());
        thread.start();
    }
}

class ExceptionHandler implements UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("exception: " + e.getMessage());
    }
}
```
如果采用线程池通过execute的方法去捕获异常，还按照在外部设置hanlder处理，这样行吗？
```
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteThread {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        Thread thread = new Thread(new ThreadPoolTask());
        thread.setUncaughtExceptionHandler(new ExceptionHandler());
        exec.execute(thread);
        exec.shutdown();
    }
}

class ThreadPoolTask implements Runnable {
    @Override
    public void run() {
        System.out.println(10 / 2);
        System.out.println(10 / 0);
        System.out.println(10 / 1);
    }
}
```

结果：
```
5
Exception in thread "pool-1-thread-1" java.lang.ArithmeticException: / by zero
    at com.koma.demo.ThreadPoolTask.run(ExecuteThread.java:24)
    at java.lang.Thread.run(Unknown Source)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
    at java.lang.Thread.run(Unknown Source)
```
由此可见，在外部设置是不行的，必须Runable中设置才行。

```
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteThread {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        Thread thread = new Thread(new ThreadPoolTask());
        exec.execute(thread);
        exec.shutdown();
    }
}

class ThreadPoolTask implements Runnable {
    @Override
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler());
        System.out.println(10 / 2);
        System.out.println(10 / 0);
        System.out.println(10 / 1);
    }
}
```
结果：
```
5
exception: / by zero
```

前面是通过execute提交任务的，如果通过submit提交的任务，无论是抛出的未检测异常还是已检查异常，都将被认为是任务返回状态的一部分。如果一个由submit提交的任务由于抛出了异常而结束，那么这个异常将被Future.get封装在ExecutionException中重新抛出。

```
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SubmitThread {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        Future<?> future = exec.submit(new Task());
        exec.shutdown();
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("exception: " + e);
        }
    }
}
```
结果：
```
5
exception: java.util.concurrent.ExecutionException: java.lang.ArithmeticException: / by zero
```