package com.example.distancecalculatormod.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class CommandDistance extends CommandBase {
    private static final String PREFIX = "§1§l[Distance]§r: ";
    private static final String ERROR_PREFIX = "§c§l[Error]§r: ";
    private static final String EXAMPLE_PREFIX = "§d§l[Example]§r: ";
    private static final int REQUIRED_ARGUMENT_COUNT = 7;

    private enum DistanceMethod {
        EUCLIDEAN, MANHATTAN
    }

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
            sendErrorMessage(sender, "Usage: <x1> <y1> <z1> <x2> <y2> <z2> [euclidean/manhattan]",
                " /distance 392 -43 81 48 293 58 euclidean",
                " /distance 392 -43 81 48 293 58 manhattan");
            return;
        }

        double[] coordinates = parseCoordinates(sender, args);
        if (coordinates == null) {
            return;
        }

        String method = args.length == REQUIRED_ARGUMENT_COUNT ? args[6].toLowerCase() : "euclidean";
        DistanceMethod distanceMethod = getDistanceMethod(method);

        if (distanceMethod != null) {
            double distance = calculateDistance(coordinates, distanceMethod);
            String message = String.format("%sThe %s distance between (%.2f, %.2f, %.2f) and (%.2f, %.2f, %.2f) is %.2f blocks.",
                    PREFIX, distanceMethod, coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5], distance);
            sender.sendMessage(new TextComponentString(message));
        } else {
            sendErrorMessage(sender, "Invalid method. Please choose 'euclidean' or 'manhattan' as the last argument.");
        }
    }

    private void sendErrorMessage(ICommandSender sender, String... messages) {
        for (String message : messages) {
            sender.sendMessage(new TextComponentString(ERROR_PREFIX + message));
        }
    }

    private double[] parseCoordinates(ICommandSender sender, String[] args) {
        double[] coordinates = new double[6];
        for (int i = 0; i < 6; i++) {
            try {
                coordinates[i] = Double.parseDouble(args[i]);
            } catch (NumberFormatException e) {
                sendErrorMessage(sender, "Invalid input. Please provide valid numbers for coordinates.");
                return null;
            }
        }
        if (!isValidCoordinate(coordinates)) {
            sendErrorMessage(sender, "Coordinates are out of bounds.", getUsage(sender));
            return null;
        }
        return coordinates;
    }

    private boolean isValidCoordinate(double[] coordinates) {
        double minXYZ = -30_000_000;
        double maxXYZ = 30_000_000;
        double minY = -319;
        double maxY = 319;

        for (double value : coordinates) {
            if (value < minXYZ || value > maxXYZ) {
                return false;
            }
        }
        return coordinates[1] >= minY && coordinates[1] <= maxY;
    }

    private DistanceMethod getDistanceMethod(String method) {
        try {
            return DistanceMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private double calculateDistance(double[] coordinates, DistanceMethod method) {
        switch (method) {
            case EUCLIDEAN:
                return calculateEuclideanDistance(coordinates);
            case MANHATTAN:
                return calculateManhattanDistance(coordinates);
            default:
                return 0;
        }
    }

    private double calculateEuclideanDistance(double[] coordinates) {
        double dx = coordinates[3] - coordinates[0];
        double dy = coordinates[4] - coordinates[1];
        double dz = coordinates[5] - coordinates[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private double calculateManhattanDistance(double[] coordinates) {
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
            for (DistanceMethod method : DistanceMethod.values()) {
                if (method.name().toLowerCase().startsWith(arg)) {
                    completions.add(method.name().toLowerCase());
                }
            }
        } else if (args.length == 8) {
            completions.addAll(Arrays.asList("<x1>", "<y1>", "<z1>", "<x2>", "<y2>", "<z2>"));
        }
        return completions;
    }
}
