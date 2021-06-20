package jack;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class App {
    static String help = "\nThree parameters must be entered in the following order:\n" +
            " source (-yelp | -ta)\n" +
            " # of results (# must divisible by 10, under 100, and cannot be 0)\n" +
            " location (-nyc for NYC vendors or 5-digit zip code)\n" + 
            "Example: ./reviewscrape -yelp 40 94038\n";

    public static void error(String err){
        System.out.println("Error: " + err + "\n" + help);
    }

    public static void main(String[] args) throws IOException {
        //CSV STUFF
        File file = new File("new.csv");
        FileWriter fileWriter = new FileWriter(file);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        String[] header = { "Name", "Address", "Website", "Email", "Cost", "Rating", "Gift Card (y/n)", "Reservations (y/n)", "Delivery (y/n)", "Source"};
        csvWriter.writeNext(header);

        //WEB SCRAPING
        int num = 0;
        String location = "";

        if(args[0].equals("-h")){
            System.out.println(help);
            System.exit(1);
        }
        if(args.length != 3) {
            error("Incorrect number of args.");
            //System.out.println("Error: Incorrect number of args.");
            System.exit(1);
        }
        try {
            num = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            error("Invalid number.");
            //System.out.println("Error: Invalid number.");
            System.exit(1);
        }
        if(num > 100){
            error("Number must be under 100.");
            // System.out.println("Error: Number must be under 100.");
            System.exit(1);
        } else if(num % 10 != 0 || num == 0) {
            error("Number is zero or not divisible by 10.");
            //System.out.println("Error: Number is zero or not a multiple of 10.");
            System.exit(1);
        }
        if(args[2].equalsIgnoreCase("-nyc")) location = "New%20York%2C%20NY";
        else {
            if(args[0].equals("-ta")){
                error("Tripadvisor source only takes -nyc location.");
                //System.out.println("Error: Tripadvisor source only takes -nyc location.");
                System.exit(1);
            }
            if(args[2].length() != 5){
                error("Invalid zip code.");
                //System.out.println("Error: Invalid zipcode.");
                System.exit(1);
            } else {
                try {
                    int zipTest = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    error("Invalid location.");
                    //System.out.println("Error: Invalid location.");
                    System.exit(1);
                }
            }
            location = args[2];
        }
        if(args[0].equals("-yelp")){
            YelpScraper yelp = new YelpScraper(num, location);
            csvWriter.writeAll(yelp.results());
        } else if(args[0].equals("-ta")){
            TAScraper trip = new TAScraper(num);
            csvWriter.writeAll(trip.results());
        } else {
            error("Invalid source.");
            //System.out.println("Error: Invalid source.");
            System.exit(1);
        }
        System.out.println("Results written to \"" + file.getName() + "\".");
        csvWriter.close();
    }
}
