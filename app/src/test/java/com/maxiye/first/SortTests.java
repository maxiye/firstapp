package com.maxiye.first;

import com.maxiye.first.util.TimeCounter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

    @Test
    public void cusSortTest() {
        ArrayList<Map<String, String>> list = new ArrayList<>(15);
        for (int i = 1; i < 16; i++){
            HashMap<String, String> map = new HashMap<>(2);
            map.put("id", String.valueOf(i));
            list.add(map);
        }
        String[] sort = new String[]{"1", "13", "2", "3", "9", "14", "4", "10", "5", "6", "15", "12", "7", "8", "11", "2", "8"};
        for (int i = 0, index = 0; i < sort.length; i++) {
            for (int j = index; j < list.size(); j++) {
                if (sort[i].equals(list.get(j).get("id"))) {
                    Map<String, String> tmp = list.set(index++, list.get(j));
                    list.set(j, tmp);
                    break;
                }
            }
        }
        System.out.println(list);
    }

    @Test
    public void copyRangeTest() {
        int[] ints = new int[]{1,2,3,4,5,6,7};//
        int off = 1, len = 7;
        int to = ints.length >= off + len ? len + off : ints.length;
        System.out.println(Arrays.toString(Arrays.copyOfRange(ints, 1, 7)));
    }
}
