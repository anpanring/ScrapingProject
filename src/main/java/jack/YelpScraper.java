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
        else numPages = (numResults / 10 + 1) * 10 ; //10
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
            //int sponsored = 0;
            Elements links = page.select("div.container__09f24__sxa9-");
            int num;
            if(start + 10 > numResults) num = leftover;
            else num = links.size() - 3;
            System.out.print("Scraping results " + String.valueOf(start+1) + " - " + String.valueOf(start + num));
            for(int i = 3; i < num + 3; i++) {
                //if(i < 3) continue;
                //if (i == 13) break;
                String[] info = new String[10]; //Array for adding to CSV
                Document site = null;
                Element link = links.get(i);
                try { //Connecting to restaurant's Yelp page
                    //String website_address = "https://www.yelp.com" + link.selectFirst("a.css-166la90").attr("href");
                    //info[2] = website_address;
                    site = Jsoup.connect("https://www.yelp.com" + link.selectFirst("a.css-166la90").attr("href")).get();
                } catch (Exception e) {
                    System.out.println("failed to connect");
                }
                Element n = null;
                try {
                    n = site.select("div.padding-t2__373c0__3FhIz").get(4);
                } catch (Exception e) {

                }
                //Element n = site.selectFirst("div.padding-b2__373c0__34gV1"); //div containing website, phone #, address
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
                info[0] = link.selectFirst("a.css-166la90").text(); //Merchant Name
                String address = "";
                //Element address;
                //Elements spans;
                try { //Address
                    address = n.selectFirst("p.css-chtywg").text();
                    /*address = address + link.selectFirst("p.css-1bmgof7 > span").text()
                            + ", " + link.selectFirst("p.css-znumc2 > span").text();
                    address = n.selectFirst("address");
                    spans = address.select("p > span");
                    StringBuilder addy = new StringBuilder();
                    System.out.println("Span size: " + spans.size());
                    for(Element span : spans){
                        addy.append(span.text());
                    }*/
                    info[1] = address;
                } catch (Exception e) {
                    //System.out.println("Failed to fetch address");
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
                System.out.print(".");
            }
            System.out.println("");
        }
        System.out.println("Success!");
        return scrapeResults;
    }
}
