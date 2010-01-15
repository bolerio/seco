/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package seco.notebook.javascript;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache which performs type lookup etc. for functions
 * 
 * @author Tor Norbye
 */
public class FunctionCache
{
    public static final FunctionCache INSTANCE = new FunctionCache();
    static final String NONE = new String("NONE");

    Map<String, String> cache = new HashMap<String, String>(500);

    public String getType(String fqn)
    {
        String type = cache.get(fqn);
        if (type == NONE)
        {
            return null;
        }
        else if (type == null)
        {
            // Special case checks
            if (fqn.endsWith("Element.getContext"))
            { // NOI18N
                // Probably a call on the HTMLCanvasElement
                // TODO - check args - see if it's passing in "2d" etc.
                // At least see if it's an element...
                return "CanvasRenderingContext2D"; // NOI18N
            }

            cache.put(fqn, NONE);
            return null;

        }

        return type;
    }

    public void wipe(String fqn)
    {
        cache.remove(fqn);
    }

    boolean isEmpty()
    {
        return cache.size() == 0;
    }
}
