#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.agent;

import java.util.logging.*;
import net.java.trueupdate.agent.spec.*;

/**
 * Unconditionally installs any available update.
 *
 * @author Christian Schlichtherle
 */
public class ApplicationListener extends UpdateAgentListener {

    // The update manager has already applied filtering and sends fully
    // populated (but not formatted) LogRecord instances, so obtaining any
    // other Logger may yield undesirable side effects.
    private static final Logger
            logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Override
    public void onUpdateNotice(UpdateAgentEvent event) throws Exception {
        // TODO: Maybe do something here before sending the request to install
        // the artifact update version in the super class.
        super.onUpdateNotice(event);
    }

    @Override protected void log(LogRecord record) {
        // If you uncomment the following line, then you will get duplicated
        // log records because the update manager has already logged this
        // record - that's how we got here!
        //logger.log(record);
    }
}
