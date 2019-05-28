/*
 * Copyright (C) 2016~2019 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.vertx.web.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Assert {

    private Assert() {
    }

    /**
     * Will always throw and IlligalArgumentException
     * 
     * @param message fail message aka: "forgot to implement this"
     */
    public static void always(String message) {
        throw new IllegalArgumentException(message);
    }

    /**
     * Tests if condition is false
     *
     * @param test    must be false
     * @param message to return in exception
     * @throws IllegalArgumentException if test is true
     */
    public static void isFalse(boolean test, String message) {
        if (test) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Tests if condition is true
     *
     * @param test    must be ture
     * @param message to return in exception
     * @throws IllegalArgumentException if test is false
     */
    public static void isTrue(boolean test, String message) {
        isFalse(!test, message);
    }

    /**
     * Tests if object is null
     *
     * @param test    must be null
     * @param message to return in exception
     * @param         <T> object type
     * @throws IllegalArgumentException if object != null
     */
    public static <T> void isNull(T test, String message) {
        isTrue(test == null, message);
    }

    /**
     * Tests if object is != null
     *
     * @param test    must not be null
     * @param message to return in exception
     * @param         <T> object type
     * @throws IllegalArgumentException if object == null
     */
    public static <T> void notNull(T test, String message) {
        isFalse(test == null, message);
    }

    /**
     * Tests if String is null or empty when trimmed
     *
     * @param value   must be non empty string
     * @param message to return in exception
     * @throws IllegalArgumentException if value == null, value == "" or
     *                                  value.trim() == ""
     */
    public static void notNullOrEmptyTrimmed(String value, String message) {
        boolean f = false;
        if (value == null) {
            f = true;
        } else {
            if (value.length() == 0) {
                f = true;
            } else if (value.trim().length() == 0) {
                f = true;
            }
        }
        isFalse(f, message);
    }

    /**
     * Tests if list is null or empty
     *
     * @param list    must be have at least one element
     * @param message to return in exception
     * @param         <T> object type
     * @throws IllegalArgumentException if list == null or list.size() == 0
     */
    public static <T> void notNullOrEmpty(Collection<T> list, String message) {
        isFalse(list == null || list.size() == 0, message);
    }

    /**
     * Tests if list is null or empty
     *
     * @param set     must be have at least one element
     * @param message to return in exception
     * @param         <T> object type
     * @throws IllegalArgumentException if list == null or list.size() == 0
     */
    public static <T> void notNullOrEmpty(Set<T> set, String message) {
        isFalse(set == null || set.size() == 0, message);
    }

    /**
     * Tests if list is null or empty
     *
     * @param array   must be have at least one element
     * @param message to return in exception
     * @param         <T> object type
     * @throws IllegalArgumentException if list == null or list.size() == 0
     */
    public static <T> void notNullOrEmpty(T[] array, String message) {
        isFalse(array == null || array.length == 0, message);
    }

    /**
     * Tests if map is null or empty
     *
     * @param map     must be have at least one element
     * @param message to return in exception
     * @param         <T> object type
     * @param         <K> value type
     * @throws IllegalArgumentException if list == null or list.size() == 0
     */
    public static <T, K> void notNullOrEmpty(Map<T, K> map, String message) {
        isFalse(map == null || map.size() == 0, message);
    }

    /**
     * Tests if list is null or empty
     *
     * @param list    must be null or empty
     * @param message to return in exception
     * @param         <T> object type
     * @throws IllegalArgumentException if list != null or list.size() greater than
     *                                  0
     */
    public static <T> void isNullOrEmpty(List<T> list, String message) {
        isTrue(list == null || list.size() == 0, message);
    }

    /**
     * Tests if set is null or empty
     *
     * @param set     must be null or empty
     * @param message to return in exception
     * @param         <T> object type
     * @throws IllegalArgumentException if list != null or list.size() greater than
     *                                  0
     */
    public static <T> void isNullOrEmpty(Set<T> set, String message) {
        isTrue(set == null || set.size() == 0, message);
    }

    /**
     * Tests if array is null or empty
     *
     * @param array   must be null or empty
     * @param message to return in exception
     * @param         <T> object type
     * @throws IllegalArgumentException if list != null or list.size() greater than
     *                                  0
     */
    public static <T> void isNullOrEmpty(T[] array, String message) {
        isTrue(array == null || array.length == 0, message);
    }

    /**
     * Tests if map is null or empty
     *
     * @param map     must be null or empty
     * @param message to return in exception
     * @param         <T> object type
     * @param         <K> value type
     * @throws IllegalArgumentException if list != null or list.size() greater than
     *                                  0
     */
    public static <T, K> void isNullOrEmpty(Map<T, K> map, String message) {
        isTrue(map == null || map.size() == 0, message);
    }
}
