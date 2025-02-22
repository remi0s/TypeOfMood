/*
 * Copyright (C) 2014 The Android Open Source Project
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

package typeofmood.ime.latin.network;

/**
 * Authentication exception. When this exception is thrown, the client may
 * try to refresh the authentication token and try again.
 */
public class AuthException extends Exception {
    public AuthException() {
        super();
    }

    public AuthException(Throwable throwable) {
        super(throwable);
    }

    public AuthException(String detailMessage) {
        super(detailMessage);
    }
}