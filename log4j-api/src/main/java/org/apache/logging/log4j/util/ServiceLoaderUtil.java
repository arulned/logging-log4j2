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
package org.apache.logging.log4j.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.status.StatusLogger;

/**
 * Loads all valid instances of a service.
 */
public class ServiceLoaderUtil {

    private static final MethodType LOAD_CLASS_CLASSLOADER = MethodType.methodType(ServiceLoader.class, Class.class,
            ClassLoader.class);

    private ServiceLoaderUtil() {
    }

    /**
     * Retrieves the available services from the caller's classloader.
     * 
     * Broken services will be ignored.
     * 
     * @param <T>         The service type.
     * @param serviceType The class of the service.
     * @param lookup      The calling class data.
     * @return A stream of service instances.
     */
    public static <T> Stream<T> loadServices(final Class<T> serviceType, Lookup lookup) {
        return loadServices(serviceType, lookup, false);
    }

    /**
     * Retrieves the available services from the caller's classloader and possibly
     * the thread context classloader.
     * 
     * Broken services will be ignored.
     * 
     * @param <T>         The service type.
     * @param serviceType The class of the service.
     * @param lookup      The calling class data.
     * @param useTccl     If true the thread context classloader will also be used.
     * @return A stream of service instances.
     */
    public static <T> Stream<T> loadServices(final Class<T> serviceType, Lookup lookup, boolean useTccl) {
        return loadServices(serviceType, lookup, useTccl, true);
    }

    static <T> Stream<T> loadServices(final Class<T> serviceType, final Lookup lookup, final boolean useTccl,
            final boolean verbose) {
        final ClassLoader classLoader = lookup.lookupClass().getClassLoader();
        Stream<T> services = loadClassloaderServices(serviceType, lookup, classLoader, verbose);
        if (useTccl) {
            final ClassLoader contextClassLoader = LoaderUtil.getThreadContextClassLoader();
            if (contextClassLoader != classLoader) {
                services = Stream.concat(services,
                        loadClassloaderServices(serviceType, lookup, contextClassLoader, verbose));
            }
        }
        final Set<Class<?>> classes = new HashSet<>();
        // only the first occurrence of a class
        return services.filter(service -> classes.add(service.getClass()));
    }

    static <T> Stream<T> loadClassloaderServices(final Class<T> serviceType, final Lookup lookup,
            final ClassLoader classLoader, final boolean verbose) {
        try {
            final MethodHandle handle = lookup.findStatic(ServiceLoader.class, "load", LOAD_CLASS_CLASSLOADER);
            final ServiceLoader<T> serviceLoader = (ServiceLoader<T>) handle.invokeExact(serviceType, classLoader);
            return serviceLoader.stream().map(provider -> {
                try {
                    return provider.get();
                } catch (ServiceConfigurationError e) {
                    if (verbose) {
                        StatusLogger.getLogger().warn("Unable to load service class for service {}", serviceType, e);
                    }
                }
                return null;
            }).filter(Objects::nonNull);
        } catch (Throwable e) {
            if (verbose) {
                StatusLogger.getLogger().error("Unable to load services for service {}", serviceType, e);
            }
        }
        return Stream.empty();
    }

}
