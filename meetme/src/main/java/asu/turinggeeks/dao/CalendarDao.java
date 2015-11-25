package asu.turinggeeks.dao;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import asu.turinggeeks.mapper.CalendarEventRowMapper;
import asu.turinggeeks.mapper.CalendarRowMapper;
import asu.turinggeeks.mapper.EndSlotMapper;
import asu.turinggeeks.mapper.OptionalSlotMapper;
import asu.turinggeeks.mapper.RequiredSlotMapper;
import asu.turinggeeks.mapper.StartSlotMapper;
import asu.turinggeeks.model.Calendar;

@Repository
public class CalendarDao {

	@Autowired
	DataSource dataSource;
	String query = null;
	JdbcTemplate jdbcTemplate;

	public boolean insertForManualCalendar(String[] start, String[] end, Calendar calendar, String emailId,
			String uuid) {
		int length = start.length;
		try {
			query = "INSERT INTO event_info "
					+ "(event_name, email_id, event_description, guest_required_email, guest_optional_email, uuid) VALUES(?,?,?,?,?,?)";
			jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.update(query, new Object[] { calendar.getEventName(), emailId, calendar.getEventDescription(),
					calendar.getGuestRequiredEmail(), calendar.getGuestOptionalEmail(), uuid });

			query = "SELECT event_id from event_info where uuid=?";
			int eventId = jdbcTemplate.queryForObject(query, new Object[] { uuid }, Integer.class);

			query = "INSERT into event_time_details " + "(event_id,start_time,end_time) VALUES(?,?,?)";
			for (int i = 0; i < length; i++) {
				jdbcTemplate.update(query, new Object[] { eventId, start[i], end[i] });
			}
			
			query = "INSERT into required_counter " + " (uuid,counter) VALUES (?,?)";
			jdbcTemplate.update(query, new Object[] { uuid, 0 });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean insertForGoogleCalendar(String[] start, String[] end, Calendar calendar, String emailId,
			String uuid) {
		try {
			query = "INSERT INTO event_info "
					+ "(event_name, email_id, event_description, start_date_google , end_date_google, start_time_google, end_time_google, uuid) VALUES(?,?,?,?,?,?)";
			jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.update(query,
					new Object[] { calendar.getEventName(), emailId, calendar.getEventDescription(),
							calendar.getStartTime(), calendar.getEndTime(), calendar.getStartTime(),
							calendar.getEndTime(), uuid });

			/*
			 * query = "SELECT event_id from event_info where uuid=?"; int
			 * eventId = jdbcTemplate.queryForObject(query, new Object[] {uuid},
			 * Integer.class);
			 * 
			 * query = "INSERT into event_time_details " +
			 * "(event_id,start_time,end_time) VALUES(?,?,?)"; for (int i = 0; i
			 * < length; i++) { jdbcTemplate.update(query, new Object[] {
			 * eventId, start[i], end[i] }); }
			 */

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean insertForGoogleCalendar(List<Calendar> calendar, String emailId) {
		try {

			jdbcTemplate = new JdbcTemplate(dataSource);
			int count = 0;

			String uuid;
			for (Calendar event : calendar) {
				uuid = emailId + event.getStartTime() + event.getEventDescription();
				query = "Select count(*) from gcalendar where email_id_google=? and start_time_google=?";
				System.out.println("out in the forloop " + uuid);
				count = jdbcTemplate.queryForObject(query, new Object[] { emailId, event.getStartTime() },
						Integer.class);
				uuid = emailId + event.getStartTime();
				if (count == 0) {
					System.out.println("in the if in the forloop " + uuid);
					query = "INSERT INTO gcalendar "
							+ "(uuid_google, event_name_google, email_id_google, event_description_google, start_date_google , end_date_google, start_time_google, end_time_google ) VALUES(?,?,?,?,?,?,?,?)";
					jdbcTemplate.update(query,
							new Object[] { uuid, event.getEventName(), emailId, event.getEventDescription(),
									event.getStartTime(), event.getEndTime(), event.getStartTime(),
									event.getEndTime() });
				}

			}

			/*
			 * query = "SELECT event_id from event_info where uuid=?"; int
			 * eventId = jdbcTemplate.queryForObject(query, new Object[] {uuid},
			 * Integer.class);
			 * 
			 * query = "INSERT into event_time_details " +
			 * "(event_id,start_time,end_time) VALUES(?,?,?)"; for (int i = 0; i
			 * < length; i++) { jdbcTemplate.update(query, new Object[] {
			 * eventId, start[i], end[i] }); }
			 */
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getFirstName(String email) {
		query = "Select first_name from user_info where email_id=?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		String firstName = jdbcTemplate.queryForObject(query, new Object[] { email }, String.class);
		return firstName;

	}

	public int getEventId(String uuid) {
		query = "SELECT event_id from event_info where uuid=?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		int eventId = jdbcTemplate.queryForObject(query, new Object[] { uuid }, Integer.class);
		return eventId;
	}

	@SuppressWarnings("unchecked")
	public List<Calendar> getAllEventDetails(int eventId) {
		List<Calendar> probableTimings = null;
		query = "select EI.event_name, EI.event_description, ET.start_time, ET.end_time from event_info as EI, event_time_details as ET where EI.event_id = ET.event_id and EI.event_id=?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		probableTimings = jdbcTemplate.query(query, new Object[] { eventId }, new CalendarRowMapper());
		return probableTimings;
	}

	public void storeRequiredUserResponse(String guestRequiredMail, String[] checkedTimings, String uuid) {
		query = "INSERT into guest_required_response " + " (uuid, guest_required_email,preferred_time) VALUES (?,?,?)";
		jdbcTemplate = new JdbcTemplate(dataSource);
		for (int i = 0; i < checkedTimings.length; i++) {
			jdbcTemplate.update(query, new Object[] { uuid, guestRequiredMail, checkedTimings[i] });
		}
		query = "Select counter from required_counter where uuid=?";
		int counterValue = jdbcTemplate.queryForObject(query, new Object[] { uuid }, Integer.class);
		query = "Update required_counter SET counter = ? where uuid=?";
		jdbcTemplate.update(query, new Object[] { counterValue + 1, uuid });

	}

	public void storeOptionalUserResponse(String guestOptionalMail, String[] checkedTimings, String uuid) {
		query = "INSERT into guest_optional_response " + " (uuid, guest_optional_email,preferred_time) VALUES (?,?,?)";
		jdbcTemplate = new JdbcTemplate(dataSource);
		for (int i = 0; i < checkedTimings.length; i++) {
			jdbcTemplate.update(query, new Object[] { uuid, guestOptionalMail, checkedTimings[i] });
		}
	}

	public String checkUserType(String guestMail, String uuid) {
		query = "SELECT guest_required_email from event_info where uuid=?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		String requiredPerson = jdbcTemplate.queryForObject(query, new Object[] { uuid }, String.class);
		String[] splitRequired = requiredPerson.split(",");
		for (int i = 0; i < splitRequired.length; i++) {
			splitRequired[i] = splitRequired[i].trim();
		}
		boolean rFlag = false;
		for (int i = 0; i < splitRequired.length; i++) {
			if (guestMail.equalsIgnoreCase(splitRequired[i])) {
				rFlag = true;
				break;
			}
		}

		query = "SELECT guest_optional_email from event_info where uuid=?";
		String optionalPerson = jdbcTemplate.queryForObject(query, new Object[] { uuid }, String.class);
		String[] splitOptional = optionalPerson.split(",");
		for (int i = 0; i < splitOptional.length; i++) {
			splitOptional[i] = splitOptional[i].trim();
		}
		boolean oFlag = false;
		for (int i = 0; i < splitOptional.length; i++) {
			if (guestMail.equalsIgnoreCase(splitOptional[i])) {
				oFlag = true;
				break;
			}
		}
		if (rFlag)
			return "required";
		else if (oFlag)
			return "optional";
		else
			return "absent";
	}

	public String getUuid(String emailId) {
		query = "SELECT uuid from event_info where email_id=?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		String uuid = jdbcTemplate.queryForObject(query, new Object[] { emailId }, String.class);
		return uuid;
	}

	@SuppressWarnings("unchecked")
	public List<Calendar> getAllEvents(String emailId) {
		List<Calendar> allEvents = null;
		query = "select event_id, event_name, event_description, uuid from event_info where email_id=?;";
		jdbcTemplate = new JdbcTemplate(dataSource);
		allEvents = jdbcTemplate.query(query, new Object[] { emailId }, new CalendarEventRowMapper());
		return allEvents;
	}

	@SuppressWarnings("unchecked")
	public List<Calendar> getStartSlot(int eventId) {
		List<Calendar> startSlot = null;
		query = "select start_time from event_time_details where event_id=?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		startSlot = jdbcTemplate.query(query, new Object[] { eventId }, new StartSlotMapper());
		return startSlot;
	}

	@SuppressWarnings("unchecked")
	public List<Calendar> getEndSlot(int eventId) {
		List<Calendar> endSlot = null;
		jdbcTemplate = new JdbcTemplate(dataSource);
		query = "select end_time from event_time_details where event_id=?";
		endSlot = jdbcTemplate.query(query, new Object[] { eventId }, new EndSlotMapper());
		return endSlot;
	}

	@SuppressWarnings("unchecked")
	public List<Calendar> getRequiredSlot(String uuid) {
		List<Calendar> requiredSlot = null;
		jdbcTemplate = new JdbcTemplate(dataSource);
		query = "select preferred_time from guest_required_response where uuid=?";
		requiredSlot = jdbcTemplate.query(query, new Object[] { uuid }, new RequiredSlotMapper());
		return requiredSlot;
	}

	@SuppressWarnings("unchecked")
	public List<Calendar> getOptionalSlot(String uuid) {
		List<Calendar> optionalSlot = null;
		jdbcTemplate = new JdbcTemplate(dataSource);
		query = "select preferred_time from guest_optional_response where uuid=?";
		optionalSlot = jdbcTemplate.query(query, new Object[] { uuid }, new OptionalSlotMapper());
		return optionalSlot;
	}

	public int getRequiredCounter(String uuid) {
		query = "Select guest_required_email from event_info where uuid=?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		String mail = jdbcTemplate.queryForObject(query, new Object[] { uuid }, String.class);
		String[] splitRequired = mail.split(",");
		return splitRequired.length;
	}

	public int getResponseCounter(String uuid) {
		query = "Select counter from required_counter where uuid=?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		int counter = jdbcTemplate.queryForObject(query, new Object[] { uuid }, Integer.class);
		return counter;
	}

	@SuppressWarnings("unchecked")
	public JSONObject fetchCalendarData(String emailId) {
		List<Calendar> calendarData = null;
		jdbcTemplate = new JdbcTemplate(dataSource);
		JSONArray jArray = new JSONArray();
		query = "select EI.event_name, EI.event_description, ET.start_time, ET.end_time from event_info as EI, event_time_details as ET where EI.event_id = ET.event_id and EI.email_id=?";
		calendarData = jdbcTemplate.query(query, new Object[] { emailId }, new CalendarRowMapper());
		for (int i = 0; i < calendarData.size(); i++) {
			String eventNameJson = calendarData.get(i).getEventName();
			String eventStartTimeJson = calendarData.get(i).getStartTime();
			String eventEndTimeJson = calendarData.get(i).getEndTime();
			JSONObject jObj = new JSONObject();
			jObj.put("title", eventNameJson);
			jObj.put("start", eventStartTimeJson);
			jObj.put("end", eventEndTimeJson);
			jArray.add(jObj);
		}
		JSONObject jObjDevice = new JSONObject();
		jObjDevice.put("device", jArray);
		JSONObject jObjDeviceList = new JSONObject();
		jObjDeviceList.put("devicelist", jObjDevice);
		try {
			FileWriter fileWriter = new FileWriter("D:\\data.json");
			fileWriter.write(jObjDeviceList.toJSONString());
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Zafar");
		return jObjDeviceList;
	}

	public boolean insertforGoogleEvent(String[] start, String[] end, Calendar calendar, String emailId,
			String eventUuid) {

		try {
			query = "INSERT INTO google_event_info "
					+ "(google_event_uuid, email_id, google_event_name, google_event_description, guest_required_email,guest_optional_email) VALUES(?,?,?,?,?,?)";
			jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.update(query,
					new Object[] { eventUuid, emailId, calendar.getEventName(), calendar.getEventDescription(),
							calendar.getGuestRequiredEmail(), calendar.getGuestOptionalEmail() });

			query = "INSERT into google_time_details " + "(google_event_uuid,start_time,end_time) VALUES(?,?,?)";
			for (int i = 0; i < start.length; i++) {
				jdbcTemplate.update(query, new Object[] { eventUuid, start[i], end[i] });
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	@SuppressWarnings("unchecked")
	public List<Calendar> getGoogleUserStartSlot(Calendar calendar) {
		List<Calendar> userStartSlot1 = null;
		List<Calendar> start1 = new ArrayList<Calendar>();
		List<Calendar> userStartSlot2 = null;
		List<Calendar> start2 = new ArrayList<Calendar>();
		String required = calendar.getGuestRequiredEmail();
		String optional = calendar.getGuestOptionalEmail();
		String[] splitRequired = required.split(",");
		String[] splitOptional = optional.split(",");
		query = "select start_time_google from gcalendar where email_id_google = ?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		for (int i = 0; i < splitRequired.length; i++) {
			userStartSlot1 = jdbcTemplate.query(query, new Object[] { splitRequired[i] }, new StartSlotMapper());
			//System.out.println("In Dao Start Slot:"+userStartSlot1.get(i).getStartTime());
			start1.addAll(userStartSlot1);
		}
		for (int i = 0; i < splitRequired.length; i++) {
			userStartSlot2 = jdbcTemplate.query(query, new Object[] { splitOptional[i] }, new StartSlotMapper());
			//System.out.println("In Dao End Slot:"+userStartSlot2.get(i).getStartTime());
			start2.addAll(userStartSlot2);
		}
		/*for (int i = 0; i < start1.size(); i++) {
			System.out.println("Startslot1" + start1.get(i).getStartTime());
		}
		for (int i = 0; i < userStartSlot1.size(); i++) {
			System.out.println("Startslot2" + start2.get(i).getStartTime());
		}*/
		List<Calendar> start = new ArrayList<Calendar>(start1);
		start.addAll(start2);
		return start;
	}

	@SuppressWarnings("unchecked")
	public List<Calendar> getGoogleUserEndSlot(Calendar calendar) {
		List<Calendar> userEndSlot1 = null;
		List<Calendar> end1 = new ArrayList<Calendar>();
		List<Calendar> userEndSlot2 = null;
		List<Calendar> end2 = new ArrayList<Calendar>();
		String required = calendar.getGuestRequiredEmail();
		String optional = calendar.getGuestOptionalEmail();
		String[] splitRequired = required.split(",");
		String[] splitOptional = optional.split(",");
		query = "select end_time_google from gcalendar where email_id_google = ?";
		jdbcTemplate = new JdbcTemplate(dataSource);
		for (int i = 0; i < splitRequired.length; i++) {
			userEndSlot1 = jdbcTemplate.query(query, new Object[] { splitRequired[i] }, new EndSlotMapper());
			end1.addAll(userEndSlot1);
		}
		for (int i = 0; i < splitRequired.length; i++) {
			userEndSlot2 = jdbcTemplate.query(query, new Object[] { splitOptional[i] }, new EndSlotMapper());
			end2.addAll(userEndSlot2);
		}
		/*for (int i = 0; i < userEndSlot1.size(); i++) {
			System.out.println("EndSlot1" + userEndSlot1.get(i).getEndTime());
		}
		for (int i = 0; i < userEndSlot2.size(); i++) {
			System.out.println("EndSlot2" + userEndSlot2.get(i).getEndTime());
		}*/
		List<Calendar> end = new ArrayList<Calendar>(end1);
		end.addAll(end2);
		return end;
	}

	public boolean insertEvent(Calendar calendar, String emailId, String uuid) {
		jdbcTemplate = new JdbcTemplate(dataSource);
		query = "INSERT INTO event_info " + "(event_name, email_id, event_description, guest_required_email, guest_optional_email, uuid) VALUES(?,?,?,?,?,?)";
		jdbcTemplate.update(query, new Object[] { calendar.getEventName(), emailId, calendar.getEventDescription(), calendar.getGuestRequiredEmail(), calendar.getGuestOptionalEmail(), uuid });
		return true;
	}

	public String getRequiredPeople(String uuid) {
		query = "Select guest_required_email from event_info where uuid=?";
		String required = jdbcTemplate.queryForObject(query, new Object[] { uuid }, String.class);
		return required;
	}

	public String getOptionalPeople(String uuid) {
		query = "Select guest_optional_email from event_info where uuid=?";
		String optional = jdbcTemplate.queryForObject(query, new Object[] { uuid }, String.class);
		return optional;
	}

	public String getGoogleRequiredPeople(String eventUuid) {
		query = "Select guest_required_email from google_event_info where google_event_uuid=?";
		String required = jdbcTemplate.queryForObject(query, new Object[] { eventUuid }, String.class);
		return required;
	}

	public String getGoogleOptionalPeople(String eventUuid) {
		query = "Select guest_optional_email from google_event_info where google_event_uuid=?";
		String optional = jdbcTemplate.queryForObject(query, new Object[] { eventUuid }, String.class);
		return optional;
	}

	@SuppressWarnings("unchecked")
	public JSONObject fetchGoogleData(String emailId) {
		List<Calendar> calendarData = null;
		jdbcTemplate = new JdbcTemplate(dataSource);
		JSONArray jArray = new JSONArray();
		query = "select event_name_google,event_description_google,start_date_google,end_date_google from gcalendar where email_id_google=?";
		calendarData = jdbcTemplate.query(query, new Object[] { emailId }, new CalendarRowMapper());
		for (int i = 0; i < calendarData.size(); i++) {
			String eventNameJson = calendarData.get(i).getEventName();
			String eventStartTimeJson = calendarData.get(i).getStartTime();
			String eventEndTimeJson = calendarData.get(i).getEndTime();
			JSONObject jObj = new JSONObject();
			jObj.put("title", eventNameJson);
			jObj.put("start", eventStartTimeJson);
			jObj.put("end", eventEndTimeJson);
			jArray.add(jObj);
		}
		JSONObject jObjDevice = new JSONObject();
		jObjDevice.put("device", jArray);
		JSONObject jObjDeviceList = new JSONObject();
		jObjDeviceList.put("devicelist", jObjDevice);
		try {
			FileWriter fileWriter = new FileWriter("D:\\data1.json");
			fileWriter.write(jObjDeviceList.toJSONString());
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jObjDeviceList;
	}

}
