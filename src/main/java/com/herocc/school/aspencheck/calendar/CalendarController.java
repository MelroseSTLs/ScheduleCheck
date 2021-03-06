package com.herocc.school.aspencheck.calendar;

import com.herocc.school.aspencheck.AspenCheck;
import com.herocc.school.aspencheck.CSVParse;
import com.herocc.school.aspencheck.District;
import com.herocc.school.aspencheck.ErrorInfo;
import com.herocc.school.aspencheck.GenericEventGenerator;
import com.herocc.school.aspencheck.GenericWebFetch;
import com.herocc.school.aspencheck.JSONReturn;
import io.swagger.v3.oas.annotations.Operation;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

@CrossOrigin
@RestController
@RequestMapping("/{district-id}/announcements")
public class CalendarController {

  @Operation(description = "Returns all announcements scheduled to run (sources gathered from config)")
  @GetMapping()
  public ResponseEntity<JSONReturn> serveEvents(@PathVariable("district-id") String district) {
    District d = AspenCheck.config.districts.get(district);
    if (d != null && d.announcementsSources.size() != 0) {
      return new ResponseEntity<>(new JSONReturn(d.events, new ErrorInfo()), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(new JSONReturn(new ArrayList<>(), new ErrorInfo("District not configured", 0, "This district doesn't have any announcement sources configured")), HttpStatus.OK);
    }
  }

  public static void refreshEvents(District d) {
    ArrayList<Event> eventList = new ArrayList<>();
    for (String url : d.announcementsSources.get(GenericEventGenerator.SourceType.ical)){
      eventList.addAll(new ICalendar(getICal(url)).getEvents(true));
    }
    for (String url : d.announcementsSources.get(GenericEventGenerator.SourceType.csv)){
      eventList.addAll(new CSVParse(GenericWebFetch.getURL(url)).getEvents(true));
    }
    d.events = eventList;
  }

  /**
   * Gets a Calendar object from a given URL
   * @param url URL of the iCal File
   * @return Calendar object
   */
  public static Calendar getICal(String url) {
    try {
      URLConnection c = new URL(url).openConnection();
      c.setConnectTimeout(15000);
      c.setReadTimeout(15000);
      c.setRequestProperty("User-Agent", AspenCheck.config.webUserAgent);

      try (InputStream is = c.getInputStream()) {
        return new CalendarBuilder().build(is);
      } catch (ParserException e) {
        AspenCheck.log.warning("Unable to parse iCal from " + url);
        e.printStackTrace();
        AspenCheck.rollbar.error(e, "Error parsing iCal from " + url);
      }
    } catch (IOException e) {
      AspenCheck.log.warning("Unable to fetch iCal from " + url);
      e.printStackTrace();
      //AspenCheck.rollbar.error(e, "Unable to fetch iCal from " + url);
    }
    return null;
  }
}
