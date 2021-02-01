package net.osmand.plus.settings.datastorage;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osmand.IndexConstants;
import net.osmand.ValueHolder;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.settings.backend.OsmandSettings;
import net.osmand.plus.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static net.osmand.plus.settings.datastorage.DataStorageMemoryItem.Directory;
import static net.osmand.plus.settings.datastorage.DataStorageMemoryItem.EXTENSIONS;
import static net.osmand.plus.settings.datastorage.DataStorageMemoryItem.PREFIX;

public class DataStorageHelper {
	public final static String INTERNAL_STORAGE = "internal_storage";
	public final static String EXTERNAL_STORAGE = "external_storage";
	public final static String SHARED_STORAGE = "shared_storage";
	public final static String MULTIUSER_STORAGE = "multiuser_storage";
	public final static String MANUALLY_SPECIFIED = "manually_specified";

	public final static String MAPS_MEMORY = "maps_memory_used";
	public final static String SRTM_SLOPE_HILLSHADE_MEMORY = "srtm_slope_hillshade_memory_used";
	public final static String TRACKS_MEMORY = "tracks_memory_used";
	public final static String NOTES_MEMORY = "notes_memory_used";
	public final static String TILES_MEMORY = "tiles_memory_used";
	public final static String OTHER_MEMORY = "other_memory_used";

	private ArrayList<DataStorageMenuItem> menuItems = new ArrayList<>();
	private DataStorageMenuItem currentDataStorage;
	private DataStorageMenuItem manuallySpecified;

	private ArrayList<DataStorageMemoryItem> memoryItems = new ArrayList<>();
	private DataStorageMemoryItem mapsMemory;
	private DataStorageMemoryItem srtmSlopeHillshadeMemory;
	private DataStorageMemoryItem tracksMemory;
	private DataStorageMemoryItem notesMemory;
	private DataStorageMemoryItem tilesMemory;
	private DataStorageMemoryItem otherMemory;

	private int currentStorageType;
	private String currentStoragePath;

	public DataStorageHelper(@NonNull OsmandApplication app) {
		prepareData(app);
	}

	private void prepareData(@NonNull OsmandApplication app) {
		OsmandSettings settings = app.getSettings();

		if (settings.getExternalStorageDirectoryTypeV19() >= 0) {
			currentStorageType = settings.getExternalStorageDirectoryTypeV19();
		} else {
			ValueHolder<Integer> vh = new ValueHolder<Integer>();
			if (vh.value != null && vh.value >= 0) {
				currentStorageType = vh.value;
			} else {
				currentStorageType = 0;
			}
		}
		currentStoragePath = settings.getExternalStorageDirectory().getAbsolutePath();

		String path;
		File dir;
		int iconId;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

			//internal storage
			path = settings.getInternalAppPath().getAbsolutePath();
			dir = new File(path);
			iconId = R.drawable.ic_action_phone;

			DataStorageMenuItem internalStorageItem = DataStorageMenuItem.builder()
					.setKey(INTERNAL_STORAGE)
					.setTitle(app.getString(R.string.storage_directory_internal_app))
					.setDirectory(path)
					.setDescription(app.getString(R.string.internal_app_storage_description))
					.setType(OsmandSettings.EXTERNAL_STORAGE_TYPE_INTERNAL_FILE)
					.setIconResId(iconId)
					.createItem();
			addItem(internalStorageItem);

			//shared storage
			dir = settings.getDefaultInternalStorage();
			path = dir.getAbsolutePath();
			iconId = R.drawable.ic_action_phone;

			DataStorageMenuItem sharedStorageItem = DataStorageMenuItem.builder()
					.setKey(SHARED_STORAGE)
					.setTitle(app.getString(R.string.storage_directory_shared))
					.setDirectory(path)
					.setType(OsmandSettings.EXTERNAL_STORAGE_TYPE_DEFAULT)
					.setIconResId(iconId)
					.createItem();
			addItem(sharedStorageItem);

			//external storage
			File[] externals = app.getExternalFilesDirs(null);
			if (externals != null) {
				int i = 0;
				for (File external : externals) {
					if (external != null) {
						++i;
						dir = external;
						path = dir.getAbsolutePath();
						iconId = getIconForStorageType(dir);
						DataStorageMenuItem externalStorageItem = DataStorageMenuItem.builder()
								.setKey(EXTERNAL_STORAGE + i)
								.setTitle(app.getString(R.string.storage_directory_external) + " " + i)
								.setDirectory(path)
								.setType(OsmandSettings.EXTERNAL_STORAGE_TYPE_EXTERNAL_FILE)
								.setIconResId(iconId)
								.createItem();
						addItem(externalStorageItem);
					}
				}
			}

			//multi user storage
			File[] obbDirs = app.getObbDirs();
			if (obbDirs != null) {
				int i = 0;
				for (File obb : obbDirs) {
					if (obb != null) {
						++i;
						dir = obb;
						path = dir.getAbsolutePath();
						iconId = getIconForStorageType(dir);
						DataStorageMenuItem multiuserStorageItem = DataStorageMenuItem.builder()
								.setKey(MULTIUSER_STORAGE + i)
								.setTitle(app.getString(R.string.storage_directory_multiuser) + " " + i)
								.setDirectory(path)
								.setType(OsmandSettings.EXTERNAL_STORAGE_TYPE_OBB)
								.setIconResId(iconId)
								.createItem();
						addItem(multiuserStorageItem);
					}
				}
			}
		}

