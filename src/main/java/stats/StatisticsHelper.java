package stats;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class StatisticsHelper {

    private final Queue<Integer> minHeap;
    private final Queue<Integer> maxHeap;
    private final int[] array;

    public StatisticsHelper(int[] array) {
        this.array = array;
        minHeap = new PriorityQueue<>();
        maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
    }

    public double getMedian() {
        for (int i: array){
            if (!minHeap.isEmpty() && i < minHeap.peek()) {
                maxHeap.offer(i);
                if (maxHeap.size() > minHeap.size() + 1) {
                    minHeap.offer(maxHeap.poll());
                }
            } else {
                minHeap.offer(i);
                if (minHeap.size() > maxHeap.size() + 1) {
                    maxHeap.offer(minHeap.poll());
                }
            }
        }
        int median;
        if (minHeap.size() < maxHeap.size()) {
            median = maxHeap.peek();
        } else if (minHeap.size() > maxHeap.size()) {
            median = minHeap.peek();
        } else {
            median = (minHeap.peek() + maxHeap.peek()) / 2;
        }
        return median;
    }

    public int mode() {
        int maxValue = 0, maxCount = 0;

        for (int k : array) {
            int count = 0;
            for (int i : array) {
                if (i == k) ++count;
            }
            if (count > maxCount) {
                maxCount = count;
                maxValue = k;
            }
        }

        return maxValue;
    }

    public int getMax(){
        return Arrays.stream(array).max().getAsInt();
    }

    public int getMin(){
        return Arrays.stream(array).min().getAsInt();
    }

    public double getMean(){
        return Arrays.stream(array).average().getAsDouble();
    }
}
