import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class GoalRecognizer {
    /**
     * Calculates the probability value for a specific goal before the normalizing constant is applied
     *
     * @param disFromAgent distance from agent to goal
     * @param disFromStart distance from start to goal
     * @return probability value before normalizing constant is applied
     */
    public static double initialProb(int disFromAgent, int disFromStart) {
        double costDifference;
        double numerator;
        double denominator;
        double probability;

        // Calculate initial probability value
        costDifference = disFromAgent - disFromStart;
        numerator = Math.pow(Math.E, -1.0 * costDifference);    // 1.0 is the chosen positive constant
        denominator = 1.0 + numerator;
        probability = numerator / denominator;

        return probability;
    }

    /**
     * Calculates and applies the normalizing constant to a distribution of probabilities
     *
     * @param initialProbs distribution of probabilities
     * @return probability distribution after being normalized
     */
    public static double[] normalizeDist(double[] initialProbs) {
        double normalizingConst;
        double[] normalizedDist;

        // Calculate normalizing constant
        normalizingConst = 0;
        for (int i = 0; i < initialProbs.length; ++i) {
            normalizingConst += initialProbs[i];
        }

        // Normalize each probability in initialProbs
        normalizedDist = new double[initialProbs.length];
        double normalizedVal;
        for (int i = 0; i < normalizedDist.length; ++i) {
            normalizedVal = initialProbs[i] / normalizingConst;
            normalizedDist[i] = normalizedVal;
        }

        return normalizedDist;
    }

    /**
     * Calculates the cost of a plan for a specified planning domain and problem
     *
     * @param domainFilePath  path to PDDL domain file
     * @param problemFilePath path to PDDL problem file
     * @return optimal cost (number of steps) to achieve goal state
     */
    public static int findCost(String domainFilePath, String problemFilePath)
    {
        int cost = -1;   // Value to return

        try {
            // Specify the directory
            File directory = new File("/Users/dynomite/Desktop/REU/Goal-Recognition/downward");

            // Add elements to command
            ArrayList<String> command = new ArrayList<>();
            command.add("./fast-downward.py");
            command.add("--alias");
            command.add("seq-sat-lama-2011");
            command.add(domainFilePath);
            command.add(problemFilePath);

            // Create a ProcessBuilder with the command and directory
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(directory);

            // Start the process
            Process process = processBuilder.start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String targetString;
                int index;
                char charCost;

                targetString = "Plan cost :";        // Sequence to search for
                index = line.indexOf("Plan cost: "); // Find target in line (if it exists)

                if (index != -1)
                {   // Target string found, get value of next character (which is the cost)
                    charCost = line.charAt(index + targetString.length());
                    cost = charCost - '0';
                }

            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Command executed with exit code: " + exitCode);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return cost;
    }

    public static void main(String[] args)
    {
        String domainFile;
        String problemFile;

        domainFile = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/domain.pddl";
        problemFile = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/prob1.pddl";

        System.out.println(GoalRecognizer.findCost(domainFile, problemFile));
    }
}
