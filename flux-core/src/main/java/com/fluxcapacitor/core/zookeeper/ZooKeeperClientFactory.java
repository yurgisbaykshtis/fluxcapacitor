/*
 * 	Copyright 2012 Chris Fregly
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.fluxcapacitor.core.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.FluxConstants;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.source.ZooKeeperConfigurationSource;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.recipes.cache.PathChildrenCache;
import com.netflix.curator.framework.recipes.cache.PathChildrenCacheEvent;
import com.netflix.curator.framework.recipes.cache.PathChildrenCacheListener;
import com.netflix.curator.retry.ExponentialBackoffRetry;

/**
 * ZooKeeper client factory.  Caches the created objects.
 *  
 * @author cfregly
 */
public class ZooKeeperClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperClientFactory.class);

    public static final Cache<String, ZooKeeperClientCacheItem> cache = CacheBuilder.newBuilder().concurrencyLevel(64)
            .build();

    /**
     * Get a started ZK client
     */
    public static CuratorFramework getStartedZKClient(String ensemble) {
        ZooKeeperClientCacheItem cachedItem = cache.getIfPresent(ensemble);
        if (cachedItem != null) {
            return cachedItem.client;
        }
        return createZKClient(ensemble);
    }

	public static void initializeAndStartZkConfigSource() {
        String zkConfigEnsemble = DynamicPropertyFactory.getInstance().getStringProperty(FluxConstants.ZK_CONFIG_ENSEMBLE, "not-found-in-flux-configuration").get();
        String zkConfigRootPath = DynamicPropertyFactory.getInstance().getStringProperty(FluxConstants.ZK_CONFIG_ROOT_PATH, "not-found-in-flux-configuration").get();

        // ZooKeeper Dynamic Override Properties
        CuratorFramework client = ZooKeeperClientFactory.getStartedZKClient(zkConfigEnsemble);
        ZooKeeperConfigurationSource zookeeperConfigSource = new ZooKeeperConfigurationSource(
                client, FluxConstants.ZK_CONFIG_ROOT_PATH);
        DynamicWatchedConfiguration zookeeperDynamicConfig = new DynamicWatchedConfiguration(
        		zookeeperConfigSource);

        // insert ZK DynamicConfig into the 2nd spot
        ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).addConfigurationAtIndex(
                zookeeperDynamicConfig, "zk dynamic override", 1);

        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, zkConfigRootPath, true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client,
					PathChildrenCacheEvent event) throws Exception {
				logger.debug("childEvent {}", event);
			}
        });

        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            logger.error("Cannot start pathChildrenCache", e);
            throw new RuntimeException("Cannot start pathChildrenCache", e);
        }
//        initializedZk = true;
    }

    /**
     * Create and start a zkclient if needed
     */
    private synchronized static CuratorFramework createZKClient(String ensemble) {
        ZooKeeperClientCacheItem cachedItem = cache.getIfPresent(ensemble);
        if (cachedItem != null) {
            return cachedItem.client;
        }

        CuratorFramework client = CuratorFrameworkFactory.newClient(ensemble,
                DynamicPropertyFactory.getInstance().getIntProperty("zookeeper.session.timeout", Integer.MIN_VALUE).get(),
                DynamicPropertyFactory.getInstance().getIntProperty("zookeeper.connection.timeout", Integer.MIN_VALUE).get(),
                new ExponentialBackoffRetry(1000, 3));
        client.start();

        cache.put(ensemble, new ZooKeeperClientCacheItem(ensemble, client));

        logger.info("Created, started, and cached zk client [{}] for ensemble [{}]", client, ensemble);

        return client;
    }
}
