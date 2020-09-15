/**
 *
 *    Copyright 2019-2020, Optimizely and contributors
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

package com.optimizely.ab.config.audience.match;

/**
 * UnexpectedValueTypeException is thrown when the condition value found in the datafile is
 * not one of an expected type for this version of the SDK.
 */
public class UnexpectedValueTypeException extends Exception {
    private static String message = "has an unsupported condition value. You may need to upgrade to a newer release of the Optimizely SDK.";

    public UnexpectedValueTypeException() {
        super(message);
    }
}
