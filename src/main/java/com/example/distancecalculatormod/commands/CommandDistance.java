package com.example.distancecalculatormod.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class CommandDistance extends CommandBase {

    // Constants for chat message prefixes, required argument count and the Logger initialization
    private static final String PREFIX = "§1§l[Distance]§r: ";
    private static final String ERROR_PREFIX = "§c§l[Error]§r: ";
    private static final String EXAMPLE_PREFIX = "§d§l[Example]§r: ";
    private static final int REQUIRED_ARGUMENT_COUNT = 7;
    private static final int DEFAULT_VALUE = 6;

    @Override
    public String getName() {
        return "distance";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /distance <x1> <y1> <z1> <x2> <y2> <z2> <euclidean/manhattan>";
    }

    @Override
public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    if (args.length < REQUIRED_ARGUMENT_COUNT - 1 || args.length > REQUIRED_ARGUMENT_COUNT) {
        sender.sendMessage(new TextComponentString(ERROR_PREFIX + "Usage: <x1> <y1> <z1> <x2> <y2> <z2> [euclidean/manhattan]\n" + EXAMPLE_PREFIX + " /distance 392 -43 81 48 293 58 euclidean\n" + EXAMPLE_PREFIX + " /distance 392 -43 81 48 293 58 manhattan"));
        return;
    }

    double x1 = Double.parseDouble(args[0]);
    double y1 = Double.parseDouble(args[1]);
    double z1 = Double.parseDouble(args[2]);
    double x2 = Double.parseDouble(args[3]);
    double y2 = Double.parseDouble(args[4]);
    double z2 = Double.parseDouble(args[5]);
    String method = args.length == REQUIRED_ARGUMENT_COUNT ? args[6] : "euclidean";

    if (isValidCoordinate(x1, y1, z1) && isValidCoordinate(x2, y2, z2)) {
        double distance = 0.0;

        if (method.equalsIgnoreCase("euclidean")) {
            distance = calculateEuclideanDistance(x1, y1, z1, x2, y2, z2);
        } else if (method.equalsIgnoreCase("manhattan")) {
            distance = calculateManhattanDistance(x1, y1, z1, x2, y2, z2);
        } else {
            sender.sendMessage(new TextComponentString(ERROR_PREFIX + "Invalid input. Please provide valid numbers for coordinates or choose 'euclidean' or 'manhattan' as the last argument."));
            return;
        }

        sender.sendMessage(new TextComponentString(PREFIX + "The " + method + " distance between (" + x1 + ", " + y1 + ", " + z1 + ") and (" + x2 + ", " + y2 + ", " + z2 + ") is ~" + String.format("%.2f", distance) + " blocks."));
    } else {
        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Coordinates are out of bounds. " + getUsage(sender)));
    }

    }

    private boolean isValidCoordinate(double x, double y, double z) {
        return x >= -30_000_000 && x <= 30_000_000 &&
               y >= -319 && y <= 319 &&
               z >= -30_000_000 && z <= 30_000_000;
    }

    private double calculateEuclideanDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        // Calculate Euclidean distance
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private double calculateManhattanDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        // Calculate Manhattan distance
        return Math.abs(x2 - x1) + Math.abs(y2 - y1) + Math.abs(z2 - z1);
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
            if ("e".startsWith(arg)) {
                completions.add("euclidean");
            }
            if ("m".startsWith(arg)) {
                completions.add("manhattan");
            }
        } else if (args.length == 8) {
            completions.addAll(Arrays.asList("<x1>", "<y1>", "<z1>", "<x2>", "<y2>", "<z2>"));
        }
        return completions;
    }
}
