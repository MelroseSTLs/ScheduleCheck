package com.herocc.school.aspencheck;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schedule {
	public Document schedPage;
	
	public int day;
	public String currentClass;
	public String block;
	public int blockOfDay;
	
	public Schedule(Document schedPage){
		this.schedPage = schedPage;
		this.day = getDay();
		this.currentClass = getCurrentClass();
		this.block = getBlock();
		this.blockOfDay = getBlockOfDay();
	}
	
	private int getDay() {
		Elements matching = schedPage.body().getElementsByAttributeValueContaining("style", "border: solid 1px red;");
		try {
			matching.first().text();
		} catch (NullPointerException e) {
			return 0;
		}
		Matcher m = Pattern.compile("\\d+").matcher(matching.first().text());
		
		if (m.find()) {
			String thing = m.group(0);
			int dayNumber = Integer.parseInt(thing);
			if (dayNumber >= 1 && dayNumber <= 7) {
				return dayNumber;
			}
		}
		return 0;
	}
	
	private String getCurrentClass(){
		Elements matching = schedPage.body().getElementsByAttributeValueContaining("style", "border: solid 1px red;");
		try {
			return matching.get(1).text(); // Get second element with red border (first is day)
		} catch (IndexOutOfBoundsException e){
			return null;
		}
	}
	
	private String getBlock(){
		String currentClass = this.getCurrentClass();
		if (currentClass == null) return null;
		String gotBlock = currentClass.replaceAll("\\d","");
		gotBlock = gotBlock.substring(gotBlock.length() - 1);
		if (Pattern.matches("[A-G]", gotBlock)) return gotBlock;
		return null;
	}
	
	private int getBlockOfDay(){
		return Integer.valueOf(String.valueOf(
						schedPage.body().getElementsByAttributeValueContaining("style", "border: solid 1px red;")
						.get(1).parent().getElementsByAttributeValueContaining("width", "5%").get(0).text().charAt(0)));
	}
	
	public JsonObjectBuilder getJsonData(boolean sensorPrivateInformation){
		String localClassName = sensorPrivateInformation ? "null" : this.currentClass;
		
		return Json.createObjectBuilder()
						.add("day", this.day)
						.add("block", this.block)
						.add("blockOfDay", this.blockOfDay)
						.add("class", localClassName);
	}
}
