package com.mkyong.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Controller
public class BaseController {

	private static int counter = 0;
	private static final String VIEW_INDEX = "index";
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	private static final java.io.File DATA_STORE_DIR =
		      new java.io.File(System.getProperty("user.home"), ".store/calendar_sample");
	
	private static final String CLIENT_ID = "972686849948-mfbvtg9uhl6ecag9234qkc4l4gp8b50p.apps.googleusercontent.com";
	private static final String CLIENT_SECRET = "aLj8gLOSzMCPoUfmDS9FfA_h";	
	 private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static com.google.api.services.calendar.Calendar client;

	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome(ModelMap model) {

		model.addAttribute("message", "Welcome");
		model.addAttribute("counter", ++counter);
		logger.debug("[welcome] counter : {}", counter);

		// Spring uses InternalResourceViewResolver and return back index.jsp
		return VIEW_INDEX;

	}
	
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String welcome() {
		ModelMap model = new ModelMap();
		model.addAttribute("message", "You have the following events coming up .. ! ");
		model.addAttribute("message", "You have the following events coming up .. ! ");
		model.addAttribute("counter", ++counter);
		logger.debug("[welcome] counter : {}", counter);

		// Spring uses InternalResourceViewResolver and return back index.jsp
		return VIEW_INDEX;

	}
	
	@RequestMapping(value = "/plussample", method = RequestMethod.GET)
	public String welcome_direct(ModelMap model) {

		model.addAttribute("message", "Welcome");
		model.addAttribute("counter", ++counter);
		logger.debug("[welcome] counter : {}", counter);

		// Spring uses InternalResourceViewResolver and return back index.jsp
		return VIEW_INDEX;

	}


	@RequestMapping(value = "/{name}", method = RequestMethod.GET)
	public String welcomeName(@PathVariable String name, ModelMap model) {

		model.addAttribute("message", "Welcome " + name);
		model.addAttribute("counter", ++counter);
		logger.debug("[welcomeName] counter : {}", counter);
		return VIEW_INDEX;

	}
	
	
@RequestMapping("/plussampleservlet")
public @ResponseBody void CallSampleServlet( 
        HttpServletRequest request, HttpServletResponse response) throws IOException{
		
	String message = "<br><div style='text-align:center;'>"
				+ "<h3>********** Hello World from plus sample servlet, Spring MVC Tutorial</h3>This message is coming from CrunchifyHelloWorld.java **********</div><br><br>";
	    
    
		 	GoogleAuthorizationCodeFlow authFlowCalendar = Utils.initializeFlowCalendar();
		    /*UserService userService = UserServiceFactory.getUserService();
		    
		    
		    
		    User user = userService.getCurrentUser();
		    Credential credential_calendar = authFlowCalendar.loadCredential(user.getUserId());
		    */
		    
		    
		    response.sendRedirect(authFlowCalendar.newAuthorizationUrl().setRedirectUri(Utils.getRedirectUri(request)).build());	   
		    return ;
		    
		    //----Credential credential = authFlow.loadCredential(Utils.getUserId(req));
		
	}

/*@RequestMapping("/finalcallback")
public @ResponseBody String set_service( 
        HttpServletRequest request, HttpServletResponse response) throws IOException{


}    */


@RequestMapping("/redirect")
public @ResponseBody String CallSampleServlet_sub( 
        HttpServletRequest request, HttpServletResponse response) throws IOException{
	final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
	GoogleAuthorizationCodeFlow authFlow = Utils.initializeFlowCalendar();
	HttpTransport httpTransport=new NetHttpTransport();
	  JsonFactory jsonFactory=new JacksonFactory();
	String code= request.getParameter("code");
	System.out.println("code "+ code);
	
	final Collection<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile;https://www.googleapis.com/auth/userinfo.email;https://www.googleapis.com/auth/calendar".split(";"));
	
	
	GoogleAuthorizationCodeFlow flow=new GoogleAuthorizationCodeFlow.Builder(httpTransport,jsonFactory,CLIENT_ID,CLIENT_SECRET,Collections.singleton(CalendarScopes.CALENDAR)).setAccessType("online").setApprovalPrompt("auto").build();
	//GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPE).build();
	  GoogleTokenResponse resp 
	  =flow.newTokenRequest(code).setRedirectUri("http://localhost:8080/counterwebapp/redirect").execute();
	  System.out.println((resp.getAccessToken()));
	  //GoogleCredential credential=new GoogleCredential().setFromTokenResponse(resp);
	  GoogleTokenResponse resp2 = new GoogleTokenResponse();
	  resp2.setAccessToken(resp.getAccessToken());
			  
	  final Credential credential = flow.createAndStoreCredential((TokenResponse) resp2, null);
	  System.out.println("credential created: "+ credential);
	  /*com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
		        Utils.HTTP_TRANSPORT_CAL, Utils.JSON_FACTORY_CAL, credential)
		        .setApplicationName("API Demo")
		        .build();*/
      // final Credential cred = flow.createAndStoreCredential(response, null);
	 	
	  
	  
	    //Credential credential_calendar = authFlow.loadCredential(Utils.getUserId(req));
	    com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
	            httpTransport, jsonFactory, credential)
	            .setApplicationName("Spring API Demo")
	            .build();	
	    
