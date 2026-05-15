package io.sclera.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.Repository.LocationRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.RecordChecklistDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.FloorRepository;
import io.sclera.dto.FloorDTO;
import io.sclera.utils.Utils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Service
@ConfigurationProperties(prefix = "sclera")
public class FloorService {
    private static final Logger log = LoggerFactory.getLogger(FloorService.class);

    @Autowired
    FloorRepository floorRepository;

    @Autowired
    LocationService locationservice;

    @Autowired
    Utils utils;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    WebClientService webClientService;

    @Autowired
    RecordChecklistService recordChecklistService;

    @Autowired
    UserActionLogService userActionLogService;
    @Autowired
    APICallService apicallService;

    @Autowired
    VdmsRepository vdmsRepository;

    String absolutePathFloorImages = "/home/sclera/images/";

    private String server_floor_images_url;
    private String server_floor_images_absolute_path;

    private Integer flag;


    public String getServer_floor_images_url() {
        return server_floor_images_url;
    }

    public void setServer_floor_images_url(String server_floor_images_url) {
        this.server_floor_images_url = server_floor_images_url;
    }

    public String getServer_floor_images_absolute_path() {
        return server_floor_images_absolute_path;
    }

    public void setServer_floor_images_absolute_path(String server_floor_images_absolute_path) {
        this.server_floor_images_absolute_path = server_floor_images_absolute_path;
    }

    public void upsertFloorsByBuildingId(Set<FloorDTO> floors, String building_id) {
        if (floors != null) {
            Set<String> floor_ids = floorRepository.getFloorIdsByBuildingId(building_id);
            if (floor_ids != null && floor_ids.size() > 0) {
                for (FloorDTO floor : floors) {
                    if (compareIds(floor_ids, floor.getFloor_id())) {
                        updateFloorByFloorId(floor, building_id);
                    } else {
                        addFloorByBuildingId(floor, building_id);
                    }
                }
            } else {
                for (FloorDTO floor : floors) {
                    addFloorByBuildingId(floor, building_id);
                }
            }
        }
    }

//	public void updateFloorByFloorId(FloorDTO floordto) {
//
//
//
//		final String absolute_path = "/home/sclera/images/";
////		final String absolute_path = "/home/rajath/Desktop/ts_images/";
//		// get image if exists
//
//		String local_image_url = floorRepository.getImageUrlById(floordto.getFloor_id());
//
//
//
//		// check image
//		if (local_image_url != null) {
//
//			if (floordto.getImage_url() != null) {
//				String local_extension = getExtensionByUrl(local_image_url);
//				String global_extension = getExtensionByUrl(floordto.getImage_url());
//
//				try {
//					String global_hash = getHashOfImage(floordto.getImage_url(), global_extension);
//					String local_hash = getHashOfImage(local_image_url, local_extension);
//
//					if (!global_hash.equals(local_hash)) // Different image
//					{
//						removeFileFromServer(absolute_path, floordto.getFloor_id(), local_extension);
//
//						byte[] image_data = getBytesArrayByImageUrl(floordto.getImage_url());
//						addFileToServer(image_data, absolute_path, floordto.getFloor_id(),
//								global_extension);
//
//						String image_url = "http://localhost:8888/images/" + floordto.getFloor_id()+"." + global_extension ;
//						floordto.setImage_url(image_url);
//					}
//					else
//					{
//						floordto.setImage_url(local_image_url);
//					}
//
//				} catch (NoSuchAlgorithmException | IOException e) {
//					System.out.println(e);
//				}
//			}
//		} else {
//			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&& INSIDE ELSE &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//			if (floordto.getImage_url() != null) {
//				String global_extension = getExtensionByUrl(floordto.getImage_url());
//				try {
//					byte[] image_data = getBytesArrayByImageUrl(floordto.getImage_url());
//					addFileToServer(image_data, absolute_path, floordto.getFloor_id(),
//							global_extension);
//					String image_url = "http://localhost:8888/images/" + floordto.getFloor_id()+"." + global_extension ;
//					floordto.setImage_url(image_url);
//
//				} catch (IOException e) {
//					System.out.println(e);
//				}
//			}
//		}
//
//		// update
//		floorRepository.updateFloorByFloorId(floordto.getName(), floordto.getInitial_position(),
//				floordto.getImage_url(), floordto.getFloor_id(),floordto.getAngle());
//		locationservice.upsertLocationByFloorId(floordto.getLocations(), floordto.getFloor_id());
//	}


