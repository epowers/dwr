/*
 * Copyright 2005 Joe Walker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.directwebremoting.dwrp;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.extend.RealScriptSession;
import org.directwebremoting.extend.ScriptConduit;

/**
 * An Alarm that goes off whenever output happens on a {@link ScriptSession}.
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class OutputAlarm extends BasicAlarm implements Alarm
{
    /**
     * @param scriptSession The script session to monitor
     * @param maxWaitAfterWrite How long do we wait after output
     */
    public OutputAlarm(RealScriptSession scriptSession, int maxWaitAfterWrite)
    {
        this.maxWaitAfterWrite = maxWaitAfterWrite;
        this.scriptSession = scriptSession;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.dwrp.Alarm#setAlarmAction(org.directwebremoting.dwrp.Sleeper)
     */
    public void setAlarmAction(Sleeper sleeper)
    {
        log.debug("OutputAlarm looking at a " + sleeper);
        try
        {
            scriptSession.addScriptConduit(conduit);
        }
        catch (IOException ex)
        {
            log.warn("Error adding monitor to script session", ex);
        }

        super.setAlarmAction(sleeper);
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.dwrp.Alarm#cancel()
     */
    public void cancel()
    {
        log.debug("OutputAlarm stopping looking at a " + sleeper);
        scriptSession.removeScriptConduit(conduit);
        super.cancel();
    }

    /**
     * A conduit to alert us if there is output
     */
    protected ScriptConduit conduit = new AlarmScriptConduit();

    /**
     * How long do we wait after output happens in case there is more output
     */
    protected int maxWaitAfterWrite;

    /**
     * The script session to monitor for output
     */
    protected RealScriptSession scriptSession;

    /**
     * The log stream
     */
    protected static final Log log = LogFactory.getLog(OutputAlarm.class);

    /**
     * @author Joe Walker [joe at getahead dot ltd dot uk]
     */
    protected class AlarmScriptConduit extends ScriptConduit
    {
        /**
         * Create an AlarmScriptConduit
         */
        protected AlarmScriptConduit()
        {
            super(RANK_PROCEDURAL);
        }

        /* (non-Javadoc)
         * @see org.directwebremoting.extend.ScriptConduit#addScript(org.directwebremoting.ScriptBuffer)
         */
        public boolean addScript(ScriptBuffer script)
        {
            log.debug("Output alarm went off. Additional wait of " + maxWaitAfterWrite);

            if (sleeper != null)
            {
                if (maxWaitAfterWrite > 0)
                {
                    try
                    {
                        Thread.sleep(maxWaitAfterWrite);
                    }
                    catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }

                if (sleeper != null)
                {
                    sleeper.wakeUp();
                }
            }

            return false;
        }
    }
}
