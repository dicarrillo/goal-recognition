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
    private static double initialProb(int disFromAgent, int disFromStart) {
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
    private static double[] normalizeDist(double[] initialProbs) {
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
    private static int findCost(String domainFilePath, String problemFilePath)
    {
        int cost = -1;   // Value to return

        try {
            // Specify the directory
            File directory = new File("/Users/dynomite/Desktop/REU/Goal-Recognition/downward");

            // Add elements to command (using old Lama version of Downward planner)
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

                System.out.println(line);   // Print planner output

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

    /**
     * Creates a probability distribution (before normalization) of goals
     *
     * @param domain path to domain file
     * @param currentPosProblems array of problem file paths, where current position is the start
     * @param goalProblems array of problem file paths, for each of the goals
     * @return array of relative probabilities for corresponding goals, before normalization
     */
    private static double[] initialProbDist(String domain, String[] currentPosProblems, String[] goalProblems)
    {
        double[] relativeProbs;
        relativeProbs = new double[goalProblems.length];

        // Populate relativeProbs with correct probabilities
        int startToGoal;
        int currentPosToGoal;
        double relativeProb;
        for (int i = 0; i < goalProblems.length; ++i)
        {
            startToGoal = findCost(domain, goalProblems[i]);
            currentPosToGoal = findCost(domain, currentPosProblems[i]);
            relativeProb = initialProb(currentPosToGoal, startToGoal);
            relativeProbs[i] = relativeProb;
        }

        return relativeProbs;
    }

    /**
     * Prints the relative probability distribution for an array of potential goals
     *
     * @param domain path to domain file
     * @param currentPosProblems array of problem file paths, where current position is the start
     * @param goalProblems array of problem file paths, for each of the goals
     */
    public static void goalProbabilites(String domain, String[] currentPosProblems, String[] goalProblems)
    {
        double[] probs;

        // Calculate probabilities
        probs = initialProbDist(domain, currentPosProblems, goalProblems);
        probs = normalizeDist(probs);

        System.out.println("\nPrinting probabilities...");

        // Print probabilities
        for (double probability : probs) {
            System.out.println(probability);
        }
    }

    public static void main(String[] args)
    {
        String domainFile;
        String[] currPosFiles;
        String[] goalFiles;

        domainFile = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/Test2/domain.pddl";

        currPosFiles = new String[3];
        currPosFiles[0] = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/Test2/currPos1.pddl";
        currPosFiles[1] = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/Test2/currPos2.pddl";
        currPosFiles[2] = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/Test2/currPos3.pddl";

        goalFiles = new String[3];
        goalFiles[0] = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/Test2/goal1.pddl";
        goalFiles[1] = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/Test2/goal2.pddl";
        goalFiles[2] = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/Test2/goal3.pddl";

        goalProbabilites(domainFile, currPosFiles, goalFiles);
    }
}
