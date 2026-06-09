# sclera-cloud-device-asset — REST API Endpoints (v1)

**Base path (all controllers):** `/api/v1/sclera-cloud-device-asset-service`

## What changed
- Every endpoint is now namespaced under `/api/v1/sclera-cloud-device-asset-service`.
- The `/user/{username}` and `/vdms/{vdmsid}` (a.k.a. `{vdms_id}`, `{email}`) **path segments were removed**.
- Where the backend still needs those values, they are now **query parameters** with the SAME name as before:
  - user identifier: `username` (and `email` on one endpoint), vdms identifier: `vdmsid` or `vdms_id` (see per-endpoint columns).
  - Example: `GET /api/v1/sclera-cloud-device-asset-service/getdocuments?username=alice&vdmsid=42`
- All other path variables (`device_id`, `dockername`, `floor_id`, …), request bodies, and HTTP verbs are UNCHANGED.
- Total endpoints: 194.

## Query-param columns
- A name (e.g. `username`, `vdmsid`, `vdms_id`, `email`) = send it as a query param.
- `—` = not used by this endpoint (do not send).

_Paths below are shown relative to the base; prepend `/api/v1/sclera-cloud-device-asset-service`._

| Method | Path (relative to base) | ?username | ?vdms |
|--------|--------------------------|-----------|-------|
| POST | `/deviceId/{deviceId}/createcalllog` | — | — |
| GET | `/getallcallstatus` | username | vdmsid |
| GET | `/getcallstatuscount` | username | vdmsid |
| GET | `/getdeviceinfo/{deviceId}` | — | — |
| GET | `/assign` | — | — |
| POST | `/insertcallresponse` | — | — |
| POST | `/upsertcallflow` | username | vdmsid |
| GET | `/browsedockers` | username | vdmsid |
| GET | `/docker/{dockername}/browsedevices` | username | vdmsid |
| GET | `/getcallflow` | username | vdmsid |
| DELETE | `/configuration/deletecallflowbyid` | username | vdmsid |
| GET | `/{deviceid}/{criteria}/{calllogid}/triggercallflow` | — | — |
| GET | `/asset-fields` | — | — |
| POST | `/addaionboardassets` | username | vdmsid |
| POST | `/updatecorrigoassets` | username | vdmsid |
| POST | `/upsertonboardassets` | username | vdmsid |
| POST | `/updateassetonboardstatus` | username | vdmsid |
| POST | `/device/{device_id}/updateassetonboarddata` | username | vdmsid |
| POST | `/docker/{dockername}/getassetonboardcount` | username | vdmsid |
| GET | `/getassetonboardassignees` | username | vdms_id |
| GET | `/getpropertydetails` | username | vdms_id |
| POST | `/upsertbuildings` | username | vdms_id |
| GET | `/building/floor/location/{location_id}/getbuildingbylocation` | username | vdms_id |
| GET | `/getbuildingsbyvdmsid` | — | vdms_id |
| DELETE | `/deletebuildings` | username | vdmsid |
| GET | `/syncbuildings` | — | — |
| GET | `/syncfloormaps` | — | vdms_id |
| POST | `/updatefloormaps` | — | vdms_id |
| GET | `/syncfloormapstiles` | — | — |
| POST | `/docker/{dockername}/troubleshoot-asset` | — | — |
| POST | `/docker/{dockername}/upsertdeviceconditions` | username | vdmsid |
| GET | `/docker/{dockername}/device/{device_id}/getdeviceconditions` | username | vdmsid |
| DELETE | `/device/{device_id}/deletealldeviceconditions` | username | vdmsid |
| GET | `/condition/{condition_id}/getdeviceconditionsbyid` | username | vdmsid |
| DELETE | `/deletedeviceconditions` | username | vdmsid |
| POST | `/docker/{dockername}/sharedeviceconditions` | username | vdmsid |
| POST | `/docker/{dockername}/resetdeviceconditions` | username | vdmsid |
| GET | `/docker/{dockername}/devices` | username | vdmsid |
| GET | `/docker/{dockername}/getfilterdevice` | username | vdmsid |
| GET | `/docker/{dockername}/getsubsystemparentdevicesbypagination` | username | vdmsid |
| GET | `/docker/{dockername}/device/{device_id}/getsubsystemdevicesbypagination` | username | vdmsid |
| POST | `/docker/{dockername}/devicesupsert` | username | vdmsid |
| POST | `/docker/{dockername}/device/{device_id}/edit` | username | vdmsid |
| PUT | `/docker/{dockername}/phoneaccount/{phoneaccount}/device/{device_id}/{vendor_type}` | username | — |
| POST | `/docker/{dockername}/phoneaccount/device/{device_id}/{vendor_type}/link` | username | vdmsid |
| PUT | `/docker/{dockername}/devices` | username | vdmsid |
| POST | `/docker/{dockername}/devices/quickupdate` | username | vdmsid |
| GET | `/docker/{dockername}/device/names` | username | vdmsid |
| POST | `/docker/{dockername}/addvirtualdevice` | username | vdmsid |
| POST | `/docker/{dockername}/updatevirtualdevice` | username | vdmsid |
| DELETE | `/docker/{dockername}/virtual-device/{virtual_device_id}` | username | vdmsid |
| DELETE | `/docker/{dockername}/deletedevices` | username | vdmsid |
| GET | `/docker/{dockername}/device/{device_id}/getdevice` | username | vdmsid |
| PUT | `/docker/{dockername}/virtual-device/{virtual_device_id}/syncstatus` | username | vdmsid |
| GET | `/test/product` | — | — |
| GET | `/docker/{dockername}/getdevicecount` | username | vdmsid |
| GET | `/docker/{dockername}/devicetopology` | username | vdmsid |
| POST | `/docker/{dockername}/updatedeviceposition` | username | vdmsid |
| GET | `/docker/{dockername}/devicelistintegration` | — | — |
| GET | `/docker/{dockername}/getgatewayid` | — | — |
| POST | `/docker/{dockername}/updatetopology` | username | vdmsid |
| POST | `/docker/{dockername}/resettopology` | username | vdmsid |
| GET | `/docker/{dockername}/device/{device_id}/getalldevicesensors` | username | vdmsid |
| POST | `/getparentdevicebypagination` | username | vdmsid |
| POST | `/docker/{dockername}/getparentdevice` | username | vdmsid |
| GET | `/docker/{dockername}/device/{device_id}/parent/{parent_id}/getsubsystemparentdeviceinfo` | username | vdmsid |
| POST | `/docker/{dockername}/updatematcheddeviceproduct` | username | vdmsid |
| POST | `/docker/{dockername}/searchdevices` | username | vdmsid |
| POST | `/docker/{dockername}/sortdevices` | username | vdmsid |
| POST | `/docker/{dockername}/filterdevices` | username | vdmsid |
| POST | `/docker/{dockername}/archivedevices` | username | vdmsid |
| POST | `/docker/{dockername}/getdeviceinfobycustomfields` | username | vdmsid |
| POST | `/docker/{dockername}/searchsortfilterdevices` | username | vdmsid |
| POST | `/docker/{dockername}/searchsortfilterdevicescount` | username | vdmsid |
| GET | `/device/network/{network_name}/getassignedemail` | — | vdms_id |
| GET | `/docker/{dockername}/getalertmessages` | username | vdmsid |
| POST | `/upsertassetimages` | username | vdms_id |
| DELETE | `/deleteassetimages` | username | vdms_id |
| DELETE | `/deletedeviceimages` | username | vdms_id |
| GET | `/device/{device_id}/getassetimages` | username | vdms_id |
| GET | `/device/{device_id}/getallassetimages` | username | vdms_id |
| POST | `/group/{group}/getalldevicespagination` | username | vdmsid |
| GET | `/getfiltervirtualdevicesbypagination` | username | vdmsid |
| GET | `/getpowersourcetopologyconnectionscount` | username | vdmsid |
| GET | `/getpowersourcetopologybypagination` | username | vdmsid |
| GET | `/location/{location_id}/getdevicesbylocationid` | username | vdmsid |
| GET | `/device/{deviceid}/getdevicerebootstatus` | username | vdmsid |
| POST | `/upsertassetocrimages` | username | vdms_id |
| DELETE | `/deleteassetocrimages` | username | vdms_id |
| GET | `/device/{device_id}/getassetocrimages` | username | vdms_id |
| POST | `/docker/{dockername}/adddevice` | username | vdmsid |
| POST | `/docker/{dockername}/exportfiltereddevices` | username | vdmsid |
| GET | `/syncdeviceonboardstatus` | — | vdmsid |
| GET | `/device/{device_id}/syncsingledeviceonboardstatus` | — | vdmsid |
| POST | `/docker/{dockername}/updateassetmatchdetails` | username | vdmsid |
| POST | `/upsertdigitaltwininstruments` | username | vdmsid |
| DELETE | `/device_id/{device_id}/deletedigitaltwin` | username | vdmsid |
| POST | `/multieditdigitaltwininstruments` | username | vdmsid |
| POST | `/docker/{dockername}/exportfilteredmeasuringinstrument` | username | vdmsid |
| PUT | `/vdms/updatedevicetype` | — | — |
| POST | `/togglednd` | — | — |
| POST | `/deviceid/{id}/timetamp/{timetamp}/updatedndstatus` | — | — |
| GET | `/docker/{docker_name}/getalldevicedetails` | username | vdmsid |
| POST | `/docker/{docker_name}/getdevicedetailsbyids` | — | — |
| POST | `/docker/{docker_name}/getdevicecustomdetailsbyids` | username | vdmsid |
| GET | `/docker/{docker_name}/getalldeviceids` | — | — |
| POST | `/adddevicehistory` | username | vdmsid |
| GET | `/device/{device_id}/getdevicehistory` | username | vdmsid |
| POST | `/devicespecification` | — | — |
| POST | `/deltadevicespecs` | — | — |
| GET | `/devicespecification/{deviceId}` | — | — |
| GET | `/installedapps/{deviceId}` | — | — |
| GET | `/systemupdates/{deviceId}` | — | — |
| POST | `/remotesupport` | — | — |
| GET | `/remotesupport/device/{deviceId}` | username | — |
| GET | `/sessions/{id}` | — | — |
| POST | `/session/approval` | — | — |
| GET | `/getdevicetechnicianaisuggestion` | — | vdmsid |
| POST | `/upsertdocument` | username | vdmsid |
| DELETE | `/documentid/{documentid}/deletedocument` | username | vdmsid |
| GET | `/getdocuments` | username | vdmsid |
| GET | `/device/{deviceid}/getdocumentbydeviceid` | username | vdmsid |
| POST | `/tagdocumenttodevice` | username | vdmsid |
| POST | `/untagdocumenttodevice` | username | vdmsid |
| POST | `/building/{building_id}/upsertfloors` | username | vdms_id |
| POST | `/upsertfloordetails` | username | vdms_id |
| DELETE | `/building/deletefloors` | username | vdms_id |
| DELETE | `/floor/{floor_id}/deletefloorimage` | username | vdms_id |
| POST | `/floor/{floor_id}/updatefloorpath` | username | — |
| GET | `/floor/{floor_id}/getfloorpathbyfloorid` | username | vdms_id |
| GET | `/floor/{floor_id}/getfloorbyfloorid` | username | vdms_id |
| GET | `/building/{building_id}/getfloorsbybuildingid` | username | vdms_id |
| GET | `/floor/{floor_id}/getfloordetailsbyfloorid` | username | vdms_id |
| POST | `/floor/{floor_id}/upsertlocations` | username | vdms_id |
| DELETE | `/building/floor/deletelocations` | email | vdms_id |
| GET | `/getlocations` | username | vdms_id |
| GET | `/floor/{floor_id}/getlocationsbyfloorid` | username | vdms_id |
| POST | `/floor/{floor_id}/location/{location_id}/updatelocationdetails` | username | vdms_id |
| POST | `/floor/{floor_id}/getlocationsbyflooridpagination` | username | vdms_id |
| GET | `/floor/{floor_id}/getlocationscountbyfloorid` | username | vdms_id |
| GET | `/location/{location_id}/getlocationdetailsbylocationid` | username | vdms_id |
| POST | `/group/{group}/getalllocationspagination` | username | vdmsid |
| POST | `/searchSortFilterLocationsCount` | username | vdms_id |
| GET | `/measuring_instrument_id/{measuring_instrument_id}/gettaggedmeasuringinstrumentlocations` | username | vdmsid |
| GET | `/getuniquelocationtypes` | username | vdms_id |
| POST | `/floor/{floor_id}/multiupdatelocations` | username | vdms_id |
| POST | `/floor/{floor_id}/upsertlocationsdetails` | username | vdms_id |
| POST | `/getalllocationsbyfilterbypagination` | username | vdms_id |
| POST | `/addlocationhistory` | username | vdmsid |
| GET | `/location/{location_id}/getlocationhistory` | username | vdmsid |
| GET | `/docker/{dockername}/getallmanagedsoftwares` | username | vdmsid |
| PUT | `/docker/all/upsertmanagedsoftware` | username | vdmsid |
| PUT | `/docker/{dockername}/taginventorydetails` | username | vdmsid |
| PUT | `/docker/{dockername}/untaginventorydetails` | username | vdmsid |
| GET | `/docker/{dockername}/managedsoftware/{managedsoftwareid}/users` | username | vdmsid |
| GET | `/docker/{dockername}/managedsoftware/{managedsoftwareid}/license` | username | vdmsid |
| GET | `/docker/{dockername}/getmanagedsoftwarecount` | username | vdmsid |
| GET | `/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance` | username | vdmsid |
| PUT | `/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance/action` | username | vdmsid |
| POST | `/docker/{dockername}/searchsortfiltermanagedsoftware` | username | vdmsid |
| POST | `/docker/{dockername}/searchsortfiltermanagedsoftwarecount` | username | vdmsid |
| GET | `/getmanagedsoftwarefieldslist` | username | vdmsid |
| GET | `/getmanagedsoftwareuserslist` | username | vdmsid |
| GET | `/getmanagedsoftwareostypeslist` | username | vdmsid |
| DELETE | `/docker/{dockername}/managedsoftware/{managedsoftwareid}/deletemanagedsoftware` | username | vdmsid |
| GET | `/docker/{dockername}/getinventoryapplications` | username | vdmsid |
| POST | `/upsertmedia` | username | vdmsid |
| DELETE | `/mediaid/{mediaid}/deletemedia` | username | vdmsid |
| GET | `/getmedias` | username | vdmsid |
| GET | `/device/{deviceid}/getmediabydeviceid` | username | vdmsid |
| POST | `/tagmediatodevice` | username | vdmsid |
| POST | `/untagmediatodevice` | username | vdmsid |
| POST | `/docker/{dockername}/device/{device_id}/note` | username | vdmsid |
| GET | `/docker/{dockername}/device/{device_id}/notes` | username | vdmsid |
| DELETE | `/docker/{dockername}/device/{device_id}/note/{note_id}` | username | vdmsid |
| POST | `/upsertpropertyservice` | username | vdmsid |
| POST | `/service/{property_service_id}/addpropertyservicelocations` | username | vdmsid |
| POST | `/updatepropertyserviceresponses` | username | vdmsid |
| GET | `/getpropertyservices` | username | vdmsid |
| GET | `/service/{property_service_id}/getpropertyservicelocations` | username | vdmsid |
| DELETE | `/deletepropertyservicerequests` | username | vdmsid |
| DELETE | `/service/{property_service_id}/deletepropertyservicelocations` | username | vdmsid |
| DELETE | `/service/{property_service_id}/deletepropertyservice` | username | vdmsid |
| GET | `/building/{building_id}/floor/{floor_id}/location/{location_id}/service/{property_service_id}/getzonemap` | username | vdmsid |
| POST | `/editdevicespecifications` | username | vdmsid |
| POST | `/adddevicespecifications` | username | vdmsid |
| GET | `/device/{device_id}/getdevicespecificationsbydeviceid` | username | vdmsid |
| POST | `/tagpowersources` | username | vdmsid |
| POST | `/untagpowersource` | username | vdmsid |
| POST | `/untagdevice` | username | vdmsid |
| POST | `/gettaggeddevices` | username | vdmsid |
| GET | `/device/{device_id}/gettaggedpowersourcesbydeviceid` | username | vdmsid |
| DELETE | `/deletespecifications` | username | vdmsid |
| POST | `/getpowerbasedloadcalculation` | username | vdmsid |
