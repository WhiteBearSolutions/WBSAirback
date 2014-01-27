package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "volume")
public class VolumeRs {
	
	private String name;
	private String path;
	private String type;
	private String fstype;
	private boolean mount;
	private String size;
	private Double sizeRaw;
	private String used;
	private Double usedRaw;
	private String stripes;
	private String compressed;
	private Integer compressedRaw;
	private String deduplicated;
	private String reservation;
	private Double reservationRaw;
	private String refReservation;
	private Double refReservationRaw;
	private String refQuota;
	private Double refQuotaRaw;
	private String available;
	private Double availableRaw;
	private String snapshotUsed;
	private Double snapshotUsedRaw;
	
	private Boolean snapshotHourly;
	private Integer snapshotHourlyRetention;
	
	private Boolean snapshotDaily;
	private Integer snapshotDailyHour;
	private Integer snapshotDailyRetention;
	


	public VolumeRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( VolumeRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de volumenes a objetos volumen
	 * @param maps
	 * @return
	 */
	public static List<VolumeRs> listMapToObject(List<Map<String, String>> mapVolumes) throws Exception {
		List<VolumeRs> vols = new ArrayList<VolumeRs>();
		if (mapVolumes != null && mapVolumes.size()>0) {
			for (Map<String, String> mapVol : mapVolumes) {
				VolumeRs vol = mapToObject(mapVol);
				vols.add(vol);
			}
		}
		return vols;
	}
	
	public static VolumeRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "volume";
				JAXBContext jc = JAXBContext.newInstance( VolumeRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					VolumeRs o = (VolumeRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<VolumeRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "volume";
			List<VolumeRs> listObjects = new ArrayList<VolumeRs>();
			
			if (xml != null && xml.length()>0) {
				int iInitList = xml.indexOf("<"+idList+">");
				int iEndList = xml.indexOf("</"+idList+">");
				if ( iInitList > 0 && iEndList > -1) { 
					String list = xml.substring(iInitList+("<"+idList+">").length(), iEndList);
					while (list.indexOf("<"+nameEntity+">")>-1) {
						String deviceXml = list.substring(list.indexOf("<"+nameEntity+">"), list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						listObjects.add(fromXML(deviceXml));
						if (list.length() > list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()) {
							list = list.substring(list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						} else {
							break;
						}
					}
				}
			}
			return listObjects;
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Convierte un mapa de valores de un  a un objeto  
	 * @param map
	 * @return
	 */
	public static VolumeRs mapToObject(Map<String, String> mapVol) throws Exception {
		VolumeRs vol = new VolumeRs();
		
		if (mapVol.get("available") != null && !mapVol.get("available").isEmpty())
			vol.setAvailable(mapVol.get("available"));
		if (mapVol.get("available-raw") != null && !mapVol.get("available-raw").isEmpty())
			vol.setAvailableRaw(Double.parseDouble(mapVol.get("available-raw")));
		if (mapVol.get("compressed") != null && !mapVol.get("compressed").isEmpty())
			vol.setCompressed(mapVol.get("compressed"));
		if (mapVol.get("compressed-raw") != null && !mapVol.get("compressed-raw").isEmpty())
			vol.setCompressedRaw(Integer.parseInt(mapVol.get("compressed-raw")));
		if (mapVol.get("deduplicated") != null && !mapVol.get("deduplicated").isEmpty())
			vol.setDeduplicated(mapVol.get("deduplicated"));
		if (mapVol.get("fstype") != null && !mapVol.get("fstype").isEmpty())
			vol.setFstype(mapVol.get("fstype"));
		if (mapVol.get("mount") != null && !mapVol.get("mount").isEmpty())
			vol.setMount(Boolean.parseBoolean(mapVol.get("mount")));
		else
			vol.setMount(false);
		if (mapVol.get("name") != null && !mapVol.get("name").isEmpty())
			vol.setName(mapVol.get("name"));
		if (mapVol.get("path") != null && !mapVol.get("path").isEmpty())
			vol.setPath(mapVol.get("path"));
		if (mapVol.get("refquota") != null && !mapVol.get("refquota").isEmpty())
			vol.setRefQuota(mapVol.get("refquota"));
		if (mapVol.get("refquota-raw") != null && !mapVol.get("refquota-raw").isEmpty())
			vol.setRefQuotaRaw(Double.parseDouble(mapVol.get("refquota-raw")));
		if (mapVol.get("refreservation") != null && !mapVol.get("refreservation").isEmpty())
			vol.setRefReservation(mapVol.get("refreservation"));
		if (mapVol.get("refreservation-raw") != null && !mapVol.get("refreservation-raw").isEmpty())
			vol.setRefReservationRaw(Double.parseDouble(mapVol.get("refreservation-raw")));
		if (mapVol.get("reservation") != null && !mapVol.get("reservation").isEmpty())
			vol.setReservation(mapVol.get("reservation"));
		if (mapVol.get("reservation-raw") != null && !mapVol.get("reservation-raw").isEmpty())
			vol.setReservationRaw(Double.parseDouble(mapVol.get("reservation-raw")));
		if (mapVol.get("size") != null && !mapVol.get("size").isEmpty())
			vol.setSize(mapVol.get("size"));
		if (mapVol.get("size-raw") != null && !mapVol.get("size-raw").isEmpty())
			vol.setSizeRaw(Double.parseDouble(mapVol.get("size-raw")));
		if (mapVol.get("snapshot-used") != null && !mapVol.get("snapshot-used").isEmpty())
			vol.setSnapshotUsed(mapVol.get("snapshot-used"));
		if (mapVol.get("snapshot-used-raw") != null && !mapVol.get("snapshot-used-raw").isEmpty())
			vol.setSnapshotUsedRaw(Double.parseDouble(mapVol.get("snapshot-used-raw")));
		if (mapVol.get("stripes") != null && !mapVol.get("stripes").isEmpty())
			vol.setStripes(mapVol.get("stripes"));
		if (mapVol.get("type") != null && !mapVol.get("type").isEmpty())
			vol.setType(mapVol.get("type"));
		if (mapVol.get("used") != null && !mapVol.get("used").isEmpty())
			vol.setUsed(mapVol.get("used"));
		if (mapVol.get("used-raw") != null && !mapVol.get("used-raw").isEmpty())
			vol.setUsedRaw(Double.parseDouble(mapVol.get("used-raw")));
		
		return vol;
	}
	
	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFstype() {
		return fstype;
	}

	public void setFstype(String fstype) {
		this.fstype = fstype;
	}

	public boolean isMount() {
		return mount;
	}

	public void setMount(boolean mount) {
		this.mount = mount;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public Double getSizeRaw() {
		return sizeRaw;
	}

	public void setSizeRaw(Double sizeRaw) {
		this.sizeRaw = sizeRaw;
	}

	public String getUsed() {
		return used;
	}

	public void setUsed(String used) {
		this.used = used;
	}

	public Double getUsedRaw() {
		return usedRaw;
	}

	public void setUsedRaw(Double usedRaw) {
		this.usedRaw = usedRaw;
	}

	public String getStripes() {
		return stripes;
	}

	public void setStripes(String stripes) {
		this.stripes = stripes;
	}

	public String getCompressed() {
		return compressed;
	}

	public void setCompressed(String compressed) {
		this.compressed = compressed;
	}

	public Integer getCompressedRaw() {
		return compressedRaw;
	}

	public void setCompressedRaw(Integer compressedRaw) {
		this.compressedRaw = compressedRaw;
	}

	public String getDeduplicated() {
		return deduplicated;
	}

	public void setDeduplicated(String deduplicated) {
		this.deduplicated = deduplicated;
	}

	public String getReservation() {
		return reservation;
	}

	public void setReservation(String reservation) {
		this.reservation = reservation;
	}

	public Double getReservationRaw() {
		return reservationRaw;
	}

	public void setReservationRaw(Double reservationRaw) {
		this.reservationRaw = reservationRaw;
	}

	public String getRefReservation() {
		return refReservation;
	}

	public void setRefReservation(String refReservation) {
		this.refReservation = refReservation;
	}

	public Double getRefReservationRaw() {
		return refReservationRaw;
	}

	public void setRefReservationRaw(Double refReservationRaw) {
		this.refReservationRaw = refReservationRaw;
	}

	public String getRefQuota() {
		return refQuota;
	}

	public void setRefQuota(String refQuota) {
		this.refQuota = refQuota;
	}

	public Double getRefQuotaRaw() {
		return refQuotaRaw;
	}

	public void setRefQuotaRaw(Double refQuotaRaw) {
		this.refQuotaRaw = refQuotaRaw;
	}

	public String getAvailable() {
		return available;
	}

	public void setAvailable(String available) {
		this.available = available;
	}

	public Double getAvailableRaw() {
		return availableRaw;
	}

	public void setAvailableRaw(Double availableRaw) {
		this.availableRaw = availableRaw;
	}

	public String getSnapshotUsed() {
		return snapshotUsed;
	}

	public void setSnapshotUsed(String snapshotUsed) {
		this.snapshotUsed = snapshotUsed;
	}

	public Double getSnapshotUsedRaw() {
		return snapshotUsedRaw;
	}

	public void setSnapshotUsedRaw(Double snapshotUsedRaw) {
		this.snapshotUsedRaw = snapshotUsedRaw;
	}

	public Boolean getSnapshotHourly() {
		return snapshotHourly;
	}

	public void setSnapshotHourly(Boolean snapshotHourly) {
		this.snapshotHourly = snapshotHourly;
	}

	public Integer getSnapshotHourlyRetention() {
		return snapshotHourlyRetention;
	}

	public void setSnapshotHourlyRetention(Integer snapshotHourlyRetention) {
		this.snapshotHourlyRetention = snapshotHourlyRetention;
	}

	public Boolean getSnapshotDaily() {
		return snapshotDaily;
	}

	public void setSnapshotDaily(Boolean snapshotDaily) {
		this.snapshotDaily = snapshotDaily;
	}

	public Integer getSnapshotDailyHour() {
		return snapshotDailyHour;
	}

	public void setSnapshotDailyHour(Integer snapshotDailyHour) {
		this.snapshotDailyHour = snapshotDailyHour;
	}

	public Integer getSnapshotDailyRetention() {
		return snapshotDailyRetention;
	}

	public void setSnapshotDailyRetention(Integer snapshotDailyRetention) {
		this.snapshotDailyRetention = snapshotDailyRetention;
	}
}
