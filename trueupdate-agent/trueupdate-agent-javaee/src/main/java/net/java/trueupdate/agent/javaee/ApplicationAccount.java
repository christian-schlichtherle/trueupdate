/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.javaee;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.*;
import net.java.trueupdate.agent.spec.ApplicationListener;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * @author Christian Schlichtherle
 */
final class ApplicationAccount {

    private final Queue<UpdateMessage> queue;
    private @CheckForNull ApplicationListener listener;

    public ApplicationAccount(final int capacity) {
        if (0 >= capacity) throw new IllegalArgumentException();
        this.queue = new LinkedBlockingQueue<UpdateMessage>(capacity);
    }

    @Nullable ApplicationListener listener() { return listener; }

    void listener(final ApplicationListener listener) {
        assert null != listener;
        this.listener = listener;
    }

    int size() { return queue.size(); }

    void enqueue(final UpdateMessage message) {
        while (!queue.offer(message)) queue.remove();
    }

    @CheckForNull UpdateMessage poll() { return queue.poll(); }
}
