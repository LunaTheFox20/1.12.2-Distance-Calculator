package com.example.distancecalculatormod.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

public class CommandDistance extends CommandBase {

    private static final String PREFIX = "§1§l[Distance]§r: ";
    private static final String ERROR_PREFIX = "§c§l[Error]§r: ";
    private static final String EXAMPLE_PREFIX = "§d§l[Example]§r: ";
    private static final int REQUIRED_ARGUMENT_COUNT = 7;

    private static final String[] VALID_METHODS = {"euclidean", "manhattan"};

    private static final Map<String, DistanceCalculator> CALCULATORS = new HashMap<>();
    static {
        CALCULATORS.put("euclidean", CommandDistance::calculateEuclideanDistance);
        CALCULATORS.put("manhattan", CommandDistance::calculateManhattanDistance);
    }

    private static final double MIN_XZ = -30_000_000;
    private static final double MAX_XZ = 30_000_000;
    private static final double MIN_Y = -319;
    private static final double MAX_Y = 319;

    @Override
    public String getName() {
        return "distance";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /distance <x1> <y1> <z1> <x2> <y2> <z2> [euclidean/manhattan]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < REQUIRED_ARGUMENT_COUNT - 1 || args.length > REQUIRED_ARGUMENT_COUNT) {
            String errorMessage = String.format("%sUsage: <x1> <y1> <z1> <x2> <y2> <z2> [euclidean/manhattan]\n%s /distance 392 -43 81 48 293 58 euclidean\n%s /distance 392 -43 81 48 293 58 manhattan",
                ERROR_PREFIX, EXAMPLE_PREFIX, EXAMPLE_PREFIX);
            sender.sendMessage(new TextComponentString(errorMessage));
            return;
        }
    
        double[] coordinates = new double[6];
        for (int i = 0; i < 6; i++) {
            try {
                coordinates[i] = Double.parseDouble(args[i]);
            } catch (NumberFormatException e) {
                String errorMessage = String.format("%sInvalid input. Please provide valid numbers for coordinates.", ERROR_PREFIX);
                sender.sendMessage(new TextComponentString(errorMessage));
                return;
            }
        }
    
        String method = args.length == REQUIRED_ARGUMENT_COUNT ? args[6] : "euclidean";
        method = method.toLowerCase();
    
        if (isValidCoordinate(coordinates)) {
            if (CALCULATORS.containsKey(method)) {
                double distance = CALCULATORS.get(method).calculate(coordinates);
                String message = String.format("%sThe %s distance between (%.2f, %.2f, %.2f) and (%.2f, %.2f, %.2f) is %.2f blocks.",
                    PREFIX, method, coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5], distance);
                sender.sendMessage(new TextComponentString(message));
            } else {
                String errorMessage = String.format("%sInvalid method. Please choose 'euclidean' or 'manhattan' as the last argument.", ERROR_PREFIX);
                sender.sendMessage(new TextComponentString(errorMessage));
            }
        } else {
            String errorMessage = String.format("%sCoordinates are out of bounds. %s", TextFormatting.RED, getUsage(sender));
            sender.sendMessage(new TextComponentString(errorMessage));
        }
    }

    private boolean isValidCoordinate(double[] coordinates) {
        for (double value : coordinates) {
            if (value < MIN_XZ || value > MAX_XZ) {
                return false;
            }
        }
        return coordinates[1] >= MIN_Y && coordinates[1] <= MAX_Y;
    }

    private interface DistanceCalculator {
        double calculate(double[] coordinates);
    }

    private static double calculateEuclideanDistance(double[] coordinates) {
        double dx = coordinates[3] - coordinates[0];
        double dy = coordinates[4] - coordinates[1];
        double dz = coordinates[5] - coordinates[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double calculateManhattanDistance(double[] coordinates) {
        return Math.abs(coordinates[3] - coordinates[0]) + Math.abs(coordinates[4] - coordinates[1]) + Math.abs(coordinates[5] - coordinates[2]);
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, net.minecraft.util.math.BlockPos targetPos) {
        List<String> completions = Lists.newArrayList();
        if (args.length == 7) {
            String arg = args[6].toLowerCase();
            for (String method : VALID_METHODS) {
                if (method.startsWith(arg)) {
                    completions.add(method);
                }
            }
        } else if (args.length == 8) {
            completions.addAll(Arrays.asList("<x1>", "<y1>", "<z1>", "<x2>", "<y2>", "<z2>"));
        }
        return completions;
    }
}
