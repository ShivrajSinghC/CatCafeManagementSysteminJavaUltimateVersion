import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MembershipFileReader {

    public static void main(String[] args) throws IOException, ParseException {
        // Check if a file name is provided as a command line argument
        if (args.length == 0) {
            System.err.println(" provide a file name as an argument.");
            System.exit(1);
        }

        // Read the input file
        String inputFileName = args[0];
        Path inputFile = Paths.get(inputFileName);
        List<String> inputLines = Files.readAllLines(inputFile);

        // Initialize data structures to store member data, total revenue, and member count by join year
        List<String[]> membersData = new ArrayList<>();
        double totalRevenue = 0;
        Map<Integer, Integer> memberCountByYear = new TreeMap<>();

        // Define a pattern to match and extract data from input lines
        Pattern pattern = Pattern.compile("([\\w-]+)\\s+([\\w-]+)\\s+has\\s+(\\d+)\\s+visits?\\s+left\\s+and\\s+paid\\s+(\\$?\\d+(\\.\\d{2})?|nothing)\\s+and\\s+\\w+\\s+joined\\s+on\\s+(\\w+)\\s+(\\d{1,2}),\\s+(\\d{4})");
        // Define date formats for parsing input and formatting output
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyyMMdd");
        // Define a decimal format for formatting revenue
        DecimalFormat revenueFormat = new DecimalFormat("0.00");

        // Process each line in the input file
        for (String line : inputLines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                // Extract data from the matched line
                String lastName = matcher.group(1);
                String firstName = matcher.group(2);
                int remainingVisits = Integer.parseInt(matcher.group(3));
                double revenue = matcher.group(4).equals("nothing") ? 0 : Double.parseDouble(matcher.group(4).replace("$", ""));
                Date joinDate = inputDateFormat.parse(matcher.group(6) + " " + matcher.group(7) + ", " + matcher.group(8));

                // Add member data to the list
                membersData.add(new String[]{
                        lastName,
                        firstName,
                        Integer.toString(remainingVisits),
                        revenueFormat.format(revenue),
                        outputDateFormat.format(joinDate)
                });

                // Update total revenue
                totalRevenue += revenue;

                // Update member count by join year
                int joinYear = Integer.parseInt(matcher.group(8));
                memberCountByYear.put(joinYear, memberCountByYear.getOrDefault(joinYear, 0) + 1);
            }
        }

        // Print total revenue and member count by join year
        System.out.printf("Total revenue to date: $%.2f%n", totalRevenue);
        System.out.println("Member Count by Join Year:");
        for (Map.Entry<Integer, Integer> entry : memberCountByYear.entrySet()) {
            System.out.printf("%d: %d%n", entry.getKey(), entry.getValue());
        }
        
                // Save member data as a .csv file
        String outputFileName = "members.csv";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFileName))) {
            // Write the header line for the .csv file
            writer.write("lastName,firstName,remainingVisits,revenue,joinDate\n");

            // Write the member data to the .csv file
            for (String[] memberData : membersData) {
                writer.write(String.join(",", memberData) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error to the output file: " + e.getMessage());
            System.exit(1);
        }

        System.out.println(" data for the members of the cat cafe has been saved to " + outputFileName);
    }
}



