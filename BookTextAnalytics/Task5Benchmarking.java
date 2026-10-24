
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Task5Benchmarking {

    // Stores a word, its frequency, and the next entry.
    static class WordEntry {
        String word;
        int count;
        WordEntry next;

        WordEntry(String word) {
            this.word = word;
            this.count = 1;
            this.next = null;
        }
    }

    // Custom hash table for storing word frequencies.
    static class CustomHashTable {
        private WordEntry[] table;
        private int capacity;
        private int uniqueWords;

        CustomHashTable(int capacity) {
            this.capacity = capacity;
            table = new WordEntry[capacity];
            uniqueWords = 0;
        }

        // Calculates the array index for a word.
        private int hash(String word) {
            return Math.floorMod(word.hashCode(), capacity);
        }

        // Adds a new word or increases its frequency.
        public void addWord(String word) {
            int index = hash(word);
            WordEntry current = table[index];

            // Check whether the word already exists.
            while (current != null) {
                if (current.word.equals(word)) {
                    current.count++;
                    return;
                }
                current = current.next;
            }

            // Add a new entry if the word is not found.
            WordEntry newEntry = new WordEntry(word);
            newEntry.next = table[index];
            table[index] = newEntry;
            uniqueWords++;
        }

        // Returns the frequency of a word.
        public int getCount(String word) {
            int index = hash(word);
            WordEntry current = table[index];

            while (current != null) {
                if (current.word.equals(word)) {
                    return current.count;
                }
                current = current.next;
            }

            return 0;
        }

        // Returns the number of different words.
        public int getUniqueWords() {
            return uniqueWords;
        }
    }

    public static void main(String[] args) {

        String filePath = "BookTextAnalytics/alice.txt";

        // Create a hash table with 1009 slots.
        CustomHashTable table = new CustomHashTable(1009);

        // Start measuring book processing time.
        long startTime = System.nanoTime();

        int totalWords = 0;

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(filePath))) {

            String line;

            // Read the book one line at a time.
            while ((line = reader.readLine()) != null) {

                // Convert to lowercase and remove punctuation.
                String cleaned = line.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", " ");

                String trimmed = cleaned.trim();

                if (trimmed.isEmpty()) {
                    continue;
                }

                // Split the line into individual words.
                String[] words = trimmed.split("\\s+");

                for (int i = 0; i < words.length; i++) {
                    if (!words[i].isEmpty()) {
                        table.addWord(words[i]);
                        totalWords++;
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading book: " + e.getMessage());
            return;
        }

        // Calculate the total book processing time.
        long endTime = System.nanoTime();

        double processingTimeMs =
            (endTime - startTime) / 1_000_000.0;

        String searchWord = "alice";
        int repetitions = 100000;

        // Measure the time for repeated word searches.
        long searchStart = System.nanoTime();

        int result = 0;

        for (int i = 0; i < repetitions; i++) {
            result = table.getCount(searchWord);
        }

        long searchEnd = System.nanoTime();

        double searchTimeMs =
            (searchEnd - searchStart) / 1_000_000.0;

        // Display benchmark results.
        System.out.println("===== TASK 5: BENCHMARKING =====");
        System.out.println("Total words: " + totalWords);
        System.out.println(
            "Unique words: " + table.getUniqueWords());

        System.out.println(
            "Book processing time: "
            + processingTimeMs + " ms");

        System.out.println(
            "Word searched: " + searchWord);

        System.out.println(
            "Occurrences of " + searchWord + ": " + result);

        System.out.println(
            "Number of searches: " + repetitions);

        System.out.println(
            "Total search time: " + searchTimeMs + " ms");

        System.out.println(
            "Average search time: "
            + (searchTimeMs / repetitions) + " ms");

        // Display the time and space complexity report.
        System.out.println("\n===== COMPLEXITY REPORT =====");

        System.out.println(
            "Adding a word: Average O(1), worst-case O(n)");

        System.out.println(
            "Searching for a word: Average O(1), worst-case O(n)");

        System.out.println(
            "Processing the book: Approximately O(n)");

        System.out.println(
            "Space complexity: O(u + n) for this implementation");

        System.out.println(
            "n = total words; u = unique words");
    }
}
