package com.maxiye.first;

import com.maxiye.first.util.TimeCounter;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 排序测试
 * Created by due on 2019/4/29.
 */
public class SortTests {

    @Test
    public void quikSortTest() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int[] arr = new int[100000];
        for (int i = 0;i < 100000;i++) {
            arr[i] = random.nextInt(100000);
        }
//        System.out.println(Arrays.toString(arr));
        int[] arr2 = Arrays.copyOf(arr, arr.length);
        TimeCounter.run(() -> quickSort0(arr, 0, 999));
//        System.out.println(Arrays.toString(arr));
        TimeCounter.run(() -> quickSort(arr2, 0, 999));
//        System.out.println(Arrays.toString(arr));

    }

    /**
     * 快排
     * @param arr Array
     * @param left int
     * @param right int
     */
    private static void quickSort(int[] arr, int left, int right) {
        if (left >= right) return;
        int i = left, j = right + 1, cmp = arr[left];
        while (true) {
            while (arr[++i] <= cmp) {
                if (i == right) break;
            }
            while (arr[--j] >= cmp) {
                if (j == left) break;
            }
            if (i >= j) break;
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        arr[left] = arr[j];
        arr[j] = cmp;
        quickSort(arr, left, j - 1);
        quickSort(arr, j + 1, right);
    }

    /**
     * 快排
     * @param arr Array
     * @param left int
     * @param right int
     */
    private static void quickSort0(int[] arr, int left, int right) {
        if (left >= right)
            return;
        int i = left, j = right, tmp = arr[left];
        while (i < j) {
            while (j > i && arr[j] >= tmp) {
                --j;
            }
            if (j > i) {
                arr[i] = arr[j];
            }
            while (i < j && arr[i] <= tmp) {
                ++i;
            }
            if (i < j) {
                arr[j] = arr[i];
            }
        }
        arr[i] = tmp;
        quickSort0(arr, left, i - 1);
        quickSort0(arr, i + 1, right);
    }
}
