package io.sclera.service;

import java.math.BigInteger;

import io.sclera.dto.VlanDTO;

/** Service contract for the matching service class. */
public interface SystemInterfaceService {

	void deleteAllInterface();

	VlanDTO getVlanDiscoverPidByInterfaceName(String interface_name);

	void updateVlanDiscoverPidByInterfaceName(String pid, BigInteger timestamp, String interface_name);
}
