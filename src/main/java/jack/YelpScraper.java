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
        for(int start = 0; start < numResults; start += 10) {
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
            int sponsored = 0;
            System.out.print("Scraping results " + String.valueOf(start+1) + " - " + String.valueOf(start+10));
            Elements links = page.select("div.container__09f24__21w3G");
            for(Element link : links) {
                sponsored++;
                if(sponsored < 3) continue;
                else if (sponsored == 13) break;
                String[] info = new String[10]; //Array for adding to CSV
                Document site = null;
                try { //Connecting to restaurant's Yelp page
                    site = Jsoup.connect("https://www.yelp.com" + link.selectFirst("a.css-166la90").attr("href")).get();
                } catch (Exception e) {
                    System.out.println("failed to connect");
                }
                Element n = site.selectFirst("div.padding-b2__373c0__34gV1"); //div containing website, phone #, address
                Element amenDiv = null;
                Elements amenities = null;
                ArrayList<String> amenList = null;
                try { //For finding amenities
                    amenDiv = site.selectFirst(".layout-wrap__373c0__34d4b");
                    amenities = amenDiv.select("span.css-1h1j0y3");
                    amenList = new ArrayList<String>();
                    for(Element e : amenities) {
                        amenList.add(e.text());
                    }
                } catch (Exception e){

                }
                info[0] = link.selectFirst("a.css-166la90").text(); //Merchant Name
                try { //Address
                    info[1] = n.selectFirst("p.css-e81eai").text();
                } catch (Exception e) {
                    info[1] = "failed to fetch address";
                }
                try { //Website
                    Element website = n.selectFirst("a");
                    if(!(website.attr("href").substring(0, 4).equals("/biz"))) info[2] = "no website";
                    else info[2] = n.selectFirst("a").text();
                } catch (Exception e) {
                    info[2] = "failed to fetch site";
                }
                try { //Cost
                    info[4] = link.selectFirst(".priceRange__09f24__2O6le").text();
                } catch (NullPointerException e) {
                    info[4] = "failed to fetch cost";
                }
                try { //Rating
                    info[5] = link.selectFirst(".i-stars__09f24__1T6rz").attr("aria-label").replace(" star rating", "");
                } catch (NullPointerException e) {
                    info[5] = "failed to fetch rating";
                }
                try {
                    if(amenList.contains("Takes Reservations")) info[7] = "y";
                    else info[7] = "n";
                } catch (Exception e){
                    info[7] = "n";
                }
                try {
                    if(amenList.contains("Offers Delivery")) info[8] = "y";
                    else info[8] = "n";
                } catch (Exception e){
                    info[8] = "n";
                }
                info[9] = "Yelp"; //Source
                /*try { //Number of reviews
                    info[2] = link.selectFirst(".reviewCount__09f24__EUXPN").text();
                } catch (NullPointerException e) {

                }*/
                scrapeResults.add(info);
                //csvWriter.writeNext(info);
                System.out.print(".");
            }
            System.out.println("");
        }
        System.out.println("Success!");
        return scrapeResults;
    }
}
