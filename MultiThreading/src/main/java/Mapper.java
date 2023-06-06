import java.util.HashMap;

public class Mapper extends Thread {
    private final String input;
    private final HashMap<String, Integer> freq = new HashMap<>();

    public Mapper(String input) {
        this.input = input;
    }

    public HashMap<String, Integer> getWordsFrequency() {
        return freq;
    }

    @Override
    public void run() {
        super.run();
        String[] list = input.split("\\s+");
        for (String str : list) {
            if (str.isEmpty()) continue;
            int cnt = freq.getOrDefault(str, 0);
            freq.put(str, cnt + 1);
        }
    }
}