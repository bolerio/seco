/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package seco.langs.javascript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;


/**
 * This represents a path in a JRuby AST.
 *
 * @todo Performance: Make a cache here since I tend to do AstPath(caretOffset) from
 *  several related services for a single parser result
 * 
 * @author Tor Norbye
 */
public class AstPath implements Iterable<Node> {
    private ArrayList<Node> path = new ArrayList<Node>(30);

    public AstPath() {
    }
        
    public AstPath(AstPath other) {
        path.addAll(other.path);
    }

    public AstPath(ArrayList<Node> path) {
        this.path = path;
    }

    /**
     * Initialize a node path to the given caretOffset
     */
    public AstPath(Node root, int caretOffset) {
        findPathTo(root, caretOffset);
    }

    /**
     * Find the path to the given node in the AST
     */
    @SuppressWarnings("unchecked")
    public AstPath(Node node, Node target) {
        if (!find(node, target)) {
            path.clear();
        } else {
            // Reverse the list such that node is on top
            // When I get time rewrite the find method to build the list that way in the first place
            Collections.reverse(path);
        }
    }

    public void descend(Node node) {
        path.add(node);
    }

    public void ascend() {
        path.remove(path.size() - 1);
    }
    
    /**
     * Return true iff this path contains a node of the given node type
     * 
     * @param nodeType The nodeType to check
     * @return true if the given nodeType is found in the path
     */
    public boolean contains(int nodeType) {
        for (int i = 0, n = path.size(); i < n; i++) {
            if (path.get(i).getType() == nodeType) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Find the position closest to the given offset in the AST. Place the path from the leaf up to the path in the
     * passed in path list.
     */
    @SuppressWarnings("unchecked")
    public Node findPathTo(Node node, int offset) {
        Node result = find(node, offset);
        path.add(node);

        // Reverse the list such that node is on top
        // When I get time rewrite the find method to build the list that way in the first place
        Collections.reverse(path);

        return result;
    }

    @SuppressWarnings("unchecked")
    private Node find(Node node, int offset) {
        int begin = node.getSourceStart();
        int end = node.getSourceEnd();

        if ((offset >= begin) && (offset <= end)) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                Node found = find(child, offset);

                if (found != null) {
                    path.add(child);

                    return found;
                }
            }

            return node;
        } else {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                Node found = find(child, offset);

                if (found != null) {
                    path.add(child);

                    return found;
                }
            }

            return null;
        }
    }

    /**
     * Find the path to the given node in the AST
     */
    @SuppressWarnings("unchecked")
    public boolean find(Node node, Node target) {
        if (node == target) {
            return true;
        }

        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            boolean found = find(child, target);

            if (found) {
                path.add(child);

                return found;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Path(");
        sb.append(path.size());
        sb.append(")=[");

        for (Node n : path) {
            String name = Token.fullName(n.getType());
            name = name.substring(name.lastIndexOf('.') + 1);
            sb.append(name);
            sb.append(":");
        }

        sb.append("]");

        return sb.toString();
    }

    public Node leaf() {
        if (path.size() == 0) {
            return null;
        } else {
            return path.get(path.size() - 1);
        }
    }

    public Node leafParent() {
        if (path.size() < 2) {
            return null;
        } else {
            return path.get(path.size() - 2);
        }
    }

    public Node leafGrandParent() {
        if (path.size() < 3) {
            return null;
        } else {
            return path.get(path.size() - 3);
        }
    }

    public Node root() {
        if (path.size() == 0) {
            return null;
        } else {
            return path.get(0);
        }
    }

    /** Return an iterator that returns the elements from the leaf back up to the root */
    public Iterator<Node> iterator() {
        return new LeafToRootIterator(path);
    }

    /** REturn an iterator that starts at the root and walks down to the leaf */
    public ListIterator<Node> rootToLeaf() {
        return path.listIterator();
    }

    /** Return an iterator that walks from the leaf back up to the root */
    public ListIterator<Node> leafToRoot() {
        return new LeafToRootIterator(path);
    }

    private static class LeafToRootIterator implements ListIterator<Node> {
        private final ListIterator<Node> it;

        private LeafToRootIterator(ArrayList<Node> path) {
            it = path.listIterator(path.size());
        }

        public boolean hasNext() {
            return it.hasPrevious();
        }

        public Node next() {
            return it.previous();
        }

        public boolean hasPrevious() {
            return it.hasNext();
        }

        public Node previous() {
            return it.next();
        }

        public int nextIndex() {
            return it.previousIndex();
        }

        public int previousIndex() {
            return it.nextIndex();
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void set(Node arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void add(Node arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
