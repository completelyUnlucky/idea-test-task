package com.boris.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {

    public static long getTimeInMinutes(String time1, String time2) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

        Date date1 = dateFormat.parse(time1);
        Date date2 = dateFormat.parse(time2);

        long differenceInMilliseconds = date2.getTime() - date1.getTime();

        return differenceInMilliseconds / (60 * 1000);
    }

    public static Map<String, String> getMinTime() throws Exception {
        Map<String, String> minTimeMap = new HashMap<>();

        InputStream fileReader = Main.class.getResourceAsStream("/tickets.json");

        BufferedReader bufferedFileReader =
                new BufferedReader(new InputStreamReader(Objects.requireNonNull(fileReader)));

        JsonNode jsonNode = new ObjectMapper().readTree(bufferedFileReader);

        for (JsonNode node : jsonNode.get("tickets")) {

            String time1 = node.get("departure_date").asText() + " " + node.get("departure_time").asText();
            String time2 = node.get("arrival_date").asText() + " " + node.get("arrival_time").asText();

            if (node.get("origin").asText().equals("VVO") &&
                    node.get("destination").asText().equals("TLV")) {
                if (minTimeMap.get(node.get("carrier").asText()) != null) {
                      if (Long.parseLong(minTimeMap.get(node.get("carrier").asText())) > getTimeInMinutes(time1, time2)) {
                          minTimeMap.replace(node.get("carrier").asText(), String.valueOf(getTimeInMinutes(time1, time2)));
                      }
                } else {
                    minTimeMap.put(node.get("carrier").asText(), Long.toString(getTimeInMinutes(time1, time2)));
                }
            }
        }
        minTimeMap.replaceAll((carrier, minutes) -> String.format("%dh %dm",
                Integer.parseInt(minutes) / 60,
                Integer.parseInt(minutes) % 60)
        );
        return minTimeMap;
    }

    public static int getMedianAndAverageDiff() throws Exception {

        InputStream fileReader = Main.class.getResourceAsStream("/tickets.json");
        BufferedReader bufferedFileReader =
                new BufferedReader(new InputStreamReader(Objects.requireNonNull(fileReader)));
        JsonNode jsonNode = new ObjectMapper().readTree(bufferedFileReader);

        List<Integer> prices = new ArrayList<>();

        for (JsonNode node : jsonNode.get("tickets")) {
            if (node.get("origin").asText().equals("VVO") && node.get("destination").asText().equals("TLV")) {
                prices.add(Integer.parseInt(node.get("price").asText()));
            }
        }

        int average = prices.stream().mapToInt(Integer::intValue).sum() / prices.size();
        int median = prices.stream()
                .sorted()
                .skip(prices.size() / 2)
                .findFirst()
                .get();

        return average - median;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getMinTime());
        System.out.println(getMedianAndAverageDiff());
    }

}