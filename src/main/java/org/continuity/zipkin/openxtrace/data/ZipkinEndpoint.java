package org.continuity.zipkin.openxtrace.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZipkinEndpoint {

	private String serviceName;
	private String ipv4;
	private String ipv6;
	private byte[] ipv4Bytes;
	private byte[] ipv6Bytes;
	private int port;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getIpv4() {
		return ipv4;
	}

	public void setIpv4(String ipv4) {
		this.ipv4 = ipv4;
	}

	public String getIpv6() {
		return ipv6;
	}

	public void setIpv6(String ipv6) {
		this.ipv6 = ipv6;
	}

	public byte[] getIpv4Bytes() {
		return ipv4Bytes;
	}

	public void setIpv4Bytes(byte[] ipv4Bytes) {
		this.ipv4Bytes = ipv4Bytes;
	}

	public byte[] getIpv6Bytes() {
		return ipv6Bytes;
	}

	public void setIpv6Bytes(byte[] ipv6Bytes) {
		this.ipv6Bytes = ipv6Bytes;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
