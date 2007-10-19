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
package org.directwebremoting.drapgen.xslt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.directwebremoting.drapgen.Generate;

/**
 * Functions to make up for the inadequacies of XSTL
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class ExtensionFunctions
{
    /**
     * Trim the string by removing whitespace from the start and end of the
     * string.
     * @see java.lang.String#trim()
     * @param source The string to trim
     * @return The trimmed version of the input string
     */
    public static String trim(String source)
    {
        return source.trim();
    }

    /**
     * Lookup the class in the class cache to detect if has been marked as
     * having a children.
     * @param className The name of the class to check for children
     * @return true if the named class is a superclass
     */
    public static boolean isSuperClass(String className)
    {
        return Generate.sources.get(className).isSuperClass();
    }

    /**
     * Convert a jsx3 classname into a Java classname
     * @param original The name of the jsx3 class
     * @return The name of a comparable Java class
     */
    public static String normalizeClassname(String original)
    {
        for (String[] swap : swaps)
        {
            Pattern pattern = Pattern.compile(swap[SEARCH], Pattern.UNIX_LINES);
            Matcher matcher = pattern.matcher(original);
            if (matcher.find())
            {
                return matcher.replaceAll(swap[REPLACE]);
            }
        }

        return original;
    }

    private static final int SEARCH = 0;
    private static final int REPLACE = 1;
    private static final String[][] swaps = new String[][]
    {
        { "^Number$", "int" },
        { "^jsx3\\.lang\\.Class$", "Class" },
        { "^Array$", "Object[]" },
        { "^Function$", "org.directwebremoting.proxy.CodeBlock" },
        { "^jsx3\\.app\\.Properties$", "java.util.Properties" },
        { "^jsx3\\.lang\\.Exception$", "Exception" },
        { "^jsx3\\.Boolean$", "Boolean" },
        { "^HTMLElement$", "String" },
        { "^jsx3\\.lang\\.IllegalArgumentException$", "IllegalArgumentException" },
        { "^jsx3\\.(.*)$", "org.directwebremoting.proxy.jsx3.$1" },
    };
}