/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.plugins.Category;
import org.apache.logging.log4j.plugins.Node;
import org.apache.logging.log4j.plugins.Plugin;
import org.apache.logging.log4j.plugins.PluginFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Factory for creating instances of {@link LinkedTransferQueue}.
 *
 * @since 2.7
 */
@Category(Node.CATEGORY)
@Plugin(value = "LinkedTransferQueue", elementType = BlockingQueueFactory.ELEMENT_TYPE)
public class LinkedTransferQueueFactory<E> implements BlockingQueueFactory<E> {
    @Override
    public BlockingQueue<E> create(final int capacity) {
        return new LinkedTransferQueue<>();
    }

    @PluginFactory
    public static <E> LinkedTransferQueueFactory<E> createFactory() {
        return new LinkedTransferQueueFactory<>();
    }
}
