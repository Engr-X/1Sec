/*
 * Copyright (c) 2025, [1Sec team]. All rights reserved.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.wangdi.onesec.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.NonNull;

import com.wangdi.onesec.interfaces.AsyncTask;
import com.wangdi.onesec.interfaces.SyncTask;
import com.wangdi.onesec.utils.BasicUtils;
import com.wangdi.onesec.utils.FileHelper;

/**
 * <p>
 * The recorder class is aimed to not only record all the session data, but also provide some methods to
 * write these data to files (The record data also can be load from these files).
 * </p>
 *
 * <p>
 * Furthermore, the recorder have ability to analyse all the data superficially.
 * For example, find the best data it contains.
 * </p>
 * 
 * <p>
 * This class's method contain I / O operations, so it should not be called in Main thread.
 * </p>
 *
 * @see NumberResponse
 * @author Di Wang
 * @version beta
 */

public class Recorder
{
    public static final String DATA_TYPE_KEY = "data_type";
    public static final String VERSION_KEY = "version";
    public static final String GROUP_SIZE_KEY = "group_size";
    public static final String DATA_SIZE_KEY = "data_size";

    public static final String BEST_KEY = "best";
    public static final String AVERAGE_KEY = "average";
    public static final String WORST_KEY = "worst";
    public static final String CURRENT_KEY = "current";

    public static final String RECORDS_KEY = "records";

    public static final String ALL_KEY = "all";
    public static final String MO3_KEY = "mo3";
    public static final String AO5_KEY = "ao5";
    public static final String AO12_KEY = "ao12";
    public static final String AO100_KEY = "ao100";
    //only number of effective data, DNF is not included
    public static final String VALID_SIZE_KEY = "valid_size";

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private static final String VERSION = "1.0";
    private static final String DATA_FILE_PREFIX = "data";
    private static final String JSON_EXTENSION_NAME = ".json";
    private static final String MANIFEST_FILE_NAME = "manifest.json";
    private static final String DATA_FILE_REGEX = "^data\\d+\\.json$";

    static
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Shutting down recorder's executor...");
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
     * Create a new recorder in ui thread (not recommended use directly).
     * 
     * @param rootDirectory     the target directory to store all the data, must be empty
     * @param groupSize         number of records per file
     * @param dataType          the type of data to record
     * @return                  the created recorder
     * 
     * @throws IOException      if the directory is not empty
     */
    @SyncTask
    public static Recorder createSync(final File rootDirectory, int groupSize, byte dataType) throws Exception
    {
        final File[] files = rootDirectory.listFiles();

        if (files == null || files.length == 0)
        {
            switch (dataType)
            {
                case Data.NUMBER_RESPONSE_TYPE: return new NumberResponse(rootDirectory, groupSize);
                default: return new Recorder(rootDirectory, groupSize, dataType);
            }
        }

        throw new IOException(BasicUtils.combined("Directory: ", rootDirectory, " is not empty"));
    }

