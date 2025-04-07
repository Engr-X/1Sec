package com.wangdi.onesec.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.javatuples.Pair;
import org.json.JSONObject;

import androidx.annotation.NonNull;

import com.wangdi.onesec.interfaces.AsyncTask;
import com.wangdi.onesec.interfaces.SyncTask;
import com.wangdi.onesec.utils.BasicUtils;
import com.wangdi.onesec.utils.FileHelper;

public final class ConfigManager
{
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    public static final String USER_CONFIGS_NAME_KEY = "name";
    public static final String USER_CONFIGS_ID_KEY = "id";

    public static final String USER_AVATAR_ID = "avatar";
    public static final String APP_SETTINGS_LANGUAGE_KEY = "language";

    public static final String COINS_KEY = "coins";
    public static final String EXPERIENCE_KEY = "experience";

    public static final String USER_CONFIGS_FILE_NAME = "user_configs.json";
    public static final String APP_SETTINGS_FILE_NAME = "settings.json";
    public static final String GAME_DATA_FILE_NAME = "game_data.json";

    private final File file;
    private final Map<String, Object> preferences;
    private final JSONObject json;

    static
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Shutting down ConfigManager's executor...");
            shutdown();

            try
            {
                if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS))
                    EXECUTOR.shutdownNow();

            }
            catch (InterruptedException e)
            {
                EXECUTOR.shutdownNow();
            }
        }));
    }

    /**
     * Creates a new ConfigManager instance with the given directory and filename.
     * This method is synchronized, meaning that only one thread can call this method
     * at the same time.
     *
     * @param directory         the directory to store the JSON file
     * @param filename          the filename of the JSON file
     * 
     * @throws Exception        if there is an error while creating the JSON file
     * 
     * @return a new ConfigManager instance
     */
    @SyncTask
    private static ConfigManager createSync(final File directory, String filename) throws Exception
    {
        if (!directory.exists() && !FileHelper.createNewDirectories(directory))
            throw new IOException("Create directories false: " + directory.getAbsolutePath());

        return new ConfigManager(directory, filename, true);
    }

    /**
     * Creates a new ConfigManager instance with the given directory and filename in the current thread.
     * This method is asynchronous, meaning that it will not block the current thread.
     *
     * @param directory         the directory to store the JSON file
     * @param filename          the filename of the JSON file
     * 
     * @throws Exception        if there is an error while creating the JSON file
     * 
     * @return a new ConfigManager instance
     */
    @AsyncTask
    public static ConfigManager create(final File directory, String filename) throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(() -> createSync(directory, filename)));
    }

    /**
     * Loads a ConfigManager instance with the given directory and filename in the current thread.
     * This method is synchronized, meaning that only one thread can call this method
     * at the same time.
     *
     * @param directory         the directory to store the JSON file
     * @param filename          the filename of the JSON file
     * 
     * @throws Exception        if there is an error while loading the JSON file
     * 
     * @return a new ConfigManager instance
     */
    @SyncTask
    private static ConfigManager loadSync(final File directory, String filename) throws Exception
    {
        if (!directory.exists() && !FileHelper.createNewDirectories(directory))
            throw new IOException("Create directories false: " + directory.getAbsolutePath());

        return new ConfigManager(directory, filename, false);
    }

    /**
     * Loads a ConfigManager instance with the given directory and filename asynchronously.
     *
     * @param directory         the directory to store the JSON file
     * @param filename          the filename of the JSON file
     * 
     * @throws Exception        if there is an error while loading the JSON file
     * 
     * @return a new ConfigManager instance
     */
    @AsyncTask
    public static ConfigManager load(final File directory, String filename) throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(() -> loadSync(directory, filename)));
    }

    /**
     * Creates or loads a ConfigManager instance based on the existence of the specified file in UI thread.
     * If the file does not exist, create a new ConfigManager instance, otherwise load the existing one.
     *
     * @param directory         the directory to store the JSON file
     * @param filename          the filename of the JSON file
     * @return                  a Pair containing the ConfigManager instance and a boolean indicating whether the instance is created or loaded
     * 
     * @throws Exception        if there is an error while creating or loading the JSON file
     */
    @SyncTask
    public static Pair<ConfigManager, Boolean> autoSync(final File directory, String filename) throws Exception
    {
        if (!directory.exists() && !FileHelper.createNewDirectories(directory))
            throw new IOException("Create directories false: " + directory.getAbsolutePath());

        final File file = new File(directory, filename);
        final boolean create = !file.exists();
        return new Pair<>(new ConfigManager(directory, filename, create), create);
    }

    /**
     * Asynchronously creates or loads a ConfigManager instance based on the existence of the specified file.
     * 
     * @param directory         the directory to store the JSON file
     * @param filename          the filename of the JSON file
     * 
     * @return                  a Pair containing the ConfigManager instance and a boolean indicating 
     *                          whether a new instance was created (true) or loaded (false)
     * 
     * @throws Exception        if there is an error while creating or loading the JSON file
     */
    @AsyncTask
    public static Pair<ConfigManager, Boolean> auto(final File directory, String filename) throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(() -> autoSync(directory, filename)));
    }

    /**
     * Shuts down the executor service, and all the threads in it.
     * 
     * <p>
     * This method is intended to be called in the main thread, before the application is
     * exited.
     * </p>
     */
    @SyncTask
    public static void shutdown()
    {
        EXECUTOR.shutdown();
    }

    @SyncTask
    public ConfigManager(final File directory, String filename, boolean create) throws Exception
    {
        this.file = new File(directory, filename);
        this.preferences = new HashMap<>();

        if (create)
        {
            this.json = new JSONObject();

            if (this.file.exists())
                throw new IOException("The file is already exist");

            if (!this.file.exists() && !FileHelper.createNewFile(this.file))
                throw new IOException("Create file false: " + this.file.getAbsolutePath());
        }
        else
        {
            final String read = FileHelper.read(this.file);

            this.json = (read.isBlank() || read.isEmpty()) ? new JSONObject() : new JSONObject(read);
            final Iterator<String> it = this.json.keys();

            while (it.hasNext())
            {
                final String key = it.next();
                this.preferences.put(key, this.json.get(key));
            }
        }
    }

    /**
     * Sets the given key-value pair in the ConfigManager's preferences and JSON object.
     * This method is synchronized, meaning that only one thread can call this method
     * at the same time.
     *
     * @param key               the key to be set in the preferences
     * @param value             the value to be associated with the key
     * 
     * @return                  the current ConfigManager instance
     * 
     * @throws Exception        if there is an error while updating the JSON object
     */
    @SyncTask
    public ConfigManager put(String key, Object value) throws Exception
    {
        this.preferences.put(key, value);
        this.json.put(key, value);
        return this;
    }

    /**
     * Returns whether the given key is present in the ConfigManager's preferences.
     *
     * @param key               the key to be checked
     * @return                  whether the key is present in the preferences
     */
    public boolean containsKey(String key)
    {
        return this.preferences.containsKey(key);
    }

    /**
     * Retrieves the value associated with the given key in the ConfigManager's preferences.
     * This method is synchronized, meaning that only one thread can call this method
     * at the same time.
     *
     * @param key               the key to be retrieved
     * @return                  the value associated with the key
     */
    @SyncTask
    public Object get(String key)
    {
        return this.preferences.get(key);
    }

    /**
     * Saves the ConfigManager's preferences to the file it was created with.
     * This method is asynchronous, meaning that it will not block the main thread.
     * This method is idempotent, meaning that it can be called multiple times without
     * any negative effects.
     *
     * @throws Exception if an error occurs while writing the preferences to the file
     */
    @AsyncTask
    public void saveToFile() throws Exception
    {
        BasicUtils.handleFutureResult(EXECUTOR.submit(this::saveTofileSync));
    }

    @NonNull
    @Override
    public String toString()
    {
        return this.preferences.toString();
    }

    /**
     * Synchronously saves the ConfigManager's preferences to the file it was created with.
     * This method is idempotent, meaning that it can be called multiple times without
     * any negative effects.
     * 
     * @return                  0 if the method was successful, non-zero if an error occurred
     * 
     * @throws Exception        if an error occurs while writing the preferences to the file
     */
    @SyncTask
    private byte saveTofileSync() throws Exception
    {
        FileHelper.write(this.file, this.json.toString(), false);
        return 0;
    }
}