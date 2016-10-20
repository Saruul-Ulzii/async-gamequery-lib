/***************************************************************************************************
 * MIT License
 *
 * Copyright (c) 2016 Rafael Ibasco
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **************************************************************************************************/

package com.ribasco.rglib.core;

import com.ribasco.rglib.core.exceptions.ReadTimeoutException;
import com.ribasco.rglib.core.session.SessionId;
import com.ribasco.rglib.core.session.SessionManager;
import com.ribasco.rglib.core.session.SessionValue;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by raffy on 9/20/2016.
 */
public class ReadRequestTimeoutTimerTask implements TimerTask {
    private static final Logger log = LoggerFactory.getLogger(ReadRequestTimeoutTimerTask.class);
    private SessionId id;
    private SessionManager sessionManager;

    public ReadRequestTimeoutTimerTask(SessionId sessionId, SessionManager sessionManager) {
        this.id = sessionId;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        log.debug("Timeout occured for Session {}", id);
        //Notify the listener that timeout has occured
        final SessionValue session = sessionManager.getSession(id);

        //Do not proceed if the session is null
        if (session == null) {
            log.error("could not find session value for id {}. Registry Size : {}", id, sessionManager.getSessionEntries().size());
            return;
        }

        //Check first if the promise has been completed
        if (session.getClientPromise() != null && !session.getClientPromise().isCompletedExceptionally()) {
            //Send a ReadTimeoutException to the client
            boolean called = session.getClientPromise().completeExceptionally(new ReadTimeoutException(sessionManager.getSessionIdFactory().duplicate(id), String.format("Timeout occured for '%s'", id)));
        }
    }
}
