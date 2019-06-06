package com.maxiye.first.util;

import java.util.Arrays;

/**
 * 可变int数组类
 * Created by zyl on 2019/6/4.
 * @author zyl
 */
@SuppressWarnings("unused")
public class IntList {
    private int[] mIntArr;
    private int postion = 0;
    private int capacity = 10;
    public IntList() {
        mIntArr = new int[10];
    }

    public IntList(int capacity) {
        this.capacity = capacity;
        mIntArr = new int[capacity];
    }

    public void add(int e) {
        if (postion >= capacity) {
            capacity += (capacity >> 1);
            mIntArr = Arrays.copyOf(mIntArr, capacity);
        }
        mIntArr[postion++] = e;
    }

    public int get(int pos) {
        return mIntArr[pos];
    }

    public int pop() {
        if (postion > 0) {
            int e = mIntArr[postion];
            mIntArr[postion--] = 0;
            return e;
        }
        throw new ArrayIndexOutOfBoundsException("No more item");
    }

    public int set(int pos, int e) {
        if (postion > pos) {
            int old = mIntArr[pos];
            mIntArr[pos] = e;
            return old;
        } else if (postion == pos) {
            mIntArr[postion++] = e;
            return 0;
        } else {
            throw new ArrayIndexOutOfBoundsException("index err");
        }
    }

    public int[] toArray() {
        return postion > 0 ? Arrays.copyOf(mIntArr, postion) : new int[1];
    }
}
