//package io.sclera.utils;
//
//import io.sclera.dto.DaintreePointsDTO;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//@Component
//public class DaintreeUtils {
//
//    public String setCategoryForPoint(DaintreePointsDTO daintreePoint) {
//        if (daintreePoint.getLights() != null && daintreePoint.getLights().equals("m:")) {
//            return "light";
//        } else if (daintreePoint.getEnergy() != null && daintreePoint.getEnergy().equals("m:")) {
//            return "energy";
//        } else if (daintreePoint.getOccupancyIndicator() != null && daintreePoint.getOccupancyIndicator().equals("m:")) {
//            return "occupancy";
//        } else if (daintreePoint.getSensor() != null && daintreePoint.getSensor().equals("m:")) {
//            return "generic";
//        } else if (daintreePoint.getPeopleCount() != null && daintreePoint.getPeopleCount().equals("m:")) {
//            return "count";
//        } else if (daintreePoint.getFault() != null && daintreePoint.getFault().equals("m:")) {
//            return "system";
//        } else if (daintreePoint.getPhysical() != null && daintreePoint.getPhysical().equals("m:")) {
//            return "generic";
//        } else return null;
//    }
//
//    public String getRowsFromDaintreeResult(ResponseEntity<String> daintreeResult) {
//        try {
//            JSONObject jsonObject = new JSONObject(daintreeResult.getBody());
//            if (jsonObject.has("rows"))
//                return jsonObject.get("rows").toString();
//            else
//                return null;
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        return null;
//    }
//
//
//    public String formatString(String originalString, String regex) {
//        if (originalString != null)
//            return originalString.replace(regex, "");
//        return "";
//    }
//
//    public String getTimeStamp(String date) {
//        Date d = new Date();
//        return String.valueOf(d.getTime());
//    }
//
//
//    public List<DaintreePointsDTO> setHaystackPoint(List<DaintreePointsDTO> daintreeDevicePoints) {
//        if (daintreeDevicePoints != null) {
//            for (DaintreePointsDTO daintreePoint : daintreeDevicePoints) {
//                if (daintreePoint.getHaystack_point_id() == null && daintreePoint.getId() != null) {
//                    daintreePoint.setHaystack_point_id(formatString(daintreePoint.getId(), "r:"));
//                }
//            }
//        }
//        return daintreeDevicePoints;
//    }
//
//    public String getSiteAddress(JSONObject jsonObject) {
//        StringBuilder addressBuilder = new StringBuilder();
//        try {
//            String[] siteArray = Arrays.stream(jsonObject.getString("geoAddr").split(",")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
//            for (Object obj : siteArray)
//                addressBuilder.append(obj.toString()).append(",");
//            return addressBuilder.substring(0, addressBuilder.length() - 1);
//        } catch (JSONException e) {
//            System.out.println(e);
//        }
//        return null;
//    }
//}
