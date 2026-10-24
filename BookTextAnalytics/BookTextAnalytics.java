
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class BookTextAnalytics {

    public static void main(String[] args) throws IOException {

        // STEP 1: Load the book.
        File file = new File("BookTextAnalytics/alice.txt");
        Scanner sc = new Scanner(file);

        StringBuilder textBuilder = new StringBuilder();

        while (sc.hasNextLine()) {
            textBuilder.append(sc.nextLine()).append(" ");
        }

        sc.close();

        String text = textBuilder.toString();

        System.out.println("Book loaded successfully!");
        System.out.println("Total characters: " + text.length());

        // STEP 2: Convert all text to lowercase.
        text = text.toLowerCase();

        System.out.println("Lowercase text preview:");
        System.out.println(
            text.substring(0, Math.min(200, text.length()))
        );

        // STEP 3: Replace punctuation with spaces.
        // Keep letters, digits, and whitespace.
        text = text.replaceAll("[^a-z0-9\\s]", " ");

        // Handle an empty book correctly.
        String[] words;

        if (text.trim().isEmpty()) {
            words = new String[0];
        } else {
            words = text.trim().split("\\s+");
        }

        // STEP 4: Store all words in our custom dynamic array.
        DynamicArray bookWords = new DynamicArray();

        for (String word : words) {
            if (!word.isEmpty()) {
                bookWords.add(word);
            }
        }

        // Total words include duplicates.
        System.out.println("Total words: " + bookWords.size());

        // STEP 5: Count word frequencies using our custom hash table.
        WordFrequencyHashTable frequency =
                new WordFrequencyHashTable();

        for (int i = 0; i < bookWords.size(); i++) {
            frequency.increment(bookWords.get(i));
        }

        // Each distinct word has one entry in the hash table.
        System.out.println("Unique words: " + frequency.size());

        // Display selected word frequencies.
        System.out.println("Frequency of alice: "
                + frequency.get("alice"));

        System.out.println("Frequency of rabbit: "
                + frequency.get("rabbit"));

        // STEP 6: Copy entries into an array for sorting.
        WordFrequency[] sortedWords = frequency.toArray();

        // Sort by frequency from highest to lowest.
        // Use alphabetical order to break ties.
        sortWords(sortedWords);

        // STEP 7: Display the top 10 words.
        System.out.println("\nTop 10 most frequent words:");

        for (int i = 0; i < 10 && i < sortedWords.length; i++) {
            System.out.println(
                sortedWords[i].word + " : "
                + sortedWords[i].count
            );
        }
    }

    // Sort the entries using selection sort.
    static void sortWords(WordFrequency[] words) {

        for (int i = 0; i < words.length - 1; i++) {

            int best = i;

            for (int j = i + 1; j < words.length; j++) {

                // Prefer the word with the higher frequency.
                if (words[j].count > words[best].count) {
                    best = j;
                }

                // If counts are equal, prefer alphabetical order.
                else if (words[j].count == words[best].count
                        && words[j].word.compareTo(words[best].word) < 0) {
                    best = j;
                }
            }

            // Swap the selected word into its correct position.
            WordFrequency temp = words[i];
            words[i] = words[best];
            words[best] = temp;
        }
    }
}


// CUSTOM DYNAMIC ARRAY
// Stores the book's words and expands when it becomes full.
class DynamicArray {

    private String[] data;
    private int size;
    private int capacity;

    public DynamicArray() {
        capacity = 10;
        size = 0;
        data = new String[capacity];
    }

    // Add a word at the end of the array.
    public void add(String word) {

        if (size == capacity) {
            resize();
        }

        data[size] = word;
        size++;
    }

    // Double the capacity and copy existing words.
    private void resize() {

        int newCapacity = capacity * 2;
        String[] newData = new String[newCapacity];

        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }

        data = newData;
        capacity = newCapacity;
    }

    // Return the number of stored words.
    public int size() {
        return size;
    }

    // Return the word at the requested index.
    public String get(int index) {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                "Invalid index: " + index
            );
        }

        return data[index];
    }
}


// STORES ONE WORD AND ITS FREQUENCY
class WordFrequency {

    String word;
    int count;

    public WordFrequency(String word, int count) {
        this.word = word;
        this.count = count;
    }
}


// CUSTOM HASH TABLE
// Uses linked lists to handle collisions.
class WordFrequencyHashTable {

    // Each array position is a bucket.
    private Node[] buckets;
    private int size;

    private static final double LOAD_FACTOR = 0.75;

    // Each node stores a word, its count, and the next node.
    private static class Node {

        String word;
        int count;
        Node next;

        Node(String word, int count, Node next) {
            this.word = word;
            this.count = count;
            this.next = next;
        }
    }

    // Start with 16 buckets.
    public WordFrequencyHashTable() {
        buckets = new Node[16];
        size = 0;
    }

    // Convert a word's hash code into a valid bucket index.
    private int hash(String word) {
        return Math.floorMod(word.hashCode(), buckets.length);
    }

    // Add a new word or increase its existing frequency.
    public void increment(String word) {

        int index = hash(word);
        Node current = buckets[index];

        // Search this bucket's linked list for the word.
        while (current != null) {

            if (current.word.equals(word)) {
                current.count++;
                return;
            }

            current = current.next;
        }

        // Resize if adding another unique word exceeds the limit.
        if ((size + 1.0) / buckets.length > LOAD_FACTOR) {
            resize();
            index = hash(word);
        }

        // Insert a new word at the beginning of the bucket.
        buckets[index] = new Node(word, 1, buckets[index]);
        size++;
    }

    // Return the frequency of a word, or zero if not found.
    public int get(String word) {

        int index = hash(word);
        Node current = buckets[index];

        while (current != null) {

            if (current.word.equals(word)) {
                return current.count;
            }

            current = current.next;
        }

        return 0;
    }

    // Return the number of unique words in the table.
    public int size() {
        return size;
    }

    // Double the bucket array and redistribute all existing nodes.
    private void resize() {

        Node[] oldBuckets = buckets;
        buckets = new Node[oldBuckets.length * 2];

        for (int i = 0; i < oldBuckets.length; i++) {

            Node current = oldBuckets[i];

            while (current != null) {

                Node nextNode = current.next;
                int newIndex = hash(current.word);

                // Move the node to its new bucket.
                current.next = buckets[newIndex];
                buckets[newIndex] = current;

                current = nextNode;
            }
        }
    }

    // Copy all unique words and their counts into an array.
    public WordFrequency[] toArray() {

        WordFrequency[] result = new WordFrequency[size];
        int index = 0;

        for (int i = 0; i < buckets.length; i++) {

            Node current = buckets[i];

            while (current != null) {

                result[index] = new WordFrequency(
                    current.word, current.count
                );

                index++;
                current = current.next;
            }
        }

        return result;
    }
}
