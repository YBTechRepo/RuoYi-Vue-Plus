package org.dromara.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.context.support.GenericApplicationContext;

final class TestJsonContext {

    private static GenericApplicationContext context;

    private TestJsonContext() {
    }

    static synchronized void initialize() {
        if (context != null) {
            return;
        }
        context = new GenericApplicationContext();
        context.registerBean(ObjectMapper.class, () -> new ObjectMapper());
        context.refresh();
        new SpringUtils().setApplicationContext(context);
    }
}
