	package com.rct.restclienttool.service;

	import com.rct.restclienttool.model.RequestHistoryEntry;

	import java.io.*;
	import java.nio.file.Files;
	import java.nio.file.Path;
	import java.nio.file.Paths;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.logging.Level;
	import java.util.logging.Logger;

	/**
	* Service class for managing request history persistence.
	* Maintains a history of executed requests with automatic size limiting.
	*
	 * @author REST Client Tool
	* @version 1.0
	* @since 1.0
	*/
	public class RequestHistoryService {
		private static final Logger LOGGER = Logger.getLogger(RequestHistoryService.class.getName());
		private static final String HISTORY_DIR = System.getProperty("user.home") + File.separator + ".restclienttool";
		private static final String HISTORY_FILE = HISTORY_DIR + File.separator + "request-history.dat";
		private static final int MAX_HISTORY_SIZE = 100;

		/**
		 * Constructor that ensures the history directory exists.
		 * Creates the directory if it doesn't exist.
		 */
		public RequestHistoryService() {
			// Create history directory if it doesn't exist
			try {
				Path historyDir = Paths.get(HISTORY_DIR);
				if (!Files.exists(historyDir)) {
					Files.createDirectories(historyDir);
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Failed to create history directory", e);
			}
		}

		/**
		 * Adds a new entry to the request history.
		 * Maintains a maximum history size by removing oldest entries.
		 *
		 * @param entry the request history entry to add
		 */
		public void addHistoryEntry(RequestHistoryEntry entry) {
			try {
				List<RequestHistoryEntry> history = loadHistory();
			   
				// Add new entry at the beginning
				history.add(0, entry);
			   
				// Trim history if it exceeds max size
				if (history.size() > MAX_HISTORY_SIZE) {
					history = history.subList(0, MAX_HISTORY_SIZE);
				}
			   
				saveHistory(history);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Failed to add history entry", e);
				// Create a new history file if there was an error
				saveHistory(new ArrayList<>());
			}
		}

		/**
		 * Loads all request history entries from persistent storage.
		 *
		 * @return list of request history entries, newest first
		 */
		public List<RequestHistoryEntry> loadHistory() {
			List<RequestHistoryEntry> history = new ArrayList<>();
		   
			File file = new File(HISTORY_FILE);
			if (!file.exists()) {
				return history;
			}
		   
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				Object obj = ois.readObject();
				if (obj instanceof List<?>) {
					for (Object item : (List<?>) obj) {
						if (item instanceof RequestHistoryEntry) {
							history.add((RequestHistoryEntry) item);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Failed to load history", e);
				// If there's an error loading, delete the corrupted file
				try {
					file.delete();
				} catch (Exception deleteEx) {
					LOGGER.log(Level.WARNING, "Failed to delete corrupted history file", deleteEx);
				}
			}
		   
			return history;
		}

		/**
		 * Clears all request history entries.
		 */
		public void clearHistory() {
			saveHistory(new ArrayList<>());
		}

		/**
		 * Saves the history list to persistent storage.
		 *
		 * @param history the list of history entries to save
		 */
		private void saveHistory(List<RequestHistoryEntry> history) {
			// Create parent directories if they don't exist
			try {
				File file = new File(HISTORY_FILE);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
			   
				try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
					oos.writeObject(history);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Failed to save history", e);
			}
		}
	}
