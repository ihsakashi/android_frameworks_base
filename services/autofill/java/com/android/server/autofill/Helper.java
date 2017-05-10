/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.server.autofill;

import android.annotation.NonNull;
import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.os.Bundle;
import android.util.DebugUtils;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public final class Helper {

    /**
     * Defines a logging flag that can be dynamically changed at runtime using
     * {@code cmd autofill debug [on|off]}.
     */
    public static boolean sDebug = false;

    /**
     * Defines a logging flag that can be dynamically changed at runtime using
     * {@code cmd autofill verbose [on|off]}.
     */
    public static boolean sVerbose = false;

    private Helper() {
        throw new UnsupportedOperationException("contains static members only");
    }

    static void append(StringBuilder builder, Bundle bundle) {
        if (bundle == null || !sVerbose) {
            builder.append("null");
            return;
        }
        final Set<String> keySet = bundle.keySet();
        builder.append("[Bundle with ").append(keySet.size()).append(" extras:");
        for (String key : keySet) {
            final Object value = bundle.get(key);
            builder.append(' ').append(key).append('=');
            builder.append((value instanceof Object[])
                    ? Arrays.toString((Objects[]) value) : value);
        }
        builder.append(']');
    }

    static String bundleToString(Bundle bundle) {
        final StringBuilder builder = new StringBuilder();
        append(builder, bundle);
        return builder.toString();
    }

    static String getUpdateActionAsString(int action) {
        return DebugUtils.flagsToString(AutofillManager.class, "ACTION_", action);
    }

    static ViewNode findViewNodeById(@NonNull AssistStructure structure, @NonNull AutofillId id) {
        final int size = structure.getWindowNodeCount();
        for (int i = 0; i < size; i++) {
            final AssistStructure.WindowNode window = structure.getWindowNodeAt(i);
            final ViewNode root = window.getRootViewNode();
            if (id.equals(root.getAutofillId())) {
                return root;
            }
            final ViewNode child = findViewNodeById(root, id);
            if (child != null) {
                return child;
            }
        }
        return null;
    }

    static ViewNode findViewNodeById(@NonNull ViewNode parent, @NonNull AutofillId id) {
        final int childrenSize = parent.getChildCount();
        if (childrenSize > 0) {
            for (int i = 0; i < childrenSize; i++) {
                final ViewNode child = parent.getChildAt(i);
                if (id.equals(child.getAutofillId())) {
                    return child;
                }
                final ViewNode grandChild = findViewNodeById(child, id);
                if (grandChild != null && id.equals(grandChild.getAutofillId())) {
                    return grandChild;
                }
            }
        }
        return null;
    }
}
