package jack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class YelpScraper {
    int numResults;
    String location;

    YelpScraper(int numResults, String location){
        this.numResults = numResults;
        this.location = location;
    }

    public ArrayList<String[]> results(){
        ArrayList<String[]> scrapeResults = new ArrayList<String[]>();
        String url;
        int leftover = numResults % 10;
        int numPages;
        if(numResults % 10 == 0) numPages = numResults;
        else numPages = (numResults / 10 + 1) * 10 ; //Round up
        for(int start = 0; start < numPages; start += 10) {
            url = "https://www.yelp.com/search?find_desc=Restaurants&find_loc=" + location + "&ns=1&start=" + String.valueOf(start);
            Document page = null;
            try { //Connecting to site
                page = Jsoup.connect(url).get();
            } catch(Exception e) { //Can't connect --> Wait 10 secs and retry
                System.out.println("Cannot reach service. ");
                try {
                    System.out.print("Retrying... ");
                    Thread.sleep(10000);
                    page = Jsoup.connect(url).get();
                } catch(Exception e1) {
                    System.out.println("Failed");
                    System.exit(1);
                }
                System.out.println("Success");
            }

            Elements links = page.select("div.container__09f24__sxa9-");
            int num;
            if(start + 10 > numResults) num = leftover;
            else num = links.size() - 3;
            System.out.print("Scraping results " + String.valueOf(start+1) + " - " + String.valueOf(start + num));
            for(int i = 2; i < num + 2; i++) {
                String[] info = new String[10]; //Array for adding to CSV
                Document site = null;
                Element link = links.get(i);
                try { //Connecting to restaurant's Yelp page
                    site = Jsoup.connect("https://www.yelp.com" + link.selectFirst("a.css-166la90").attr("href")).get();
                } catch (Exception e) {
                    System.out.println("failed to connect");
                }
                Element n = null;
                try {
                    n = site.selectFirst("body");
                } catch (Exception e) {

                }

                info[0] = link.selectFirst("a.css-166la90").text(); //Merchant Name - FIXED
                try { //Address - FIXED
                    info[1] = n.selectFirst("p.css-chtywg").text();;
                } catch (Exception e) {
                    info[1] = "failed to fetch address";
                }
                try { //Website - FIXED
                    Elements webLinks = site.select("a.css-ac8spe");
                    info[2] = "failed to fetch site";
                    for(Element e : webLinks){
                        if(e.attr("target").equals("_blank")) {
                            info[2] = e.text();
                            break;
                        }
                    }
                } catch (Exception e) {
                    info[2] = "failed to fetch site";
                }
                try { //Cost - FIXED
                    info[4] = link.selectFirst(".priceRange__09f24__2GspP").text();
                } catch (NullPointerException e) {
                    info[4] = "failed to fetch cost";
                }
                try { //Rating - FIXED
                    info[5] = link.selectFirst(".i-stars__09f24___sZu0").attr("aria-label").replace(" star rating", "");
                } catch (NullPointerException e) {
                    info[5] = "failed to fetch rating";
                }
                /*try { //Gift card - IN PROGRESS
                    if(link.selectFirst(".raw__09f24__3Azrj").text().equals) info[6] = "y";
                    else info[6] = "n";
                } catch (Exception e) {
                    info[6] = "n";
                }*/

                Element amenDiv = null;
                Elements amenities = null;
                ArrayList<String> amenList = null;
                try { //For finding amenities
                    amenDiv = site.selectFirst(".layout-2-units__373c0__7D8Tx");
                    amenities = amenDiv.select("span.css-1h1j0y3");
                    amenList = new ArrayList<String>();
                    for(Element e : amenities) {
                        amenList.add(e.text());
                    }
                } catch (Exception e){

                }
                try { //Reservations - FIXED
                    if(amenList.contains("Takes Reservations")) info[7] = "y";
                    else info[7] = "n";
                } catch (Exception e){
                    info[7] = "n";
                }
                try { //Delivery - FIXED
                    if(amenList.contains("Offers Delivery")) info[8] = "y";
                    else info[8] = "n";
                } catch (Exception e){
                    info[8] = "n";
                }
                info[9] = "Yelp"; //Source

                scrapeResults.add(info);
                System.out.print(".");
            }
            System.out.println("");
        }
        System.out.println("Success!");
        return scrapeResults;
    }
}
