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

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An Alarm that goes off after a certain length of time.
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class TimedAlarm extends BasicAlarm implements Alarm
{
    /**
     * @param waitTime How long we wait before the Alarm goes off
     */
    public TimedAlarm(long waitTime)
    {
        this.waitTime = waitTime;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.dwrp.PollHandler.Alarm#cancel()
     */
    public void cancel()
    {
        timer.cancel();
        super.cancel();
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.dwrp.PollHandler.Alarm#setAlarmAction(org.directwebremoting.dwrp.PollHandler.Sleeper)
     */
    public void setAlarmAction(Sleeper sleeper)
    {
        TimerTask task = new TimerTask()
        {
            public void run()
            {
                try
                {
                    raiseAlarm();
                }
                catch (Exception ex)
                {
                    log.warn("Unexpected error raising alarm", ex);
                }
            }
        };

        timer = new Timer();
        timer.schedule(task, waitTime);
        super.setAlarmAction(sleeper);
    }

    /**
     * The future result that allows us to cancel the timer
     */
    protected Timer timer;

    /**
     * How long do we wait for?
     */
    protected long waitTime;

    /**
     * The log stream
     */
    protected static final Log log = LogFactory.getLog(TimedAlarm.class);
}
