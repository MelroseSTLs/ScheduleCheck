package com.herocc.school.aspencheck;

import com.herocc.school.aspencheck.calendar.Event;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.time.ZoneId;
import java.util.List;

public abstract class GenericEventGenerator {
  public abstract List<Event> getEvents(boolean checkEventsOccurringNow);
  
  public JsonArrayBuilder getJsonData() {
    JsonArrayBuilder jsonEvents = Json.createArrayBuilder();
    for (Event event : getEvents(true)) {
      JsonObjectBuilder jsonAnn = Json.createObjectBuilder();
      jsonAnn
          .add("title", event.getTitle())
          .add("description", event.getDescription())
          .add("startTime", event.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond())
          .add("endTime", event.getEndTime().atZone(ZoneId.systemDefault()).toEpochSecond());
      jsonEvents.add(jsonAnn);
    }
    return jsonEvents;
  }
}