import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Scanner;

public class Main {
    static final int maxExplored = 30;
    static final int maxReading = 20;

    static String[] exploredTitles = new String[maxExplored];
    static String[] exploredAuthors = new String[maxExplored];
    static String[] exploredUrls = new String[maxExplored];
    static int exploredCount = 0;

    static String[] readingTitles = new String[maxReading];
    static String[] readingAuthors = new String[maxReading];
    static String[] readingUrls = new String[maxReading];
    static int readingCount = 0;

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        int choice;

        do {
            System.out.println("\n--- BOOK EXPLORER ---");
            System.out.println("1. Explore Books");
            System.out.println("2. Search by Title");
            System.out.println("3. Search by Author");
            System.out.println("4. Add Book to Reading List");
            System.out.println("5. View Reading List");
            System.out.println("6. Remove Book");
            System.out.println("7. Display Book Details");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Enter a number.");
                scanner.nextLine();
                choice = -1;
                continue;
            }

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    exploreBooks();
                    break;
                case 2:
                    System.out.print("Enter title: ");
                    searchBook(scanner.nextLine().trim());
                    break;
                case 3:
                    System.out.print("Enter author: ");
                    searchBook(scanner.nextLine().trim(), true);
                    break;
                case 4:
                    addBook();
                    break;
                case 5:
                    viewReadingList();
                    break;
                case 6:
                    removeBook();
                    break;
                case 7:
                    displayBookDetails();
                    break;
                case 0:
                    System.out.println("Exiting Book Explorer.");
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        } while (choice != 0);
    }

    static void exploreBooks() throws Exception {
        exploredCount = 0;

        Document doc = Jsoup.connect("https://books.toscrape.com/")
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();

        Elements cards = doc.select("article.product_pod");

        for (Element card : cards) {
            Element link = card.selectFirst("h3 a");
            if (link == null) {
                continue;
            }

            String title = link.attr("title").trim();
            if (title.isEmpty()) {
                title = link.text().trim();
            }
            if (title.isEmpty()) {
                continue;
            }

            String url = link.absUrl("href");
            boolean duplicate = false;
            for (int i = 0; i < exploredCount; i++) {
                if (exploredUrls[i].equals(url)) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                continue;
            }

            String author = "Not listed on catalog";

            exploredTitles[exploredCount] = title;
            exploredAuthors[exploredCount] = author;
            exploredUrls[exploredCount] = url;
            exploredCount++;

            if (exploredCount >= 15 || exploredCount >= maxExplored) {
                break;
            }
        }

        if (exploredCount < 10) {
            System.out.println("Fewer than 10 books retrieved.");
            return;
        }

        System.out.println("\n--- Explored Books ---");
        for (int i = 0; i < exploredCount; i++) {
            System.out.println((i + 1) + ". " + exploredTitles[i] + " | Author: " + exploredAuthors[i]);
        }
    }

    static void searchBook(String title) {
        searchBook(title, false);
    }

    static void searchBook(String keyword, boolean searchByAuthor) {
        if (exploredCount == 0) {
            System.out.println("No books loaded. Explore books first.");
            return;
        }
        if (keyword.isEmpty()) {
            System.out.println("Keyword cannot be empty.");
            return;
        }

        String query = keyword.toLowerCase();
        boolean found = false;

        for (int i = 0; i < exploredCount; i++) {
            boolean match;
            if (searchByAuthor) {
                match = exploredAuthors[i].toLowerCase().contains(query);
            } else {
                match = exploredTitles[i].toLowerCase().contains(query);
            }

            if (match) {
                System.out.println((i + 1) + ". " + exploredTitles[i] + " | Author: " + exploredAuthors[i] + " | " + exploredUrls[i]);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching books found.");
        }
    }
    //adding book method
    static void addBook() {
        if (readingCount >= maxReading) {
            System.out.println("Reading list is full.");
            return;
        }
        if (exploredCount == 0) {
            System.out.println("No books available to add.");
            return;
        }

        System.out.print("Enter book number (1-" + exploredCount + "): ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid number.");
            scanner.nextLine();
            return;
        }
        int num = scanner.nextInt();
        scanner.nextLine();

        if (num < 1 || num > exploredCount) {
            System.out.println("Number out of range.");
            return;
        }

        int idx = num - 1;
        for (int i = 0; i < readingCount; i++) {
            if (readingUrls[i].equals(exploredUrls[idx])) {
                System.out.println("Book already in reading list.");
                return;
            }
        }

        readingTitles[readingCount] = exploredTitles[idx];
        readingAuthors[readingCount] = exploredAuthors[idx];
        readingUrls[readingCount] = exploredUrls[idx];
        readingCount++;

        System.out.println("Added: " + exploredTitles[idx]);
    }

    static void viewReadingList() {
        if (readingCount == 0) {
            System.out.println("Reading list is empty.");
            return;
        }

        System.out.println("\n--- Reading List ---");
        for (int i = 0; i < readingCount; i++) {
            System.out.println((i + 1) + ". " + readingTitles[i] + " | Author: " + readingAuthors[i]);
        }
    }

    static void removeBook() {
        if (readingCount == 0) {
            System.out.println("Reading list is empty.");
            return;
        }

        viewReadingList();
        System.out.print("Enter book number to remove: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid number.");
            scanner.nextLine();
            return;
        }
        int num = scanner.nextInt();
        scanner.nextLine();

        if (num < 1 || num > readingCount) {
            System.out.println("Invalid number.");
            return;
        }

        int idx = num - 1;
        String removed = readingTitles[idx];

        for (int i = idx; i < readingCount - 1; i++) {
            readingTitles[i] = readingTitles[i + 1];
            readingAuthors[i] = readingAuthors[i + 1];
            readingUrls[i] = readingUrls[i + 1];
        }

        readingCount--;
        readingTitles[readingCount] = null;
        readingAuthors[readingCount] = null;
        readingUrls[readingCount] = null;

        System.out.println("Removed: " + removed);
    }

    static void displayBookDetails() {
        if (exploredCount == 0) {
            System.out.println("No books loaded.");
            return;
        }

        System.out.print("Enter book number (1-" + exploredCount + "): ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid number.");
            scanner.nextLine();
            return;
        }
        int num = scanner.nextInt();
        scanner.nextLine();

        if (num < 1 || num > exploredCount) {
            System.out.println("Invalid number.");
            return;
        }

        int idx = num - 1;
        System.out.println("\n--- Book Details ---");
        System.out.println("Title : " + exploredTitles[idx]);
        System.out.println("Author: " + exploredAuthors[idx]);
        System.out.println("URL   : " + exploredUrls[idx]);
    }
}