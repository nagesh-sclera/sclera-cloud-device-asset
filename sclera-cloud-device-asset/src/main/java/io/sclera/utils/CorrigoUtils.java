package io.sclera.utils;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.sclera.dto.DeviceAlertDTO;
import io.sclera.dto.LocationAlertDTO;
import io.sclera.client.WorkorderTemplateClient;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONObject;

/**
 * Utility for the Corrigo integration providing date/time formatting, work-order description and
 * address building, and OAuth token storage.
 */
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CorrigoUtils {

    private static final Logger log = LoggerFactory.getLogger(CorrigoUtils.class);

    @Autowired
    WorkorderTemplateClient workorderTemplateService;

    private String oauth_token;

    public String getOauth_token() {
        return oauth_token;
    }

    public void setOauth_token(String oauth_token) {
        this.oauth_token = oauth_token;
    }

    /**
     * Extracts the date portion of an ISO-style date-time string and returns it formatted as MM/dd/yyyy.
     */
    public String getDatefromDateTime(String start_date) {
        try {
            DateFormat f = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
            Date d = f.parse(start_date);
            DateFormat date = new SimpleDateFormat("MM/dd/yyyy");

            return date.format(d);
        } catch (Exception e) {
            log.debug("{}", e);
        }
        return null;
    }

    /**
     * Extracts the time portion of an ISO-style date-time string and returns it formatted as an
     * uppercase 12-hour time (hh:mm a).
     */
    public String getTimefromDateTime(String date) {
        try {
            DateFormat f = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
            Date d = f.parse(date);
            DateFormat time = new SimpleDateFormat("hh:mm a");

            return time.format(d).toUpperCase();
        } catch (Exception e) {
            log.debug("{}", e);
        }
        return null;
    }

    /**
     * Formats a duration in minutes as an hours:minutes string, zero-padding minutes below ten.
     */
    public String formatHoursMins(Double total_duration) {
        try {
            if (total_duration != null) {
                int hours = 0;
                int mins = 0;

                String formatted_result = "";

                if (total_duration >= 60) {
                    hours = (int) (total_duration / 60);
                    mins = (int) (total_duration % 60);
                } else {
                    mins = total_duration.intValue();
                }
                if (mins < 10) {
                    formatted_result = hours + ":0" + mins;
                } else {
                    formatted_result = hours + ":" + mins;
                }
                return formatted_result;
            }
        } catch (Exception e) {
            log.debug("{}", e);
        }
        return null;
    }

    /**
     * Adds the given duration in minutes to a start time and returns the resulting time formatted as
     * hh:mm a.
     */
    public String getEndTime(String start_time, String duration) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
            Date d = df.parse(start_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, (int) Double.parseDouble(duration));
            String newTime = df.format(cal.getTime());
            return newTime;
        } catch (Exception e) {
            log.debug("{}", e);
        }
        return null;
    }

    /**
     * Builds a work-order description string from either a device alert (asset and location) or a
     * location alert.
     */
    public String getWorkOrderDescription(DeviceAlertDTO deviceAlert, LocationAlertDTO locationAlert) {
        String workorder_description = null;
        if (deviceAlert != null && deviceAlert.getName() != null) {
            workorder_description = "Asset: " + deviceAlert.getName();
            if (deviceAlert.getLocation() != null) {
                workorder_description = workorder_description + " ( " + deviceAlert.getLocation() + ", " + deviceAlert.getFloor() + ", " + deviceAlert.getBuilding() + " ) ";
            }
        } else if (locationAlert != null && locationAlert.getName() != null) {
            workorder_description = "Location: " + locationAlert.getName() + ", " + locationAlert.getFloor_name() + ", " + locationAlert.getBuilding_name();
        }
        return workorder_description;
    }

    /**
     * Appends the work-order template's comment (looked up by template id) to the description when both
     * are present.
     */
    public String getFormattedWorkorderComment(String workorder_description, String workorder_template_id) {
        if (!workorder_description.equals("")) {
            String workorder_template_comment = workorderTemplateService.getWorkOrderTemplateComment(workorder_template_id);
            if (workorder_template_comment != null && !workorder_template_comment.equals("")) {
                workorder_description = workorder_description + " - " + workorder_template_comment;
            }
        }
        return workorder_description;
    }

    /**
     * Returns the comment prefix describing whether a device went offline or came online based on the
     * alert type.
     */
    public String deviceOnlineOfflineComment(String alert_type) {
        String alert = null;
        if (alert_type.equals("device_offline")) {
            alert = "Device went offline at ";
        }
        if (alert_type.equals("device_online")) {
            alert = "Device came online at ";
        }
        return alert;

    }

    /**
     * Reformats a date string from MM/dd/yyyy to dd/mm/yyyy.
     */
    public String formatDateField(String dateValue) {
        try {
            SimpleDateFormat inputDateFormate = new SimpleDateFormat("mm/dd/yyyy");
            Date currentDate = inputDateFormate.parse(dateValue);
            SimpleDateFormat newDateFormate = new SimpleDateFormat("dd/mm/yyyy");
            return newDateFormate.format(currentDate);
        } catch (ParseException e) {
            log.debug("{}", e);
        }
        return null;
    }

    /**
     * Builds a single-line address string from the street, city, state and zip fields of a Corrigo
     * work-order JSON payload.
     */
    public String formatAddressBuilder(JSONObject corrigoWorkorderFormat) throws JSONException {
        try {
            StringBuilder fullAddress = new StringBuilder();
            JSONObject getAddressFromData = corrigoWorkorderFormat.getJSONObject("Data").getJSONObject("Address");

            String street = getAddressFromData.has("Street") ?
                    getAddressFromData.getString("Street") : "";
            String street2 =getAddressFromData.has("Street2") ?
                    getAddressFromData.getString("Street2") : "";
            String city =   getAddressFromData.has("City") ?
                    getAddressFromData.getString("City") : "";
            String state =  getAddressFromData.has("State") ?
                    getAddressFromData.getString("State") : "";
            String zip =    getAddressFromData.has("Zip") ?
                    getAddressFromData.getString("Zip") : "";
            if (street != null && !street.isEmpty()) fullAddress.append(street);
            if (street2 != null && !street2.isEmpty()) fullAddress.append(fullAddress.length() == 0 ? street2 : " " + street2);
            if (city != null && !city.isEmpty()) fullAddress.append(fullAddress.length() == 0 ? city : " " + city);
            if (state != null && !state.isEmpty()) fullAddress.append(fullAddress.length() == 0 ? state : ", " + state);
            if (zip != null && !zip.isEmpty()) fullAddress.append(fullAddress.length() == 0 ? zip : " " + zip);
            return fullAddress.toString();

        } catch (JSONException e) {
            log.debug("{}", e);
        }
        return null;

    }
    /**
     * Returns tomorrow's date formatted as yyyy-MM-dd.
     */
    public String getCurrentDate() {
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return tomorrow.format(formatter);
        } catch (Exception e) {
            log.debug("{}", e);
        }
        return null;
    }

    /**
     * Returns the date that is the given number of days before the supplied yyyy-MM-dd date.
     */
    public String getDuplicateDateLimit(String dateStr, int days) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(dateStr, formatter);
            LocalDate newDate = date.minusDays(days);
            return String.valueOf(newDate.format(formatter));
        } catch (Exception e) {
            log.debug("{}", e);
        }
        return null;
    }
}