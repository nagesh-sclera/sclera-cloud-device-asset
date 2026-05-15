package io.sclera.service;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.MediaRepository;
import io.sclera.dto.DocumentMediaDTO;
import io.sclera.utils.FileUtils;

@Service
public class MediaService {

	@Autowired
	FileUtils fileUtils;

	@Autowired
	MediaRepository mediaRepository;

	@Autowired
	DeviceService deviceService;

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


	public void deleteMedia(String username, String vdmsid, String mediaid) {

		deleteTagRecordByMediaId(mediaid);
		mediaRepository.deleteMediaById(mediaid);

	}

	public void deleteTagRecordByMediaId(String mediaid) {
		List<String> device_ids = mediaRepository.getMediaByDeviceId(mediaid);
		mediaRepository.deleteTagRecordByMediaId(mediaid);

		for (String device_id : device_ids) {
			deviceService.updateDeviceMediaCountByDeviceId(device_id);
		}
	}


	public Set<DocumentMediaDTO> getMedias(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
		// TODO Auto-generated method stub
		Integer offset = pagesize * (pageno - 1);
		return mediaRepository.getMedias(pagesize, offset, searchkey);
	}

	public Set<DocumentMediaDTO> getMediasByDeviceId(String username, String vdmsid, String deviceid, Integer pageno, Integer pagesize) {
		// TODO Auto-generated method stub
		Integer offset = pagesize * (pageno - 1);
		return mediaRepository.getMediasByDeviceIdByPagination(deviceid, pagesize, offset);
	}

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

	public Integer getMediasCountByDeviceId(String device_id) {
		return mediaRepository.getMediasCountByDeviceId(device_id);
	}

	public void updateMediaDeviceId(String device_id, String existing_device_id, Set<String> retainDevices) {
		mediaRepository.updateMediaDeviceId(device_id, existing_device_id);
		deviceService.updateDeviceMediaCountByDeviceId(device_id);
		if (!retainDevices.isEmpty() && retainDevices.contains(existing_device_id)) {
			deviceService.updateDeviceMediaCountByDeviceId(existing_device_id);
		}
	}
}
