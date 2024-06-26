// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.flink.sink;

import org.apache.doris.flink.exception.DorisRuntimeException;
import org.apache.doris.flink.rest.models.BackendV2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

public class TestBackendUtil {

    private MockedStatic<BackendUtil> backendUtilMockedStatic;

    @Before
    public void setUp() throws Exception {
        backendUtilMockedStatic = mockStatic(BackendUtil.class);
    }

    @Test
    public void testGetAvailableBackend() throws Exception {
        backendUtilMockedStatic.when(() -> BackendUtil.tryHttpConnection(any())).thenReturn(true);
        List<BackendV2.BackendRowV2> backends =
                Arrays.asList(
                        newBackend("127.0.0.2", 8040),
                        newBackend("127.0.0.3", 8040),
                        newBackend("127.0.0.4", 8040));
        BackendUtil backendUtil = new BackendUtil(backends);
        Assert.assertEquals(backends.get(0).toBackendString(), backendUtil.getAvailableBackend());
        Assert.assertEquals(backends.get(1).toBackendString(), backendUtil.getAvailableBackend());
        Assert.assertEquals(backends.get(2).toBackendString(), backendUtil.getAvailableBackend());
        Assert.assertEquals(backends.get(0).toBackendString(), backendUtil.getAvailableBackend());
    }

    @Test(expected = DorisRuntimeException.class)
    public void testNoAvailableBackend() throws Exception {
        backendUtilMockedStatic.when(() -> BackendUtil.tryHttpConnection(any())).thenReturn(false);
        List<BackendV2.BackendRowV2> backends =
                Arrays.asList(
                        newBackend("127.0.0.1", 12345),
                        newBackend("127.0.0.2", 12346),
                        newBackend("127.0.0.3", 12347));
        BackendUtil backendUtil = new BackendUtil(backends);
        backendUtil.getAvailableBackend();
    }

    @Test
    public void testTryHttpConnection() {
        backendUtilMockedStatic.when(() -> BackendUtil.tryHttpConnection(any())).thenReturn(true);
        boolean flag = BackendUtil.tryHttpConnection("127.0.0.1:8040");
        Assert.assertTrue(flag);
    }

    @Test
    public void testTryHttpConnectionError() {
        boolean flag = BackendUtil.tryHttpConnection("127.0.0.1:1");
        Assert.assertFalse(flag);
    }

    @Test
    public void testInitBackends() {
        BackendUtil backendUtil = new BackendUtil("127.0.0.1:1");
        Assert.assertTrue(backendUtil.getBackends().isEmpty());
    }

    private BackendV2.BackendRowV2 newBackend(String host, int port) {
        BackendV2.BackendRowV2 backend = new BackendV2.BackendRowV2();
        backend.setIp(host);
        backend.setHttpPort(port);
        return backend;
    }

    @After
    public void after() {
        if (backendUtilMockedStatic != null) {
            backendUtilMockedStatic.close();
        }
    }
}
