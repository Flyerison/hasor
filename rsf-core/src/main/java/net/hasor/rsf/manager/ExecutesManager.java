/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.rsf.manager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.hasor.rsf.utils.NameThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 业务线程
 * @version : 2014年11月11日
 * @author 赵永春(zyc@hasor.net)
 */
public class ExecutesManager {
    protected Logger                               logger           = LoggerFactory.getLogger(getClass());
    private ThreadPoolExecutor                     defaultExecutor  = null;
    private final Map<Integer, ThreadPoolExecutor> servicePoolCache = new HashMap<Integer, ThreadPoolExecutor>();
    //
    public ExecutesManager(int minCorePoolSize, int maxCorePoolSize, int queueSize, long keepAliveTime) {
        logger.info("executesManager init ->> minCorePoolSize ={}, maxCorePoolSize ={}, queueSize ={}, keepAliveTime ={}", minCorePoolSize, maxCorePoolSize, queueSize, keepAliveTime);
        //
        final BlockingQueue<Runnable> inWorkQueue = new LinkedBlockingQueue<Runnable>(queueSize);
        this.defaultExecutor = new ThreadPoolExecutor(minCorePoolSize, maxCorePoolSize,//
                keepAliveTime, TimeUnit.SECONDS, inWorkQueue,//
                new NameThreadFactory("RSF-Biz-%s"), new ThreadPoolExecutor.AbortPolicy());
    }
    //
    public Executor getExecute(byte[] serviceUniqueName) {
        if (this.servicePoolCache.isEmpty() == false) {
            int nameHashCode = Arrays.hashCode(serviceUniqueName);
            ThreadPoolExecutor executor = this.servicePoolCache.get(nameHashCode);
            if (executor != null) {
                return executor;
            }
        }
        return this.defaultExecutor;
    }
}