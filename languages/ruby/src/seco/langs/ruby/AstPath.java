package seco.langs.ruby; 

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jruby.ast.Node;
import org.jruby.lexer.yacc.ISourcePosition;


/**
 * This represents a path in a JRuby AST.
 *
 * @author Tor Norbye
 */
public class AstPath implements Iterable<Node> {
    private ArrayList<Node> path = new ArrayList<Node>(30);

    public AstPath() {
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
        ISourcePosition pos = node.getPosition();
        int begin = pos.getStartOffset();
        int end = pos.getEndOffset();

        if ((offset >= begin) && (offset <= end)) {
            List<Node> children = (List<Node>)node.childNodes();

            for (Node child : children) {
                Node found = find(child, offset);

                if (found != null) {
                    path.add(child);

                    return found;
                }
            }

            return node;
        } else {
            List<Node> children = (List<Node>)node.childNodes();

            for (Node child : children) {
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

        List<Node> children = (List<Node>)node.childNodes();

        for (Node child : children) {
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
            String name = n.getClass().getName();
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
        return new LeafToRootIterator();
    }

    /** REturn an iterator that starts at the root and walks down to the leaf */
    public ListIterator<Node> rootToLeaf() {
        return path.listIterator();
    }

    /** Return an iterator that walks from the leaf back up to the root */
    public ListIterator<Node> leafToRoot() {
        return new LeafToRootIterator();
    }

    private class LeafToRootIterator implements ListIterator<Node> {
        private ListIterator<Node> it;

        private LeafToRootIterator() {
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

