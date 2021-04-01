package structure;

import java.util.*;

public class SkipList<T>  {
    private enum NodeType {
        /**
         * 头节点
         */
        HEAD,
        /**
         * 中间节点
         */
        MIDDLE,
        /**
         * 尾节点
         */
        TAIL;
    }

    private class Node {
        private final T val;
        private Node right = null;
        private Node down = null;
        private Node up = null;
        private Node left = null;
        private final NodeType nodeType;

        public Node(T val, NodeType nodeType) {
            this.val = val;
            this.nodeType = nodeType;
        }
    }

    private static final int ZERO = 0;

    private final int LEVEL_NUM;

    private final Comparator<T> comparator;

    private long size;

    private List<Node> heads;

    public static <E> SkipList<E> newSkipList(int totalLevel, Comparator<E> comparator) {
        return new SkipList<>(totalLevel, comparator);
    }

    private SkipList(int totalLevel, Comparator<T> comparator) {
        this.LEVEL_NUM = totalLevel;
        this.comparator = comparator;
        heads = new ArrayList<>();

        for (int i = 0; i < LEVEL_NUM; i++) {
            Node head = new Node(null, NodeType.HEAD);
            head.right = new Node(null, NodeType.TAIL);
            head.left = head;
            heads.add(head);
            if (i >= 1) {
                heads.get(i - 1).down = head;
            }
        }
    }

    public boolean find(T target) {
        Node current = this.heads.get(0);
        while (current != null) {
            if (compare(current, target) < ZERO && compare(current.right, target) >= ZERO) {
                if (compare(current.right, target) == ZERO) {
                    return true;
                }
                current = current.down;
            } else {
                current = current.right;
            }
        }
        return false;
    }

    /**
     * 删除值小于等于target的所有节点
     * @param target
     * @return
     */
    public List<T> eraseLessThanOrEqualTo(T target) {
        Node first = null;
        int lv = 0;
        Node current = heads.get(lv);
        while (current != null) {
            if (compare(current, target) <= ZERO && compare(current.right, target) > ZERO) {
                first = heads.get(lv).right;
                heads.get(lv).right = current.right;
                current.right.left = heads.get(lv);
                current = current.down;
                lv += 1;
            } else {
                current = current.right;
            }
        }

        List<T> ans = new ArrayList<>();
        while (compare(first, target) <= 0) {
            ans.add(first.val);
            first = first.right;
        }
        size -= ans.size();
        return ans;
    }

    public void add(T val) {
        Stack<Node> stack = new Stack<>();
        Node current = this.heads.get(0);
        while (current != null) {
            if (compare(current, val) < ZERO && compare(current.right, val) >= ZERO) {
                stack.push(current);
                current = current.down;
            } else {
                current = current.right;
            }
        }

        Node prev = null;
        while (!stack.isEmpty()) {
            current = stack.pop();
            Node node = new Node(val, NodeType.MIDDLE);
            Node right = current.right;
            current.right = node;
            node.right = right;

            node.left = current;
            right.left = node;

            node.down = prev;
            prev = node;

            Random rand = new Random();
            if (rand.nextInt(2) == ZERO) {
                break;
            }
        }
        this.size++;
    }

    public int erase(T target) {
        int deleted = 0;
        Node current = this.heads.get(0);
        while (current != null) {
            if (compare(current, target) < ZERO && compare(current.right, target) >= ZERO) {
                deleted = 0;
                Node tmp = current;
                while (compare(tmp.right, target) == ZERO) {
                    tmp.right = tmp.right.right;
                    tmp.right.left = tmp;
                    deleted += 1;
                }
                current = current.down;
            } else {
                current = current.right;
            }
        }

        this.size -= deleted;
        return deleted;
    }


    public List<T> getAll() {
        List<T> ans = new LinkedList<>();
        Node head = this.heads.get(LEVEL_NUM - 1);
        while (head.right.nodeType != NodeType.TAIL) {
            ans.add(head.right.val);
            head = head.right;
        }
        return ans;
    }

    public long size() {
        return this.size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        for (int i = 0; i < heads.size(); i++) {
            Node current = heads.get(i);
            while (current.nodeType != NodeType.TAIL) {
                sb.append(current.nodeType == NodeType.HEAD ? current.nodeType.name() : current.val).append(" -> ");
                current = current.right;
            }
            sb.append(current.nodeType.name());
            sb.append("\n");
        }
        return sb.toString();
    }

    private int compare(Node node, T target) {
        switch (node.nodeType) {
            case HEAD:
                return -1;
            case TAIL:
                return 1;
            default:
                return this.comparator.compare(node.val, target);
        }
    }
}
