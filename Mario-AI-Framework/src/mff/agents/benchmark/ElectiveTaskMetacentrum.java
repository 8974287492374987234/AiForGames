package mff.agents.benchmark;

import mff.agents.astarGrid.AStarTree;
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
import static mff.agents.common.AStarGridHelper.giveLevelTilesWithPath;


public class ElectiveTaskMetacentrum {

    private static final String AGENT_TYPE = "astarGrid";

    private static final float NODE_DEPTH_WEIGHT = 1f;
    private static final float TIME_TO_FINISH_WEIGHT = 2f;
    private static final float DISTANCE_FROM_PATH_TOLERANCE = 1f;
    private static final float DISTANCE_FROM_PATH_ADDITIVE_PENALTY = 5f;

    private record LevelSet(int levelCount, int timeLimit) {}

    private static final Map<String, LevelSet> levelSets = new LinkedHashMap<>() {{
        put("original", new LevelSet(15, 200));
        put("krys", new LevelSet(100, 30));
        put("patternCount", new LevelSet(100, 30));
    }};

    private static final DecimalFormat twoFractionDigitsDotSeparator;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        twoFractionDigitsDotSeparator = new DecimalFormat("0.00", symbols);
    }

    public static void main(String[] args) throws IOException {
        try {
            AgentBenchmarkGame.maxTime = Long.parseLong(args[0]);
            AStarTree.DISTANCE_FROM_PATH_MULTIPLICATIVE_PENALTY = Float.parseFloat(args[1]);
        } catch (Exception e) {
            System.out.println("Meta parameters not set successfully.");
            throw e;
        }
        AStarTree.NODE_DEPTH_WEIGHT = NODE_DEPTH_WEIGHT;
        AStarTree.TIME_TO_FINISH_WEIGHT = TIME_TO_FINISH_WEIGHT;
        AStarTree.DISTANCE_FROM_PATH_TOLERANCE = DISTANCE_FROM_PATH_TOLERANCE;
        AStarTree.DISTANCE_FROM_PATH_ADDITIVE_PENALTY = DISTANCE_FROM_PATH_ADDITIVE_PENALTY;

        warmup();

        for (var levelSet : levelSets.entrySet()) {
            String levelSetName = levelSet.getKey();
            LevelSet settings = levelSet.getValue();

            File log = prepareLog("agent-benchmark" + File.separator + AGENT_TYPE + "-" + levelSetName
                    + "-TTT-" + AgentBenchmarkGame.maxTime
                    + "-DFPMP-" + AStarTree.DISTANCE_FROM_PATH_MULTIPLICATIVE_PENALTY
                    + ".csv");
            if (log == null)
                return;

            try (FileWriter logWriter = new FileWriter(log)) {
                logWriter.write("TTT:" + AgentBenchmarkGame.maxTime + "\n");
                logWriter.write("DFPMP:" + AStarTree.DISTANCE_FROM_PATH_MULTIPLICATIVE_PENALTY + "\n");
                logWriter.write("NDW:" + AStarTree.NODE_DEPTH_WEIGHT + "\n");
                logWriter.write("TTFW:" + AStarTree.TIME_TO_FINISH_WEIGHT + "\n");
                logWriter.write("DFPT:" + AStarTree.DISTANCE_FROM_PATH_TOLERANCE + "\n");
                logWriter.write("DFPAP:" + AStarTree.DISTANCE_FROM_PATH_ADDITIVE_PENALTY + "\n");
                logWriter.write("level,win/fail,% travelled,run time,game ticks,planning time,total plannings,nodes evaluated,most backtracked nodes\n");

                for (int i = 1; i <= settings.levelCount(); i++) {
                    String levelPath = "./levels/" + levelSetName + "/lvl-" + i + ".txt";
                    System.out.println(AGENT_TYPE + "-" + levelSetName + "-" + i);

                    AgentBenchmarkGame game = new AgentBenchmarkGame();
                    IMarioAgentMFF agent = new mff.agents.astarGrid.Agent();
                    giveLevelTilesWithPath(agent, levelPath);
                    AgentStats agentStats = game.runGame(agent, getLevel(levelPath), settings.timeLimit(), 0, false);
                    agentStats.level = levelSetName + "-" + i;
                    printStats(logWriter, agentStats, ((IAgentBenchmarkBacktrack) agent).getMostBacktrackedNodes());
                    logWriter.flush();
                }
            }
        }
    }

    private static void warmup() {
        System.out.println("WARMUP: " + AGENT_TYPE + "-original-1");
        String levelPath = "./levels/original/lvl-1.txt";
        AgentBenchmarkGame game = new AgentBenchmarkGame();
        IMarioAgentMFF agent = new mff.agents.astarGrid.Agent();
        giveLevelTilesWithPath(agent, levelPath);
        game.runGame(agent, getLevel(levelPath), 200, 0, false);
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
