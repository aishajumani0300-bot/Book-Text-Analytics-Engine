
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BookTextAnalytics {

    public static void main(String[] args) {

        // TASK 1: Load and analyze the book.
        StringBuilder text = new StringBuilder();

        try {
            Scanner sc = new Scanner(
                new File("BookTextAnalytics/alice.txt"));

            while (sc.hasNextLine()) {
                text.append(sc.nextLine()).append(" ");
            }

            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("Book file not found!");
            return;
        }

        String book = text.toString().toLowerCase();

        System.out.println("Book loaded successfully!");
        System.out.println("Total characters: " + text.length());

        String cleaned = book.replaceAll("[^a-z0-9\\s]", " ");
        System.out.println("Lowercase text preview:");
        System.out.println(cleaned.substring(
            0, Math.min(180, cleaned.length())));

        String[] tokens = cleaned.trim().isEmpty()
            ? new String[0] : cleaned.trim().split("\\s+");

        DynamicArray bookWords = new DynamicArray();

        for (int i = 0; i < tokens.length; i++) {
            bookWords.add(tokens[i]);
        }

        WordFrequencyHashTable frequencies =
            new WordFrequencyHashTable();

        for (int i = 0; i < bookWords.size(); i++) {
            frequencies.increment(bookWords.get(i));
        }

        System.out.println("Total words: " + bookWords.size());
        System.out.println("Unique words: " + frequencies.size());
        System.out.println("Frequency of alice: "
            + frequencies.get("alice"));
        System.out.println("Frequency of rabbit: "
            + frequencies.get("rabbit"));

        // Sort word frequencies to find the top 10 words.
        WordFrequency[] top = frequencies.toArray();

        for (int i = 0; i < top.length - 1; i++) {
            int max = i;

            for (int j = i + 1; j < top.length; j++) {
                if (top[j].count > top[max].count) {
                    max = j;
                } else if (top[j].count == top[max].count
                        && top[j].word.compareTo(top[max].word) < 0) {
                    max = j;
                }
            }

            WordFrequency temp = top[i];
            top[i] = top[max];
            top[max] = temp;
        }

        System.out.println("\nTop 10 most frequent words:");

        for (int i = 0; i < Math.min(10, top.length); i++) {
            System.out.println(top[i].word + " : " + top[i].count);
        }

        // TASK 2: Count adjacent word transitions.
        WordTransitionHashTable transitions =
            new WordTransitionHashTable();

        for (int i = 0; i < bookWords.size() - 1; i++) {
            transitions.addTransition(
                bookWords.get(i), bookWords.get(i + 1));
        }

        System.out.println("\n===== TASK 2: WORD TRANSITIONS =====");
        System.out.println("Total transitions: "
            + transitions.getTotalTransitions());
        System.out.println("Unique transitions: "
            + transitions.getUniqueTransitions());
        System.out.println("Transition alice -> was: "
            + transitions.getTransitionCount("alice", "was"));

        System.out.println("\nWords that follow 'alice':");
        transitions.printNeighbors("alice");

        // TASK 3: Build a Trie for prefix and spelling searches.
        TriePrefixAndEditDistance trie =
            new TriePrefixAndEditDistance();

        for (int i = 0; i < bookWords.size(); i++) {
            trie.insert(bookWords.get(i));
        }

        System.out.println("\n===== TASK 3: TRIE SEARCH =====");

        System.out.println("Words starting with 'rab':");
        trie.printWordsWithPrefix("rab");

        System.out.println("\nWords similar to 'rabit':");
        trie.printSimilarWords("rabit", 1);
    }
}


// Stores words in a resizable array.
class DynamicArray {
    private String[] data = new String[100];
    private int count = 0;

    // Adds a word and expands the array when full.
    void add(String word) {
        if (count == data.length) {
            String[] bigger = new String[data.length * 2];

            for (int i = 0; i < count; i++) {
                bigger[i] = data[i];
            }

            data = bigger;
        }

        data[count] = word;
        count++;
    }

    // Returns the word at the given position.
    String get(int index) {
        return data[index];
    }

    // Returns the number of stored words.
    int size() {
        return count;
    }
}


// Stores a word and its frequency.
class WordFrequency {
    String word;
    int count;

    WordFrequency(String word, int count) {
        this.word = word;
        this.count = count;
    }
}


