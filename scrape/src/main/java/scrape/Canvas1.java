import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A class to be used to parse the website, scrape, and insert into a database.
 */
public class Canvas1 {
	/**
	 * Scrapes the canvas.net site for specific data and inserts it into a
	 * database.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws Exception {
		int i = 0;
		int j = 1;
///////////////////////////////////////DATABASE CONNECTION///////////////////////////////
		Class.forName("com.mysql.jdbc.Driver");
		String dbUrl = "jdbc:mysql://localhost:3306/moocs160";
		Connection conn = DriverManager.getConnection(dbUrl, "root", "");

		PreparedStatement preparedStatementData = conn
				.prepareStatement("insert into course_data"
						+ "(title, short_desc, long_desc, course_link,"
						+ "video_link, start_date, course_length, course_image,"
						
						+"category,site,course_fee,language,certificate,"
						+ "university,time_scraped) values"
						+ "( ?, ?, ?, ?, ?, ?, ?, ?, ?,? ,?, ?,?,?,?)");

		
		
		PreparedStatement preparedStatementDetails = conn
				.prepareStatement("insert into coursedetails(id, profname, profimage, course_id) values "
						+ "(?, ?, ?, ?)");

		String canvasUrl = "http://canvas.net";
		Document homePage = Jsoup.connect(canvasUrl).get();

//-------------------------------get course link --------------------------------------
		Elements courseLinks = homePage
				.select(".btn.btn-action.learn-more-button");
		
		// get short descriptions
		Elements shortDescs = homePage
				.select(".last.fineprint.pad-box-mini.top-rule-box.featured-course-desc");
		ArrayList<String> shortDescriptions = new ArrayList<String>();
		
		String sDesc = "";
		for (Element e : shortDescs) {
			sDesc = e.text();
			sDesc = sDesc.substring(0, 160);
			shortDescriptions.add(sDesc);
		}

//--------------------------MAKING LOOP FOR EACH ELEMENT------------------------------------
		
		
		Document coursePage;
		for (Element link : courseLinks) {
			// get course url
			String courseUrl = link.attr("href");
			coursePage = Jsoup.connect(courseUrl).get();

//-------------------------FOR LONG DESCRIPTION------------------------------------------
			Elements longDescs = coursePage
					.select("div[class=block-box two-thirds first-box] p");
			String longDescription = longDescs.text();

			// get video link`
			Elements vid = coursePage.select("div.block-box.two-thirds iframe");
			String videoLink = vid.attr("src");
			if(videoLink.length() > 0)
			{
				videoLink = "http:" + videoLink;
			}
			

//------------------------ FOR COURSE IMAGE-------------------------------------------------
			Element image = coursePage.select(
					"div[class=featured-course-image] span").get(0);

			String attr = image.attr("style");
			String courseImage = (canvasUrl + attr.substring(attr.indexOf("/"),
					attr.indexOf(")")));

// ---------------------------FOR COURSE NAME---------------------------------------------
			Elements course = coursePage.select("h2[class=emboss-light]");
			String courseName = course.text();
//----------------------------FOR COURSE FEE---------------------------------------------
			Elements university = coursePage.select("h4[class=emboss-light]");
			String universityName = university.text();
			
	       //  Elements fee = coursePage.select("strong[class=course-cost]");
			//int feeCourse = fee.size();
			 int courseFee = 0;
			
			 String language = "English";            
               
             String certificate = "yes"; 
//--------------------------FOR TIMESCRAPED--------------------------------------------             
			
			java.util.Date dtime = new java.util.Date(); 
           
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			Elements time = coursePage.select( "timeScraped");
            String timeScraped = sdf.format(dtime); 
         
			

//----------------------------FOR START DATE-----------------------------------------------
			Element start = coursePage.select(
					"div[class=course-detail-info]" + " > p > strong").get(0);
			String sd = start.text();

			boolean endDefined = true;
			String selfPacedPattern = "Self-paced, available (.*)";
			if (sd.matches(selfPacedPattern)) {
				sd = sd.replaceAll(selfPacedPattern, "$1");
				endDefined = false;
			}

			SimpleDateFormat dt = new SimpleDateFormat("MMM dd, yyyy");
			Date startDate = dt.parse(sd);
			DateTime dateTime1 = new DateTime(startDate);

//------------------------------FOR COURSE LENGHT-----------------------------------
			int courseLength;
			if (endDefined) {
				// get end date
				Element end = coursePage.select(
						"div[class=course-detail-info]" + " > p > strong").get(
						1);
				String ed = end.text();

				Date endDate = dt.parse(ed);

				// weeks
				DateTime dateTime2 = new DateTime(endDate);
				courseLength = Weeks.weeksBetween(dateTime1, dateTime2)
						.getWeeks();
			} else {
				courseLength = -1;
				//System.out.println("Undefine");
			}

			
			
			
			
			ArrayList<String> professors = new ArrayList<String>();
			ArrayList<String> instructorImages = new ArrayList<String>();	
			
//---------------------- ------FOR PROFESSOR NAME-----------------------------------------------
			Elements professor = coursePage
					.select("div[class=instructor-bio] h3");
			String p = "";
			
			for(Element e : professor)
			{
				p = e.text();
				p = p.substring(0, Math.min(p.length(), 30));
				professors.add(p);
			}
			
// ---------------------------------FOR INSTRUCTOR IMAGE ------------------------------------
			Elements instructorImage = coursePage
					.select("div[class=instructor-bio] img");
			for (Element e : instructorImage) 
			{
					instructorImages.add(e.attr("abs:src"));
			}

//-------------INSERT DATA INTO DATABASE SQL3_160---------------------------------------------
			preparedStatementData.setString(1, courseName);
			preparedStatementData.setString(2, shortDescriptions.get(i));
			preparedStatementData.setString(3, longDescription);
			preparedStatementData.setString(4, courseUrl);
			preparedStatementData.setString(5, videoLink);
			preparedStatementData.setDate(6,
					new java.sql.Date(startDate.getTime()));
			preparedStatementData.setInt(7, courseLength);
			preparedStatementData.setString(8, courseImage);
			preparedStatementData.setString(9, "");			
			preparedStatementData.setString(10, "Canvas");
			preparedStatementData.setInt(11, courseFee);
			preparedStatementData.setString(12, language);
			preparedStatementData.setString(13,certificate);
			preparedStatementData.setString(14, universityName);
			
			preparedStatementData.setString(15,timeScraped);
		
			
			
			
			

			preparedStatementData.executeUpdate();
			
			for(int k = 0; k < professors.size(); k++) {
				preparedStatementDetails.setInt(1, j);
				preparedStatementDetails.setString(2, professors.get(k));
				
				if(k < instructorImages.size())
				{
					preparedStatementDetails.setString(3, instructorImages.get(k));
				} else {
					preparedStatementDetails.setString(3, "");
				}
					j++;
					Statement stmt = conn.createStatement();
					String sql;
					sql = "SELECT id FROM course_data ORDER BY id DESC LIMIT 1";
					ResultSet rs = stmt.executeQuery(sql);

					int id = 0;
					while (rs.next())
					{
						id = rs.getInt("id");
					}
					preparedStatementDetails.setInt(4, id);
					preparedStatementDetails.executeUpdate();
			}
			i++;
		}
		conn.close();
	}
}


