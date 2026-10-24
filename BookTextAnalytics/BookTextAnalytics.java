
import java.io.*;
import java.util.*;

public class BookTextAnalytics {

    public static void main(String[] args) throws IOException {

        // STEP 1: Load the book file.
        File file = new File("BookTextAnalytics/alice.txt");
        Scanner sc = new Scanner(file);

        // Store the complete book text in a String.
        String text = "";

        // Read the book line by line.
        while (sc.hasNextLine()) {
            text = text + sc.nextLine() + " ";
        }

        // Close the Scanner after reading the file.
        sc.close();

        // Display book loading information.
        System.out.println("Book loaded successfully!");
        System.out.println("Total characters: " + text.length());

        // STEP 2: Convert the text to lowercase.
        text = text.toLowerCase();

        // Display a preview of the lowercase text.
        System.out.println("Lowercase text preview:");
        System.out.println(
            text.substring(0, Math.min(200, text.length()))
        );

        // STEP 3: Replace punctuation with spaces.
        // Keep letters, digits, and whitespace.
        text = text.replaceAll("[^a-z0-9\\s]", " ");

        // Split the cleaned text into individual words.
        String[] words = text.trim().isEmpty()
                ? new String[0]
                : text.trim().split("\\s+");

        // STEP 4: Store the words in our custom dynamic array.
        DynamicArray bookWords = new DynamicArray();

        // Add every word, including duplicates.
        for (String word : words) {
            if (!word.isEmpty()) {
                bookWords.add(word);
            }
        }

        // Display the total number of stored words.
        System.out.println("Total words: " + bookWords.size());

        // STEP 5: Count unique words.
        // HashSet stores each distinct word only once.
        HashSet<String> uniqueWords = new HashSet<>();

        for (int i = 0; i < bookWords.size(); i++) {
            uniqueWords.add(bookWords.get(i));
        }

        // Display the vocabulary size.
        System.out.println("Unique words: " + uniqueWords.size());

        // STEP 6: Calculate the frequency of each word.
        // HashMap stores a word and its occurrence count.
        HashMap<String, Integer> frequency = new HashMap<>();

        for (int i = 0; i < bookWords.size(); i++) {

            // Retrieve the word at the current index.
            String word = bookWords.get(i);

            // Increase the count if the word already exists.
            if (frequency.containsKey(word)) {
                frequency.put(word, frequency.get(word) + 1);
            } else {
                // Otherwise, record its first occurrence.
                frequency.put(word, 1);
            }
        }

        // Display the frequencies of selected words.
        System.out.println("Frequency of alice: "
                + frequency.getOrDefault("alice", 0));

        System.out.println("Frequency of rabbit: "
                + frequency.getOrDefault("rabbit", 0));

        // STEP 7: Prepare the word frequencies for sorting.
        ArrayList<Map.Entry<String, Integer>> sortedWords =
                new ArrayList<>(frequency.entrySet());

        // Sort words from highest frequency to lowest.
        // If frequencies are equal, sort alphabetically.
        sortedWords.sort((a, b) -> {
            int result = b.getValue().compareTo(a.getValue());

            if (result == 0) {
                return a.getKey().compareTo(b.getKey());
            }

            return result;
        });

        // STEP 8: Display the top 10 most frequent words.
        System.out.println("\nTop 10 most frequent words:");

        for (int i = 0; i < 10 && i < sortedWords.size(); i++) {
            System.out.println(
                sortedWords.get(i).getKey() + " : "
                + sortedWords.get(i).getValue()
            );
        }
    }
}


/*
 * CUSTOM DYNAMIC ARRAY
 *
 * This class stores cleaned book words in order.
 * Unlike a fixed-size array, it expands when necessary.
 */
class DynamicArray {

    // The array that holds the words.
    private String[] data;

    // Number of words currently stored.
    private int size;

    // Maximum number of words the array can hold.
    private int capacity;

    // Constructor: initialize an empty dynamic array.
    public DynamicArray() {
        capacity = 10;
        size = 0;
        data = new String[capacity];
    }

    // Add a word to the end of the dynamic array.
    public void add(String word) {

        // Expand the array when it becomes full.
        if (size == capacity) {
            resize();
        }

        // Store the word at the next available position.
        data[size] = word;

        // Increase the number of stored words.
        size++;
    }

    // Double the array capacity when required.
    private void resize() {

        // Create a larger array.
        int newCapacity = capacity * 2;
        String[] newData = new String[newCapacity];

        // Copy all existing words into the larger array.
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }

        // Replace the old array with the new array.
        data = newData;
        capacity = newCapacity;
    }

    // Return the number of words stored.
    public int size() {
        return size;
    }

    // Retrieve a word by its index.
    public String get(int index) {

        // Check that the requested index is valid.
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                "Invalid index: " + index
            );
        }

        // Return the word at that position.
        return data[index];
    }
}