// Counts word frequencies using a hash table.
class WordFrequencyHashTable {
    private class Node {
        String word;
        int count;
        Node next;

        Node(String word) {
            this.word = word;
            count = 1;
        }
    }

    private Node[] buckets = new Node[11];
    private int uniqueWords = 0;

    // Calculates a valid hash table position.
    private int hash(String word) {
        return Math.floorMod(word.hashCode(), buckets.length);
    }

    // Adds a new word or increases its frequency.
    void increment(String word) {
        int index = hash(word);
        Node current = buckets[index];

        while (current != null) {
            if (current.word.equals(word)) {
                current.count++;
                return;
            }
            current = current.next;
        }

        Node newNode = new Node(word);
        newNode.next = buckets[index];
        buckets[index] = newNode;
        uniqueWords++;

        if ((double) uniqueWords / buckets.length > 0.75) {
            resize();
        }
    }

    // Returns the frequency of a word.
    int get(String word) {
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

    // Returns the number of unique words.
    int size() {
        return uniqueWords;
    }

    // Doubles the table size and moves its entries.
    private void resize() {
        Node[] oldBuckets = buckets;
        buckets = new Node[oldBuckets.length * 2 + 1];

        for (int i = 0; i < oldBuckets.length; i++) {
            Node current = oldBuckets[i];

            while (current != null) {
                Node next = current.next;
                int index = hash(current.word);

                current.next = buckets[index];
                buckets[index] = current;
                current = next;
            }
        }
    }

    // Copies all word frequencies into an array.
    WordFrequency[] toArray() {
        WordFrequency[] result = new WordFrequency[uniqueWords];
        int position = 0;

        for (int i = 0; i < buckets.length; i++) {
            Node current = buckets[i];

            while (current != null) {
                result[position] =
                    new WordFrequency(current.word, current.count);
                position++;
                current = current.next;
            }
        }

        return result;
    }
}


// Counts adjacent word pairs using a hash table.
class WordTransitionHashTable {
    private class TargetNode {
        String word;
        int count;
        TargetNode next;

        TargetNode(String word) {
            this.word = word;
            count = 1;
        }
    }

    private class SourceNode {
        String word;
        TargetNode neighbors;
        SourceNode next;

        SourceNode(String word) {
            this.word = word;
        }
    }

    private SourceNode[] buckets = new SourceNode[101];
    private int sourceCount = 0;
    private int totalTransitions = 0;
    private int uniqueTransitions = 0;

    // Calculates the hash table position for a word.
    private int hash(String word) {
        return Math.floorMod(word.hashCode(), buckets.length);
    }

    // Adds a transition or increases its existing count.
    void addTransition(String source, String target) {
        totalTransitions++;

        int index = hash(source);
        SourceNode current = buckets[index];

        while (current != null && !current.word.equals(source)) {
            current = current.next;
        }

        if (current == null) {
            current = new SourceNode(source);
            current.next = buckets[index];
            buckets[index] = current;
            sourceCount++;

            if ((double) sourceCount / buckets.length > 0.75) {
                resize();
            }
        }

        TargetNode neighbor = current.neighbors;

        while (neighbor != null) {
            if (neighbor.word.equals(target)) {
                neighbor.count++;
                return;
            }
            neighbor = neighbor.next;
        }

        TargetNode newTarget = new TargetNode(target);
        newTarget.next = current.neighbors;
        current.neighbors = newTarget;
        uniqueTransitions++;
    }

    // Doubles the table and repositions source words.
    private void resize() {
        SourceNode[] oldBuckets = buckets;
        buckets = new SourceNode[oldBuckets.length * 2 + 1];

        for (int i = 0; i < oldBuckets.length; i++) {
            SourceNode current = oldBuckets[i];

            while (current != null) {
                SourceNode next = current.next;
                int index = hash(current.word);

                current.next = buckets[index];
                buckets[index] = current;
                current = next;
            }
        }
    }

    // Returns the total number of adjacent pairs.
    int getTotalTransitions() {
        return totalTransitions;
    }

    // Returns the number of distinct adjacent pairs.
    int getUniqueTransitions() {
        return uniqueTransitions;
    }

    // Returns how often one word follows another.
    int getTransitionCount(String source, String target) {
        int index = hash(source);
        SourceNode current = buckets[index];

        while (current != null) {
            if (current.word.equals(source)) {
                TargetNode neighbor = current.neighbors;

                while (neighbor != null) {
                    if (neighbor.word.equals(target)) {
                        return neighbor.count;
                    }
                    neighbor = neighbor.next;
                }

                return 0;
            }

            current = current.next;
        }

        return 0;
    }

