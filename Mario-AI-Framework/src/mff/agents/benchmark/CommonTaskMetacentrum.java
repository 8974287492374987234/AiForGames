package mff.agents.benchmark;

import mff.agents.astar.AStarTree;
import mff.agents.common.IMarioAgentMFF;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static mff.LevelLoader.getLevel;

public class CommonTaskMetacentrum {

    private static final String AGENT_TYPE = "astar";
    private static final int LEVELS_PER_SET = 15;

    private static final Map<String, Integer> levelSets = new LinkedHashMap<>() {{
        put("original", 200);
        put("krys", 30);
        put("patternCount", 30);
    }};

    private static final DecimalFormat twoFractionDigitsDotSeparator;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        twoFractionDigitsDotSeparator = new DecimalFormat("0.00", symbols);
    }

    public static void main(String[] args) throws IOException {
        try {
            AStarTree.SEARCH_STEPS = Integer.parseInt(args[0]);
            AStarTree.TIME_TO_FINISH_WEIGHT = Float.parseFloat(args[1]);
        } catch (Exception e) {
            System.out.println("Meta parameters not set successfully.");
            throw e;
        }

        warmup();

        for (var levelSet : levelSets.entrySet()) {
            String levelSetName = levelSet.getKey();
            int timeLimit = levelSet.getValue();

            File log = prepareLog("agent-benchmark" + File.separator + AGENT_TYPE + "-" + levelSetName
                    + "-SS-" + AStarTree.SEARCH_STEPS
                    + "-TTFW-" + AStarTree.TIME_TO_FINISH_WEIGHT
                    + ".csv");
            if (log == null)
                return;

            try (FileWriter logWriter = new FileWriter(log)) {
                logWriter.write("SS:" + AStarTree.SEARCH_STEPS + "\n");
                logWriter.write("TTFW:" + AStarTree.TIME_TO_FINISH_WEIGHT + "\n");
                logWriter.write("level,win/fail,% travelled,run time,game ticks,planning time,total plannings,nodes evaluated,most backtracked nodes\n");

                for (int i = 1; i <= LEVELS_PER_SET; i++) {
                    String levelPath = "./levels/" + levelSetName + "/lvl-" + i + ".txt";
                    System.out.println(AGENT_TYPE + "-" + levelSetName + "-" + i);

                    AgentBenchmarkGame game = new AgentBenchmarkGame();
                    IMarioAgentMFF agent = new mff.agents.astar.Agent();
                    AgentStats agentStats = game.runGame(agent, getLevel(levelPath), timeLimit, 0, false);
                    agentStats.level = levelSetName + "-" + i;
                    printStats(logWriter, agentStats, ((IAgentBenchmarkBacktrack) agent).getMostBacktrackedNodes());
                    logWriter.flush();
                }
            }
        }
    }

    private static void warmup() {
        System.out.println("WARMUP: " + AGENT_TYPE + "-original-1");
        AgentBenchmarkGame game = new AgentBenchmarkGame();
        game.runGame(new mff.agents.astar.Agent(), getLevel("./levels/original/lvl-1.txt"), 200, 0, false);
    }

    private static void printStats(FileWriter writer, AgentStats stats, int mostBacktrackedNodes) throws IOException {
        writer.write(stats.level + ','
                + stats.win + ','
                + twoFractionDigitsDotSeparator.format(stats.percentageTravelled) + ','
                + stats.runTime + ','
                + stats.totalGameTicks + ','
                + stats.totalPlanningTime + ','
                + stats.searchCalls + ','
                + stats.nodesEvaluated + ','
                + mostBacktrackedNodes + '\n'
        );
    }

    private static File prepareLog(String name) throws IOException {
        File agentBenchmarkFolder = new File("agent-benchmark");
        if (!agentBenchmarkFolder.exists()) {
            if (!agentBenchmarkFolder.mkdir()) {
                System.out.println("Can't create folder: " + agentBenchmarkFolder.getName());
                return null;
            }
        }

        File log = new File(name);
        if (log.exists()) {
            if (!log.delete()) {
                System.out.println("Can't delete file: " + log.getName());
                return null;
            }
        }
        if (!log.createNewFile()) {
            System.out.println("Can't create file: " + log.getName());
            return null;
        }
        return log;
    }
}
