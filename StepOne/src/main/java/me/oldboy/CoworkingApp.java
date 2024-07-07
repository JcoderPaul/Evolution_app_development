package me.oldboy;

import me.oldboy.output.cli.CoworkingCli;

/**
 * Main class for run coworking system application.
 */
public class CoworkingApp {
    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        CoworkingCli.getInstance();
        CoworkingCli.getInterface();
    }
}
