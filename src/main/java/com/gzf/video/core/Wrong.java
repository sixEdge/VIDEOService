/*
 * Copyright (c) 2017  Six Edge.
 *
 * This Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *               http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.gzf.video.core;

/**
 * {@link Wrong}, another kind of {@link RuntimeException},
 * rather than using {@code throw}, {@link Wrong}
 * alternative {@code throw} via {@code return}.
 * (immutable and thread safe).
 */
public abstract class Wrong {

    private String message;

    public Wrong(final String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
