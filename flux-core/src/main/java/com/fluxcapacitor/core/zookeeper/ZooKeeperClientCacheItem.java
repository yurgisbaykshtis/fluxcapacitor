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

import com.netflix.curator.framework.CuratorFramework;

public class ZooKeeperClientCacheItem {
    public final String ensemble;
    public final CuratorFramework client;

    public ZooKeeperClientCacheItem(String ensemble, CuratorFramework client) {
        this.ensemble = ensemble;
        this.client = client;
    }
}
