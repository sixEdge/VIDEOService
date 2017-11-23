package com.gzf.video.core.dispatcher;

import com.gzf.video.core.controller.action.Action;

import java.util.*;

import static com.gzf.video.core.dispatcher.CustomParametersParser.CustomParameter;

class ActionMapper {

    private boolean notUseCustomParameter;

    private Node root;

    {
        root = addLeaf(new Node("", null, null, new ArrayList<>()));
        List<Node> list = new ArrayList<>(1);
        list.add(root);
        root.parent = new Node(null, null, null, list);
    }

    ActionMapper(boolean notUseRequestParameterDispatcher) {
        this.notUseCustomParameter = notUseRequestParameterDispatcher;
    }

    void put(CustomParameter customParameters, Action action) {
        String[] pSegments = customParameters.getPathSegments();
        Objects.requireNonNull(action);
        Objects.requireNonNull(pSegments);

        Node dstNode = pSegments.length == 0
                ? root
                : put0(pSegments, root, 1);

        if (notUseCustomParameter) {
            dstNode.action = action;
        } else {
            Node node;
            if (!checkReqParams(dstNode.customParameters, customParameters)) {
                node = new Node(dstNode);
                node.children = new ArrayList<>();
                dstNode.parent.children.add(node);
            } else {
                node = dstNode;
            }
            node.customParameters = customParameters;
        }
    }

    private static Node put0(final String[] pSegments, final Node currentNode, final int segIdx) {
        if (segIdx == pSegments.length) {
            return currentNode;
        }

        String seg = pSegments[segIdx];

        for (Node child : currentNode.children) {
            if (seg.equals(child.pathSegment))
                return put0(pSegments, child, segIdx + 1);
        }

        // no child match

        Node node = currentNode.children.get(0);
        if (node.pathSegment == null) {
            // child is a leaf
            node.pathSegment = seg;
        } else {
            // create a new branch
            node = new Node(seg, null, currentNode, new ArrayList<>());
            currentNode.children.add(node);
        }

        return put0(pSegments, addLeaf(node), segIdx + 1);
    }

    private static Node addLeaf(final Node node) {
        node.children.add(new Node(null, null, node, new ArrayList<>()));
        return node;
    }




    Action get(final String path, final Map<String, List<String>> parameters) {
        String[] pSegment = path.split("/");
        return notUseCustomParameter
                ? get1(pSegment, root, 0)
                : get0(pSegment, parameters, root, 0);
    }

    private static Action get0(final String[] pSegments,
                               final Map<String, List<String>> parameters,
                               final Node currentNode,
                               final int segIdx) {
        Action action;
        String seg = currentNode.pathSegment;
        if (pSegments.length == segIdx) {
            return checkReqParams(currentNode.parent.customParameters, parameters)
                    ? currentNode.parent.action
                    : null;
        } else if ((!seg.isEmpty() && seg.charAt(0) == '{') || pSegments[segIdx].equals(seg)) {
            int plusSegIdx = segIdx + 1;
            for (Node childNode : currentNode.children) {
                action = get0(pSegments, parameters, childNode, plusSegIdx);
                if (action != null) {
                    return action;
                }
            }
        }
        return null;
    }

    private static Action get1(final String[] pSegments, final Node currentNode, final int segIdx) {
        Action action;
        if (pSegments.length == segIdx) {
            return currentNode.parent.action;
        } else if (pSegments[segIdx].equals(currentNode.pathSegment)) {
            for (Node childNode : currentNode.children) {
                action = get1(pSegments, childNode, segIdx + 1);
                if (action != null) {
                    return action;
                }
            }
        }
        return null;
    }


    boolean isConflicting(final CustomParameter cp) {
        return isConflicting0(cp.getPathSegments(), cp, root, 0) != null;
    }


    private CustomParameter isConflicting0(final String[] pSegments,
                                           final CustomParametersParser.CustomParameter cp,
                                           final Node currentNode,
                                           final int segIdx) {
        String seg = currentNode.pathSegment;
        if (pSegments.length == segIdx) {
            return notUseCustomParameter || checkReqParams(currentNode.parent.customParameters, cp)
                    ? currentNode.parent.customParameters
                    : null;
        } else if (seg != null
                && (!seg.isEmpty() && seg.charAt(0) == '{')
                || pSegments[segIdx].equals(seg)) {
            CustomParameter dstCp;
            for (Node childNode : currentNode.children) {
                dstCp = isConflicting0(pSegments, cp, childNode, segIdx + 1);
                if (dstCp != null && (notUseCustomParameter || checkReqParams(dstCp, cp))) {
                    return cp;
                }
            }
        }
        return null;
    }

    private boolean checkReqParams(final CustomParameter dstCp, final CustomParameter cp) {
        Objects.requireNonNull(cp);
        if (dstCp == null) {
            return false;
        }
        final Map<String, String> p = dstCp.getRequestParameters();
        final Map<String, Class> c = dstCp.getCustomRequestParameters();

        final Map<String, String> params = cp.getRequestParameters();
        final Map<String, Class> customParams = cp.getCustomRequestParameters();

        if (p.size() != params.size() || c.size() != customParams.size()) {
            return false;
        }

        for (Map.Entry<String, String> e : p.entrySet()) {
            if (!e.getValue().equals(params.get(e.getKey()))) {
                return false;
            }
        }

        for (Map.Entry<String, Class> e : c.entrySet()) {
            if (!e.getValue().equals(customParams.get(e.getKey()))) {
                return false;
            }
        }

        return true;
    }

    private static boolean checkReqParams(final CustomParameter dstCp,
                                   final Map<String, List<String>> params) {
        final Map<String, String> p = dstCp.getRequestParameters();
        final Map<String, Class> c = dstCp.getCustomRequestParameters();

        if (p.size() + c.size() != params.size()) {
            return false;
        }

        for (Map.Entry<String, String> e : p.entrySet()) {
            List<String> l = params.get(e.getKey());
            if (l.size() != 1 || !e.getValue().equals(l.get(0))) {
                return false;
            }
        }

        for (Map.Entry<String, Class> e : c.entrySet()) {
            List<String> l = params.get(e.getKey());
            if (l == null) {
                return false;
            }
        }

        return true;
    }


    private static class Node {
        String pathSegment;
        CustomParameter customParameters;
        Node parent;
        List<Node> children;

        Action action;

        private Node(final String pathSegment, final CustomParameter customParameters,
                     final Node parent, final List<Node> children) {
            this.pathSegment = pathSegment;
            this.customParameters = customParameters;
            this.parent = parent;
            this.children = children;
        }

        private Node(Node node) {
            this.pathSegment = node.pathSegment;
            this.customParameters = node.customParameters;
            this.parent = node.parent;
            this.children = node.children;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "pathSegment=" + pathSegment +
                    ", customParameters=" + customParameters +
                    ", children=" + children +
                    ", action=" + action +
                    '}';
        }
    }
}
