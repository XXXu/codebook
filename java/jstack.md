## 如何使用jstack分析线程状态
### 以下代码举例：
```
public class JstackCase {
    public static Executor executor = Executors.newFixedThreadPool(5);
    public static Object lock = new Object();
    public static void main(String[] args) {

        Task task1 = new Task();
        Task task2 = new Task();
        executor.execute(task1);
        executor.execute(task2);

    }
    static class Task implements Runnable {

        @Override
        public void run() {
            synchronized (lock) {
                calculate();
            }
        }

        public void calculate() {
            long i = 0;
            while (true) {
                i++;
            }
        }
    }
}
```
正常的程序是不会有上述代码，这里只是为了让一个线程占用较高的cpu，运行此程序后，使用top命令查看：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20210810180051.png)

1. 上图中可以看出pid为51256的java进程占用了较多的cpu资源.
2. 通过top -Hp 51256可以查看该进程下各个线程的cpu使用情况.

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20210810180334.png)

上图中可以看出pid为51271的线程占了较多的cpu资源，利用jstack命令可以继续查看该线程当前的堆栈状态。

### jstack命令
通过top命令定位到cpu占用率较高的线程之后，继续使用jstack pid命令查看当前java进程的堆栈状态：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20210810180648.png)

jstack命令生成的thread dump信息包含了JVM中所有存活的线程，为了分析指定线程，必须找出对应线程的调用栈，应该如何找？

在top命令中，已经获取到了占用cpu资源较高的线程pid，将该pid转成16进制的值，在thread dump中每个线程都有一个nid，找到对应的nid即可；
隔段时间再执行一次stack命令获取thread dump，区分两份dump是否有差别，在nid=0xc847的线程调用栈中，发现该线程一直在执行JstackCase类第29行的calculate方法，
得到这个信息，就可以检查对应的代码是否有问题。

### 通过thread dump分析线程状态
除了上述的分析，大多数情况下会基于thead dump分析当前各个线程的运行情况，如是否存在死锁、是否存在一个线程长时间持有锁不放等等。
在dump中，线程一般存在如下几种状态：
1. RUNNABLE，线程处于执行中
2. BLOCKED，线程被阻塞
3. WAITING，线程正在等待

#### 实例1：多线程竞争sysnchronized锁

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20210810181318.png)

很明显：线程1获取到锁，处于RUNNABLE状态，线程2处于BLOCK状态：
1. locked <0x00000000d70d2838>说明线程1对地址为0x00000000d70d2838对象进行了加锁；
2. waiting to lock <0x00000000d70d2838> 说明线程2在等待地址为0x00000000d70d2838对象上的锁；
3. waiting for monitor entry [0x00007fa471db0000]说明线程1是通过synchronized关键字进入了监视器的临界区，并处于"Entry Set"队列，等待monitor.

### 实例2：通过wait挂起线程
```
static class Task implements Runnable {
    @Override
    public void run() {
        synchronized (lock) {
            try {
                lock.wait();
                //TimeUnit.SECONDS.sleep(100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```
dump结果：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20210810182012.png)

线程1和2都处于WAITING状态：
1.线程1和2都是先locked <0x00000000d70d28b0>，再waiting on <0x00000000d70d28b0>，之所以先锁再等同一个对象，是因为wait方法需要先通过synchronized
获得该地址对象的monitor；
2.waiting on <0x00000000d70d28b0>说明线程执行了wait方法之后，释放了monitor，进入到"Wait Set"队列，等待其它线程执行地址为0x00000000d70d28b0对象的notify
方法，并唤醒自己

### 补充
1. prio: 表示线程优先级，就是Thread中定义的：MIN_PRIORITY、NORM_PRIORITY、MAX_PRIORITY，默认是5

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20210810203222.png)

2. os_prio：表示操作系统级别的优先级
3. tid：表示java内的线程id，同样在Thread中的tid

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20210810203254.png)

4. nid: 表示操作系统级别的线程id的16进制形式，上述解释过