    /**
     * Create a new recorder in specific thread (recommended).
     * 
     * @param rootDirectory     the target directory to store all the data, must be empty
     * @param groupSize         number of records per file
     * @param dataType          the type of data to record
     * @return                  the created recorder
     * 
     * @throws IOException      if the directory is not empty
     */
    @AsyncTask
    public static Recorder create(final File rootDirectory, int groupSize, byte dataType) throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(() -> createSync(rootDirectory, groupSize, dataType)));
    }

    /**
     * Loads a recorder from a given directory in ui thread (not recommended use directly).
     * 
     * @param rootDirectory     the target directory to store all the data
     * @param dataType          the type of data to record
     * @return                  the loaded recorder
     * 
     * @throws DataFormatException if the recorder is not in the correct format
     */
    @SyncTask
    private static Recorder loadSync(final File rootDirectory, byte dataType) throws Exception
    {
        switch (dataType)
        {
            case Data.NUMBER_RESPONSE_TYPE: return new NumberResponse(rootDirectory);
            default: return new Recorder(rootDirectory, dataType);
        }
    }

    /**
     * Loads a recorder from a given directory in specific thread (recommended).
     * 
     * @param rootDirectory     the target directory to store all the data
     * @param dataType          the type of data to record
     * @return                  the loaded recorder
     * 
     * @throws DataFormatException if the recorder is not in the correct format
     */
    @AsyncTask
    public static Recorder load(final File rootDirectory, byte dataType) throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(() -> loadSync(rootDirectory, dataType)));
    }

    /**
     * Auto create a recover class in ui thread (not recommended use directly). It will delete some data the root folder contain !!!
     * if the root folder is empty create a new recorder instance. If the folder is not empty and does not
     * contain manifest.json, it will delete all the files in th folder and create a new instance.
     * On the other hand it will just load recorder class from directories.
     *
     * @param rootDirectory     the target directory to store all the data
     * @param dataType          the type of data to record
     * @return                  the loaded recorder
     *
     * @throws DataFormatException if the recorder is not in the correct format
     */
    @SyncTask
    private static Recorder autoSync(final File rootDirectory, int groupSize, byte dataType) throws Exception
    {
        final File[] files = FileHelper.listFiles(rootDirectory);

        if (files != null && files.length > 0)
        {
            if (new File(rootDirectory, MANIFEST_FILE_NAME).exists())
                return loadSync(rootDirectory, dataType);

            FileHelper.deleteDirectories(rootDirectory, false);
        }

        return createSync(rootDirectory, groupSize, dataType);
    }

    /**
     * Auto create a recover class in specific thread (recommended). It will delete some data the root folder contain !!!
     * if the root folder is empty create a new recorder instance. If the folder is not empty and does not
     * contain manifest.json, it will delete all the files in th folder and create a new instance.
     * On the other hand it will just load recorder class from directories.
     *
     * @param rootDirectory     the target directory to store all the data
     * @param dataType          the type of data to record
     * @return                  the loaded recorder
     *
     * @throws DataFormatException if the recorder is not in the correct format
     */
    @AsyncTask
    public static Recorder auto(final File rootDirectory, int groupSIze, byte dataType) throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(() -> autoSync(rootDirectory, groupSIze, dataType)));
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

    protected final int groupSize;
    protected final byte dataType;
    protected final File rootDirectory;
    protected final File manifestFile;
    protected final JSONObject AllJson;
    protected final List<File> dataFiles;

    protected int size ;
    protected int validSize;
    protected Data current;
    protected Data best;
    protected Data worst;
    protected File currentDataFile;

    protected JSONObject manifestJson;
    protected JSONObject currentDataFileJson;
    protected JSONArray records;

    // load from directory
    @SyncTask
    private Recorder(final File rootDirectory, byte dataType) throws Exception
    {
        this.rootDirectory = rootDirectory;
        this.manifestFile = new File(rootDirectory, MANIFEST_FILE_NAME);
        this.manifestJson = new JSONObject(FileHelper.read(this.manifestFile));
        this.AllJson = this.manifestJson.getJSONObject(ALL_KEY);

        this.dataType = (byte)(this.manifestJson.getInt(DATA_TYPE_KEY));

        if (this.dataType != dataType)
            throw new IOException(BasicUtils.combined("Cannot load from directory, data type unmatched: expected: ", this.dataType, " but given: ", dataType));

        this.size = this.manifestJson.getInt(DATA_SIZE_KEY);
        this.validSize = this.manifestJson.getInt(VALID_SIZE_KEY);
        this.groupSize = this.manifestJson.getInt(GROUP_SIZE_KEY);
        this.best = this.AllJson.has(BEST_KEY) ? Data.load(this.AllJson.getJSONObject(BEST_KEY), this.dataType) : null;
        this.worst = this.AllJson.has(WORST_KEY) ? Data.load(this.AllJson.getJSONObject(WORST_KEY), this.dataType) : null;
        this.current = this.AllJson.has(CURRENT_KEY) ? Data.load(this.AllJson.getJSONObject(CURRENT_KEY), this.dataType) : null;

        final String version = this.manifestJson.getString(VERSION_KEY);

        if (!version.equals(VERSION))
            throw new DataFormatException(BasicUtils.combined("Data load error: expected type: ", this.dataType, " but given: ", dataType));

        final int index = this.size / this.groupSize;
        this.currentDataFile = this.getDataFile(index);

        final File[] dataFile = FileHelper.listFiles(this.rootDirectory, DATA_FILE_REGEX);
        this.dataFiles = new ArrayList<>(Arrays.asList(dataFile));

        if (!this.currentDataFile.exists())
        {
            final Pair<File, JSONObject> newFile = this.createNewDataFile(index);
            this.currentDataFile = newFile.getValue0();
            this.currentDataFileJson = newFile.getValue1();
        }
        else
            this.currentDataFileJson = new JSONObject(FileHelper.read(this.currentDataFile));

        this.records = this.currentDataFileJson.getJSONArray(RECORDS_KEY);
    }

    // create new instance
    @SyncTask
    private Recorder(final File rootDirectory, int groupSize, byte dataType) throws Exception
    {
        if (!rootDirectory.exists() && !FileHelper.createNewDirectories(rootDirectory))
            throw new IOException("Create directory false: " + rootDirectory.getAbsolutePath());

        this.dataType = dataType;
        this.size = this.validSize = 0;
        this.groupSize = groupSize;
        this.best = this.worst = this.current = null;
        this.rootDirectory = rootDirectory;
        this.manifestFile = new File(rootDirectory, MANIFEST_FILE_NAME);
        this.dataFiles = new ArrayList<>();

        if (FileHelper.createNewFile(this.manifestFile) && !this.manifestFile.exists())
            throw new IOException("Create file false: " + this.manifestFile.getAbsolutePath());

        this.manifestJson = new JSONObject();
        this.AllJson = new JSONObject();
        this.manifestJson.put(VERSION_KEY, VERSION).put(DATA_TYPE_KEY, dataType).put(GROUP_SIZE_KEY, groupSize)
                         .put(DATA_SIZE_KEY, 0).put(GROUP_SIZE_KEY, groupSize).put(ALL_KEY, AllJson)
                         .put(VALID_SIZE_KEY, 0);

        final Pair<File, JSONObject> newData = this.createNewDataFile(0);

        this.currentDataFile = newData.getValue0();
        this.currentDataFileJson = newData.getValue1();
        this.records = this.currentDataFileJson.getJSONArray(RECORDS_KEY);

        this.updateManifestFile();
    }

    /**
     * Adds a new data object to the record in ui thread (not recommended use directly).
     * 
     * @param data              the data object to add
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @SyncTask
    protected void addDataSync(final Data data) throws Exception
    {
        if (data.getDataType() != this.dataType)
            throw new IOException(BasicUtils.combined("Try to add in compatible data type, expected: ",
                                  this.dataType, " but given: ", data.getDataType()));

        this.manifestJson.put(DATA_SIZE_KEY, ++this.size);

        if (data.isValid())
        {
            if (this.best == null || data.compare(this.best) == Data.BETTER)
            {
                this.best = data;
                this.AllJson.put(BEST_KEY, data.toJson());
            }

            if (this.worst == null || data.compare(this.worst) == Data.WORSE)
            {
                this.worst = data;
                this.AllJson.put(WORST_KEY, data.toJson());
            }

            this.manifestJson.put(VALID_SIZE_KEY, ++this.validSize);
        }

        this.AllJson.put(CURRENT_KEY, data);

        if (this.size % this.groupSize == 1 && this.size != 1)
        {
            final Pair<File, JSONObject> newData = this.createNewDataFile(this.size / groupSize);
            this.dataFiles.add(newData.getValue0());
            this.currentDataFile = newData.getValue0();
            this.currentDataFileJson = newData.getValue1();
            this.records = this.currentDataFileJson.getJSONArray(RECORDS_KEY);
        }

        this.records.put(data.toJson());

        this.updateDataFile();
        this.updateManifestFile();
    }

    /**
     * Adds a new data object to the record in specific thread (recommended).
     * 
     * @param data              the data object to add
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @AsyncTask
    public void addData(final Data data) throws Exception
    {
        BasicUtils.handleFutureResult(EXECUTOR.submit(() -> {
            this.addDataSync(data);
            return null;
        }));
    }

    /**
     * Deletes the record in ui thread (not recommended use directly).
     * 
     * @return                  whether the record is deleted successfully
     * 
     * @throws Exception        if an error occurs, for example no such file, permission denied
     */
    @SyncTask
    protected boolean deleteSync() throws Exception
    {
        return FileHelper.deleteDirectories(this.rootDirectory, true);
    }

    /**
     * Deletes the record in a specific thread (recommended).
     * 
     * @return                  whether the record is deleted successfully
     * 
     * @throws Exception        if an error occurs, for example no such file, permission denied
     */
    @AsyncTask
    public boolean delete() throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(this::deleteSync));
    }

    /**
     * Returns all data objects in the record in ui thread (not recommended use directly).
     * 
     * @return                  all data objects in the record
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @SyncTask
    public List<Data> getAllDataSync() throws Exception
    {
        final List<Data> allData = new ArrayList<>(this.dataFiles.size() * this.groupSize);

        for (final File dataFile : this.dataFiles)
        {
            final JSONObject part = new JSONObject(FileHelper.read(dataFile));
            final JSONArray partSessions = part.getJSONArray(RECORDS_KEY);

            for (int i = 0; i < partSessions.length(); i++)
                allData.add(Data.load(partSessions.getJSONObject(i), this.dataType));
        }

        return allData;
    }

    /**
     * Returns all data objects in the record in specific thread (recommended).
     * 
     * @return                  all data objects in the record
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @AsyncTask
    public List<Data> getAllData() throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(this::getAllDataSync));
    }

    /**
     * Returns the JSONObject representation of this Recorder object in ui thread (not recommended use directly).
     * 
     * @return                  the JSONObject representation of this Recorder object
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @SyncTask
    protected JSONObject toJsonSync() throws Exception
    {
        final JSONObject json = new JSONObject(FileHelper.read(this.manifestFile));
        final JSONArray sessions = new JSONArray();

        for (final File dataFile : this.dataFiles)
        {
            final JSONObject part = new JSONObject(FileHelper.read(dataFile));
            final JSONArray partSessions = part.getJSONArray(RECORDS_KEY);

            for (int i = 0; i < partSessions.length(); i++)
                sessions.put(partSessions.get(i));
        }

        json.put(VERSION_KEY, VERSION).put(BEST_KEY, this.best).put(GROUP_SIZE_KEY, this.groupSize).put(RECORDS_KEY, sessions);
        return json;
    }

    /**
     * Returns the JSONObject representation of this Recorder object in a specific thread (recommended).
     * 
     * @return                  the JSONObject representation of this Recorder object
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @AsyncTask
    public JSONObject toJson() throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(this::toJsonSync));
    }

    /**
     * Returns the string representation of this Recorder object.
     *
     * @return                  the string representation of this Recorder object in JSON format
     * 
     * @throws RuntimeException if an exception occurs while converting to JSON
     */
    @NonNull
    @Override
    public String toString()
    {
        try
        {
            return this.toJson().toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the current manifest file content to the manifest file.
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @SyncTask
    protected void updateManifestFile() throws Exception
    {
        FileHelper.write(this.manifestFile, this.manifestJson.toString(4), false);
    }

    /**
     * Writes the current data file content to the data file.
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @SyncTask
    protected void updateDataFile() throws Exception
    {
        FileHelper.write(this.currentDataFile, this.currentDataFileJson.toString(4), false);
    }

    /**
     * Retrieves the data file corresponding to the specified index.
     *
     * @param index             the index of the data file to retrieve
     * @return                  the File object representing the data file
     */
    @SyncTask
    protected File getDataFile(int index)
    {
        return new File(this.rootDirectory, BasicUtils.combined(DATA_FILE_PREFIX, index, JSON_EXTENSION_NAME));
    }

    /**
     * Creates a new data file for storing records and initializes it with an empty JSON array.
     *
     * @param index             the index to use for naming the data file
     * @return                  a Pair containing the created File object and its initial JSONObject content
     * @throws Exception        if the file cannot be created or an I/O error occurs
     */
    @SyncTask
    protected Pair<File, JSONObject> createNewDataFile(int index) throws Exception
    {
        final File dataFile = new File(this.rootDirectory, BasicUtils.combined(DATA_FILE_PREFIX, index, JSON_EXTENSION_NAME));

        if (FileHelper.createNewFile(dataFile) && !dataFile.exists())
            throw new IOException("Create file false: " + this.manifestFile.getAbsolutePath());

        this.dataFiles.add(dataFile);

        final JSONObject content = new JSONObject().put(RECORDS_KEY, new JSONArray());
        FileHelper.write(dataFile, content.toString(), false);
        return new Pair<>(dataFile, content);
    }

    public static final class NumberResponse extends Recorder
    {
        public final Data.Statics<Object> all;
        public final Data.Statics<Double> MO3;
        public final Data.Statics<Double> AO5;
        public final Data.Statics<Double> AO12;
        public final Data.Statics<Double> AO100;

        private final StreamingAggregator.MO mo3;
        private final StreamingAggregator.AO ao5;
        private final StreamingAggregator.AO ao12;
        private final StreamingAggregator.AO ao100;

        private final JSONObject MO3Json;
        private final JSONObject AO5Json;
        private final JSONObject AO12Json;
        private final JSONObject AO100Json;
        private final LinkedList<Double> last100Data;

        // create new instance
        @SyncTask
        private NumberResponse(final File directory, int groupSize) throws Exception
        {
            super(directory, groupSize, Data.NUMBER_RESPONSE_TYPE);

            this.mo3 = new StreamingAggregator.MO(3);
            this.ao5 = new StreamingAggregator.AO(5);
            this.ao12 = new StreamingAggregator.AO(12);
            this.ao100 = new StreamingAggregator.AO(100);

            this.MO3Json = new JSONObject(); this.AO5Json = new JSONObject(); this.AO12Json = new JSONObject(); this.AO100Json = new JSONObject();
            this.all = new Data.Statics<>();
            this.MO3 = new Data.Statics<>(); this.AO5 = new Data.Statics<>(); this.AO12 = new Data.Statics<>(); this.AO100 = new Data.Statics<>();

            this.validSize = 0;

            this.manifestJson.put(MO3_KEY, this.MO3Json).put(AO5_KEY, this.AO5Json).put(AO12_KEY, this.AO12Json)
                             .put(AO100_KEY, this.AO100Json).put(VALID_SIZE_KEY, 0);

            this.last100Data = new LinkedList<>();
            this.updateManifestFile();
        }

        // load from file
        @SyncTask
        private NumberResponse(final File directory) throws Exception
        {
            super(directory, Data.NUMBER_RESPONSE_TYPE);

            this.mo3 = new StreamingAggregator.MO(3);
            this.ao5 = new StreamingAggregator.AO(5);
            this.ao12 = new StreamingAggregator.AO(12);
            this.ao100 = new StreamingAggregator.AO(100);

            this.MO3Json = this.manifestJson.getJSONObject(MO3_KEY);
            this.AO5Json = this.manifestJson.getJSONObject(AO5_KEY);
            this.AO12Json = this.manifestJson.getJSONObject(AO12_KEY);
            this.AO100Json = this.manifestJson.getJSONObject(AO100_KEY);

            this.last100Data = new LinkedList<>();

            final int endIndex = this.size - 1, startIndex = Math.max((endIndex - 99), 0);
            final int fileEndIndex = endIndex / this.groupSize, fileStartIndex = startIndex / this.groupSize;

            for (int i = fileStartIndex; i <= fileEndIndex; i++)
            {
                final JSONObject json = new JSONObject(FileHelper.read(this.getDataFile(i)));
                final JSONArray sessions = json.getJSONArray(RECORDS_KEY);

                for (int j = 0; j < sessions.length(); j++)
                {
                    final Data data = Data.load(sessions.getJSONObject(j), Data.NUMBER_RESPONSE_TYPE);
                    final int index = ((Data.NumberResponse)(data)).getSerial();

                    if (index >= startIndex && index <= endIndex)
                        this.last100Data.add(((Data.NumberResponse)(data)).getTime());
                }
            }

            final Double average = this.AllJson.has(AVERAGE_KEY) ? this.AllJson.getDouble(AVERAGE_KEY) : null;
            this.all = new Data.Statics<>(this.current, this.best, this.worst, average);

            this.MO3 = new Data.Statics<>(
                    this.MO3Json.has(CURRENT_KEY) ? this.MO3Json.getDouble(CURRENT_KEY) : null,
                    this.MO3Json.has(BEST_KEY) ? this.MO3Json.getDouble(BEST_KEY) : null,
                    this.MO3Json.has(WORST_KEY) ? this.MO3Json.getDouble(WORST_KEY) : null,
                    this.MO3Json.has(AVERAGE_KEY) ? this.MO3Json.getDouble(AVERAGE_KEY) : null
            );

            this.AO5 = new Data.Statics<>(
                    this.AO5Json.has(CURRENT_KEY) ? this.AO5Json.getDouble(CURRENT_KEY) : null,
                    this.AO5Json.has(BEST_KEY) ? this.AO5Json.getDouble(BEST_KEY) : null,
                    this.AO5Json.has(WORST_KEY) ? this.AO5Json.getDouble(WORST_KEY) : null,
                    this.AO5Json.has(AVERAGE_KEY) ? this.AO5Json.getDouble(AVERAGE_KEY) : null
            );

            this.AO12 = new Data.Statics<>(
                    this.AO12Json.has(CURRENT_KEY) ? this.AO12Json.getDouble(CURRENT_KEY) : null,
                    this.AO12Json.has(BEST_KEY) ? this.AO12Json.getDouble(BEST_KEY) : null,
                    this.AO12Json.has(WORST_KEY) ? this.AO12Json.getDouble(WORST_KEY) : null,
                    this.AO12Json.has(AVERAGE_KEY) ? this.AO12Json.getDouble(AVERAGE_KEY) : null
            );

            this.AO100 = new Data.Statics<>(
                    this.AO100Json.has(CURRENT_KEY) ? this.AO100Json.getDouble(CURRENT_KEY) : null,
                    this.AO100Json.has(BEST_KEY) ? this.AO100Json.getDouble(BEST_KEY) : null,
                    this.AO100Json.has(WORST_KEY) ? this.AO100Json.getDouble(WORST_KEY) : null,
                    this.AO100Json.has(AVERAGE_KEY) ? this.AO100Json.getDouble(AVERAGE_KEY) : null
            );
        }
        /**
         * Adds a new data in specific thread (recommended), which is "Do not finished"
         *
         * @throws Exception    if an error occurs while adding the data
         */
        @AsyncTask
        public void addDNF() throws Exception
        {
            this.addData(new Data.NumberResponse(this.size));
        }

        /**
         * Adds a new data object with the given time to the record in specific thread (recommended).
         * 
         * @param time          the time to include in the data object
         * 
         * @throws Exception    if an error occurs while adding the data
         */
        @AsyncTask
        public void addData(double time) throws Exception
        {
            if (time == Data.DNF) this.addDNF();
            else this.addData(new Data.NumberResponse(this.size, time));
        }

        /**
         * Adds a new data object with the given time to the record in ui thread (not recommended use directly).
         * 
         * @param data          the data object to include in the record
         * 
         * @throws Exception    if an error occurs while adding the data
         */
        @Override
        @SyncTask
        protected void addDataSync(final Data data) throws Exception
        {
            if (data.getDataType() != this.dataType)
                throw new IOException(BasicUtils.combined("Try to add in compatible data type, expected: ",
                        this.dataType, " but given: ", data.getDataType()));

            final double time = ((Data.NumberResponse)(data)).getTime();
            this.last100Data.add(time);
            this.records.put(data.toJson());

            this.updateAll(data, time);
            this.updateMO3(time);
            this.updateAO5(time);
            this.updateAO12(time);
            this.updateAO100(time);

            if (this.size % (this.groupSize + 1) == 0)
            {
                final Pair<File, JSONObject> newData = this.createNewDataFile(this.size / groupSize);
                this.dataFiles.add(newData.getValue0());
                this.currentDataFile = newData.getValue0();
                this.currentDataFileJson = newData.getValue1();
                this.records = this.currentDataFileJson.getJSONArray(RECORDS_KEY);
            }

            if (this.last100Data.size() > 100)
                this.last100Data.pop();

            this.updateDataFile();
            this.updateManifestFile();
        }

        /**
         * Updates the overall statistics and JSON representations with the given data object and time.
         * This method updates the size, current, best, worst, and average values of the recorder
         * based on the provided data and time. It also updates the corresponding fields in the
         * manifest and JSON representations.
         *
         * @param data          the data object to be processed and included in the statistics
         * @param time          the time value associated with the data object
         *
         * @throws Exception    if an error occurs during the update process
         */
        @SyncTask
        private void updateAll(final Data data, double time) throws Exception
        {
            // updateSize
            this.manifestJson.put(DATA_SIZE_KEY, ++this.size);

            this.current = data;
            this.all.current = data;
            this.AllJson.put(CURRENT_KEY, data.toJson());

            if (data.isValid())
            {
                if (this.best == null || data.compare(this.best) == Data.BETTER)
                {
                    this.best = data;
                    this.all.best = data;
                    this.AllJson.put(BEST_KEY, data.toJson());
                }

                if (this.worst == null || data.compare(this.worst) == Data.WORSE)
                {
                    this.worst = data;
                    this.all.worst = data;
                    this.AllJson.put(WORST_KEY, data.toJson());
                }

                final double oldAverage = (this.all.average == null) ? 0.0 : (double)(this.all.average);
                final double average = (oldAverage * this.validSize + time) / (++this.validSize);
                this.all.average = average;
                this.AllJson.put(AVERAGE_KEY, average);
                this.manifestJson.put(VALID_SIZE_KEY, this.validSize);
            }
        }

        /**
         * Updates the MO3 statistics with the given time value.
         * This method updates the current, average, best, and worst values of the MO3 data set
         * with the provided time. The updates are reflected in both the MO3 data fields and its
         * corresponding JSON representation.
         * 
         * @param time          the time value to be added to the MO3 data set
         * 
         * @throws Exception    if an error occurs during the update process
         */
        @SyncTask
        private void updateMO3(double time) throws Exception
        {
            final Double current = this.mo3.add(time);
            final Double average = this.mo3.getAverage(), best = this.mo3.getMin(), worst = this.mo3.getMax();
            this.MO3.update(current, best, worst, average);
            this.MO3Json.put(AVERAGE_KEY, Data.format(average)).put(CURRENT_KEY, Data.format(current))
                        .put(BEST_KEY, Data.format(best)).put(WORST_KEY, Data.format(worst));
        }

        /**
         * Updates the AO5 statistics with the given time value.
         * This method updates the current, average, best, and worst values of the AO5 data set
         * with the provided time. The updates are reflected in both the AO5 data fields and its
         * corresponding JSON representation.
         * 
         * @param time          the time value to be added to the AO5 data set
         * 
         * @throws Exception    if an error occurs during the update process
         */
        @SyncTask
        private void updateAO5(double time) throws Exception
        {
            final Double current = this.ao5.add(time);
            final Double average = this.ao5.getAverage(), best = this.ao5.getMin(), worst = this.ao5.getMax();
            this.AO5.update(current, best, worst, average);
            this.AO5Json.put(AVERAGE_KEY, Data.format(average)).put(CURRENT_KEY, Data.format(current))
                        .put(BEST_KEY, Data.format(best)).put(WORST_KEY, Data.format(worst));
        }

        /**
         * Updates the AO12 statistics with the given time value.
         * This method updates the current, average, best, and worst values of the AO12 data set
         * with the provided time. The updates are reflected in both the AO12 data fields and its
         * corresponding JSON representation.
         * 
         * @param time          the time value to be added to the AO12 data set
         * 
         * @throws Exception    if an error occurs during the update process
         */

        @SyncTask
        private void updateAO12(double time) throws Exception
        {
            final Double current = this.ao12.add(time);
            final Double average = this.ao12.getAverage(), best = this.ao12.getMin(), worst = this.ao12.getMax();
            this.AO12.update(current, best, worst, average);
            this.AO12Json.put(AVERAGE_KEY, Data.format(average)).put(CURRENT_KEY, Data.format(current))
                         .put(BEST_KEY, Data.format(best)).put(WORST_KEY, Data.format(worst));
        }

        /**
         * Updates the average, best, worst and current time of the AO100 statistics object.
         * Also updates the corresponding fields in the AO100Json object.
         *
         * @param time          the time to be added to the AO100 statistics
         *
         * @throws Exception    if an error occurs while updating the statistics
         */
        @SyncTask
        private void updateAO100(double time) throws Exception
        {
            final Double current = this.ao100.add(time);
            final Double average = this.ao100.getAverage(), best = this.ao100.getMin(), worst = this.ao100.getMax();
            this.AO100.update(current, best, worst, average);
            this.AO100Json.put(AVERAGE_KEY, Data.format(average)).put(CURRENT_KEY, Data.format(current))
                          .put(BEST_KEY, Data.format(best)).put(WORST_KEY, Data.format(worst));
        }
    }
}