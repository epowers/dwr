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
import java.io.PrintWriter;

import org.directwebremoting.util.JavascriptUtil;
import org.directwebremoting.util.MimeConstants;

/**
 * A version of the Plain Javascript Marshaller that uses iframe syntax
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class HtmlCallMarshaller extends BaseCallMarshaller
{
    /* (non-Javadoc)
     * @see org.directwebremoting.dwrp.BaseCallMarshaller#getOutboundMimeType()
     */
    protected String getOutboundMimeType()
    {
        return MimeConstants.MIME_HTML;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.dwrp.BaseCallMarshaller#sendOutboundScriptPrefix(java.io.PrintWriter)
     */
    protected void sendOutboundScriptPrefix(PrintWriter out) throws IOException
    {
        synchronized (out)
        {
            out.println("<html><body><script type='text/javascript'>"); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.dwrp.BaseCallMarshaller#sendOutboundScriptSuffix(java.io.PrintWriter)
     */
    protected void sendOutboundScriptSuffix(PrintWriter out) throws IOException
    {
        synchronized (out)
        {
            out.println("</script></body></html>"); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.dwrp.BaseCallMarshaller#sendScript(java.io.PrintWriter, java.lang.String)
     */
    protected void sendScript(PrintWriter out, String script) throws IOException
    {
        synchronized (out)
        {
            String modScript = "window.parent.DWREngine._eval(\"" + JavascriptUtil.escapeJavaScript(script) + "\");"; //$NON-NLS-1$ //$NON-NLS-2$
            out.println(modScript);

            if (out.checkError())
            {
                throw new IOException("Error flushing buffered stream"); //$NON-NLS-1$
            }
        }
    }
}
