import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Reducer extends Thread {
    private final LinkedBlockingQueue<WordCountPair> queue;
    private final HashMap<String, Integer> freq = new HashMap<>();

    public Reducer(LinkedBlockingQueue<WordCountPair> input) {
        queue = input;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            if (queue.isEmpty()) continue;
            WordCountPair pair = queue.poll();
            if (pair.word == null) break;
            int cnt = freq.getOrDefault(pair.word, 0);
            freq.put(pair.word, cnt + pair.count);
        }
    }

    public HashMap<String, Integer> getWordsFrequency() {
        return freq;
    }

    public static class WordCountPair {
        private String word;
        private Integer count;

        public WordCountPair(String word, Integer count) {
            this.word = word;
            this.count = count;
        }

        public String getWord() {
            return word;
        }

        public Integer getCount() {
            return count;
        }
    }
}