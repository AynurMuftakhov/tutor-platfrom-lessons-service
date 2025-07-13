package com.mytutorplatform.lessonsservice.controller;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SimpleTest {
    @Test
    public void test() {
        Map<Node, Integer> map = new HashMap<>();

        map.put(new Node(1), 1);
        map.put(new Node(2), 2);
        map.put(new Node(3), 2);
        map.put(new Node(4), 2);
        map.put(new Node(5), 2);
        map.put(new Node(6), 2);
        map.put(new Node(7), 2);
        map.put(new Node(8), 2);

        while (true){
           //do nothing
        }

    }

    class Node {
        int val;

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            return this.val == ((Node) obj).val;
        }

        public Node(int val) {
            this.val = val;
        }
        public int getVal() {return val;}
    }
}
