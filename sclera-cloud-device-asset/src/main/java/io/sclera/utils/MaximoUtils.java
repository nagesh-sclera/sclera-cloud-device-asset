package io.sclera.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.MaximoDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MaximoUtils {

    private String token = null;
    private Long tokenCreatedAt = null;
    private String serverUrl = null;
    private String siteId=null;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getTokenCreatedAt() {
        return tokenCreatedAt;
    }

    public void setTokenCreatedAt(Long tokenCreatedAt) {
        this.tokenCreatedAt = tokenCreatedAt;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Map<String, String> buildParams(String workOrderId, MaximoDTO maximoDTO, Integer pageno, Integer pagesize) {
        Map<String, String> params = new HashMap<>();
        String oslcSelectValue = String.join(",",
                "wonum", "description", "location", "worktype", "status", "assetnum", "statusdate", "glaccount",
                "wopriority", "parent", "istask", "in_shared", "in_impact", "woeq11", "assetlocpriority",
                "in_irn", "changeby", "changedate", "parentchgsstatus", "calcpriority", "schedstart", "jpnum",
                "jp_frequency", "pluscjprevnum", "pmnum", "route", "targstartdate", "targcompdate", "sneconstraint",
                "fnlconstraint", "pcacthrs", "ams", "lms", "aos", "los", "origrecordid", "worklog.description",
                "hasfollowupwork", "interruptible", "reportedby", "supervisor", "owner", "in_ifmsource", "reportdate",
                "crewid", "in_ifmpriorty", "phone", "leadcraft", "vendor", "amcrew", "crewworkgroup", "lead", "siteid",
                "assignment.craft", "assignment.status", "assignment.laborhrs", "assignment.changeby",
                "assignment.scheduledate", "assignment.finishdate", "worklog.description_longdescription",
                "worklog.logtype", "worklog.createby"
        );

        StringBuilder whereClause = new StringBuilder();

        // Filter by workOrderId
        if (workOrderId != null && !workOrderId.isEmpty() && !workOrderId.equals("all")) {
            whereClause.append("wonum=\"").append(workOrderId).append("\"");
        } else if (maximoDTO != null && maximoDTO.getWonum() != null && !maximoDTO.getWonum().isEmpty()) {
            whereClause.append("wonum=\"%").append(maximoDTO.getWonum()).append("%\"");
        }

        // Filter by status
        if (maximoDTO != null && maximoDTO.getStatus() != null && !maximoDTO.getStatus().isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" and ");
            }
            whereClause.append("status=\"").append(maximoDTO.getStatus()).append("\"");
        }

        // Filter by assetnum (with wildcard)
        if (maximoDTO != null && maximoDTO.getAssetnum() != null && !maximoDTO.getAssetnum().isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" and ");
            }
            whereClause.append("assetnum=\"%").append(maximoDTO.getAssetnum()).append("%\"");
        }

        // Filter by schedstart
        if (maximoDTO != null && maximoDTO.getSchedstart() != null && !maximoDTO.getSchedstart().isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" and ");
            }
            whereClause.append("schedstart=\"").append(maximoDTO.getSchedstart()).append("\"");
        }

        // Filter by description (with wildcard)
        if (maximoDTO != null && maximoDTO.getDescription() != null && !maximoDTO.getDescription().isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" and ");
            }
            whereClause.append("description=\"%").append(maximoDTO.getDescription()).append("%\"");
        }

        // Filter by calcpriority
        if (maximoDTO != null && maximoDTO.getCalcpriority() != null) {
            if (whereClause.length() > 0) {
                whereClause.append(" and ");
            }
            whereClause.append("calcpriority=").append(maximoDTO.getCalcpriority());
        }

        // Filter by location
        if (maximoDTO != null && maximoDTO.getLocation() != null && !maximoDTO.getLocation().isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" and ");
            }
            whereClause.append("location=\"%").append(maximoDTO.getLocation()).append("%\"");
        }


        // Filter by siteid
        if (maximoDTO != null && maximoDTO.getSiteid() != null && !maximoDTO.getSiteid().isEmpty()) {
            String siteIdJson = maximoDTO.getSiteid();
            try {
                JSONArray siteArray = JSONArray.parseArray(siteIdJson);
                if (whereClause.length() > 0) {
                    whereClause.append(" and ");
                }
                whereClause.append("siteID in [");
                for (int i = 0; i < siteArray.size(); i++) {
                    if (i > 0) {
                        whereClause.append(",");
                    }
                    JSONObject site = siteArray.getJSONObject(i);
                    whereClause.append("\"").append(site.getString("siteid")).append("\"");
                }
                whereClause.append("]");

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing siteId JSON", e);
            }
        }

        if (whereClause.length() > 0) {
            whereClause.append(" and ");
            whereClause.append("istask=false");
        }

        if (whereClause.length() > 0) {
            params.put("oslc.where", whereClause.toString());
        }

        params.put("oslc.select", oslcSelectValue);
        params.put("oslc.pageSize", pagesize.toString());
        params.put("oslc.orderBy", "-wonum");
        params.put("pageno", pageno.toString());


        System.out.println("params body:  " + params);
        return params;
    }

    public JSONArray getSites(){
        List<Map<String, String>> siteMap = new ArrayList<>();

        siteMap.add(createSite("AA", "Kazakhstan, Almaty"));
        siteMap.add(createSite("AC", "Miragolf One, Cordoba Argentina"));
        siteMap.add(createSite("AK", "Ankara, Turkey"));
        siteMap.add(createSite("AL-AFO", "Aloha AFO"));
        siteMap.add(createSite("AN", "Austin"));
        siteMap.add(createSite("AR", "Algeria, Algiers"));
        siteMap.add(createSite("AT", "Alpharetta, GA"));
        siteMap.add(createSite("AW", "Pennsylvania, Allentown"));
        siteMap.add(createSite("AX", "Aix en Provence, France"));
        siteMap.add(createSite("AY", "England, Aylesbury"));
        siteMap.add(createSite("AZ", "Arizona"));
        siteMap.add(createSite("BA", "India, Bangalore"));
        siteMap.add(createSite("BC", "Brazil Campinas"));
        siteMap.add(createSite("BD", "Colorado, Boulder"));
        siteMap.add(createSite("BE", "Buenos Aires"));
        siteMap.add(createSite("BH", "England, Berkshire"));
        siteMap.add(createSite("BI", "Brasilia Intel"));
        siteMap.add(createSite("BJ", "China, Beijing"));
        siteMap.add(createSite("BL", "Belgium, Brussels"));
        siteMap.add(createSite("BS", "UK, Basingstoke"));
        siteMap.add(createSite("BV", "Brasov, Romania"));
        siteMap.add(createSite("BW", "Germany, Braunschweig"));
        siteMap.add(createSite("BX", "England, Brighton"));
        siteMap.add(createSite("BZ", "Brazil, Sao Paulo"));
        siteMap.add(createSite("CA", "California Sites"));
        siteMap.add(createSite("CB", "Colombia, Bogota"));
        siteMap.add(createSite("CD", "China, Chengdu"));
        siteMap.add(createSite("CE", "Egypt, Cairo"));
        siteMap.add(createSite("CF", "Ireland, Cork"));
        siteMap.add(createSite("CH", "Chandler, Arizona"));
        siteMap.add(createSite("CL", "California, Irvine"));
        siteMap.add(createSite("CR", "Costa Rica"));
        siteMap.add(createSite("CT", "Stamford CT"));
        siteMap.add(createSite("CX", "Columbia MD"));
        siteMap.add(createSite("CZ", "Czech Republic, Prague"));
        siteMap.add(createSite("DB", "UAE, Dubai"));
        siteMap.add(createSite("DC", "Washington DC"));
        siteMap.add(createSite("DG", "Dresden, Germany"));
        siteMap.add(createSite("DK", "Soborg, Denmark"));
        siteMap.add(createSite("DL", "Dalian"));
        siteMap.add(createSite("DL-F68", "Dalian Fab 68"));
        siteMap.add(createSite("DM", "Santo Domingo"));
        siteMap.add(createSite("DR", "Duisburg, Germany"));
        siteMap.add(createSite("DY", "Daventry, England"));
        siteMap.add(createSite("FACPOR", "Reference Factory POR Site"));
        siteMap.add(createSite("FC", "Fort Collins"));
        siteMap.add(createSite("FF", "Fairfax, VA"));
        siteMap.add(createSite("FI", "Finland, Espoo"));
        siteMap.add(createSite("FM", "Folsom"));
        siteMap.add(createSite("FSA", "America, Field Sales"));
        siteMap.add(createSite("FT", "Germany, Frankfurt"));
        siteMap.add(createSite("GC", "Mexico"));
        siteMap.add(createSite("GK", "Poland, Gdansk"));
        siteMap.add(createSite("GL", "Galati, Romania"));
        siteMap.add(createSite("GRD", "Garden Center One, Cordoba Argentina"));
        siteMap.add(createSite("GS", "St Leon-Rot, Germany"));
        siteMap.add(createSite("HD", "Hudson"));
        siteMap.add(createSite("HD-F17", "Hudson Fab 17"));
        siteMap.add(createSite("HH", "Germany, Hansestadt Hamburg"));
        siteMap.add(createSite("HK", "China, Hong Kong"));
        siteMap.add(createSite("HO", "Vietnam, Ho Chi Minh City"));
        siteMap.add(createSite("HW", "High Wycombe, England"));
        siteMap.add(createSite("HY", "Telangana, India"));
        siteMap.add(createSite("IC", "Illinois"));
        siteMap.add(createSite("IDCJ", "IDC Jerusalem"));
        siteMap.add(createSite("IDPJ", "IDP Jerusalem"));
        siteMap.add(createSite("IF", "Idaho Falls ID"));
        siteMap.add(createSite("IGR", "Greece, Athens"));
        siteMap.add(createSite("IJ", "Japan, Ibaraki"));
        siteMap.add(createSite("IK", "Korea, DITC"));
        siteMap.add(createSite("IP", "Pisa, Italy"));
        siteMap.add(createSite("IR", "Ireland"));
        siteMap.add(createSite("IR-F10", "Ireland Fab 10"));
        siteMap.add(createSite("IR-F14", "Ireland Fab 14"));
        siteMap.add(createSite("IR-F24", "Ireland Fab 24"));
        siteMap.add(createSite("IR-F34", "Ireland Fab 34"));
        siteMap.add(createSite("IS", "Israel"));
        siteMap.add(createSite("ISHA", "Israel IDC"));
        siteMap.add(createSite("ISJR", "Israel Jerusalem"));
        siteMap.add(createSite("ISLC", "Israel Lachish"));
        siteMap.add(createSite("ISLC-F28", "Israel Lachish Fab 28"));
        siteMap.add(createSite("ISLC-F38", "Israel Lachish Fab38"));
        siteMap.add(createSite("ISLCF28A", "Israel Lachish F28A"));
        siteMap.add(createSite("ISPTK", "Israel Petach Tikva"));
        siteMap.add(createSite("IW", "Poland, Warsaw"));
        siteMap.add(createSite("JH", "South Africa, Johannesburg"));
        siteMap.add(createSite("KA", "Germany, Karlsruhe"));
        siteMap.add(createSite("KK", "Poland, Krakow"));
        siteMap.add(createSite("KL", "Kula Lumpur Damansara"));
        siteMap.add(createSite("KM", "Malaysia, Kulim"));
        siteMap.add(createSite("KT", "Belgium, Kontich"));
        siteMap.add(createSite("KV", "Ukraine, Kiev"));
        siteMap.add(createSite("LD", "London England"));
        siteMap.add(createSite("LI", "Lima"));
        siteMap.add(createSite("LM", "Luxembourg"));
        siteMap.add(createSite("LN", "Lincoln England"));
        siteMap.add(createSite("LP", "Lodz, Poland"));
        siteMap.add(createSite("LS", "Lausanne, Switzerland"));
        siteMap.add(createSite("LT", "Livingston, Scotland"));
        siteMap.add(createSite("LZ", "Linz, Austria"));
        siteMap.add(createSite("MA", "Spain, Madrid"));
        siteMap.add(createSite("MB", "India, Mumbai"));
        siteMap.add(createSite("MC", "Massachusetts"));
        siteMap.add(createSite("ME", "Australia, Melbourne"));
        siteMap.add(createSite("MG", "Moirans, France"));
        siteMap.add(createSite("MGW", "Canada, Vancouver, BC"));
        siteMap.add(createSite("MI", "Italy, Milan"));
        siteMap.add(createSite("MN", "Manila NCC"));
        siteMap.add(createSite("MP", "France, Montpellier"));
        siteMap.add(createSite("MR", "Canada, Montreal, Quebec"));
        siteMap.add(createSite("MS", "Russia, Moscow"));
        siteMap.add(createSite("MU", "Germany, Munich"));
        siteMap.add(createSite("MX", "Mexico, Mexico City"));
        siteMap.add(createSite("MY", "Malaysia"));
        siteMap.add(createSite("NC", "France, Nice"));
        siteMap.add(createSite("ND", "India, New Delhi"));
        siteMap.add(createSite("NE", "France, Nantes"));
        siteMap.add(createSite("NH", "Heron Cove at Merrimack, NH"));
        siteMap.add(createSite("NJ", "New Jersey"));
        siteMap.add(createSite("NL", "Amsterdam"));
        siteMap.add(createSite("NM", "New Mexico"));
        siteMap.add(createSite("NN", "Russia, Nizhny Novgorod"));
        siteMap.add(createSite("NS", "Russia, Novosibirsk"));
        siteMap.add(createSite("NU", "Nuremberg, Germany"));
        siteMap.add(createSite("OC-F12", "Ocotillo Fab 12"));
        siteMap.add(createSite("OC-F22", "Ocotillo Fab 22"));
        siteMap.add(createSite("OC-F32", "Ocotillo Fab 32"));
        siteMap.add(createSite("OC-F42", "Ocotillo Fab 42"));
        siteMap.add(createSite("OC-F52", "Ocotillo Fab 52"));
        siteMap.add(createSite("OJ", "Japan, Osaka"));
        siteMap.add(createSite("OR", "Oregon"));
        siteMap.add(createSite("OS", "Norway, Oslo"));
        siteMap.add(createSite("OT", "Canada, Ottawa"));
        siteMap.add(createSite("PA", "France, Paris"));
        siteMap.add(createSite("PD", "Germany, Paderborn"));
        siteMap.add(createSite("PG", "Malaysia, Penang"));
        siteMap.add(createSite("PN", "Pune CommerZone"));
        siteMap.add(createSite("QU", "Quito"));
        siteMap.add(createSite("RA-D1C", "Ronler Acres Fab D1C"));
        siteMap.add(createSite("RA-D1D", "Ronler Acres Fab D1D"));
        siteMap.add(createSite("RA-D1X", "Ronler Acres Fab D1X"));
        siteMap.add(createSite("RA-F20", "Ronler Acres Fab F20"));
        siteMap.add(createSite("RA-RA4", "Ronler Acres Fab Site RA4"));
        siteMap.add(createSite("RB", "Romania, Bucharest"));
        siteMap.add(createSite("RD", "North Carolina"));
        siteMap.add(createSite("RM", "Italy, Rome"));
        siteMap.add(createSite("RR-F11X", "Rio Rancho Fab 11 Expansion"));
        siteMap.add(createSite("RS", "Saudi Arabia, Riyadh"));
        siteMap.add(createSite("S3IT", "S3 Information technology Group - CSIS"));
        siteMap.add(createSite("SA", "South Africa, Johannesburg"));
        siteMap.add(createSite("SC", "Santa Clara"));
        siteMap.add(createSite("SC-IMO", "Santa Clara IMO"));
        siteMap.add(createSite("SD", "Sydney Pacific Highway"));
        siteMap.add(createSite("SE", "China, Shenzhen"));
        siteMap.add(createSite("SF", "California, San Francisco"));
        siteMap.add(createSite("SG", "Singapore Haw Par"));
        siteMap.add(createSite("SH", "PRC, Shanghai"));
        siteMap.add(createSite("SI", "Ireland, Shanon"));
        siteMap.add(createSite("SJ", "San Jose, California"));
        siteMap.add(createSite("SK", "Sweden"));
        siteMap.add(createSite("SO", "South Carolina, Columbia"));
        siteMap.add(createSite("SS", "Seville, Spain"));
        siteMap.add(createSite("ST", "Chile, Providencia"));
        siteMap.add(createSite("SW", "Swindon"));
        siteMap.add(createSite("SZ", "Switzerland, Zurich"));
        siteMap.add(createSite("TI", "Timisoara, Romania"));
        siteMap.add(createSite("TK", "Japan, Tokyo Kokusai"));
        siteMap.add(createSite("TL", "France, Toulouse"));
        siteMap.add(createSite("TM", "Finland, Tampere"));
        siteMap.add(createSite("TO", "Canada, Markham, Ontario"));
        siteMap.add(createSite("TR", "Turkey, Istanbul"));
        siteMap.add(createSite("TU", "Tunisia, Tunis"));
        siteMap.add(createSite("TW", "Taiwan"));
        siteMap.add(createSite("UL", "Germany, Ulm"));
        siteMap.add(createSite("VC", "British Columbia"));
        siteMap.add(createSite("VN", "Austria, Vienna"));
        siteMap.add(createSite("VR", "Villach, Austria"));
        siteMap.add(createSite("VZ", "Caracas"));
        siteMap.add(createSite("WA", "Washington"));
        siteMap.add(createSite("XA", "PRC, Xian"));
        siteMap.add(createSite("YTC1", "Yintai Centre Tower"));
        siteMap.add(createSite("ZJ", "Zhejiang, China"));


        JSONArray jsonArrayResult = convertListOfMapToJsonArray(siteMap);
        return jsonArrayResult;
    }

    private static Map<String, String> createSite(String siteId, String description) {
        Map<String, String> siteMap = new HashMap<>();
        siteMap.put("siteid", siteId);
        siteMap.put("description", description);
        return siteMap;
    }

    public static JSONArray convertListOfMapToJsonArray(List<Map<String, String>> list) {
        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> site : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(site);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}