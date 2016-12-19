/*
 *  Copyright 2001-2016 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans.ser;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages a map of deserializers that assist with data migration.
 * <p>
 * Deserializers handle situations where the data being read does not match the
 * bean in the classpath. See also {@code RenameHandler}.
 * <p>
 * Normally, it makes sense to customize the shared singleton instance, because
 * the classpath is static and fixed and the transformations are common.
 * <p>
 * Implementations must be thread-safe singletons.
 *
 * @author Stephen Colebourne
 */
public final class SerDeserializers {

    /**
     * Shared global instance which can be mutated.
     */
    public static final SerDeserializers INSTANCE = new SerDeserializers();

    /**
     * The deserializers.
     */
    private final ConcurrentMap<Class<?>, SerDeserializer> deserializers =
            new ConcurrentHashMap<Class<?>, SerDeserializer>();
    /**
     * The deserializer providers.
     */
    private final CopyOnWriteArrayList<SerDeserializerProvider> providers =
            new CopyOnWriteArrayList<SerDeserializerProvider>();

    /**
     * Creates an instance.
     */
    public SerDeserializers() {
    }

    /**
     * Creates an instance using additional providers.
     * 
     * @param providers  the providers to use
     */
    public SerDeserializers(SerDeserializerProvider... providers) {
        this.providers.addAll(Arrays.asList(providers));
    }

    //-----------------------------------------------------------------------
    /**
     * Adds the deserializer to be used for the specified type.
     * 
     * @param type  the type, not null
     * @param deserializer  the deserializer, not null
     * @return this, for chaining, not null
     */
    public SerDeserializers register(Class<?> type, SerDeserializer deserializer) {
        deserializers.put(type, deserializer);
        return this;
    }

    /**
     * Gets the map of deserializers which can be modified.
     * 
     * @return the map of deserializers, not null
     */
    public ConcurrentMap<Class<?>, SerDeserializer> getDeserializers() {
        return deserializers;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds the deserializer provider to be used.
     * 
     * @param provider  the deserializer provider, not null
     * @return this, for chaining, not null
     */
    public SerDeserializers registerProvider(SerDeserializerProvider provider) {
        providers.add(provider);
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Finds the deserializer for the specified type.
     * <p>
     * The {@code DefaultDeserializer} is used if one has not been registered.
     * 
     * @param type  the type, not null
     * @return the deserializer, not null
     */
    public SerDeserializer findDeserializer(Class<?> type) {
        SerDeserializer deser = deserializers.get(type);
        if (deser != null) {
            return deser;
        }
        for (SerDeserializerProvider provider : providers) {
            deser = provider.findDeserializer(type);
            if (deser != null) {
                return deser;
            }
        }
        return DefaultDeserializer.INSTANCE;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