    public void updateFloorByFloorId(FloorDTO floordto, String building_id) {


        final String absolute_path = "/home/sclera/images/";
//		final String absolute_path = "/home/rajath/Desktop/ts_images/";
        // get image if exists

        String local_image_url = floorRepository.getImageUrlById(floordto.getFloor_id());

        String floor_image_url = floordto.getImage_url();
        floordto.setImage_url(null);

        // check image
        if (local_image_url != null) {

            if (floor_image_url != null) {
                String local_extension = getExtensionByUrl(local_image_url);
                String global_extension = getExtensionByUrl(floor_image_url);

                try {
                    String global_hash = getHashOfImage(floor_image_url, global_extension);
                    String local_hash = getHashOfImage(local_image_url, local_extension);

                    if (!global_hash.equals(local_hash)) // Different image
                    {
                        removeFileFromServer(absolute_path, floordto.getFloor_id(), local_extension);

                        byte[] image_data = getBytesArrayByImageUrl(floordto.getImage_url());
                        addFileToServer(image_data, absolute_path, floordto.getFloor_id(),
                                global_extension);

                        String image_url = "http://localhost:8888/images/" + floordto.getFloor_id() + "." + global_extension;
                        floordto.setImage_url(image_url);
                    } else {
                        floordto.setImage_url(local_image_url);
                    }

                } catch (NoSuchAlgorithmException | IOException e) {
                    log.error("Exception in update Floor By FloorId, Error message : ",e);
                }
            }
        } else {
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&& INSIDE ELSE &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            if (floor_image_url != null) {
                String global_extension = getExtensionByUrl(floor_image_url);
                try {
                    byte[] image_data = getBytesArrayByImageUrl(floor_image_url);
                    addFileToServer(image_data, absolute_path, floordto.getFloor_id(),
                            global_extension);
                    String image_url = "http://localhost:8888/images/" + floordto.getFloor_id() + "." + global_extension;
                    floordto.setImage_url(image_url);

                } catch (IOException e) {
                    log.error("Exception in update Floor By FloorId, Error message : ",e);
                }
            }
        }

        // update
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        int rowsEffected = floorRepository.updateFloorByFloorId(floordto.getName(), floordto.getInitial_position(),
                floordto.getImage_url(), floordto.getFloor_id(), floordto.getAngle(), timestamp);
        if(rowsEffected>0){
            syncFloorToADCServer(building_id, floordto);
        }
        locationservice.upsertLocationByFloorId(floordto.getLocations(), floordto.getFloor_id());
    }


//	public String addFloorByBuildingId(FloorDTO floordto, String building_id) {
//		final String absolute_path = "/home/sclera/images/";
////		final String absolute_path = "/home/rajath/Desktop/ts_images/";
//
//		if (floordto.getFloor_id() == null) {
//			String id = Generators.timeBasedGenerator().generate().toString();
//			floordto.setFloor_id(id);
//		}
//
//		if (floordto.getImage_url() != null) {
//			String global_extension = getExtensionByUrl(floordto.getImage_url());
//			try {
//				byte[] image_data = getBytesArrayByImageUrl(floordto.getImage_url());
//				addFileToServer(image_data, absolute_path, floordto.getFloor_id(), global_extension);
//				String image_url = "http://localhost:8888/images/" + floordto.getFloor_id()+"." + global_extension ;
//				floordto.setImage_url(image_url);
//
//			} catch (IOException e) {
//				System.out.println(e);
//			}
//		}
//
//		floorRepository.addFloorByBuildingId(floordto.getFloor_id(), floordto.getName(), floordto.getInitial_position(),
//				floordto.getImage_url(), building_id,floordto.getAngle());
//		locationservice.upsertLocationByFloorId(floordto.getLocations(), floordto.getFloor_id());
//		return floordto.getFloor_id();
//	}


    public String addFloorByBuildingId(FloorDTO floordto, String building_id) {
        final String absolute_path = "/home/sclera/images/";
//		final String absolute_path = "/home/rajath/Desktop/ts_images/";

        String floor_image_url = floordto.getImage_url();
        floordto.setImage_url(null);

        if (floordto.getFloor_id() == null) {
            String id = Generators.timeBasedGenerator().generate().toString();
            floordto.setFloor_id(id);
        }


        if (floor_image_url != null) {
            String global_extension = getExtensionByUrl(floor_image_url);
            try {
                byte[] image_data = getBytesArrayByImageUrl(floordto.getImage_url());
                addFileToServer(image_data, absolute_path, floordto.getFloor_id(), global_extension);
                String image_url = "http://localhost:8888/images/" + floordto.getFloor_id() + "." + global_extension;
                floordto.setImage_url(image_url);

            } catch (IOException e) {
                log.error("Exception in add Floor By Building Id, Error message : ",e);
            }
        }
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        int rowsEffected = floorRepository.addFloorByBuildingId(floordto.getFloor_id(), floordto.getName(), floordto.getInitial_position(),
                floordto.getImage_url(), building_id, floordto.getAngle(), timestamp);
        if (rowsEffected > 0) {
            syncFloorToADCServer(building_id, floordto);
        }
        locationservice.upsertLocationByFloorId(floordto.getLocations(), floordto.getFloor_id());
        return floordto.getFloor_id();
    }

    public void syncFloorToADCServer(String building_id, FloorDTO floordto){
       try {
           VdmsDTO vdmsDetails = vdmsRepository.getSyncDetailsForADC();
           if(building_id != null && !building_id.isEmpty()){
               floordto.setBuildingId(building_id);
           }
           if(floordto.getFloor_id() != null && !floordto.getFloor_id().isEmpty()) {
               floordto.setId(floordto.getFloor_id());
           }
           Boolean status = apicallService.syncFloorToADC(building_id, Collections.singletonList(floordto),vdmsDetails.getCustomer_org_id(), vdmsDetails.getAdc_configuration_id());
           log.info("Floor synced to ADC server successfully, Floor ID: {}, Status: {}", floordto.getFloor_id(), status);
       }
      catch (Exception e){
              log.error("Error syncing floor to ADC server", e);
       }
    }



    public boolean compareIds(Set<String> floor_ids, String floor_id) {
        return floor_ids.stream().anyMatch(b -> b.equals(floor_id));
    }

    public String getHashOfImage(String imageurl, String extension) throws IOException, NoSuchAlgorithmException {


        byte[] data = getBytesArrayByImageUrl(imageurl);

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);
        byte[] digest = md5.digest();

