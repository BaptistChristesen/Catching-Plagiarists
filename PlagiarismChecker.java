import java.io.*;
import java.util.*;

class FileHitCount implements Comparable<FileHitCount> {
    String file1, file2;
    int hits;

    public FileHitCount(String file1, String file2, int hits) {
        this.file1 = file1;
        this.file2 = file2;
        this.hits = hits;
    }

    public int compareTo(FileHitCount f) {
        return f.hits - this.hits;
    }

    public String toString() {
        return "[" + file1 + ", " + file2 + "] -> " + hits;
    }
}

public class PlagiarismChecker {
    static File[] directories;
    static String[] directoryNames;
    static ArrayList<FileHitCount> hitList;

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to the Plagiarism Checker program!");
        System.out.println("This program will compare documents in different directories for plagiarism.\n");

        getDirectories();

        int choice = getDirectoryChoice();
        String selectedDirectory = directoryNames[choice - 1];

        int phraseLength = getPhraseLength();
        int cutoffThreshold = getCutoffThreshold();

        System.out.println("\nComparing documents in directory: " + selectedDirectory);
        System.out.println("Searching for " + phraseLength + "-word phrases with a cutoff threshold of " + cutoffThreshold + "\n");

        File[] files = new File(selectedDirectory).listFiles();
        Map<String, Set<String>> phraseSets = new HashMap<>();
        for (File file : files) {
            String fileName = file.getName();
            String fileText = readFile(file);
            Set<String> phrases = getPhrases(fileText, phraseLength);
            phraseSets.put(fileName, phrases);
        }

        hitList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String file1Name = files[i].getName();
            Set<String> file1Phrases = phraseSets.get(file1Name);
            for (int j = i + 1; j < files.length; j++) {
                String file2Name = files[j].getName();
                Set<String> file2Phrases = phraseSets.get(file2Name);
                int hits = countSharedPhrases(file1Phrases, file2Phrases);
                if (hits >= cutoffThreshold) {
                    hitList.add(new FileHitCount(file1Name, file2Name, hits));
                }
            }
        }
        
        FileHitCount[] hitArray = hitList.toArray(new FileHitCount[0]);
        mergeSort(hitArray, 0, hitArray.length - 1);
        hitList = new ArrayList<>(Arrays.asList(hitArray));

        for (FileHitCount f : hitList) {
            System.out.println(f);
        }
    }

    private static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }
    private static void getDirectories() {
        File currentDirectory = new File(".");
        File[] allDirectories = currentDirectory.listFiles(File::isDirectory);
        ArrayList<File> validDirectories = new ArrayList<File>();

        for (File d : allDirectories) {
            File[] files = d.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    validDirectories.add(d);
                    break;
                }
            }
        }

        directories = new File[validDirectories.size()];
        directoryNames = new String[validDirectories.size()];

        for (int i = 0; i < validDirectories.size(); i++) {
            directories[i] = validDirectories.get(i);
            directoryNames[i] = validDirectories.get(i).getName();
        }
    }

    private static int getDirectoryChoice() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please select a directory to check for plagiarism:");
        for (int i = 0; i < directories.length; i++) {
            System.out.println((i+1) + ") " + directoryNames[i]);
        }
        int choice = 0;
        boolean validChoice = false;
        while (!validChoice) {
            try {
                System.out.print("Enter the number of the directory you want to select: ");
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= directories.length) {
                    validChoice = true;
                } else {
                    System.out.println("Invalid choice. Please enter a number between 1 and " + directories.length + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and " + directories.length + ".");
            }
        }

        return choice;
    }
    private static int getPhraseLength() {
        Scanner scanner = new Scanner(System.in);
        int phraseLength = 0;
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Enter the length of phrases to compare (integer value): ");
            try {
                phraseLength = Integer.parseInt(scanner.nextLine());
                if (phraseLength <= 0) {
                    System.out.println("Invalid input. Please enter a positive integer value.");
                } else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer value.");
            }
        }
        scanner.close();
        return phraseLength;
    }
    private static int getCutoffThreshold() {
        Scanner scanner = new Scanner(System.in);
        int cutoffThreshold = 0;
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Enter the cutoff threshold for shared phrases (integer value): ");
            try {
                cutoffThreshold = Integer.parseInt(scanner.nextLine());
                if (cutoffThreshold <= 0) {
                    System.out.println("Invalid input. Please enter a positive integer value.");
                } else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer value.");
            }
        }
        scanner.close();
        return cutoffThreshold;
    }
    private static Set<String> getPhrases(String text, int length) {
        Set<String> phrases = new HashSet<>();
        String[] words = text.split("\s+");
        for (int i = 0; i <= words.length - length; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + length; j++) {
                sb.append(words[j]).append(" ");
            }
            String phrase = sb.toString().trim();
            phrases.add(phrase);
        }
        return phrases;
    }
    private static int countSharedPhrases(Set<String> phrases1, Set<String> phrases2) {
        int count = 0;
        for (String phrase : phrases1) {
            if (phrases2.contains(phrase)) {
                count++;
            }
        }
        return count;
    }

    private static void mergeSort(FileHitCount[] array, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(array, left, mid);
            mergeSort(array, mid + 1, right);
            merge(array, left, mid, right);
        }
    }

    private static void merge(FileHitCount[] array, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        FileHitCount[] leftArray = new FileHitCount[n1];
        FileHitCount[] rightArray = new FileHitCount[n2];

        for (int i = 0; i < n1; i++) {
            leftArray[i] = array[left + i];
        }
        for (int j = 0; j < n2; j++) {
            rightArray[j] = array[mid + 1 + j];
        }
    
        int i = 0, j = 0;
        int k = left;
        while (i < n1 && j < n2) {
            if (leftArray[i].compareTo(rightArray[j]) <= 0) {
                array[k] = leftArray[i];
                i++;
            } else {
                array[k] = rightArray[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            array[k] = leftArray[i];
            i++;
            k++;
        }

        while (j < n2) {
            array[k] = rightArray[j];
            j++;
            k++;
        }
    }
}