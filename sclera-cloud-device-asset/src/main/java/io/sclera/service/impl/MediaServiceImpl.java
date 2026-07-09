package io.sclera.service.impl;
import io.sclera.service.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.MediaRepository;
import io.sclera.dto.DocumentMediaDTO;
import io.sclera.service.MediaService;
import io.sclera.utils.FileUtils;

/**
 * Manages document/media assets and their associations with devices.
 *
 * <p>Supports creating and updating media records, deleting media, paginated
 * retrieval (globally or scoped to a device), and tagging/untagging media to
 * devices while keeping per-device media counts in sync.
 *
 * <p>Collaborators: {@link MediaRepository} for persistence,
 * {@link DeviceService} for updating device media counts, and {@link FileUtils}
 * for file-related utilities.
 */
@Service
public class MediaServiceImpl implements MediaService {

	@Autowired
	FileUtils fileUtils;

	@Autowired
	MediaRepository mediaRepository;

	@Autowired
	DeviceService deviceService;

	/**
	 * Creates a new media record or updates an existing one.
	 *
	 * <p>When the supplied media has no id, a new time-based id and creation
	 * timestamp are generated; otherwise the existing record is updated.
	 *
	 * @param username the user performing the operation
	 * @param vdmsid the VDMS identifier scoping the operation
	 * @param media the media payload to persist
	 * @return the id of the created or updated media record
	 */
	public String upsertMedia(String username, String vdmsid, DocumentMediaDTO media) {

		if (media.getId() == null) {
			String id = Generators.timeBasedGenerator().generate().toString();
			media.setId(id);
			BigInteger createdTimestamp = BigInteger.valueOf(System.currentTimeMillis());


			mediaRepository.upsertMedia(media.getId(), media.getName(), media.getCategory(),

					media.getDescription(), media.getLink(), username, createdTimestamp, null);

			return id;

		} else {
			mediaRepository.upsertMedia(media.getId(), media.getName(), media.getCategory(),
					media.getDescription(), media.getLink(), username, null, null);

			return media.getId();
		}

	}


	/**
	 * Deletes a media record and its device tag associations.
	 *
	 * @param username the user performing the operation
	 * @param vdmsid the VDMS identifier scoping the operation
	 * @param mediaid the id of the media record to delete
	 */
	public void deleteMedia(String username, String vdmsid, String mediaid) {

		deleteTagRecordByMediaId(mediaid);
		mediaRepository.deleteMediaById(mediaid);

	}

	/**
	 * Removes all device tag records for a media item and refreshes the media
	 * count of each previously tagged device.
	 *
	 * @param mediaid the id of the media whose tag records are removed
	 */
	public void deleteTagRecordByMediaId(String mediaid) {
		List<String> device_ids = mediaRepository.getMediaByDeviceId(mediaid);
		mediaRepository.deleteTagRecordByMediaId(mediaid);

		for (String device_id : device_ids) {
			deviceService.updateDeviceMediaCountByDeviceId(device_id);
		}
	}


	/**
	 * Returns a paginated set of media records matching an optional search key.
	 *
	 * @param username the user performing the operation
	 * @param vdmsid the VDMS identifier scoping the operation
	 * @param pageno the one-based page number
	 * @param pagesize the number of records per page
	 * @param searchkey the search filter applied to media records
	 * @return the matching media records for the requested page
	 */
	public Set<DocumentMediaDTO> getMedias(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
		// TODO Auto-generated method stub
		return new java.util.HashSet<>(mediaRepository.getMedias(searchkey, PageRequest.of(pageno - 1, pagesize)));
	}

	/**
	 * Returns a paginated set of media records tagged to a specific device.
	 *
	 * @param username the user performing the operation
	 * @param vdmsid the VDMS identifier scoping the operation
	 * @param deviceid the id of the device whose media are retrieved
	 * @param pageno the one-based page number
	 * @param pagesize the number of records per page
	 * @return the media records tagged to the device for the requested page
	 */
	public Set<DocumentMediaDTO> getMediasByDeviceId(String username, String vdmsid, String deviceid, Integer pageno, Integer pagesize) {
		// TODO Auto-generated method stub
		return new java.util.HashSet<>(mediaRepository.getMediasByDeviceIdByPagination(deviceid, PageRequest.of(pageno - 1, pagesize)));
	}

	/**
	 * Tags media to devices according to the given share method.
	 *
	 * <p>When the share method is {@code replace}, existing tags on the affected
	 * devices are cleared first; for {@code add} or {@code replace} the supplied
	 * media are tagged and the device media counts are refreshed.
	 *
	 * @param username the user performing the operation
	 * @param vdmsid the VDMS identifier scoping the operation
	 * @param share_method the tagging mode, {@code add} or {@code replace}
	 * @param medias the media-to-device associations to apply
	 */
	public void tagMediaToDevice(String username, String vdmsid, String share_method, Set<DocumentMediaDTO> medias) {


		if (share_method != null && share_method.equals("replace")) {
			Set<String> device_ids = new HashSet<>();

			for (DocumentMediaDTO media : medias) {
				device_ids.add(media.getDevice_id());
			}
			for (String device_id : device_ids) {
				this.untagMediasByDeviceId(username, vdmsid, device_id);
			}
		}
		if (share_method != null && (share_method.equals("add") || share_method.equals("replace"))) {
			for (DocumentMediaDTO media : medias) {
				try {
					mediaRepository.tagMediaToDevice(media.getId(), media.getDevice_id());
					deviceService.updateDeviceMediaCountByDeviceId(media.getDevice_id());
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}

	private void untagMediasByDeviceId(String username, String vdmsid, String device_id) {

		Set<DocumentMediaDTO> delete_medias = mediaRepository.getMediasByDeviceId(device_id);
		if (delete_medias != null) {
			this.untagMediaToDevice(username, vdmsid, delete_medias);
		}
	}

	/**
	 * Removes the given media-to-device tags and refreshes the affected device
	 * media counts.
	 *
	 * @param username the user performing the operation
	 * @param vdmsid the VDMS identifier scoping the operation
	 * @param medias the media-to-device associations to untag
	 */
	public void untagMediaToDevice(String username, String vdmsid, Set<DocumentMediaDTO> medias) {

		for (DocumentMediaDTO media : medias) {
			try {
				mediaRepository.untagMediaToDevice(media.getId(), media.getDevice_id());
				deviceService.updateDeviceMediaCountByDeviceId(media.getDevice_id());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

	/**
	 * Returns the number of media records tagged to a device.
	 *
	 * @param device_id the id of the device
	 * @return the count of media tagged to the device
	 */
	public Integer getMediasCountByDeviceId(String device_id) {
		return mediaRepository.getMediasCountByDeviceId(device_id);
	}

	/**
	 * Reassigns media tagged to one device over to another device and refreshes
	 * the relevant device media counts.
	 *
	 * @param device_id the target device id receiving the media
	 * @param existing_device_id the source device id whose media are reassigned
	 * @param retainDevices device ids whose counts should also be refreshed when
	 *        the source device is among them
	 */
	public void updateMediaDeviceId(String device_id, String existing_device_id, Set<String> retainDevices) {
		mediaRepository.updateMediaDeviceId(device_id, existing_device_id);
		deviceService.updateDeviceMediaCountByDeviceId(device_id);
		if (!retainDevices.isEmpty() && retainDevices.contains(existing_device_id)) {
			deviceService.updateDeviceMediaCountByDeviceId(existing_device_id);
		}
	}
}
