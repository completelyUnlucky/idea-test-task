package com.boris.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Main {

    private static final File file = new File("src/main/resources/tickets.json");

    public static long getTimeInMinutes(String time1, String time2) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

        Date date1 = dateFormat.parse(time1);
        Date date2 = dateFormat.parse(time2);

        long differenceInMilliseconds = date2.getTime() - date1.getTime();

        return differenceInMilliseconds / (60 * 1000);
    }

    public static Map<String, String> getMinTime() throws Exception {
        Map<String, String> map = new HashMap<>();

        InputStream fileReader = Main.class.getResourceAsStream("/tickets.json");

        BufferedReader bufferedFileReader =
                new BufferedReader(new InputStreamReader(Objects.requireNonNull(fileReader)));

        JsonNode jsonNode = new ObjectMapper().readTree(bufferedFileReader);

        for (JsonNode node : jsonNode.get("tickets")) {

            String time1 = node.get("departure_date").asText() + " " + node.get("departure_time").asText();
            String time2 = node.get("arrival_date").asText() + " " + node.get("arrival_time").asText();

            if (node.get("origin").asText().equals("VVO") &&
                    node.get("destination").asText().equals("TLV")) {
                if (map.get(node.get("carrier").asText()) != null &&
                        Long.parseLong(map.get(node.get("carrier").asText())) < getTimeInMinutes(time1, time2)) {
                    map.replace(node.get("carrier").asText(), String.valueOf(getTimeInMinutes(time1, time2)));
                } else {
                    map.put(node.get("carrier").asText(), Long.toString(getTimeInMinutes(time1, time2)));
                }
            }
        }
        return map;
    }

    public static long getMedianAndAverageDiff() throws Exception {

        InputStream fileReader = Main.class.getResourceAsStream("/tickets.json");

        BufferedReader bufferedFileReader =
                new BufferedReader(new InputStreamReader(Objects.requireNonNull(fileReader)));

        JsonNode jsonNode = new ObjectMapper().readTree(bufferedFileReader);

        long median = 0L;
        long sum = 0L;

        for (JsonNode node : jsonNode.get("tickets")) {

            if (node.get("origin").asText().equals("VVO") &&
                    node.get("destination").asText().equals("TLV")) {
                if (jsonNode.get("tickets").size() % 2 == 1) {
                    median = Long.parseLong(String.valueOf(jsonNode.get("tickets").get(jsonNode.get("tickets").size() / 2).get("price")));
                } else {
                    median = (Long.parseLong(String.valueOf(jsonNode.get("tickets").get(jsonNode.get("tickets").size() / 2).get("price"))) +
                            Long.parseLong(String.valueOf(jsonNode.get("tickets").get(jsonNode.get("tickets").size() / 2 + 1).get("price")))) / 2;
                }
                sum += Long.parseLong(String.valueOf(node.get("price")));
            }
        }

        long average = sum / jsonNode.get("tickets").size();

        return average - median;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getMinTime());
        System.out.println(getMedianAndAverageDiff());
    }

}