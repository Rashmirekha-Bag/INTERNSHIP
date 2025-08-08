import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileHandlingUtility {

    private static final String SAMPLE_FILE = "sample.txt";
    private static final String OUTPUT_FILE = "output.txt";
    private static final String MODIFIED_FILE = "modified.txt";

    public static void main(String[] args) {
        FileHandlingUtility utility = new FileHandlingUtility();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== CODTECH File Handling Utility ===");
        System.out.println("Demonstrating File Operations in Java\n");

        try {
            // Demonstrate all file operations
            utility.demonstrateFileOperations();

            // Interactive menu for user operations
            utility.runInteractiveMenu(scanner);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /**
     * Demonstrates all file operations with sample data
     */
    public void demonstrateFileOperations() {
        System.out.println("--- Demonstration of File Operations ---\n");

        try {
            // 1. Create and write to a sample file
            createSampleFile();

            // 2. Read the sample file
            readFile(SAMPLE_FILE);

            // 3. Write new content to output file
            writeToFile(OUTPUT_FILE, "This is new content written to output file.");

            // 4. Modify existing file
            modifyFile(SAMPLE_FILE, MODIFIED_FILE);

            // 5. Append to existing file
            appendToFile(OUTPUT_FILE, "\nThis line was appended to the file.");

            // 6. Display file information
            displayFileInfo(SAMPLE_FILE);

        } catch (IOException e) {
            System.err.println("Error during demonstration: " + e.getMessage());
        }
    }

    /**
     * Creates a sample file with initial content
     */
    private void createSampleFile() throws IOException {
        System.out.println("1. Creating sample file...");

        String sampleContent = "Welcome to CODTECH File Handling Utility!\n" +
                "This is line 2 of the sample file.\n" +
                "Line 3 contains some sample data: 12345\n" +
                "This file will be used for demonstration purposes.\n" +
                "Created on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        writeToFile(SAMPLE_FILE, sampleContent);
        System.out.println("✓ Sample file created successfully!\n");
    }

    /**
     * Reads and displays content from a file
     * 
     * @param filename The name of the file to read
     */
    public void readFile(String filename) throws IOException {
        System.out.println("2. Reading file: " + filename);

        if (!Files.exists(Paths.get(filename))) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename))) {
            String line;
            int lineNumber = 1;

            System.out.println("--- File Content ---");
            while ((line = reader.readLine()) != null) {
                System.out.println(lineNumber + ": " + line);
                lineNumber++;
            }
            System.out.println("--- End of File ---\n");
        }
    }

    /**
     * Writes content to a file (overwrites existing content)
     * 
     * @param filename The name of the file to write to
     * @param content  The content to write
     */
    public void writeToFile(String filename, String content) throws IOException {
        System.out.println("3. Writing to file: " + filename);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            writer.write(content);
        }

        System.out.println("✓ Content written successfully!\n");
    }

    /**
     * Appends content to an existing file
     * 
     * @param filename The name of the file to append to
     * @param content  The content to append
     */
    public void appendToFile(String filename, String content) throws IOException {
        System.out.println("5. Appending to file: " + filename);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(content);
        }

        System.out.println("✓ Content appended successfully!\n");
    }

    /**
     * Modifies an existing file by replacing specific text
     * 
     * @param sourceFile The source file to modify
     * @param targetFile The target file to save modifications
     */
    public void modifyFile(String sourceFile, String targetFile) throws IOException {
        System.out.println("4. Modifying file: " + sourceFile + " -> " + targetFile);

        List<String> lines = Files.readAllLines(Paths.get(sourceFile));
        List<String> modifiedLines = new ArrayList<>();

        for (String line : lines) {
            // Example modification: Replace "sample" with "MODIFIED"
            String modifiedLine = line.replace("sample", "MODIFIED")
                    .replace("Sample", "MODIFIED");
            modifiedLines.add(modifiedLine);
        }

        // Add modification timestamp
        modifiedLines.add("--- File modified on: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " ---");

        Files.write(Paths.get(targetFile), modifiedLines);
        System.out.println("✓ File modified and saved as: " + targetFile + "\n");
    }

    /**
     * Displays file information such as size, last modified date, etc.
     * 
     * @param filename The name of the file to get information about
     */
    public void displayFileInfo(String filename) throws IOException {
        System.out.println("6. File Information for: " + filename);

        Path path = Paths.get(filename);
        if (!Files.exists(path)) {
            System.out.println("File does not exist!");
            return;
        }

        System.out.println("--- File Details ---");
        System.out.println("File Name: " + path.getFileName());
        System.out.println("File Size: " + Files.size(path) + " bytes");
        System.out.println("Last Modified: " + Files.getLastModifiedTime(path));
        System.out.println("Readable: " + Files.isReadable(path));
        System.out.println("Writable: " + Files.isWritable(path));
        System.out.println("--- End of File Details ---\n");
    }

    /**
     * Runs an interactive menu for user file operations
     * 
     * @param scanner Scanner object for user input
     */
    public void runInteractiveMenu(Scanner scanner) {
        System.out.println("--- Interactive File Operations Menu ---");

        while (true) {
            System.out.println("\nChoose an operation:");
            System.out.println("1. Read a file");
            System.out.println("2. Write to a file");
            System.out.println("3. Append to a file");
            System.out.println("4. Modify a file");
            System.out.println("5. Display file information");
            System.out.println("6. List files in current directory");
            System.out.println("7. Delete a file");
            System.out.println("8. Exit");
            System.out.print("Enter your choice (1-8): ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number between 1-8.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        handleReadFile(scanner);
                        break;
                    case 2:
                        handleWriteFile(scanner);
                        break;
                    case 3:
                        handleAppendFile(scanner);
                        break;
                    case 4:
                        handleModifyFile(scanner);
                        break;
                    case 5:
                        handleFileInfo(scanner);
                        break;
                    case 6:
                        listCurrentDirectoryFiles();
                        break;
                    case 7:
                        handleDeleteFile(scanner);
                        break;
                    case 8:
                        System.out.println("Thank you for using File Handling Utility!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please enter a number between 1-8.");
                }
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Interactive file reading
     */
    private void handleReadFile(Scanner scanner) throws IOException {
        System.out.print("Enter filename to read: ");
        String filename = scanner.nextLine().trim();
        if (!filename.isEmpty()) {
            readFile(filename);
        }
    }

    /**
     * Interactive file writing
     */
    private void handleWriteFile(Scanner scanner) throws IOException {
        System.out.print("Enter filename to write to: ");
        String filename = scanner.nextLine().trim();
        System.out.print("Enter content to write: ");
        String content = scanner.nextLine();

        if (!filename.isEmpty()) {
            writeToFile(filename, content);
        }
    }

    /**
     * Interactive file appending
     */
    private void handleAppendFile(Scanner scanner) throws IOException {
        System.out.print("Enter filename to append to: ");
        String filename = scanner.nextLine().trim();
        System.out.print("Enter content to append: ");
        String content = scanner.nextLine();

        if (!filename.isEmpty()) {
            appendToFile(filename, "\n" + content);
        }
    }

    /**
     * Interactive file modification
     */
    private void handleModifyFile(Scanner scanner) throws IOException {
        System.out.print("Enter source filename: ");
        String sourceFile = scanner.nextLine().trim();
        System.out.print("Enter target filename: ");
        String targetFile = scanner.nextLine().trim();

        if (!sourceFile.isEmpty() && !targetFile.isEmpty()) {
            modifyFile(sourceFile, targetFile);
        }
    }

    /**
     * Interactive file information display
     */
    private void handleFileInfo(Scanner scanner) throws IOException {
        System.out.print("Enter filename for information: ");
        String filename = scanner.nextLine().trim();

        if (!filename.isEmpty()) {
            displayFileInfo(filename);
        }
    }

    /**
     * Lists all files in the current directory
     */
    private void listCurrentDirectoryFiles() throws IOException {
        System.out.println("Files in current directory:");
        System.out.println("--- Directory Listing ---");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."))) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    System.out.println("[FILE] " + file.getFileName() + " (" + Files.size(file) + " bytes)");
                } else if (Files.isDirectory(file)) {
                    System.out.println("[DIR] " + file.getFileName() + "/");
                }
            }
        }
        System.out.println("--- End of Directory Listing ---");
    }

    /**
     * Interactive file deletion
     */
    private void handleDeleteFile(Scanner scanner) throws IOException {
        System.out.print("Enter filename to delete: ");
        String filename = scanner.nextLine().trim();

        if (!filename.isEmpty()) {
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                System.out.print("Are you sure you want to delete '" + filename + "'? (y/N): ");
                String confirmation = scanner.nextLine().trim().toLowerCase();

                if (confirmation.equals("y") || confirmation.equals("yes")) {
                    Files.delete(path);
                    System.out.println("✓ File deleted successfully!");
                } else {
                    System.out.println("File deletion cancelled.");
                }
            } else {
                System.out.println("File not found: " + filename);
            }
        }
    }
}
