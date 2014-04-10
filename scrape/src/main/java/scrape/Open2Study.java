package scrape;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.jdbc.Statement;




public class Open2Study {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		Document doc;
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/moocs160?zeroDateTimeBehavior=convertToNull","root","");

		try {
	 
			// need http protocol
			doc = Jsoup.connect("https://www.open2study.com/courses").get();
	 
			// get all links
			Elements links = doc.select("a[href^=/node]");
			Elements images = doc.select("img.image-style-course-logo-subjects-block"); //get all images for each course
			ArrayList<String> listOfLinks = new ArrayList<String>();
			ArrayList<String> courseImages = new ArrayList<String>();
			
			//put each link into a list
			for (Element link : links) {
				 
				listOfLinks.add(link.absUrl("href"));
			}
			
			//put each image into the list
			for(Element imgs: images){
				courseImages.add(imgs.attr("src"));

			}
			
			int imageIndex = 0;
			
			
			//Get data for all the individual courses
			for(String s: listOfLinks){
				
				Statement statement = (Statement) connection.createStatement();
				
				doc = Jsoup.connect(s).get(); //connect to each course page
				
				//Get course title
				String crstitle = doc.select("h1").first().text();
				crstitle = crstitle.split("\\(")[0]; //clean up course title
				System.out.println("Title: "+ crstitle.toString());
				
				//Get the course's short description
				String short_description = doc.select("h3").first().text();
				short_description = short_description.replace("'", "''");
				System.out.println("Short_Desc: " + short_description);
				
				//Get the course's long description
				String long_description = doc.select(".full-body").first().text();
				long_description = long_description.replace("'", "''");
				System.out.println("Full description: "+ long_description);
				
				//Get course URL
				String crsurl = s; 
				System.out.println("URL: " + crsurl);
				
				//Get the video link of the course
				String video_link = doc.select("iframe.media-youtube-player").attr("src");
				System.out.println("Video Link: "+ video_link);
				
				//Try to see if the course has a start date and set it if does. If it doesn't,
				//print "No start date.".
				java.sql.Date sqlStartDate;
				int courseLength;
				try{
					DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
					DateTime startTime = fmt.parseDateTime(doc.select("h2.offering_dates_date").first().text());
					Date startDate = startTime.toDate();
					sqlStartDate = new java.sql.Date(startDate.getTime());
					System.out.println("Start Date: " + sqlStartDate);
					
					Elements times = doc.select("h2.offering_dates_date");
					DateTime endTime = fmt.parseDateTime(times.get(1).text());

					courseLength = Weeks.weeksBetween(startTime, endTime).getWeeks();
					System.out.println("Course Length: " + courseLength);
				}
				catch(NullPointerException e){
					//Date startDate = new java.util.Date("0000/00/00");
					sqlStartDate = null; //new java.sql.Date(startDate.getTime());
					courseLength = 0;
				}
				
				//Get course image
				String course_image = courseImages.get(imageIndex);
				imageIndex++; 
				System.out.println("Course Image: " + course_image);
				
				//All video's are from Open2Study
				String site = "Open2Study";
				System.out.println("Site: " + site);
				
				//All the courses listed are Free.
				int course_fee = 0;
				System.out.println("Course Fee: " + course_fee);
				
				//All the courses are taught in English
				String language = "English";
				System.out.println("Course language: " + language);
				
				String certificate = "yes";
				
				//Time scraped. 
				java.util.Date dt = new java.util.Date();
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String timeScraped = sdf.format(dt);
				System.out.println("Time Scraped: " + timeScraped);
				
				//university.
				String university = doc.select("div#provider-logo").select("a").attr("href");
				university = university.substring(university.lastIndexOf("/") + 1).trim();
				university = university.replace("-", " ");
				System.out.println("University: "+ university);
				
				String query = "insert into course_data values(null,'"+crstitle+"','"+short_description+"','"+long_description+"','"+crsurl+"','"+video_link+"','"+sqlStartDate+"','"+courseLength+"','"+course_image+"','category','"+site+"', '"+course_fee+"', '"+language+"', '"+certificate+"', '"+university+"', '"+timeScraped+"');";
				if(sqlStartDate == null){
					query = "insert into course_data values(null,'"+crstitle+"','"+short_description+"','"+long_description+"','"+crsurl+"','"+video_link+"','0000-00-00','"+courseLength+"','"+course_image+"','category','"+site+"', '"+course_fee+"', '"+language+"', '"+certificate+"', '"+university+"', '"+timeScraped+"');";
				}
				System.out.println(query);
				statement.executeUpdate(query);// skip writing to database; focus on data printout to a text file instead.
				
				String query1 = "SELECT * FROM course_data ORDER BY id DESC LIMIT 1;";
				ResultSet rs = statement.executeQuery(query1);
				int lastID = 0;
				while(rs.next()){
					lastID = rs.getInt(1);
				}
				
				
				
				
				//For second table
				//Get professor names and images
				Elements profImages = doc.select("img.image-style-teacher-small-profile"); 
				ArrayList<String> professorImages = new ArrayList<String>();
				
				
				Elements profNames = doc.select("li.views-row.views-row-1.tri-views-col-1.views-row-odd.views-row-first.views-row-last");
				Elements profNames1 = doc.select("li.views-row.views-row-1.tri-views-col-1.views-row-odd.views-row-first.views-row-first");
				Elements profNames2 = doc.select("li.views-row.views-row-2.tri-views-col-2.views-row-odd.views-row-first.views-row-last");
				Elements profNames3 = doc.select("li.views-row.views-row-2.tri-views-col-2.views-row-even.views-row-last");
				
				ArrayList<String> professorNames = new ArrayList<String>();
				
				for(Element imgs: profImages){
					professorImages.add(imgs.attr("src"));
				}
				
				for(Element names: profNames){
					professorNames.add(names.select("h3").text());
				}
				
				for(Element names: profNames1){
					professorNames.add(names.select("h3").text());
				}
				for(Element names: profNames2){
					professorNames.add(names.select("h3").text());
				}
				for(Element names: profNames3){
					professorNames.add(names.select("h3").text());
				}
				
				for(int i =0; i < professorNames.size() & i < professorImages.size(); i++){
					System.out.println("Professor" + i + " Name: " + professorNames.get(i));
					System.out.println("Professor" + i + " Image: " + professorImages.get(i));
					String query2 = "insert into coursedetails values(null,'"+professorNames.get(i)+"','"+professorImages.get(i)+"','"+lastID+"');";
					System.out.println(query2);
					statement.executeUpdate(query2);
				}
				
				System.out.println("----------------------------------------------");
				statement.close(); 
			}
			
			
			
			
			
			
			
			
			
	 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//connection.close();
	 
  }
}
