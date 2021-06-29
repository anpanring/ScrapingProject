# Scraping Project

## What it does
* Currently supports two sources: Yelp and Tripadvisor.
* Parses restaurants at specified location and returns following information:
  * Vendor/business name
  * Address/location
  * Website
  * Email for general business or business owner
  * Cost (in $ signs)
  * Average rating
  * If the vendor sells gift cards (y/n)
  * If the vendor takes reservations (y/n)
  * If the vendor offers delivery (y/n)
  * Source of data (Yelp/Tripadvisor)
  
## How to run
From within the project directory, use the command ./reviewscrape with these three
parameters in the following order:
1. Source: options are either -yelp for Yelp or -ta for Tripadvisor
2. Number of results: any number that is divisible by 10, under 100, and not 0.
3. Location: Can either use -nyc for New York businesses or specify a 5-digit
zip code.

Example: ./reviewscrape -yelp 40 94038  
This command would return a .csv file with 40 results from where I live in
Moss Beach.

 * ./reviewscrape -h
    * Will show instructions and example on how to run program

Results will be written to file "new.csv" in the project folder.

## What I would do next with more time
 * Find gift card information
 * Find email info from Yelp
 * Try to speed up the program by reorganizing the code
 * More error checking
 
 ## Caveats
 I was unable to get access to any of the APIs for Google Business, Tripadvisor, etc.
 so this project only uses web scraping. Being able to use these APIs would help speed up
 the program significantly and keep it from being dependent on the websites' current HTML/CSS
 organization, so it is definitely I would like to incorporate into my project if I could gain
 access. I also had trouble scraping information regarding gift cards from both sites, as well
 as email information from Yelp, so these sections are left blank in the CSV. These are 
 issues I will work on fixing in the future.
