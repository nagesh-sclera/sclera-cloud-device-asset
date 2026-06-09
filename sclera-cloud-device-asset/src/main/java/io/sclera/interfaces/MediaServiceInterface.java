package io.sclera.interfaces;

import java.util.List;
import java.util.Set;

import io.sclera.dto.DocumentMediaDTO;

/** Service contract for the matching service class. */
public interface MediaServiceInterface {

	String upsertMedia(String username, String vdmsid, DocumentMediaDTO media);

	void deleteMedia(String username, String vdmsid, String mediaid);

	void deleteTagRecordByMediaId(String mediaid);

	Set<DocumentMediaDTO> getMedias(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey);

	Set<DocumentMediaDTO> getMediasByDeviceId(String username, String vdmsid, String deviceid, Integer pageno, Integer pagesize);

	void tagMediaToDevice(String username, String vdmsid, String share_method, Set<DocumentMediaDTO> medias);

	void untagMediaToDevice(String username, String vdmsid, Set<DocumentMediaDTO> medias);

	Integer getMediasCountByDeviceId(String device_id);

	void updateMediaDeviceId(String device_id, String existing_device_id, Set<String> retainDevices);
}
