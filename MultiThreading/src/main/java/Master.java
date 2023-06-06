import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class Master {
    private HashMap<String, Integer> freq;

    public Master(String input) throws InterruptedException {
        LinkedBlockingQueue<Reducer.WordCountPair>[] queues = new LinkedBlockingQueue[8];
        Reducer[] reducers = new Reducer[8];
        for (int i = 0; i < 8; i++) {
            queues[i] = new LinkedBlockingQueue<>();
            reducers[i] = new Reducer(queues[i]);
            reducers[i].setDaemon(true);
            reducers[i].start();
        }

        Mapper[] mappers = new Mapper[8];
        String[] lines = input.split("\n+");
        for (int i = 0; i < lines.length; i++) {
            Mapper mapper = mappers[i & 7];
            if (mapper != null) {
                mapper.join();
                Set<Map.Entry<String, Integer>> freq = mapper.getWordsFrequency().entrySet();
                for (Map.Entry<String, Integer> entry : freq) {
                    int hsh = entry.getKey().hashCode();
                    queues[hsh & 7].add(new Reducer.WordCountPair(entry.getKey(), entry.getValue()));
                }
            }
            mapper = new Mapper(lines[i]);
            mapper.setDaemon(true);
            mapper.start();
            mappers[i & 7] = mapper;
        }
        for (int i = 0; i < 8; i++) {
            Mapper mapper = mappers[i];
            if (mapper != null) {
                mapper.join();
                Set<Map.Entry<String, Integer>> freq = mapper.getWordsFrequency().entrySet();
                for (Map.Entry<String, Integer> entry : freq) {
                    int hsh = entry.getKey().hashCode();
                    queues[hsh & 7].add(new Reducer.WordCountPair(entry.getKey(), entry.getValue()));
                }
            }
        }


        LinkedBlockingQueue<Reducer.WordCountPair> mergerQueue = new LinkedBlockingQueue<>();
        Reducer merger = new Reducer(mergerQueue);
        merger.setDaemon(true);
        merger.start();

        for (int i = 0; i < 8; i++) {
            queues[i].add(new Reducer.WordCountPair(null, 0));
            reducers[i].join();
            Set<Map.Entry<String, Integer>> freq = reducers[i].getWordsFrequency().entrySet();
            for (Map.Entry<String, Integer> entry : freq) {
                mergerQueue.add(new Reducer.WordCountPair(entry.getKey(), entry.getValue()));
            }
        }

        mergerQueue.add(new Reducer.WordCountPair(null, 0));
        merger.join();
        freq = merger.getWordsFrequency();
    }

    public HashMap<String, Integer> getWordsFrequency() {
        return freq;
    }
}
