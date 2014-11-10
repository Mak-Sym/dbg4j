

package org.dbg4j.core;

import javax.annotation.Nonnull;

import org.dbg4j.core.adapters.EvaluationAdapter;

public class CustomEvaluationAdapter implements EvaluationAdapter {

    public static final String VALUE = "CustomEvaluationAdapter";

    @Nonnull
    @Override
    public String evaluate(Class clz, Object arg) {
        return VALUE;
    }
}
