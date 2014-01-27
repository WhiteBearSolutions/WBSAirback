package com.whitebearsolutions.imagine.wbsairback.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.util.Command;

public class CronTasks {
	private GeneralSystemConfiguration _sc;
	private Calendar _end;
	
	private final static Logger logger = LoggerFactory.getLogger(CronTasks.class);
	
	public CronTasks() throws Exception {
		this._end = Calendar.getInstance();
		logger.info("CronTask [{}]: init ...",this._end.getTime().toString()); 
		
		this._sc = new GeneralSystemConfiguration();
		try {
			for(Map<String, String> _volume : VolumeManager.getMountableLogicalVolumes()) {
				if(ShareManager.isShare(ShareManager.CIFS, _volume.get("vg"), _volume.get("name"))) {
					StringBuilder _sb = new StringBuilder();
					logger.info("Cleanning recycle for [{}/{}] ...", _volume.get("vg"), _volume.get("name"));
					_sb.append("/usr/bin/find ");
					_sb.append(VolumeManager.getLogicalVolumeDevicePath(_volume.get("vg"), _volume.get("name")));
					_sb.append("/.recycle/* -atime 1 | xargs rm -rf");
					Command.systemCommand(_sb.toString());
					logger.info("Cleanning recycle for [{}/{}] done", _volume.get("vg"), _volume.get("name"));
				}
			}
		} catch(Exception _ex) {
			logger.error("Error on cleaning recycle process. Ex: {}", _ex.getMessage());
		}
		
		try {
			if(this._sc.getReportHour() == this._end.get(Calendar.HOUR_OF_DAY)) {
				logger.info("Sending HTML mail report ... ");
				MailReport _mr = new MailReport();
				_mr.sendMail();
				logger.info("HTML Report sended successfuly");
			}
		} catch (Exception _ex) {
			logger.error("Error on report sending process. Ex: {}", _ex.getMessage());
		}
		try {
			if(this._sc.getExportHour() == this._end.get(Calendar.HOUR_OF_DAY)) {
				logger.info("Exporting catalog and configuration ... ");
				export();
				logger.info("Catalog exported successfully");
			}
		} catch (Exception _ex) {
			logger.error("Error exporting catalog. Ex: {}", _ex.getMessage());
		}

		try {
			processSnapshots();
		} catch (Exception _ex) {
			logger.error("Error processing snapshots. Ex: {}", _ex.getMessage());
		}
		
		logger.info("CronTask [{}]: finished", Calendar.getInstance().getTime().toString());
	}
	
	private void processSnapshots() throws Exception {
		for(Map<String, String> _volume : VolumeManager.getLogicalVolumes()) {
			try {
				logger.info("Cleanning manual snapshots [{}/{}] ...",_volume.get("vg"),_volume.get("name"));
				if (VolumeManager.isSnapshotManualRemoveOn(_volume.get("vg"), _volume.get("name")))
					VolumeManager.removeAllLogicalVolumeSnapshot(_volume.get("vg"), _volume.get("name"), VolumeManager.LV_SNAPSHOT_MANUAL);
				logger.info("Manual snapshots [{}/{}] cleaned successfully",_volume.get("vg"),_volume.get("name"));
			} catch(Exception _ex) {
				logger.error("Error cleaning manual snapshots fo [{}/{}]. Ex: {}",new Object[]{_volume.get("vg"),_volume.get("name"), _ex.getMessage()});
			}
		}
		for(Map<String, String> _snapshot : VolumeManager.getPlannedSnapshots(VolumeManager.LV_SNAPSHOT_DAILY)) {
			int _hour = -1, _retention = 1;
			try {
				_hour = Integer.parseInt(_snapshot.get("hour"));
			} catch(NumberFormatException _ex) {}
			try {
				_retention = Integer.parseInt(_snapshot.get("retention"));
			} catch(NumberFormatException _ex) {}
			if(_hour == this._end.get(Calendar.HOUR_OF_DAY)) {
				logger.info("Renewing daily snapshots [{}/{}] ...", _snapshot.get("group"), _snapshot.get("volume"));
				try {
					Map<Date, Map<String, String>> _snapshots = VolumeManager.getLogicalVolumeSnapshotsOrderedByDate(_snapshot.get("group"), _snapshot.get("volume"), VolumeManager.LV_SNAPSHOT_DAILY);
					List<Date> _keys = new ArrayList<Date>(_snapshots.keySet());
					for(int i = (_snapshots.size() - _retention); --i >= 0; ) {
						Map<String, String> _old_snapshot = _snapshots.get(_keys.get(i));
						VolumeManager.removeLogicalVolumeSnapshot(_snapshot.get("group"), _snapshot.get("volume"), _old_snapshot.get("name"));
					}
					VolumeManager.createLogicalVolumeSnapshot(_snapshot.get("group"), _snapshot.get("volume"), VolumeManager.LV_SNAPSHOT_DAILY);
					logger.info("Daily snapshots [{}/{}] renewed successfully",_snapshot.get("group"), _snapshot.get("volume"));
				} catch(Exception _ex) {
					logger.error("Error renewing daily snaphosts for [{}/{}]. Ex: {} ", new Object[]{_snapshot.get("group"), _snapshot.get("volume"), _ex.getMessage()});
				}
			}
		}
		for(Map<String, String> _snapshot : VolumeManager.getPlannedSnapshots(VolumeManager.LV_SNAPSHOT_HOURLY)) {
			int _retention = 1;
			logger.info("Renewing hourly snapshots [{}/{}] ...", _snapshot.get("group"), _snapshot.get("volume"));
			try {
				try {
					_retention = Integer.parseInt(_snapshot.get("retention"));
				} catch(NumberFormatException _ex) {}
				
				Map<Date, Map<String, String>> _snapshots = VolumeManager.getLogicalVolumeSnapshotsOrderedByDate(_snapshot.get("group"), _snapshot.get("volume"), VolumeManager.LV_SNAPSHOT_HOURLY);
				List<Date> _keys = new ArrayList<Date>(_snapshots.keySet());
				for(int i = (_snapshots.size() - _retention); --i >= 0; ) {
					Map<String, String> _old_snapshot = _snapshots.get(_keys.get(i));
					VolumeManager.removeLogicalVolumeSnapshot(_snapshot.get("group"), _snapshot.get("volume"), _old_snapshot.get("name"));
				}
				VolumeManager.createLogicalVolumeSnapshot(_snapshot.get("group"), _snapshot.get("volume"), VolumeManager.LV_SNAPSHOT_HOURLY);
				logger.info("Hourly snapshots renewed successfully for [{}/{}]",_snapshot.get("group"), _snapshot.get("volume"));
			} catch(Exception _ex) {
				logger.error("Error renewing hourly snapshots for [{}/{}]. Ex: {}", new Object[]{_snapshot.get("group"), _snapshot.get("volume"), _ex.getMessage()});
			}
		}
	}
	
	private void export() throws Exception {
		if(this._sc.getExportShare() == null) {
			throw new Exception("export volume not defined");
		}
		this._sc.exportConfigutation(this._sc.getExportShare());
	}
	
	public static void main(String[] args) {
		try {
			new CronTasks();
		} catch(Exception _ex) {
			logger.error("Error on main CronTasks. Ex: {}" + _ex.getMessage());
		}
	}
}
