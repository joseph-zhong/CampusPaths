package com.example.campuspaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import hw8.CampusBuilding;
import hw8.CampusPath;
import hw8.Coordinate;


/**
 * Methods to parse the campus data files using methods available on Android
 * (which does not support OpenCSV).
 */
public class AndroidParser {

    // Not an ADT

    /**
     * Returns the rows from the given file, each of which should match the
     * shape defined by CampusBuilding.
     * @param blgsStream Stream containing the TSV file to read. (This should
     *    be a relative path from the root directory.)
     * @throws IOException if any I/O error occurs reading the file
     * @return List of CampusPath objects, one describing each row.
     */
    public static List<CampusBuilding> parseBuildingData(InputStream blgsStream)
            throws IOException {
        Scanner scanner = new Scanner(new InputStreamReader(blgsStream));
        scanner.nextLine();     // Skipping header line
        List<CampusBuilding> buildings = new LinkedList<>();
        while(scanner.hasNextLine()) {
            String[] bits = scanner.nextLine().split("\t");
            String shortName = bits[0];
            String longName = bits[1];
            Coordinate location = new Coordinate(Double.parseDouble(bits[2].split(",")[0]),
                    Double.parseDouble(bits[2].split(",")[1]));
            CampusBuilding building = new CampusBuilding();
            building.setShortName(shortName);
            building.setLongName(longName);
            building.setLocation(location);
            buildings.add(building);
        }

        return buildings;
    }

    /**
     * Returns the rows from the given file, each of which should match the
     * shape defined by CampusPath.
     * @param pathsStream Path to the TSV file to read. (This should be a
     *    relative path from the root directory.)
     * @throws IOException if any I/O error occurs reading the file
     * @return List of CampusPath objects, one describing each row.
     */
    public static List<CampusPath> parsePathData(InputStream pathsStream)
            throws IOException {
        Scanner scanner = new Scanner(new InputStreamReader(pathsStream));
        scanner.nextLine();     // Skipping header line
        List<CampusPath> paths = new LinkedList<>();
        while(scanner.hasNextLine()) {
            String[] bits = scanner.nextLine().split("\t");
            Coordinate origin = new Coordinate(Double.parseDouble(bits[0].split(",")[0]),
                    Double.parseDouble(bits[0].split(",")[1]));
            Coordinate destination = new Coordinate(Double.parseDouble(bits[1].split(",")[0]),
                    Double.parseDouble(bits[1].split(",")[1]));
            Double distance = Double.parseDouble(bits[2]);
            CampusPath path = new CampusPath();
            path.setDestination(destination);
            path.setDistance(distance);
            path.setOrigin(origin);
            paths.add(path);
        }

        return paths;
    }

}
