package io.sclera.interfaces;

import java.math.BigInteger;

import io.sclera.dto.VlanDTO;

/** Service contract for the matching service class. */
public interface SystemInterfaceServiceInterface {

	void deleteAllInterface();

	VlanDTO getVlanDiscoverPidByInterfaceName(String interface_name);

	void updateVlanDiscoverPidByInterfaceName(String pid, BigInteger timestamp, String interface_name);
}