        return new BigInteger(1, digest).toString(16);
    }

    public byte[] getBytesArrayByImageUrl(String image_url) throws IOException {
//		URL url = new URL(image_url);
//		InputStream inputstream = url.openStream();
//		return IOUtils.toByteArray(inputstream);

        URL url = new URL(image_url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        if (con.getHeaderField("Location") != null && (con.getResponseCode() / 100) == 3) {
            url = new URL(con.getHeaderField("Location"));

        }
        InputStream inputstream = url.openStream();
        return IOUtils.toByteArray(inputstream);

    }

    public String getExtensionByUrl(String url) {
        return url.substring(url.lastIndexOf(".") + 1, url.length());
    }

    public String addFileToServer(byte[] image, String directory, String file_name, String file_extension)
            throws IOException {

//		 String dir = "/home/rajath/Desktop/ts_images/";
        String dir = "/home/sclera/images/";

        if (image != null) {
            Path path = Paths.get(directory + file_name + "." + file_extension);
            Files.write(path, image);
            return dir + file_name + file_extension;
        } else {
            System.out.println("Error");
            return null;
        }

    }

    public void removeFileFromServer(String absolute_path, String file_name, String file_extension) {
        File file = new File(absolute_path + file_name + "." + file_extension);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }
        }
    }


    //delete floor not tagged to a location
    public void deleteUnlikedFloors() {
        Set<String> unlikedFloorIds = floorRepository.getUnlinkedFloorIds();
        for (String floorId : unlikedFloorIds) {
            deleteFloorById(floorId);
        }
    }

    private void deleteFloorById(String floorId) {
        String imageUrl = floorRepository.getImageUrlById(floorId);
        floorRepository.deleteById(floorId);
        utils.removeFileFromServer(absolutePathFloorImages, floorId, getExtensionByUrl(imageUrl));
    }


    /******************************************************************* new floor changes *************************************************************************/

    public Set<FloorDTO> upsertFloorsByBuildingId(String username, String vdms_id, String building_id, Set<FloorDTO> floors, HttpServletRequest httpServletRequest) {
        String action;
        for (FloorDTO floor : floors) {
            action = "UPDATE";
            if (floor.getFloor_id() == null) {
                floor.setFloor_id(Generators.timeBasedGenerator().generate().toString());
                action = "ADD";
            }

            this.upsertFloorByBuildingId(building_id, floor, action, username, httpServletRequest);
            if (floor.getLocations() != null) {
                Set<LocationDTO> locations = locationservice.upsertLocationsByFloorId(username, vdms_id, floor.getFloor_id(), floor.getLocations(), httpServletRequest);
                floor.setLocations(locations);
            }
        }
        return floors;
    }

    private void upsertFloorByBuildingId(String building_id, FloorDTO floor, String action, String username, HttpServletRequest httpServletRequest) {
        try {
            BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
            int rowsEffected = floorRepository.upsertFloorByBuildingId(floor.getFloor_id(), floor.getName(), floor.getInitial_position(), floor.getAngle(), building_id, timestamp);
            // CALL API ONLY AFTER SUCCESSFUL UPSERT
            if (rowsEffected > 0) {
                syncFloorToADCServer(building_id, floor);
            }
            if (action.equals("ADD")) {
                userActionLogService.addUserAction(username, "maps", action, "A Floor with name: " + floor.getName() + " and  id: " + floor.getFloor_id() + " is added", "success", "floor", floor.getFloor_id());
                log.info("endpoint: {}, upsertFloorByBuildingId, description: A new floor is added, params: floor: {}", httpServletRequest.getRequestURI(), floor);
            } else {
                userActionLogService.addUserAction(username, "maps", action, "A Floor with name: " + floor.getName() + " and  id: " + floor.getFloor_id() + " is updated", "success", "floor", floor.getFloor_id());
                log.info("endpoint: {},upsertFloorByBuildingId, description: A floor is updated, params: floor: {}", httpServletRequest.getRequestURI(), floor);
            }
        } catch (Exception e) {
            if (action.equals("ADD")) {
                userActionLogService.addUserAction(username, "maps", action, " Unable to add Floor with name: " + floor.getName() + " and  id: " + floor.getFloor_id(), "failed", "floor", floor.getFloor_id());
                log.error("Exception in ADD floor, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            } else {
                userActionLogService.addUserAction(username, "maps", action, " Unable to update Floor with name: " + floor.getName() + " and  id: " + floor.getFloor_id(), "failed", "floor", floor.getFloor_id());
                log.error("Exception in UPDATE floor, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
        }
    }


    public void deleteFloorImageByFloorId(String username, String vdms_id, String floor_id, String clear_path, HttpServletRequest httpServletRequest) {
        FloorDTO floorDTO = this.getFloorById(floor_id);
        List<FloorDTO> floors = new ArrayList<>();
        floors.add(floorDTO);
        String data = null;
        try {
            data = webClientService.deleteFloorMapsByImageUrl(vdms_id, floors);
            userActionLogService.addUserAction(username,"maps","DELETE"," Floor Image and Tiles for Floor with id:" + floorDTO.getFloor_id() + " and name: " + floorDTO.getName() + " deleted successfully " , "success","floor",floorDTO.getFloor_id());
            log.info("endpoint: {}, deleteFloorImageByFloorId, description: Floor Image and Tiles deleted, params: floorDTO: {} ", httpServletRequest.getRequestURI(), floorDTO);

        } catch (Exception e) {
            userActionLogService.addUserAction(username,"maps","DELETE"," Unable to delete Floor Image and Tiles for Floor with id:" + floorDTO.getFloor_id() + " and name: " + floorDTO.getName() , "failed","floor",floorDTO.getFloor_id());
            log.error("Exception in deleting Floor Image By FloorId, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
        }
        if (data != null) {
            if (floorDTO.getLocal_image_url() != null) {
                this.removeFloorImageFromServer(floorDTO.getLocal_image_url());
            }
            floorDTO.setImage_url(null);
            floorDTO.setLocal_image_url(null);
            try {
                floorRepository.updateLocalImageUrl(null, floorDTO.getFloor_id());
                userActionLogService.addUserAction(username,"maps","DELETE"," Local Image for Floor with id:" + floorDTO.getFloor_id() + " and name: " + floorDTO.getName() + " deleted successfully " , "success","floor",floorDTO.getFloor_id());
                log.info("endpoint: {}, deleteFloorImageByFloorId, description: Local image deleted, params: floorDTO: {} ", httpServletRequest.getRequestURI(), floorDTO);

            } catch (Exception e) {
                userActionLogService.addUserAction(username,"maps","DELETE"," Unable to delete Local Image for Floor with id:" + floorDTO.getFloor_id() + " and name: " + floorDTO.getName()  , "failed","floor",floorDTO.getFloor_id());
                log.error("Exception in deleting Local Image for Floor, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
            this.addFloorMapZoomLevels(floorDTO.getFloor_id(), null, null);
            if (clear_path.equalsIgnoreCase("yes")) {
                floorDTO.setPath(null);
                try {
                    floorRepository.updatePathByFloorId(floorDTO.getPath(), floor_id);
                    userActionLogService.addUserAction(username,"maps","DELETE"," Path for Floor with id:" + floorDTO.getFloor_id() + " and name: " + floorDTO.getName() + " deleted successfully " , "success","floor",floorDTO.getFloor_id());
                    log.info("endpoint: {}, deletePathforFloor, description: Path deleted, params: floorDTO: {} ", httpServletRequest.getRequestURI(), floorDTO);

                } catch (Exception e) {
                    userActionLogService.addUserAction(username,"maps","DELETE"," Unable to delete Path for Floor with id:" + floorDTO.getFloor_id() + " and name: " + floorDTO.getName()  , "failed","floor",floorDTO.getFloor_id());
                    log.error("Exception in deleting Path for Floor, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
                }
                // new changes
                locationservice.updateArea(username,floor_id, null, 0, httpServletRequest);
            }
        }
        this.addFloorImageUrlById(floorDTO.getImage_url(), floorDTO.getInitial_position(), floorDTO.getAngle(), floorDTO.getFloor_id());
    }

    public void upsertFloorByBuildingIdsFromBackend(String building_id, FloorDTO floor) {
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        int rowseffected = floorRepository.upsertFloorByBuildingIdsFromBackend(floor.getFloor_id(), floor.getName(), floor.getInitial_position(), floor.getAngle(), building_id, floor.getImage_url(), floor.getPath(), timestamp);
        if (rowseffected > 0) {
            syncFloorToADCServer(building_id, floor);
        }
    }


    private FloorDTO getFloorById(String floor_id) {
        return floorRepository.getFloorById(floor_id);
    }

    public String updatePathByFloorId(String username,String floor_id, String path, HttpServletRequest httpServletRequest) {
        try {
            floorRepository.updatePathByFloorId(path, floor_id);
            userActionLogService.addUserAction(username,"maps","UPDATE"," Path for Floor with id:" + floor_id + " updated successfully " , "success","floor",floor_id);
            log.info("endpoint: {}, updatePathByFloorId, params: floor_id: {}", httpServletRequest.getRequestURI(), floor_id);
            return floorRepository.getFloorPathByFloorId(floor_id);
        } catch (Exception e) {
            userActionLogService.addUserAction(username,"maps","UPDATE"," Unable to update Path for Floor with id:" + floor_id  , "failed","floor",floor_id);
            log.error("Exception in UPDATE Path By FloorId, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
        }

        return null;
    }

    public String getFloorPathByFloorId(String username, String vdms_id, String floor_id) {
        return floorRepository.getFloorPathByFloorId(floor_id);
    }

    private void removeFloorImageFromServer(String image_url) {
        String file_name = this.getFileNameByImageUrl(image_url);
        // delete the image from server as well
        removeFileFromServer(server_floor_images_absolute_path, file_name);
    }

    public Integer addFloorImageByFloorId(String username, String vdms_id, MultipartFile floor_image, String floor_dto, HttpServletRequest httpServletRequest) {
        log.info("Entered here flag before: {}", flag);
        if (flag != null && flag == 2) {
            log.info("Entered here for thread still in process, flag: {}", flag);
            return flag;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String updated_image_url = null;
        try {
            FloorDTO floorDTO = objectMapper.readValue(floor_dto, FloorDTO.class);
            FloorDTO floorDetails = this.getFloorById(floorDTO.getFloor_id());
            List<FloorDTO> floors = new ArrayList<>();
            FloorDTO coordinates = this.mapInitialPositionToCoordinates(floorDTO.getInitial_position(), floorDTO.getFloor_id());
            if (floorDTO.getImage_url() != null) {
                if (floor_image == null) {
                    log.info("Entered here since image url present and image null");
                    updated_image_url = floorDTO.getImage_url();
                    coordinates.setImage_url(updated_image_url);
                    floors.add(coordinates);
                    try {
                        floors = webClientService.uploadFloorImages(vdms_id, floors);
                        userActionLogService.addUserAction(username,"maps","UPDATE"," Floor Map Tiles Updated Successfully for floor with id: " + floorDTO.getFloor_id() ,"success", "floor",floorDTO.getFloor_id());
                        log.info("endpoint: {}, addFloorImageByFloorId, description: Floor Map Tiles updated, params: floorDTO: {}", httpServletRequest.getRequestURI(), floorDTO);

                    } catch (Exception e) {
                        userActionLogService.addUserAction(username,"maps","UPDATE"," Unable to update Floor Map Tiles for floor with id: " + floorDTO.getFloor_id() ,"failed","floor",floorDTO.getFloor_id());
                        log.error("Exception in UPDATE floor Image By FloorId, endpoint: {} ,  Error message :  ", httpServletRequest.getRequestURI(), e);

                    }
                    this.updateZoomLevels(floors);
                    log.info("Image URL processed successfully for floor ID: {}", floorDTO.getFloor_id());

                } else {

                    List<FloorDTO> floorMap = new ArrayList<>();
                    floorMap.add(floorDTO);
                    String data = null;
                    try {
                        data = webClientService.deleteFloorMapsByImageUrl(vdms_id, floorMap);
                        userActionLogService.addUserAction(username,"maps","DELETE"," Floor Map Tiles deleted Successfully for floor with id: " + floorDTO.getFloor_id() ,"success", "floor",floorDTO.getFloor_id());
                        log.info("endpoint: {}, deleteFloorImages, description: Floor Map Tiles deleted, params: floorDTO: {}", httpServletRequest.getRequestURI(), floorDTO);

                    } catch (Exception e) {
                        userActionLogService.addUserAction(username,"maps","DELETE"," Unable to delete Floor Map Tiles for floor with id: " + floorDTO.getFloor_id() ,"failed", "floor",floorDTO.getFloor_id());
                        log.error("Exception in DELETE Floor Image By FloorId, endpoint: {},  Error message : ", httpServletRequest.getRequestURI(), e);

                    }
                    if (data != null) {
                       // log.info("Floor map tiles deleted successfully for floor ID: {}. Removing local image and resetting zoom levels.", floorDTO.getFloor_id());
                        if (floorDetails.getLocal_image_url() != null) {
                            this.removeFloorImageFromServer(floorDetails.getLocal_image_url());
                        }
                        this.addFloorMapZoomLevels(floorDTO.getFloor_id(), null, null);
                    }
                }
                flag = 0;
            }
            if (floor_image != null) {   // Add floor image
                log.info("Entered here for upload of image. Floor ID:{}", floorDTO.getFloor_id());
                floors.add(coordinates);
                try {
                    floors = webClientService.addFloorImages(vdms_id, floor_image, floors);
                    userActionLogService.addUserAction(username,"maps","ADD"," Floor Map Tiles Added Successfully for floor with id: " + floorDTO.getFloor_id() ,"success","floor",floorDTO.getFloor_id());
                    log.info("endpoint: {}, addFloorImageByFloorId, description: Floor Map Tiles added successfully, params: floorDTO: {}", httpServletRequest.getRequestURI(), floorDTO);

                } catch (Exception e) {
                    userActionLogService.addUserAction(username,"maps","ADD"," Unable to add Floor Map Tiles for floor with id: " + floorDTO.getFloor_id() ,"failed", "floor",floorDTO.getFloor_id());
                    log.error("Exception in ADD Floor Image By FloorId, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);

                }
                for (FloorDTO floor_dtos : floors) {
                    if (floor_dtos.getImage_url() != null) {
                        updated_image_url = floor_dtos.getImage_url();
                        log.info("Image URL found for Floor ID: {}. Processing local storage update.", floor_dtos.getFloor_id());
                        String local_image_url = this.addLocalFloorImagesToServer(floor_image.getBytes(), server_floor_images_absolute_path, this.getFileNameByImageUrl(floor_dtos.getImage_url()));
                        if (local_image_url != null) {
                            floorRepository.updateLocalImageUrl(local_image_url, floor_dtos.getFloor_id());
                            log.info("Updated local image URL for Floor ID: {} to {}", floor_dtos.getFloor_id(), local_image_url);
                        }
                    }
                }
                this.updateZoomLevels(floors);
                log.info("Zoom levels updated successfully for all floors after image upload. Floor ID: {}", floorDTO.getFloor_id());

                flag = 0;
            }
            if (floorDTO.isUpdateAllFloors()) {
                this.updateFloorOrientationsByBuildingId(username, vdms_id,floorDTO.getInitial_position(), floorDTO.getAngle(), floorDTO.getBuilding_id(), httpServletRequest);
                try {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        log.info("Flag" + flag);
                        flag = 2;
                        log.info("Entered here since isupdateALlfloors is true");
                        Set<FloorDTO> floorsDetails = this.getFloorsDetailsByBuildingId(floorDTO.getBuilding_id());
                        List<FloorDTO> floorMaps = new ArrayList<>();
                        for (FloorDTO floordto : floorsDetails) {
                            if (!floordto.getFloor_id().equals(floorDTO.getFloor_id())) {
                                if (floordto.getImage_url() != null) {
                                    log.info("Processing floor ID: {} for update.", floordto.getFloor_id());
                                    FloorDTO mapCoordinates = this.mapInitialPositionToCoordinates(floorDTO.getInitial_position(), floordto.getFloor_id());
                                    mapCoordinates.setImage_url(floordto.getImage_url());
                                    floorMaps.add(mapCoordinates);
                                }
                            }
                        }
                        floorMaps = this.updateAllFloors(vdms_id, floorMaps);
                        this.updateZoomLevels(floorMaps);
                        flag = 0;
                    });
                    executorService.shutdown();
                } catch (Exception e) {
                    log.error("Exception in add Floor Image By Floor Id, Error message : ",e);
                }
            }

            this.addFloorImageUrlById(updated_image_url, floorDTO.getInitial_position(), floorDTO.getAngle(), floorDTO.getFloor_id());
            userActionLogService.addUserAction(username,"maps","ADD"," Floor Details Added/Updated Successfully for floor with id: " + floorDTO.getFloor_id() ,"success","floor",floorDTO.getFloor_id());
            log.info("endpoint: {},description: add Floor Details for floor, params: floorDTO: {}", httpServletRequest.getRequestURI(), floorDTO);

            if (floorDTO.isUpdateAllFloors()) {
                return 1;
            }
        } catch (IOException e) {
//            userActionLogService.addUserAction(username,"maps","ADD"," Unable to Add/Update Floor Details for floor with id:  Added/Updated Successfully","failed","floor", "");
            log.error("Exception in adding Floor Details for floor with id: ", e);
        }
        return flag;
    }

    public String addFloorImagesToServer(byte[] image, String directory, String file_name, String file_extension)
            throws IOException {
        if (image != null) {
            String addTimestamp = String.valueOf(System.nanoTime());
            Path path = Paths.get(directory + file_name + "_" + addTimestamp + "." + file_extension);
            Files.write(path, image);
            return server_floor_images_url + file_name + "_" + addTimestamp + "." + file_extension;
        } else {
            return null;
        }

    }

    public String getImageExtensionByImageUrl(String image_url) {
        return image_url.substring(image_url.lastIndexOf(".") + 1);
    }


    private void addFloorImageUrlById(String updated_image_url, String initial_position, Integer angle, String floor_id) {
        floorRepository.addFloorImageUrlById(updated_image_url, initial_position, angle, floor_id);
    }

    private void updateFloorOrientationsByBuildingId(String username, String vdms_id,String initial_position, Integer angle, String building_id, HttpServletRequest httpServletRequest) {
        try {
            floorRepository.updateFloorOrientationsByBuildingId(initial_position, angle, building_id);
            userActionLogService.addUserAction(username,"maps","UPDATE"," Floor Details Updated Successfully for all floors with Building id: " + building_id ,"success","building",building_id);
            log.info("endpoint: {},update FloorOrientationsByBuildingId, params: building_id: {}", httpServletRequest.getRequestURI(), building_id);

        } catch (Exception e) {
            userActionLogService.addUserAction(username,"maps","UPDATE"," Unable to update Floor Details for all floors with Building id: " + building_id ,"failed", "building",building_id);
            log.error("Exception in UPDATE Floor Orientations By BuildingId, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
        }
    }


    public Set<FloorDTO> getFloorsByBuildingId(String username, String vdms_id, String building_id, String field, String field_id) {
        Set<FloorDTO> floors = floorRepository.getFloorsByBuildingId(building_id);
        List<String> floor_ids = floors.stream()
                .map(FloorDTO::getFloor_id).distinct()
                .collect(Collectors.toList());

        Set<RecordChecklistDTO> allRecordChecklist = recordChecklistService.getAllRecordChecklistByBuildings(Collections.singletonList("all"),floor_ids, Collections.singletonList(building_id));

        for (FloorDTO floorDTO : floors) {
            if (field != null) {
                Map<String, Long> defaultCounts = new HashMap<>();
                defaultCounts.put("tagged", 0L);
                defaultCounts.put("inspection", 0L);
                defaultCounts.put("scheduled_services", 0L);
                defaultCounts.put("reactive_services", 0L);
                Map<String, Long> computedCounts = allRecordChecklist.stream()
                        .filter(recordChecklistDTO ->(recordChecklistDTO.getFloor_id() != null && recordChecklistDTO.getFloor_id().equals(floorDTO.getFloor_id())))
                        .collect(Collectors.groupingBy(
                                record -> {
                                    String type = record.getRecord_type();
                                    boolean hasInspectionId = record.getInspection_record_id() != null;
                                    if ((field.equals("all_task") || field.equals("tagged")) && "checklist".equals(type) && !hasInspectionId)
                                        return "tagged";
                                    if ((field.equals("all_task") || field.equals("inspection")) && "checklist".equals(type) && hasInspectionId)
                                        return "inspection";
                                    if ((field.equals("all_task") || field.equals("scheduled_services")) && "service".equals(type) && hasInspectionId)
                                        return "scheduled_services";
                                    if ((field.equals("all_task") || field.equals("reactive_services")) && "service".equals(type) && !hasInspectionId)
                                        return "reactive_services";
                                    return "unknown"; // Exclude unmatched records
                                },
                                Collectors.counting()
                        ));
                defaultCounts.putAll(computedCounts);
                JSONObject counts = new JSONObject();
                System.out.println("defaultCounts count for tagged :" + defaultCounts.get("tagged"));
                if (field.equals("all_task") || field.equals("tagged")) {
                    JSONObject tagged_count = new JSONObject();
                    tagged_count.put("all_count",defaultCounts.get("tagged"));
                    counts.put("tagged_procedure",tagged_count );
                }
                if (field.equals("all_task") || field.equals("reactive_services")) {
                    JSONObject reactive_service_count = new JSONObject();
                    reactive_service_count.put("all_count",defaultCounts.get("reactive_services"));
                    counts.put("service_requests", reactive_service_count);
                }
                if (field.equals("all_task") || field.equals("scheduled_services")) {
                    JSONObject scheduled_service_count = new JSONObject();
                    scheduled_service_count.put("all_count",defaultCounts.get("scheduled_services"));
                    counts.put("service_checklist", scheduled_service_count);
                }
                if (field.equals("all_task") || field.equals("inspection")) {
                    JSONObject inspection_count = new JSONObject();
                    inspection_count.put("all_count",defaultCounts.get("inspection"));
                    counts.put("inspection_checklist", inspection_count);
                }
                floorDTO.setCounts(counts);
            }
//                floorDTO.setCounts(recordChecklistService.getAllMapCounts(field, field_id, "all", floorDTO.getFloor_id(), "all"));
        }
        return floors;

    }

    public FloorDTO getFloorByFloorId(String username, String vdms_id, String floor_id) {
        FloorDTO floor = floorRepository.getFloorById(floor_id);
        Set<LocationDTO> locations = locationservice.getLocationsByFloorId(floor.getFloor_id(), vdms_id);
        floor.setLocations(locations);

        return floor;
    }

    public FloorDTO getFloorDetailsByFloorId(String username, String vdms_id, String floor_id) {
        return this.getFloorById(floor_id);
    }


    public String getFileNameByImageUrl(String image_url) {
        return image_url.substring(image_url.lastIndexOf("/") + 1);
    }


    public FloorDTO getFloorByLocationId(String location_id) {
        FloorDTO floor = floorRepository.getFloorByLocationId(location_id);
        if (floor != null) {
            LocationDTO location = locationservice.getLocationByLocationId(location_id);
            if (location != null) {
                Set<LocationDTO> locations = new HashSet<>();
                locations.add(location);
                floor.setLocations(locations);
            }
        }
        return floor;
    }

    public Set<FloorDTO> getFloorsDetailsByBuildingId(String building_id) {
        return floorRepository.getFloorsDetailsByBuildingId(building_id);
    }

    public void deleteFloorsByBuildingId(String vdmsid, String building_id, String username, HttpServletRequest httpServletRequest) {
        // change to list
        Set<FloorDTO> floors = this.getFloorsDetailsByBuildingId(building_id);

        for (FloorDTO floor : floors) {
            this.deleteFloorByFloorId(floor.getFloor_id(), floor.getLocal_image_url(), username, httpServletRequest);
        }
    }

    public void deleteFloorByFloorId(String floor_id, String local_image_url, String username, HttpServletRequest httpServletRequest) {
        String endpoint = httpServletRequest != null ? httpServletRequest.getRequestURI() : "SOCKET CALL";
        locationservice.deleteLocationsByFloorId(floor_id, username, false);
        if (local_image_url != null) {
            this.removeFloorImageFromServer(local_image_url);
        }
        FloorDTO floorDTO = this.getFloorNameByFloorId(floor_id);
        try {
            floorRepository.deleteById(floorDTO.getFloor_id());
            //Api call to delete floor from ADC server
            syncDeleteFloorToADC(floor_id, floorDTO.getBuildingId());
            userActionLogService.addUserAction(username, "maps", "DELETE", "A Floor with name : " + floorDTO.getName() + " and id : " + floor_id + " is deleted.", "success", "floor", floor_id);
            log.info("endpoint: {}, deleteFloorByFloorId,  params: floorDTO: {} ", endpoint, floorDTO);

        } catch (Exception ex) {
            userActionLogService.addUserAction(username, "maps", "DELETE", "Unable to delete floor with name : " + floorDTO.getName() + " and id : " + floor_id, "failed", "floor", floor_id);
            log.error("Exception in Deleting Floor By FloorId,endpoint: {} ,  Error message : ", endpoint, ex);

        }
    }
    public void syncDeleteFloorToADC(String floorId, String buildingId) {

        try {
            VdmsDTO vdmsDetails = vdmsRepository.getSyncDetailsForADC();
            List<String> floorIds = Collections.singletonList(floorId);
            Boolean status = apicallService.deleteFloorFromADC(
                    vdmsDetails.getCustomer_org_id(),
                    vdmsDetails.getAdc_configuration_id(),
                    buildingId,
                    floorIds
            );

            log.info("Floor DELETE synced to ADC successfully, Floor ID: {}, Status: {}",
                    floorId, status);
        } catch (Exception e) {
            log.error("Exception while syncing deleted floor to ADC, Floor ID: {}", floorId, e);
        }
    }

    public void deleteFloorsByIds(String username, String vdms_id, Set<String> floor_ids, HttpServletRequest httpServletRequest) {
        try {
            String data = this.deleteFloorMapsForFloors(vdms_id, floor_ids);
            if (data != null) {
                for (String floor_id : floor_ids) {
                    FloorDTO floorDTO = this.getFloorById(floor_id);
                    this.deleteFloorByFloorId(floor_id, floorDTO.getLocal_image_url(), username, httpServletRequest);
                }
            }
        } catch (Exception e) {
            log.error("Exception in delete Floors By Ids, Error message : ",e);
        }

    }

    public String deleteFloorMapsForFloors(String vdms_id, Set<String> floor_ids) {
        List<FloorDTO> floors = new ArrayList<>();
        for (String floor_id : floor_ids) {
            FloorDTO floorDTO = this.getFloorById(floor_id);
            floors.add(floorDTO);
        }
        log.info("Floors given to cloud" + floors.get(0));
        return webClientService.deleteFloorMapsByImageUrl(vdms_id, floors);
    }

    private String getFloorImageUrlById(String floor_id) {
        return floorRepository.getImageUrlById(floor_id);
    }


    public void removeFileFromServer(String absolute_path, String file_name) {
        File file = new File(absolute_path + file_name);
        if (file.exists()) {
            if (file.delete()) {
                log.info("File deleted successfully");
            } else {
                log.info("Failed to delete the file");
            }
        }
    }


    //  upsertFloorImageByBuildingFromBackend to be deleted after sync
    public String upsertFloorImageByBuildingFromBackend(FloorDTO floor) {

        String updated_image_url = null;
        String image_url = floor.getImage_url();

        FloorDTO existing_floor = getFloorById(floor.getFloor_id());

        if (existing_floor != null) {
            if (existing_floor.getImage_url() != null) {
                this.removeFloorImageFromOldServer(existing_floor.getImage_url());
            }
        }
        // if the floor has image url
        if (image_url != null) {
            try {
                InputStream in = new URL(image_url).openStream();
                updated_image_url = this.addFloorImagesToServer(in.readAllBytes(), server_floor_images_absolute_path, floor.getFloor_id(), getImageExtensionByImageUrl(image_url));


            } catch (IOException e) {
                log.error("Exception in upsert Floor Image By Building From Backend, Error message : ", e);
            }
        }
        return updated_image_url;
    }

    //  removeFloorImageFromOldServer to be deleted after sync
    private void removeFloorImageFromOldServer(String image_url) {
        String file_name = this.getFileNameByImageUrl(image_url);
        removeFileFromServer(absolutePathFloorImages, file_name); // check here if image needs to be deleted from old path
    }


    public void addFloorMapZoomLevels(String floor_id, String min_zoom, String max_zoom) {
        floorRepository.updateFloorMapZoomLevels(min_zoom, max_zoom, floor_id);
    }

    public void updateZoomLevels(List<FloorDTO> floors) {
        for (FloorDTO floor : floors) {
            this.addFloorMapZoomLevels(floor.getFloor_id(), floor.getMin_zoom(), floor.getMax_zoom());
        }
    }


    public String removeTimestampFromFileName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("_"));
    }

    public FloorDTO mapInitialPositionToCoordinates(String initial_position, String floor_id) {

        FloorDTO floor = new FloorDTO();
        if (initial_position != null) {

            try {
                JSONArray jsonArray = JSONArray.parseArray(initial_position);
                JSONArray upper = jsonArray.getJSONArray(0);
                JSONArray lower = jsonArray.getJSONArray(1);
                floor.setTopleft_latitude(upper.getString(0));
                floor.setTopleft_longitude(upper.getString(1));
                floor.setBottomright_latitude(lower.getString(0));
                floor.setBottomright_longitude(lower.getString(1));
                floor.setFloor_id(floor_id);
            } catch (JSONException e) {
                log.error("Exception in mapInitial Position To Coordinates, Error message : ", e);
            }
        }
        return floor;
    }

    // method to add image to vdms box
    public String addLocalFloorImagesToServer(byte[] image, String directory, String file_name)
            throws IOException {
        if (image != null) {
            Path path = Paths.get(directory + file_name);
            Files.write(path, image);
            return server_floor_images_url + file_name;
        } else {
            return null;
        }
    }

    //  syncLocationsFromBackend to be deleted after sync
    public List<FloorDTO> updateFloorImages(String vdms_id, List<FloorDTO> floorImages) {


        List<FloorDTO> floors = webClientService.syncFloorMapImageByFloorId(vdms_id, floorImages);

        List<FloorDTO> floors_response = new ArrayList<>();

        if (floors != null && floors.size() > 0) {
            for (FloorDTO floorImage : floors) {
                if (floorImage.getImage_url() != null && floorImage.getLocal_image_url() != null) {
                    floorRepository.updateImageUrls(floorImage.getLocal_image_url(), floorImage.getImage_url(), floorImage.getFloor_id());
                } else {
                    floors_response.add(floorImage);
                }

            }
        }
        return floors_response;

    }

    public List<FloorDTO> updateAllFloors(String vdms_id, List<FloorDTO> floorsList) {
        List<FloorDTO> floors_response = new ArrayList<>();
        for (FloorDTO floorDTO : floorsList) {
            List<FloorDTO> floor_dtos = new ArrayList<>();
            floor_dtos.add(floorDTO);
            floors_response.addAll(webClientService.uploadFloorImages(vdms_id, floor_dtos));
        }
        return floors_response;
    }


    //  syncFloorMapsTiles to be deleted after sync
    public List<FloorDTO> syncFloorMapsTiles(String vdms_id, List<FloorDTO> floors_response) {
        List<FloorDTO> floorMaps = new ArrayList<>();
        List<FloorDTO> floors = webClientService.syncFloorMapTilesFolder(vdms_id, floors_response);
        try {
            for (FloorDTO floor : floors) {
                if (floor.getImage_url() == null) {
                    floorMaps.add(this.getFloorById(floor.getFloor_id()));
                } else {
                    floorRepository.updateFloorMapZoomLevels(floor.getMin_zoom(), floor.getMax_zoom(), floor.getFloor_id());
                }
            }
        } catch (Exception e) {
            log.error("Exception in sync Floor Maps Tiles, Error message : ", e);
        }
        return floorMaps;
    }


    private FloorDTO getFloorNameByFloorId(String floor_id) {
        return floorRepository.getFloorById(floor_id);
    }


    public List<FloorDTO> getBatchFloorsByPagination(Set<String> floorIds) {

        List<FloorDTO> floors = new ArrayList<>();

        int pageNo = 1;
        int pageSize = 500;

        while (true) {
            int offset = pageSize * (pageNo - 1);
            List<FloorDTO> batchResults = floorRepository.getBatchFloorsByPagination(floorIds, pageSize, offset);

            if (batchResults == null || batchResults.isEmpty()) {
                break;
            }

            floors.addAll(batchResults);

            if (batchResults.size() < pageSize) {
                break;
            }

            pageNo++;
        }
        return floors;
    }

    public void deleteFloorsByIdsOnSync(String username, String vdmsId, Set<String> floorIds) {
        log.info("Entered deleteFloorsByIdsOnSync with floorIds: {}", floorIds);
        // Fetch floors only once
        List<FloorDTO> floors = getBatchFloorsByPagination(floorIds);

        if (floors == null || floors.isEmpty()) {
            return;
        }

        processFloorDeletions(username, vdmsId, floors);

    }

    public void processFloorDeletions(String username, String vdmsId, List<FloorDTO> floors) {
        log.info("Processing deletion for floors");
        List<FloorDTO> cloudFloors = floors.stream()
                .filter(floor -> floor.getImage_url() != null && !floor.getImage_url().isEmpty())
                .collect(Collectors.toList());

        List<String> localImageUrls = floors.stream()
                .filter(floor -> floor.getLocal_image_url() != null && !floor.getLocal_image_url().isEmpty())
                .map(FloorDTO::getLocal_image_url)
                .collect(Collectors.toList());


        // Delete cloud images (bulk call)
        if (!cloudFloors.isEmpty()) {
            webClientService.deleteFloorMapsByImageUrl(vdmsId, cloudFloors);
            log.info("Deleted cloud images : " + cloudFloors.size());
        }

        // Delete local images
        for (String localImageUrl : localImageUrls) {
            this.removeFloorImageFromServer(localImageUrl);
            log.info("Deleted local images : " + localImageUrl.length());
        }

        // Delete floors
        for (FloorDTO floor : floors) {
            String floorId = floor.getFloor_id();
            try {
                locationservice.deleteLocationsByFloorId(floorId, username, true);
                floorRepository.deleteById(floorId);

                userActionLogService.addUserAction(
                        username,
                        "maps",
                        "DELETE",
                        "Floor deleted successfully. Name: " + floor.getName() + ", Id: " + floorId,
                        "success",
                        "floor",
                        floorId
                );

                log.info("deleteFloorByFloorId, floorDTO: {}", floor);

            } catch (Exception ex) {

                userActionLogService.addUserAction(
                        username,
                        "maps",
                        "DELETE",
                        "Unable to delete floor. Name: " + floor.getName() + ", Id: " + floorId,
                        "failed",
                        "floor",
                        floorId
                );

                log.error("Exception in deleteFloorByFloorId, floorId: {}", floorId, ex);
            }
        }
    }

    public List<FloorDTO> getFloorsByBuildingIds(Set<String> buildingIds) {
        List<FloorDTO> floors = new ArrayList<>();

        int pageNo = 1;
        int pageSize = 500;

        while (true) {
            int offset = pageSize * (pageNo - 1);
            List<FloorDTO> batchResults = floorRepository.getFloorIdsByBuildingIds(buildingIds, pageSize, offset);

            if (batchResults == null || batchResults.isEmpty()) {
                break;
            }

            floors.addAll(batchResults);

            if (batchResults.size() < pageSize) {
                break;
            }

            pageNo++;
        }
        return floors;
    }


    /******************************************************************* new location changes *************************************************************************/


}















