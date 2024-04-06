import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


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
        String plannerScript;

        plannerScript = "./fast-downward.py " + domainFilePath + " " + problemFilePath + " --search \"astar(lmcut())\"";

        try {
            File workingDir;
            BufferedReader reader;
            ProcessBuilder processBuilder;
            Process process;
            String line;
            int exitCode;

            // Create process builder
            processBuilder = new ProcessBuilder(plannerScript.split("\\s+"));

            // Specify the working directory
            workingDir = new File("/Users/dynomite/Desktop/REU/Goal-Recognition/downward");

            // Set the working directory
            processBuilder.directory(workingDir);

            // Redirect error stream to output stream
            processBuilder.redirectErrorStream(true);

            // Start the process
            process = processBuilder.start();

            // Read output
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish
            exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Exited with error code " + exitCode);
            }
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void main(String[] args)
    {
        String domainFile;
        String problemFile;

        domainFile = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/domain.pddl";
        problemFile = "/Users/dynomite/Desktop/REU/Goal-Recognition/Test-Files/prob1.pddl";

        GoalRecognizer.findCost(domainFile, problemFile);
    }
}
