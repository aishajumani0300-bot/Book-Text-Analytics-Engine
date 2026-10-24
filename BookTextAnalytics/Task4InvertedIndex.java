import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Task4InvertedIndex {

    static class LineNode {
        int lineNumber;
        LineNode next;

        LineNode(int lineNumber) {
            this.lineNumber = lineNumber;
            this.next = null;
        }
    }

    static class WordEntry {
        String word;
        LineNode firstLine;
        LineNode lastLine;
        WordEntry next;

        WordEntry(String word) {
            this.word = word;
            this.firstLine = null;
            this.lastLine = null;
            this.next = null;
        }

        void addLine(int lineNumber) {
            // Store each line only once for this word.
            if (lastLine != null &&
                lastLine.lineNumber == lineNumber) {
                return;
            }

            LineNode newNode = new LineNode(lineNumber);

            if (firstLine == null) {
                firstLine = newNode;
                lastLine = newNode;
            } else {
                lastLine.next = newNode;
                lastLine = newNode;
            }
        }
    }

    static class InvertedIndex {
        private WordEntry[] table;
        private int capacity;

        InvertedIndex(int capacity) {
            this.capacity = capacity;
            table = new WordEntry[capacity];
        }

        private int hash(String word) {
            return Math.floorMod(word.hashCode(), capacity);
        }

        private WordEntry findWord(String word) {
            int index = hash(word);
            WordEntry current = table[index];

            while (current != null) {
                if (current.word.equals(word)) {
                    return current;
                }
                current = current.next;
            }

            return null;
        }

        public void addOccurrence(String word, int lineNumber) {
            int index = hash(word);
            WordEntry entry = findWord(word);

            if (entry == null) {
                entry = new WordEntry(word);
                entry.next = table[index];
                table[index] = entry;
            }

            entry.addLine(lineNumber);
        }

        public void searchInPageRange(
                String word, int startPage, int endPage) {

            word = word.toLowerCase();
            WordEntry entry = findWord(word);

            System.out.println("\nSearch word: " + word);
            System.out.println(
                "Line range: " + startPage + " to " + endPage);

            if (entry == null) {
                System.out.println("Word not found.");
                return;
            }

            LineNode current = entry.firstLine;
            boolean found = false;

            while (current != null &&
                   current.lineNumber < startPage) {
                current = current.next;
            }

            while (current != null &&
                   current.lineNumber <= endPage) {
                System.out.println(
                    "Found on line: " + current.lineNumber);
                found = true;
                current = current.next;
            }

            if (!found) {
                System.out.println(
                    "Word not found in this line range.");
            }
        }
    }

    public static void main(String[] args) {
        InvertedIndex index = new InvertedIndex(1009);

        String filePath = "BookTextAnalytics/alice.txt";

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(filePath))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                String cleaned = line.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", " ");

                String[] words = cleaned.trim().split("\\s+");

                if (cleaned.trim().isEmpty()) {
                    continue;
                }

                for (int i = 0; i < words.length; i++) {
                    if (!words[i].isEmpty()) {
                        index.addOccurrence(words[i], lineNumber);
                    }
                }
            }

            System.out.println("Task 4: Inverted Index built!");
            System.out.println("Book lines processed: " + lineNumber);

         index.searchInPageRange("alice", 1, 100);
         index.searchInPageRange("rabbit", 1, 100);
         index.searchInPageRange("queen", 1, 100);

        } catch (IOException e) {
            System.out.println("Error reading book: " + e.getMessage());
        }
    }
}
