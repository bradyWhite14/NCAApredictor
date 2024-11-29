/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.ncaachamppred;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class NCAAchampPred {

    // Define the Team class with relevant stats
    static class Team {
        String school;
        double wlPct, SRS, SOS, ORtg, Pace, eFG, TOV, TRB;

        public Team(String school, double wlPct, double SRS, double SOS, double ORtg, double Pace, double eFG, double TOV, double TRB) {
            this.school = school;
            this.wlPct = wlPct;
            this.SRS = SRS;
            this.SOS = SOS;
            this.ORtg = ORtg;
            this.Pace = Pace;
            this.eFG = eFG;
            this.TOV = TOV;
            this.TRB = TRB;
        }

         public double calculateScore() {
    double offenseScore = (ORtg / 120) * 0.25 + (eFG / 0.60) * 0.15; // Normalize ORtg and eFG to expected ranges
    double defenseScore = (SRS / 25) * 0.30 + (TRB / 50) * 0.10; 
    double turnoverPenalty = (1 - TOV / 20) * 0.20; // Higher turnover rate reduces score
    double strengthAdjustment = (SOS / 10) * 0.20;

    return wlPct * 0.25 + offenseScore + defenseScore + strengthAdjustment + turnoverPenalty;
}

    }

    public static void main(String[] args) {
        String url = "https://www.sports-reference.com/cbb/seasons/men/2025-advanced-school-stats.html"; // Per Game Stats URL

        List<Team> teams = new ArrayList<>();

        try {
            // Connect to the URL and fetch the HTML document
            Document doc = Jsoup.connect(url).get();

            // Locate the stats table
            Element table = doc.getElementById("adv_school_stats");
            if (table == null) {
                System.out.println("Table not found.");
                return;
            }

            // Extract the headers from the table
            Elements headers = table.select("thead tr th");
            List<String> headerList = new ArrayList<>();
            for (Element header : headers) {
                headerList.add(header.text());
            }

            // Debug: Print all headers to check the column names
            System.out.println("Table Headers:");
            for (String header : headerList) {
                System.out.println(header);
            }

            // Extract rows from the table
            Elements rows = table.select("tbody tr");
            for (Element row : rows) {
                Elements columns = row.select("td");
                
                // Skip rows with insufficient data
                if (columns.size() < 26) continue;

                // Extract and debug data for each column
                String school = columns.get(0).text(); // School Name
                double wlPct = parseDoubleSafely(columns.get(4).text());
                double SRS = parseDoubleSafely(columns.get(5).text()); // Assuming SRS is the 6th column (index 5)
                double SOS = parseDoubleSafely(columns.get(6).text()); // Assuming SOS is the 7th column (index 6)
                double ORtg = parseDoubleSafely(columns.get(21).text()); // Adjust ORtg index
                double Pace = parseDoubleSafely(columns.get(20).text()); // Adjust Pace index
                double eFG = parseDoubleSafely(columns.get(29).text()); // Adjust eFG index
                double TOV = parseDoubleSafely(columns.get(30).text()); // Adjust TOV index
                double TRB = parseDoubleSafely(columns.get(25).text()); // Adjust TRB index

                // Debug: Print extracted values for each row
                System.out.println("School: " + school);
                System.out.println("W-L%: " + wlPct + ", SRS: " + SRS + ", SOS: " + SOS);
                System.out.println("ORtg: " + ORtg + ", Pace: " + Pace + ", eFG: " + eFG);
                System.out.println("TOV: " + TOV + ", TRB: " + TRB);
                System.out.println("-----------------------------------------------------");

                // Add a Team object to the list
                teams.add(new Team(school, wlPct, SRS, SOS, ORtg, Pace, eFG, TOV, TRB));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (teams.isEmpty()) {
            System.out.println("No valid data available to predict a champion.");
            return;
        }

        // Sort teams by score in descending order
        teams.sort(Comparator.comparingDouble(Team::calculateScore).reversed());

        // Display top 10 teams
        System.out.println("Top 10 Teams:");
        for (int i = 0; i < 10 && i < teams.size(); i++) {
            Team team = teams.get(i);
            System.out.printf("%d. %s - Score: %.2f%n", i + 1, team.school, team.calculateScore());
        }

        // Predict champion
        System.out.println("\nPredicted NCAA Champion: " + teams.get(0).school);
    }

    // Helper method to safely parse doubles from strings
    private static double parseDoubleSafely(String value) {
        value = value.replace("\"", "").trim();
        if (value.isEmpty()) {
            return 0.0;  // Handle empty or missing values
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;  // Return 0 for invalid values
        }
    }
}