	    System.out.println("<h1>HELLO WORLD Prafull</h1>");

	    DateTime now = new DateTime(System.currentTimeMillis());
	    System.out.println("<h2>" + service.getApplicationName() + "</h2>");
	    CalendarList feed = service.calendarList().list().execute();
	    Events even = service.events().list("primary").setMaxResults(10).setTimeMin(now).setOrderBy("startTime").setSingleEvents(true).execute();
	    com.google.api.services.calendar.model.Events events = service.events().list("primary")
	            .setMaxResults(10)
	            .setTimeMin(now)
	            .setOrderBy("startTime")
	            .setSingleEvents(true)
	            .execute();
	    System.out.println("No upcoming events first found.");
	    List<Event> items = events.getItems();
	    if (items.size() == 0) {
	        System.out.println("No upcoming events found.");
	    } else {
	    	System.out.println("Upcoming events");
	        for (Event event : items) {
	            DateTime start = event.getStart().getDateTime();
	            if (start == null) {
	                start = event.getStart().getDate();
	            }
	            System.out.printf("%s (%s)\n", event.getSummary(), start);
	        }
	    } 
	  final HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
		// Make an authenticated request
		final GenericUrl url = new GenericUrl(USER_INFO_URL);
		final HttpRequest req = requestFactory.buildGetRequest(url);
		req.getHeaders().setContentType("application/json");
		final String jsonIdentity = req.execute().parseAsString();
		final String jsonIdentit1y = req.execute().parseAsString();

		Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
		          "Oauth2").build();
		 Userinfoplus userinfo = oauth2.userinfo().get().execute();
		 System.out.println(userinfo.toPrettyString());
		
		 com.google.appengine.repackaged.com.google.gson.Gson usergson = new com.google.appengine.repackaged.com.google.gson.Gson();
		 Userinfoplus user = usergson.fromJson(jsonIdentity, Userinfoplus.class);
		 System.out.println(user.getFamilyName()+ "  "+ user.getGivenName()+ " "+ user.getEmail());
		System.out.println(jsonIdentity);
		return VIEW_INDEX;
		
		
	// Exchange authorization code for user credentials.
	/*GoogleTokenResponse tokenResponse = authFlow.newTokenRequest(request.getParameter("code"))
	    .setRedirectUri(Utils.getRedirectUri(request)).execute();
	// Save the credentials for this user so we can access them from the main servlet.
	authFlow.createAndStoreCredential(tokenResponse, Utils.getUserId(request));
	response.sendRedirect(Utils.MAIN_SERVLET_PATH);
*/
}
	

	@RequestMapping(value="/oauth2callback*",method=RequestMethod.GET,params="code")
	public String CallSampleredirectServlet( 
	        HttpServletRequest request, HttpServletResponse response) throws IOException{ 
		/*String message = "<br><div style='text-align:center;'>"
				+ "<h3>********** Hello World, Spring MVC Tutorial</h3>This message is coming from CrunchifyHelloWorld.java **********</div><br><br>";*/
		String message = null;
	    DateTime now = new DateTime(System.currentTimeMillis());
	    logger.debug("[welcomeName] counter : {}", ++counter);
	    logger.debug("code : {}", request.getParameter("code"));
		
	
	    GoogleAuthorizationCodeFlow authFlow = Utils.initializeFlowCalendar();
	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser(); 
	    Credential credential_calendar = authFlow.loadCredential(user.getUserId());
	    GoogleTokenResponse tokenResponse = authFlow.newTokenRequest(request.getParameter("code"))
	    	    .setRedirectUri(Utils.getRedirectUri(request)).execute();
	    	// Save the credentials for this user so we can access them from the main servlet.
	    	authFlow.createAndStoreCredential(tokenResponse, Utils.getUserId(request));
	    	response.sendRedirect(Utils.MAIN_SERVLET_PATH);   
	    
	    
	    //respWriter.println("<h2>" + service.getApplicationName() + "</h2>");

	        
	        com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
	            Utils.HTTP_TRANSPORT_CAL, Utils.JSON_FACTORY_CAL, credential_calendar)
	            .setApplicationName("API Demo")
	            .build();
	        


	        return VIEW_INDEX;
	        
	    /*
	   CalendarList feed = service.calendarList().list().execute();
	    Events even = service.events().list("primary").setMaxResults(10).setTimeMin(now).setOrderBy("startTime").setSingleEvents(true).execute();
	    
	    com.google.api.services.calendar.model.Events events = service.events().list("primary")
	        .setMaxResults(10)
	        .setTimeMin(now)
	        .setOrderBy("startTime")
	        .setSingleEvents(true)
	        .execute();
	        
	    List<Event> items = events.getItems();
	   
	    return VIEW_INDEX;
	    /*
	    if (items.size() == 0) {
	       // respWriter.println("No upcoming events found.");
	    } else {
	        //respWriter.println("Upcoming events");
	        for (Event event : items) {
	            DateTime start = event.getStart().getDateTime();
	            if (start == null) {
	                start = event.getStart().getDate();
	            }
	            //respWriter.printf("%s (%s)\n", event.getSummary(), start);
	        }
	    }
	 
*/		/*return new ModelAndView("welcome", "message", message);*/
	}

	@RequestMapping("/checklogin")
	public @ResponseBody String newcallback( 
	        HttpServletRequest request, HttpServletResponse response) throws IOException, GeneralSecurityException{
			
		String message = "<br><div style='text-align:center;'>"
					+ "<h3>********** Hello World from plus sample servlet, Spring MVC Tutorial</h3>This message is coming from CrunchifyHelloWorld.java **********</div><br><br>";
		    
	    
		String APPLICATION_NAME = "PlusSample";
		java.io.File DATA_STORE_DIR =
		      new java.io.File(System.getProperty("user.home"), ".store/plus_sample");
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);;

		// Set up the HTTP transport and JSON factory
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		// Load client secrets
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
		    new InputStreamReader(BaseController.class.getResourceAsStream("/client_secrets.json")));
		
		// set up authorization code flow
	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	        httpTransport, JSON_FACTORY, clientSecrets,
	        Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(dataStoreFactory)
	        .build();
	    
	 // Authorize
	    //http://localhost:8080/counterwebapp/oauth2callback
	    //LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost("http://localhost:8080/counterwebapp/oauth2callback").setPort(8080).build();
	    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
	 Credential credential =
	     new AuthorizationCodeInstalledApp(flow,receiver).authorize("user");
		
	// Set up the main Google+ class
	 Plus plus = new Plus.Builder(httpTransport, jsonFactory, credential)
	     .setApplicationName("Spring API Demo")
	     .build();

	 // Make a request to access your profile and display it to console
	 Person profile = plus.people().get("me").execute();
	 /*System.out.println("ID: " + profile.getId());
	 System.out.println("Name: " + profile.getDisplayName());
	 System.out.println("Image URL: " + profile.getImage().getUrl());
	 System.out.println("Profile URL: " + profile.getUrl());*/
	 
	 logger.debug("ID : ",profile.getId());
	 logger.debug("Name : ",profile.getDisplayName());
	 logger.debug("[welcomeName] counter : {}", ++counter);
	
		return VIEW_INDEX;
			    //----Credential credential = authFlow.loadCredential(Utils.getUserId(req));
			
		}
	
	
	@RequestMapping("/simplelogin")
	public @ResponseBody void simplemethod( 
	        HttpServletRequest request, HttpServletResponse response) throws IOException, GeneralSecurityException{
	String REDIRECT_URI = "http://localhost:8080/counterwebapp/redirect";
	HttpTransport httpTransport=new NetHttpTransport();
	  JsonFactory jsonFactory=new JacksonFactory();
	  final Collection<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile;https://www.googleapis.com/auth/userinfo.email;https://www.googleapis.com/auth/calendar".split(";"));
	  GoogleAuthorizationCodeFlow flow=new GoogleAuthorizationCodeFlow.Builder(httpTransport,jsonFactory,CLIENT_ID,CLIENT_SECRET,SCOPE).setAccessType("online").setApprovalPrompt("auto").build();
	  //GoogleAuthorizationCodeFlow flow=new GoogleAuthorizationCodeFlow.Builder(httpTransport,jsonFactory,CLIENT_ID,CLIENT_SECRET,Collections.singleton(CalendarScopes.CALENDAR)).setAccessType("online").setApprovalPrompt("auto").build();
	  String url=flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
	  System.out.println("Please open the following URL in your browser then type the authorization code:");
	  System.out.println("  " + url);	
	  response.sendRedirect(
			  flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build());
	  return;
	  /*String code= request.getParameter("code");
	  System.out.println("  " + code);	
	  GoogleTokenResponse resp=flow.newTokenRequest(code).setRedirectUri("http://localhost:8080/counterwebapp/redirect").execute();
	  GoogleCredential credential=new GoogleCredential().setFromTokenResponse(resp);
	  return VIEW_INDEX;*/
	  //googleDriveClient=new Drive.Builder(httpTransport,jsonFactory,credential).build();
	}
}