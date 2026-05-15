package io.sclera.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class ConditionUtils {

    public Boolean verifyCurrentSystemTimeWithinScheduledTime(String startTimeString, String endTimeString) {
        try {
            System.out.println("Start Time String " + startTimeString + "   endTimeString " + endTimeString);
            LocalTime startTime = LocalTime.parse(startTimeString);
            LocalTime endTime = LocalTime.parse(endTimeString);
            LocalTime currentTime = LocalTime.now();
            LocalTime minimumTime = LocalTime.parse("00:00");
            LocalTime maximumTime = LocalTime.parse("23:59");

            System.out.println("startTime " + startTime + "   endTime " + endTime);
            System.out.println("currentTime " + currentTime);
            System.out.println("minimumTime " + minimumTime + "   maximumTime " + maximumTime);

            if (startTime.getHour() < endTime.getHour()) {
                System.out.println("Inside first condition");
                if (startTime.getHour() != currentTime.getHour() && endTime.getHour() != currentTime.getHour()) {
                    if (currentTime.getHour() > startTime.getHour() && currentTime.getHour() < endTime.getHour()) {
                        System.out.println("Inside first a condition result " + true);
                        return true;
                    } else {
                        System.out.println("Inside first a condition result " + false);
                        return false;
                    }
                } else if (currentTime.getHour() == startTime.getHour()) {
                    if (currentTime.getMinute() >= startTime.getMinute()) {
                        System.out.println("Inside first b condition result " + true);
                        return true;
                    } else {
                        System.out.println("Inside first b condition result " + false);
                        return false;
                    }
                } else if (currentTime.getHour() == endTime.getHour()) {
                    if (currentTime.getMinute() <= endTime.getMinute()) {
                        System.out.println("Inside first c condition result " + true);
                        return true;
                    } else {
                        System.out.println("Inside first c condition result " + false);
                        return false;
                    }
                }
            } else if (startTime.getHour() > endTime.getHour()) {
                System.out.println("Inside second condition");
                if (startTime.getHour() != currentTime.getHour() && currentTime.getHour() != endTime.getHour()) {
                    if (!(currentTime.getHour() > endTime.getHour() && currentTime.getHour() < startTime.getHour())) {
                        System.out.println("Inside second a condition result " + true);
                        return true;
                    } else {
                        System.out.println("Inside second a condition result " + false);
                        return false;
                    }
                } else if (currentTime.getHour() == startTime.getHour()) {
                    if (currentTime.getMinute() >= startTime.getMinute()) {
                        System.out.println("Inside second b condition result " + true);
                        return true;
                    } else {
                        System.out.println("Inside second b condition result " + false);
                        return false;
                    }
                } else if (currentTime.getHour() == endTime.getHour()) {
                    if (currentTime.getMinute() <= endTime.getMinute()) {
                        System.out.println("Inside second c condition result " + true);
                        return true;
                    } else {
                        System.out.println("Inside second c condition result " + false);
                        return false;
                    }
                }
            } else {
                System.out.println("Inside third condition");
                if (currentTime.getHour() != currentTime.getHour()) {
                    System.out.println("Inside third a condition result " + false);
                    return false;
                } else {
                    if (startTime.getMinute() > endTime.getMinute()) {
                        if (!(currentTime.getMinute() >= endTime.getMinute() && currentTime.getMinute() <= startTime.getMinute())) {
                            System.out.println("Inside third b condition result " + true);
                            return true;
                        } else {
                            System.out.println("Inside third b condition result " + false);
                            return false;
                        }
                    }
                    if (currentTime.getMinute() >= startTime.getMinute() && currentTime.getMinute() <= endTime.getMinute()) {
                        System.out.println("Inside third c condition result " + true);
                        return true;
                    } else {
                        System.out.println("Inside third c condition result " + false);
                        return false;
                    }
                }
            }
            return false;

        } catch (Exception e) {
            System.out.println("Error comparing current time with scheduled time " + e);
            return false;
        }

    }


    public Boolean verifyDayOfWeek(JSONObject scheduleDays) {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        String dayOfWeekText = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        JSONArray weekDays = scheduleDays.getJSONArray("day_of_week");
        System.out.println("weekDays " + weekDays);
        for (int i = 0; i < weekDays.size(); i++) {
            JSONObject dayCondition = weekDays.getJSONObject(i);
            if (dayCondition.getString("day").equalsIgnoreCase(dayOfWeekText)) {
                return true;
            }
        }
        return false;
    }

}
