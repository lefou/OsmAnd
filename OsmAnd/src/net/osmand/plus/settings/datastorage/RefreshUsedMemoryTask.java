package net.osmand.plus.settings.datastorage;

import android.os.AsyncTask;

import net.osmand.plus.settings.datastorage.DataStorageHelper.UpdateMemoryInfoUIAdapter;
import net.osmand.plus.settings.datastorage.DataStorageMemoryItem.Directory;

import java.io.File;

import static net.osmand.plus.settings.datastorage.DataStorageFragment.UI_REFRESH_TIME_MS;
import static net.osmand.plus.settings.datastorage.DataStorageMemoryItem.EXTENSIONS;
import static net.osmand.plus.settings.datastorage.DataStorageMemoryItem.PREFIX;
import static net.osmand.util.Algorithms.objectEquals;

public class RefreshUsedMemoryTask extends AsyncTask<DataStorageMemoryItem, Void, Void> {
	private UpdateMemoryInfoUIAdapter listener;
	private File rootDir;
	private DataStorageMemoryItem otherMemory;
	private String[] directoriesToAvoid;
	private String[] prefixesToAvoid;
	private String taskKey;
	private long lastRefreshTime;

	public RefreshUsedMemoryTask(UpdateMemoryInfoUIAdapter listener,
	                             DataStorageMemoryItem otherMemory,
	                             File rootDir,
	                             String[] directoriesToAvoid,
	                             String[] prefixesToAvoid,
	                             String taskKey) {
		this.listener = listener;
		this.otherMemory = otherMemory;
		this.rootDir = rootDir;
		this.directoriesToAvoid = directoriesToAvoid;
		this.prefixesToAvoid = prefixesToAvoid;
		this.taskKey = taskKey;
	}

	@Override
	protected Void doInBackground(DataStorageMemoryItem... items) {
		lastRefreshTime = System.currentTimeMillis();
		if (rootDir.canRead()) {
			calculateMultiTypes(rootDir, items);
		}
		return null;
	}

	private void calculateMultiTypes(File rootDir, DataStorageMemoryItem... items) {
		File[] subFiles = rootDir.listFiles();
		if (subFiles == null) return;

		for (File file : subFiles) {
			if (isCancelled()) {
				break;
			}
			nextFile : {
				if (file.isDirectory()) {
					//check current directory should be avoid
					if (directoriesToAvoid != null) {
						for (String directoryToAvoid : directoriesToAvoid) {
							if (objectEquals(file.getAbsolutePath(), directoryToAvoid)) {
								break nextFile;
							}
						}
					}
					//check current directory matched items type
					for (DataStorageMemoryItem item : items) {
						Directory[] directories = item.getDirectories();
						if (directories == null) {
							continue;
						}
						for (Directory dir : directories) {
							if (file.getAbsolutePath().equals(dir.getAbsolutePath())
									|| (file.getAbsolutePath().startsWith(dir.getAbsolutePath()))) {
								if (dir.isGoDeeper()) {
									calculateMultiTypes(file, items);
									break nextFile;
								} else if (dir.isSkipOther()) {
									break nextFile;
								}
							}
						}
					}
					//current directory did not match to any type
					otherMemory.addBytes(getDirectorySize(file));
				} else if (file.isFile()) {
					//check current file should be avoid
					if (prefixesToAvoid != null) {
						for (String prefixToAvoid : prefixesToAvoid) {
							if (file.getName().toLowerCase().startsWith(prefixToAvoid.toLowerCase())) {
								break nextFile;
							}
						}
					}
					//check current file matched items type
					for (DataStorageMemoryItem item : items) {
						Directory[] directories = item.getDirectories();
						if (directories == null) {
							continue;
						}
						for (Directory dir : directories) {
							if (rootDir.getAbsolutePath().equals(dir.getAbsolutePath())
									|| (rootDir.getAbsolutePath().startsWith(dir.getAbsolutePath()) && dir.isGoDeeper())) {
								int checkingType = dir.getCheckingType();
								switch (checkingType) {
									case EXTENSIONS : {
										String[] extensions = item.getExtensions();
										if (extensions != null) {
											for (String extension : extensions) {
												if (file.getAbsolutePath().endsWith(extension)) {
													item.addBytes(file.length());
													break nextFile;
												}
											}
										} else {
											item.addBytes(file.length());
											break nextFile;
										}
										break ;
									}
									case PREFIX : {
										String[] prefixes = item.getPrefixes();
										if (prefixes != null) {
											for (String prefix : prefixes) {
												if (file.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
													item.addBytes(file.length());
													break nextFile;
												}
											}
										} else {
											item.addBytes(file.length());
											break nextFile;
										}
										break ;
									}
								}
								if (dir.isSkipOther()) {
									break nextFile;
								}
							}
						}
					}
					//current file did not match any type
					otherMemory.addBytes(file.length());
				}
			}
			refreshUI();
		}
	}

	private long getDirectorySize(File dir) {
		long bytes = 0;
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (isCancelled()) {
					break;
				}
				if (file.isDirectory()) {
					bytes += getDirectorySize(file);
				} else if (file.isFile()) {
					bytes += file.length();
				}
			}
		}
		return bytes;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		if (listener != null) {
			listener.onMemoryInfoUpdate();
		}
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);
		if (listener != null) {
			listener.onFinishUpdating(taskKey);
		}
	}

	private void refreshUI() {
		long currentTime = System.currentTimeMillis();
		if ((currentTime - lastRefreshTime) > UI_REFRESH_TIME_MS) {
			lastRefreshTime = currentTime;
			publishProgress();
		}
	}
}
