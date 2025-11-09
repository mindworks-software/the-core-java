package com.korwe.thecore.router;

import org.apache.camel.impl.engine.DefaultTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreTracer extends DefaultTracer {
    private static final Logger LOG = LoggerFactory.getLogger("com.korwe.thecore.router.CoreTracer");

    @Override
    protected void dumpTrace(String out, Object node) {
        LOG.info(out);
    }

}
