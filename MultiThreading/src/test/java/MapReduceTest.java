import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

public class MapReduceTest {
    @Test
    public void testMaster() throws InterruptedException {
        Master master = new Master("hello world");
        HashMap<String, Integer> result = master.getWordsFrequency();
        assertEquals(2, result.size());
        assertTrue(result.containsKey("hello"));
        assertTrue(result.containsKey("world"));
    }

    @Test
    public void testMultipleLines() throws InterruptedException {
        Master master = new Master("hello world\nworld hello");
        HashMap<String, Integer> result = master.getWordsFrequency();
        assertEquals(2, result.size());
        assertTrue(result.containsKey("hello"));
        assertTrue(result.containsKey("world"));
        assertEquals(2, (int) result.get("hello"));
        assertEquals(2, (int) result.get("world"));
    }

    @Test
    public void testEmptyInput() throws InterruptedException {
        Master master = new Master("");
        HashMap<String, Integer> result = master.getWordsFrequency();
        assertEquals(0, result.size());
    }

    @Test
    public void testNullInput() {
        try {
            Master master = new Master(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException | InterruptedException e) {
            // expected
        }
    }

    @Test
    public void testReducer() throws InterruptedException {
        LinkedBlockingQueue<Reducer.WordCountPair> queue = new LinkedBlockingQueue<>();
        Reducer reducer = new Reducer(queue);
        reducer.start();
        queue.add(new Reducer.WordCountPair("hello", 5));
        queue.add(new Reducer.WordCountPair("hello", 4));
        queue.add(new Reducer.WordCountPair("world", 5));
        queue.add(new Reducer.WordCountPair(null, 0));
        reducer.join();
        HashMap<String, Integer> result = reducer.getWordsFrequency();
        assertTrue(result.containsKey("hello"));
        assertTrue(result.containsKey("world"));
        assertEquals(9, (int) result.get("hello"));
        assertEquals(5, (int) result.get("world"));
    }

    @Test
    public void testMapper() throws InterruptedException {
        Mapper mapper = new Mapper("hello world");
        mapper.start();
        mapper.join();
        HashMap<String, Integer> result = mapper.getWordsFrequency();
        assertEquals(2, result.size());
        assertTrue(result.containsKey("hello"));
        assertTrue(result.containsKey("world"));
    }

    @Test
    public void testMultipleMappers() throws InterruptedException {
        Mapper mapper1 = new Mapper("hello hello world");
        Mapper mapper2 = new Mapper("hi there");
        mapper1.start();
        mapper2.start();
        mapper1.join();
        mapper2.join();
        HashMap<String, Integer> result1 = mapper1.getWordsFrequency();
        HashMap<String, Integer> result2 = mapper2.getWordsFrequency();
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());
        assertTrue(result1.containsKey("hello"));
        assertEquals(2, (int) result1.get("hello"));
        assertTrue(result1.containsKey("world"));
        assertTrue(result2.containsKey("hi"));
        assertTrue(result2.containsKey("there"));
    }

    @Test
    public void testMultipleMappersWithOverlap() throws InterruptedException {
        Master master = new Master("hello world\nhi there world");

        HashMap<String, Integer> result = master.getWordsFrequency();
        assertEquals(4, result.size());
        assertTrue(result.containsKey("hello"));
        assertTrue(result.containsKey("world"));
        assertTrue(result.containsKey("hi"));
        assertTrue(result.containsKey("there"));
        assertEquals(2, (int) result.get("world"));
    }

    @Test
    public void testEmptyLine() throws InterruptedException {
        Master master = new Master("\n");
        HashMap<String, Integer> result = master.getWordsFrequency();
        assertEquals(0, result.size());
    }

    @Test
    public void testmultipleEmptyLines() throws InterruptedException {
        Master master = new Master("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        HashMap<String, Integer> result = master.getWordsFrequency();
        assertEquals(0, result.size());
    }
}