    // Prints every word that follows the given word.
    void printNeighbors(String source) {
        int index = hash(source);
        SourceNode current = buckets[index];

        while (current != null && !current.word.equals(source)) {
            current = current.next;
        }

        if (current == null) {
            System.out.println("No neighbors found.");
            return;
        }

        TargetNode neighbor = current.neighbors;

        while (neighbor != null) {
            System.out.println(source + " -> " + neighbor.word
                + " : " + neighbor.count);
            neighbor = neighbor.next;
        }
    }
}


// Uses a Trie for prefix search and edit distance for spelling matches.
class TriePrefixAndEditDistance {
    private class TrieNode {
        TrieNode[] children = new TrieNode[36];
        boolean isEndOfWord;
    }

    private TrieNode root = new TrieNode();
    private String[] words = new String[100];
    private int wordCount = 0;

    // Converts a letter or digit into an array position.
    private int getIndex(char ch) {
        if (ch >= 'a' && ch <= 'z') {
            return ch - 'a';
        }

        if (ch >= '0' && ch <= '9') {
            return ch - '0' + 26;
        }

        return -1;
    }

    // Inserts a word into the Trie.
    void insert(String word) {
        TrieNode current = root;

        for (int i = 0; i < word.length(); i++) {
            int index = getIndex(word.charAt(i));

            if (index == -1) {
                return;
            }

            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }

            current = current.children[index];
        }

        if (!current.isEndOfWord) {
            current.isEndOfWord = true;
            addUniqueWord(word);
        }
    }

    // Stores each unique word for spelling comparisons.
    private void addUniqueWord(String word) {
        if (wordCount == words.length) {
            String[] bigger = new String[words.length * 2];

            for (int i = 0; i < wordCount; i++) {
                bigger[i] = words[i];
            }

            words = bigger;
        }

        words[wordCount] = word;
        wordCount++;
    }

    // Finds the Trie node at the end of a prefix.
    private TrieNode findPrefixNode(String prefix) {
        TrieNode current = root;

        for (int i = 0; i < prefix.length(); i++) {
            int index = getIndex(prefix.charAt(i));

            if (index == -1 || current.children[index] == null) {
                return null;
            }

            current = current.children[index];
        }

        return current;
    }

    // Prints up to 20 words that begin with the prefix.
    void printWordsWithPrefix(String prefix) {
        TrieNode node = findPrefixNode(prefix);

        if (node == null) {
            System.out.println("No matching words found.");
            return;
        }

        StringBuilder path = new StringBuilder(prefix);
        int[] printed = {0};

        printFromNode(node, path, printed);

        if (printed[0] == 0) {
            System.out.println("No matching words found.");
        }
    }

    // Traverses the Trie to print matching words.
    private void printFromNode(TrieNode node, StringBuilder path,
                               int[] printed) {
        if (printed[0] >= 20) {
            return;
        }

        if (node.isEndOfWord) {
            System.out.println(path);
            printed[0]++;
        }

        for (int i = 0; i < 36 && printed[0] < 20; i++) {
            if (node.children[i] != null) {
                char ch;

                if (i < 26) {
                    ch = (char) ('a' + i);
                } else {
                    ch = (char) ('0' + i - 26);
                }

                path.append(ch);
                printFromNode(node.children[i], path, printed);
                path.deleteCharAt(path.length() - 1);
            }
        }
    }

    // Calculates the minimum edits needed to change one word into another.
    private int editDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insert = dp[i][j - 1] + 1;
                    int delete = dp[i - 1][j] + 1;
                    int replace = dp[i - 1][j - 1] + 1;

                    dp[i][j] = Math.min(insert,
                        Math.min(delete, replace));
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    // Prints up to 20 words within the allowed edit distance.
    void printSimilarWords(String query, int maxDistance) {
        int found = 0;

        for (int i = 0; i < wordCount && found < 20; i++) {
            int distance = editDistance(query, words[i]);

            if (distance <= maxDistance) {
                System.out.println(words[i]
                    + " (distance: " + distance + ")");
                found++;
            }
        }

        if (found == 0) {
            System.out.println("No similar words found.");
        }
    }
}
