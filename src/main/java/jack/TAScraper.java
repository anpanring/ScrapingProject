package jack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Base64;

public class TAScraper {
    int numResults;

    TAScraper(int numResults) {
        this.numResults = numResults;
    }

    public ArrayList<String[]> results() {
        ArrayList<String[]> scrapeResults = new ArrayList<String[]>();
        int numPages = numResults / 30 + 1;
        int leftOver = numResults % 30;
        String url;
        int counter = 0;
        for(int k = 0; k < numPages; k++){
            if(k == 0) url = "https://www.tripadvisor.com/Restaurants-g60763-New_York_City_New_York.html";
            else url = "https://www.tripadvisor.com/Restaurants-g60763-oa" + k*30 + "-New_York_City_New_York.html";
            Document page = null;
            try { //Connecting to site
                page = Jsoup.connect(url).get();
            } catch (Exception e) { //Can't connect --> Wait 10 secs and retry
                System.out.println("Cannot reach service. ");
                try {
                    System.out.print("Retrying... ");
                    Thread.sleep(10000);
                    page = Jsoup.connect(url).get();
                } catch (Exception e1) {
                    System.out.println("Failed");
                    System.exit(1);
                }
                System.out.println("Success");
            }
            int num;
            if(numResults < 30) num = numResults + 1;
            else if(counter % 30 == 0 && counter + 30 > numResults){
                num = counter + leftOver + 1;
            }
            else num = (k+1)*30+1;
            Element list = page.selectFirst("div._1kXteagE");
            for(int i = k*30+1; i < num; i++) {
                String[] info = new String[10]; //Array for adding to CSV
                Element item;
                try {
                    item = list.selectFirst("[data-test='" + String.valueOf(i) + "_list_item']");
                } catch (Exception e3) {
                    System.out.println("Error: Location only has " + i + " restaurants.");
                    break;
                }
                Document site = null;
                try {
                    site = Jsoup.connect("https://www.tripadvisor.com/" + item.selectFirst("a._15_ydu6b").attr("href")).get();
                } catch (Exception e) {
                    System.out.print("Error: Could not connect to site.");
                    System.exit(1);
                }
                if(i % 10 == 1) System.out.print("Scraping results " + String.valueOf(i) + " - " + String.valueOf(i+9) + ".");
                else if(i % 10 == 0) System.out.print(".\n");
                else System.out.print(".");
                Element n = null;
                try {
                    n = site.selectFirst(".page");
                } catch (Exception e) {
                    n = site;
                }
                try { //NAME
                    info[0] = n.selectFirst("._3a1XQ88S").text();
                } catch (Exception e) {

                }
                try { //ADDRESS
                    info[1] = n.selectFirst("[href='#MAPVIEW']").text();
                } catch (Exception e) {

                }
                try { //WEBSITE AND EMAIL
                    Element emailDiv = n.selectFirst("div._105c0u5l");
                    Elements websiteEmail = emailDiv.select("div._36TL14Jn > span > a");
                    for(Element ele : websiteEmail) {
                        String link = ele.attr("href");
                        if (link.startsWith("mailto:")) {
                            link = link.replace("?subject=?", "");
                            link = link.replace("mailto:", "");
                            info[3] = link;
                        }
                        else {
                            byte[] decodedBytes = Base64.getDecoder().decode(ele.attr("data-encoded-url"));
                            String decodedString = new String(decodedBytes);
                            decodedString = decodedString.replace(decodedString.substring(decodedString.length()-4), "");
                            decodedString = decodedString.replace(decodedString.substring(0, 4), "");
                            info[2] = decodedString;
                        }
                    }
                } catch (Exception e) {

                }
                try { //PRICE
                    Element priceBar = n.selectFirst("._1ud-0ITN");
                    Elements priceStuff = priceBar.select("a");
                    for(Element e : priceStuff) {
                        String tmp = e.text();
                        if(tmp.startsWith("$")) info[4] = tmp;
                    }
                } catch (Exception e) {

                }
                try { //RATING
                    info[5] = n.selectFirst("span.r2Cf69qf").text();
                } catch (Exception e) {

                }
                info[7] = "n";
                info[8] = "n";
                try { //RESERVATIONS/DELIVERY
                    Element resDiv = n.selectFirst("._1IradaSj");
                    Elements resEles = resDiv.select("._2571uoYJ");
                    for(Element e : resEles) {
                        if(e.text().equals("Reserve a table")) info[7] = "y";
                        else if(e.text().equals("Get food delivered")) info[8] = "y";
                    }

                } catch (Exception e) {

                }
                info[9] = "Tripadvisor"; //SOURCE
                scrapeResults.add(info);
                counter++;
            }
        }
        System.out.println("Success!");
        return scrapeResults;
    }
}
