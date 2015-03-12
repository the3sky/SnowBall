package blue.stack.snowball.app.tools;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
    static final int corePoolSize = 5;
    static final long keepAliveTime = 5000;
    static final int maxPoolSize = 10;

    public CustomThreadPoolExecutor() {
        super(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
    }
}
