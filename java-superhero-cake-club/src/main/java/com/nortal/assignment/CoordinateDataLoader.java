package com.nortal.assignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nortal.assignment.model.Rooftop;
import com.nortal.assignment.model.Superhero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Joosep Lall.
 */
public class CoordinateDataLoader {
    private static final Logger LOG = LoggerFactory.getLogger(CoordinateDataLoader.class);

    private List<Rooftop> rooftops = new ArrayList<>();
    private List<Superhero> superheroes = new ArrayList<>();

    public CoordinateDataLoader() {
    }

    /**
     * Read superhero and rooftop coordinate data.
     * Input file is expected to have the following format
     * <ul>
     * <li>First line contains single numeric value (n) - count of superheroes</li>
     * <li>Following n lines contain superhero data: name;x;y</li>
     * <li>Rest of the lines contain rooftops data: building name;x;y</li>
     * </ul>
     *
     * <b>Sample:</b>
     * 2
     * Superman;10;10
     * Batman;45;23
     * Central Perk;25;33
     * Moe's;44;30
     *
     */
    public void readDataFromFile(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int numHeroes = 0;
        int count = 0;
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (count == 0) {
                    numHeroes = Integer.parseInt(line);
                    count++;
                    continue;
                }
                List<String> item = Arrays.asList(line.split(";"));

                if (count <= numHeroes) {
                    superheroes.add(new Superhero(item.get(0), Double.parseDouble(item.get(1)), Double.parseDouble(item.get(2))));
                } else {
                    rooftops.add(new Rooftop(item.get(0), Double.parseDouble(item.get(1)), Double.parseDouble(item.get(2))));
                }
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("{} superheroes and {} rooftops read from stream.", superheroes.size(), rooftops.size());
    }

    public List<Rooftop> getRooftops() {
        return rooftops;
    }

    public List<Superhero> getSuperheroes() {
        return superheroes;
    }
}
