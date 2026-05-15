package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaximoDTO {

    private String wonum;
    private String description;
    private String siteid;
    private String location;
    private String worktype;
    private String status;
    private String assetnum;
    private String statusdate;
    private String glaccount;
    private String parent;
    private Boolean istask;
    private Boolean inShared;
    private Boolean inImpact;
    private String woeq11;
    private Integer assetlocationpriority;
    private Boolean inIrn;
    private String changeby;
    private String changedate;
    private Boolean parentchgsstatus;
    private Integer calcpriority;
    private String jpnum;
    private String jpFrequency;
    private Integer pluscjprevnum;
    private String pmnum;
    private String route;
    private String targstartdate;
    private String targcompdate;
    private String sneconstraint;
    private String fnlconstraint;
    private String pcacthrs;
    private Boolean ams;
    private Boolean lms;
    private Boolean aos;
    private Boolean los;
    private String origrecordid;
    private String wplaborDescription;//crossCheck
    private Boolean hasfollowupwork;
    private Boolean interruptible;
    private String reportedby;
    private String supervisor;
    private String owner;
    private String inIfmsource;
    private String reporteddate;
    private String crewid;
    private Integer inIfmpriority; // cross check
    private String phone;
    private String leadcraft;
    private String vendor;
    private String amcrew;
    private String crewworkgroup;
    private String lead;
    private String schedstart;

    public MaximoDTO(String wonum, String workorderDescription, String siteid, String location, String worktype, String status, String assetnum, String statusdate, String glaccount, String parent, Boolean istask, Boolean inShared, Boolean inImpact, String woeq11, Integer assetlocationpriority, Boolean inIrn, String changeby, String changeddate, Boolean parentchgsstatus, Integer calcpriority, String jpnum, String jpFrequency, Integer pluscjprevnum, String pmnum, String route, String targstartdate, String targcompdate, String sneconstraint, String fnlconstraint, String pcacthrs, Boolean ams, Boolean lms, Boolean aos, Boolean los, String origrecordid, String wplaborDescription, Boolean hasfollowupwork, Boolean interruptible, String reportedby, String supervisor, String owner, String inIfmsorce, String reporteddate, String crewid, Integer inIfmpriority, String phone, String leadcraft, String vendor, String amcrew, String crewworkgroup, String lead, String schedstart) {
        this.wonum = wonum;
        this.description = workorderDescription;
        this.siteid = siteid;
        this.location = location;
        this.worktype = worktype;
        this.status = status;
        this.assetnum = assetnum;
        this.statusdate = statusdate;
        this.glaccount = glaccount;
        this.parent = parent;
        this.istask = istask;
        this.inShared = inShared;
        this.inImpact = inImpact;
        this.woeq11 = woeq11;
        this.assetlocationpriority = assetlocationpriority;
        this.inIrn = inIrn;
        this.changeby = changeby;
        this.changedate = changeddate;
        this.parentchgsstatus = parentchgsstatus;
        this.calcpriority = calcpriority;
        this.jpnum = jpnum;
        this.jpFrequency = jpFrequency;
        this.pluscjprevnum = pluscjprevnum;
        this.pmnum = pmnum;
        this.route = route;
        this.targstartdate = targstartdate;
        this.targcompdate = targcompdate;
        this.sneconstraint = sneconstraint;
        this.fnlconstraint = fnlconstraint;
        this.pcacthrs = pcacthrs;
        this.ams = ams;
        this.lms = lms;
        this.aos = aos;
        this.los = los;
        this.origrecordid = origrecordid;
        this.wplaborDescription = wplaborDescription;
        this.hasfollowupwork = hasfollowupwork;
        this.interruptible = interruptible;
        this.reportedby = reportedby;
        this.supervisor = supervisor;
        this.owner = owner;
        this.inIfmsource = inIfmsorce;
        this.reporteddate = reporteddate;
        this.crewid = crewid;
        this.inIfmpriority = inIfmpriority;
        this.phone = phone;
        this.leadcraft = leadcraft;
        this.vendor = vendor;
        this.amcrew = amcrew;
        this.crewworkgroup = crewworkgroup;
        this.lead = lead;
        this.schedstart = schedstart;
    }

    public MaximoDTO() {
    }

    public String getWonum() {
        return wonum;
    }

    public void setWonum(String wonum) {
        this.wonum = wonum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSiteid() {
        return siteid;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWorktype() {
        return worktype;
    }

    public void setWorktype(String worktype) {
        this.worktype = worktype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssetnum() {
        return assetnum;
    }

    public void setAssetnum(String assetnum) {
        this.assetnum = assetnum;
    }

    public String getStatusdate() {
        return statusdate;
    }

    public void setStatusdate(String statusdate) {
        this.statusdate = statusdate;
    }

    public String getGlaccount() {
        return glaccount;
    }

    public void setGlaccount(String glaccount) {
        this.glaccount = glaccount;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Boolean getIstask() {
        return istask;
    }

    public void setIstask(Boolean istask) {
        this.istask = istask;
    }

    public Boolean getInShared() {
        return inShared;
    }

    public void setInShared(Boolean inShared) {
        this.inShared = inShared;
    }

    public Boolean getInImpact() {
        return inImpact;
    }

    public void setInImpact(Boolean inImpact) {
        this.inImpact = inImpact;
    }

    public String getWoeq11() {
        return woeq11;
    }

    public void setWoeq11(String woeq11) {
        this.woeq11 = woeq11;
    }

    public Integer getAssetlocationpriority() {
        return assetlocationpriority;
    }

    public void setAssetlocationpriority(Integer assetlocationpriority) {
        this.assetlocationpriority = assetlocationpriority;
    }

    public Boolean getInIrn() {
        return inIrn;
    }

    public void setInIrn(Boolean inIrn) {
        this.inIrn = inIrn;
    }

    public String getChangeby() {
        return changeby;
    }

    public void setChangeby(String changeby) {
        this.changeby = changeby;
    }

    public String getChangedate() {
        return changedate;
    }

    public void setChangedate(String changedate) {
        this.changedate = changedate;
    }

    public Boolean getParentchgsstatus() {
        return parentchgsstatus;
    }

    public void setParentchgsstatus(Boolean parentchgsstatus) {
        this.parentchgsstatus = parentchgsstatus;
    }

    public Integer getCalcpriority() {
        return calcpriority;
    }

    public void setCalcpriority(Integer calcpriority) {
        this.calcpriority = calcpriority;
    }

    public String getJpnum() {
        return jpnum;
    }

    public void setJpnum(String jpnum) {
        this.jpnum = jpnum;
    }

    public String getJpFrequency() {
        return jpFrequency;
    }

    public void setJpFrequency(String jpFrequency) {
        this.jpFrequency = jpFrequency;
    }

    public Integer getPluscjprevnum() {
        return pluscjprevnum;
    }

    public void setPluscjprevnum(Integer pluscjprevnum) {
        this.pluscjprevnum = pluscjprevnum;
    }

    public String getPmnum() {
        return pmnum;
    }

    public void setPmnum(String pmnum) {
        this.pmnum = pmnum;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getTargstartdate() {
        return targstartdate;
    }

    public void setTargstartdate(String targstartdate) {
        this.targstartdate = targstartdate;
    }

    public String getTargcompdate() {
        return targcompdate;
    }

    public void setTargcompdate(String targcompdate) {
        this.targcompdate = targcompdate;
    }

    public String getSneconstraint() {
        return sneconstraint;
    }

    public void setSneconstraint(String sneconstraint) {
        this.sneconstraint = sneconstraint;
    }

    public String getFnlconstraint() {
        return fnlconstraint;
    }

    public void setFnlconstraint(String fnlconstraint) {
        this.fnlconstraint = fnlconstraint;
    }

    public String getPcacthrs() {
        return pcacthrs;
    }

    public void setPcacthrs(String pcacthrs) {
        this.pcacthrs = pcacthrs;
    }

    public Boolean getAms() {
        return ams;
    }

    public void setAms(Boolean ams) {
        this.ams = ams;
    }

    public Boolean getLms() {
        return lms;
    }

    public void setLms(Boolean lms) {
        this.lms = lms;
    }

    public Boolean getAos() {
        return aos;
    }

    public void setAos(Boolean aos) {
        this.aos = aos;
    }

    public Boolean getLos() {
        return los;
    }

    public void setLos(Boolean los) {
        this.los = los;
    }

    public String getOrigrecordid() {
        return origrecordid;
    }

    public void setOrigrecordid(String origrecordid) {
        this.origrecordid = origrecordid;
    }

    public String getWplaborDescription() {
        return wplaborDescription;
    }

    public void setWplaborDescription(String wplaborDescription) {
        this.wplaborDescription = wplaborDescription;
    }

    public Boolean getHasfollowupwork() {
        return hasfollowupwork;
    }

    public void setHasfollowupwork(Boolean hasfollowupwork) {
        this.hasfollowupwork = hasfollowupwork;
    }

    public Boolean getInterruptible() {
        return interruptible;
    }

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
    }

    public String getReportedby() {
        return reportedby;
    }

    public void setReportedby(String reportedby) {
        this.reportedby = reportedby;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getInIfmsource() {
        return inIfmsource;
    }

    public void setInIfmsource(String inIfmsource) {
        this.inIfmsource = inIfmsource;
    }

    public String getReporteddate() {
        return reporteddate;
    }

    public void setReporteddate(String reporteddate) {
        this.reporteddate = reporteddate;
    }

    public String getCrewid() {
        return crewid;
    }

    public void setCrewid(String crewid) {
        this.crewid = crewid;
    }

    public Integer getInIfmpriority() {
        return inIfmpriority;
    }

    public void setInIfmpriority(Integer inIfmpriority) {
        this.inIfmpriority = inIfmpriority;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLeadcraft() {
        return leadcraft;
    }

    public void setLeadcraft(String leadcraft) {
        this.leadcraft = leadcraft;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getAmcrew() {
        return amcrew;
    }

    public void setAmcrew(String amcrew) {
        this.amcrew = amcrew;
    }

    public String getCrewworkgroup() {
        return crewworkgroup;
    }

    public void setCrewworkgroup(String crewworkgroup) {
        this.crewworkgroup = crewworkgroup;
    }

    public String getLead() {
        return lead;
    }

    public void setLead(String lead) {
        this.lead = lead;
    }

    public String getSchedstart() {
        return schedstart;
    }

    public void setSchedstart(String schedstart) {
        this.schedstart = schedstart;
    }

    @Override
    public String toString() {
        return "MaximoDTO{" +
                "wonum='" + wonum + '\'' +
                ", workorderDescription='" + description + '\'' +
                ", siteid='" + siteid + '\'' +
                ", location='" + location + '\'' +
                ", worktype='" + worktype + '\'' +
                ", status='" + status + '\'' +
                ", assetnum='" + assetnum + '\'' +
                ", statusdate='" + statusdate + '\'' +
                ", glaccount='" + glaccount + '\'' +
                ", parent='" + parent + '\'' +
                ", istask=" + istask +
                ", inShared=" + inShared +
                ", inImpact=" + inImpact +
                ", woeq11='" + woeq11 + '\'' +
                ", assetlocationpriority=" + assetlocationpriority +
                ", inIrn=" + inIrn +
                ", changeby='" + changeby + '\'' +
                ", changeddate='" + changedate + '\'' +
                ", parentchgsstatus=" + parentchgsstatus +
                ", calcpriority=" + calcpriority +
                ", jpnum='" + jpnum + '\'' +
                ", jpFrequency='" + jpFrequency + '\'' +
                ", pluscjprevnum=" + pluscjprevnum +
                ", pmnum='" + pmnum + '\'' +
                ", route='" + route + '\'' +
                ", targstartdate='" + targstartdate + '\'' +
                ", targcompdate='" + targcompdate + '\'' +
                ", sneconstraint='" + sneconstraint + '\'' +
                ", fnlconstraint='" + fnlconstraint + '\'' +
                ", pcacthrs='" + pcacthrs + '\'' +
                ", ams=" + ams +
                ", lms=" + lms +
                ", aos=" + aos +
                ", los=" + los +
                ", origrecordid='" + origrecordid + '\'' +
                ", wplaborDescription='" + wplaborDescription + '\'' +
                ", hasfollowupwork=" + hasfollowupwork +
                ", interruptible=" + interruptible +
                ", reportedby='" + reportedby + '\'' +
                ", supervisor='" + supervisor + '\'' +
                ", owner='" + owner + '\'' +
                ", inIfmsorce='" + inIfmsource + '\'' +
                ", reporteddate='" + reporteddate + '\'' +
                ", crewid='" + crewid + '\'' +
                ", inIfmpriority=" + inIfmpriority +
                ", phone='" + phone + '\'' +
                ", leadcraft='" + leadcraft + '\'' +
                ", vendor='" + vendor + '\'' +
                ", amcrew='" + amcrew + '\'' +
                ", crewworkgroup='" + crewworkgroup + '\'' +
                ", lead='" + lead + '\'' +
                ", schedstart='" + schedstart + '\'' +
                '}';
    }
}