		//manually specified storage
		manuallySpecified = DataStorageMenuItem.builder()
				.setKey(MANUALLY_SPECIFIED)
				.setTitle(app.getString(R.string.storage_directory_manual))
				.setDirectory(currentStoragePath)
				.setType(OsmandSettings.EXTERNAL_STORAGE_TYPE_SPECIFIED)
				.setIconResId(R.drawable.ic_action_folder)
				.createItem();
		menuItems.add(manuallySpecified);

		if (currentDataStorage == null) {
			currentDataStorage = manuallySpecified;
		}

		initMemoryUsed(app);
	}

	private void initMemoryUsed(@NonNull OsmandApplication app) {
		mapsMemory = DataStorageMemoryItem.builder()
				.setKey(MAPS_MEMORY)
				.setExtensions(IndexConstants.BINARY_MAP_INDEX_EXT)
				.setDirectories(
						new Directory(app.getAppPath(IndexConstants.MAPS_PATH).getAbsolutePath(), false, EXTENSIONS, false),
						new Directory(app.getAppPath(IndexConstants.ROADS_INDEX_DIR).getAbsolutePath(), true, EXTENSIONS, false),
						new Directory(app.getAppPath(IndexConstants.WIKI_INDEX_DIR).getAbsolutePath(), true, EXTENSIONS, false),
						new Directory(app.getAppPath(IndexConstants.WIKIVOYAGE_INDEX_DIR).getAbsolutePath(), true, EXTENSIONS, false),
						new Directory(app.getAppPath(IndexConstants.BACKUP_INDEX_DIR).getAbsolutePath(), true, EXTENSIONS, false))
				.createItem();
		memoryItems.add(mapsMemory);

		srtmSlopeHillshadeMemory = DataStorageMemoryItem.builder()
				.setKey(SRTM_SLOPE_HILLSHADE_MEMORY)
				.setExtensions(IndexConstants.BINARY_SRTM_MAP_INDEX_EXT)
				.setDirectories(
						new Directory(app.getAppPath(IndexConstants.SRTM_INDEX_DIR).getAbsolutePath(), true, EXTENSIONS, false),
						new Directory(app.getAppPath(IndexConstants.TILES_INDEX_DIR).getAbsolutePath(), false, PREFIX, true))
				.setPrefixes("Hillshade")
				.createItem();
		memoryItems.add(srtmSlopeHillshadeMemory);

		tracksMemory = DataStorageMemoryItem.builder()
				.setKey(TRACKS_MEMORY)
//				.setExtensions(IndexConstants.GPX_FILE_EXT, ".gpx.bz2")
				.setDirectories(
						new Directory(app.getAppPath(IndexConstants.GPX_INDEX_DIR).getAbsolutePath(), true, EXTENSIONS, false))
				.createItem();
		memoryItems.add(tracksMemory);

		notesMemory = DataStorageMemoryItem.builder()
				.setKey(NOTES_MEMORY)
//				.setExtensions("")
				.setDirectories(
						new Directory(app.getAppPath(IndexConstants.AV_INDEX_DIR).getAbsolutePath(), true, EXTENSIONS, false))
				.createItem();
		memoryItems.add(notesMemory);

		tilesMemory = DataStorageMemoryItem.builder()
				.setKey(TILES_MEMORY)
//				.setExtensions("")
				.setDirectories(
						new Directory(app.getAppPath(IndexConstants.TILES_INDEX_DIR).getAbsolutePath(), true, EXTENSIONS, false))
				.createItem();
		memoryItems.add(tilesMemory);

		otherMemory = DataStorageMemoryItem.builder()
				.setKey(OTHER_MEMORY)
				.createItem();
		memoryItems.add(otherMemory);
	}

	public ArrayList<DataStorageMenuItem> getStorageItems() {
		return menuItems;
	}

	private int getIconForStorageType(File dir) {
		return R.drawable.ic_action_folder;
	}

	public DataStorageMenuItem getCurrentStorage() {
		return currentDataStorage;
	}

	private void addItem(@NonNull DataStorageMenuItem item) {
		if (currentStorageType == item.getType() && currentStoragePath.equals(item.getDirectory())) {
			currentDataStorage = item;
		}
		menuItems.add(item);
	}

	public DataStorageMenuItem getManuallySpecified() {
		return manuallySpecified;
	}

	@Nullable
	public DataStorageMenuItem getStorage(@Nullable String key) {
		if (menuItems != null && key != null) {
			for (DataStorageMenuItem menuItem : menuItems) {
				if (key.equals(menuItem.getKey())) {
					return menuItem;
				}
			}
		}
		return null;
	}

	public int getCurrentType() {
		return currentStorageType;
	}

	public String getCurrentPath() {
		return currentStoragePath;
	}

	public ArrayList<DataStorageMemoryItem> getMemoryInfoItems() {
		return memoryItems;
	}

	public RefreshUsedMemoryTask calculateMemoryUsedInfo(UpdateMemoryInfoUIAdapter listener) {
		File rootDir = new File(currentStoragePath);
		RefreshUsedMemoryTask task = new RefreshUsedMemoryTask(listener, otherMemory, rootDir, null, null, OTHER_MEMORY);
		task.execute(mapsMemory, srtmSlopeHillshadeMemory, tracksMemory, notesMemory);
		return task;
	}

	public RefreshUsedMemoryTask calculateTilesMemoryUsed(UpdateMemoryInfoUIAdapter listener) {
		File rootDir = new File(tilesMemory.getDirectories()[0].getAbsolutePath());
		RefreshUsedMemoryTask task = new RefreshUsedMemoryTask(listener, otherMemory, rootDir, null, srtmSlopeHillshadeMemory.getPrefixes(), TILES_MEMORY);
		task.execute(tilesMemory);
		return task;
	}

	public long getTotalUsedBytes() {
		long total = 0;
		if (memoryItems != null && memoryItems.size() > 0) {
			for (DataStorageMemoryItem mi : memoryItems) {
				total += mi.getUsedMemoryBytes();
			}
			return total;
		}
		return -1;
	}

	public static String getFormattedMemoryInfo(long bytes, String[] formatStrings) {
		int type = 0;
		double memory = (double) bytes / 1024;
		while (memory > 1024 && type < formatStrings.length) {
			++type;
			memory = memory / 1024;
		}
		String formattedUsed = new DecimalFormat("#.##").format(memory);
		return String.format(formatStrings[type], formattedUsed);
	}

	public interface UpdateMemoryInfoUIAdapter {

		void onMemoryInfoUpdate();
		
		void onFinishUpdating(String taskKey);

	}
}