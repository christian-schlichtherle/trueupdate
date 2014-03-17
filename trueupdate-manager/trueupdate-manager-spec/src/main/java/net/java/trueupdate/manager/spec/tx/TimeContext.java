package net.java.trueupdate.manager.spec.tx;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A context for the method {@link Commands#time}.
 *
 * @author Christian Schlichtherle
 */
abstract public class TimeContext {

    protected void logStarting(Method method) {
        logger().log(startingLevel(method), startingMessage(method), method);
    }

    protected void logSucceeded(Method method, long durationMillis) {
        logger().log(succeededLevel(method), succeededMessage(method),
                params(method, true, durationMillis));
    }

    protected void logFailed(Method method, long durationMillis) {
        logger().log(failedLevel(method), failedMessage(method),
                params(method, false, durationMillis));
    }

    abstract protected Logger logger();

    protected Level startingLevel(Method method) {
        return method.startingLevel();
    }

    abstract protected String startingMessage(Method method);

    protected Level succeededLevel(Method method) {
        return method.succeededLevel();
    }

    abstract protected String succeededMessage(Method method);

    protected Level failedLevel(Method method) {
        return method.failedLevel();
    }

    abstract protected String failedMessage(Method method);

    protected Object[] params(final Method method, final boolean succeeded, final long durationMillis) {
        final int millis = (int) (durationMillis % 1000);
        final int seconds = (int) (durationMillis / 1000 % 60);
        final int minutes = (int) (durationMillis / 1000 / 60 % 60);
        final int hours = (int) (durationMillis / 1000 / 60 / 60);
        return new Object[] {
                method.ordinal(),
                succeeded ? 0 : 1,
                hours, minutes, seconds, millis };
    }

    public enum Method {

        perform {
            @Override void invoke(Command command) throws Exception {
                command.perform();
            }
        },

        revert {
            @Override void invoke(Command command) throws Exception {
                command.revert();
            }
        };

        abstract void invoke(Command command) throws Exception;

        public Level startingLevel() { return Level.FINE; }
        public Level succeededLevel() { return Level.INFO; }
        public Level failedLevel() { return Level.WARNING; }
    } // Method
}
