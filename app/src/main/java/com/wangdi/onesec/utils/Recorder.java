/**
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

package com.wangdi.onesec.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

import kotlin.Pair;

import androidx.annotation.NonNull;

import com.wangdi.onesec.AsyncTask;
import com.wangdi.onesec.SyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The recorder class is aimed to not only record all the session data, but also provide some methods to
 * write these data to files (The record data also can be load from these files).
 *
 * <p>
 * Furthermore, the recorder have ability to analyse all the data superficially.
 * For example, find the best data it contains.
 * </p>
 *
 *
 * @see NumberResponseRecorder
 * @author Di Wang
 * @version beta
 */

public class Recorder
{
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private static final String VERSION = "1.0";
    private static final String DATA_FILE_PREFIX = "data";
    private static final String JSON_EXTENSION_NAME = ".json";
    private static final String MANIFEST_FILE_NAME = "manifest.json";
    private static final String DATA_FILE_REGEX = "^data\\d+\\.json$";

    public static final String DATA_TYPE_KEY = "data_type";
    public static final String VERSION_KEY = "version";
    public static final String GROUP_SIZE_KEY = "group_size";
    public static final String DATA_SIZE_KEY = "data_size";
    public static final String BEST_KEY = "best";

    public static final String RECORDS_KEY = "records";

    static
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            EXECUTOR.shutdown();

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
     * Create a new recorder in ui thread.
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
                case Data.NUMBER_RESPONSE_TYPE: return new NumberResponseRecorder(rootDirectory, groupSize);
                default: return new Recorder(rootDirectory, groupSize, dataType);
            }
        }

        throw new IOException(BasicUtils.combined("Directory: ", rootDirectory, " is not empty"));
    }

    /**
     * Create a new recorder in specific thread.
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
     * Loads a recorder from a given directory in ui thread.
     * 
     * @param rootDirectory     the target directory to store all the data
     * @param dataType          the type of data to record
     * @return                  the loaded recorder
     * 
     * @throws DataFormatException if the recorder is not in the correct format
     */
    @SyncTask
    public static Recorder loadSync(final File rootDirectory, byte dataType) throws Exception
    {
        switch (dataType)
        {
            case Data.NUMBER_RESPONSE_TYPE: return new NumberResponseRecorder(rootDirectory);
            default: return new Recorder(rootDirectory, dataType);
        }
    }

    /**
     * Loads a recorder from a given directory in specific thread.
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

    private static void shutdown()
    {
        EXECUTOR.shutdown();
    }

    protected final byte dataType;
    protected int size ;
    protected final int groupSize;
    protected Data best;
    protected final File rootDirectory;
    protected final File manifestFile;

    protected JSONObject manifestJson;
    protected File currentDataFile;
    protected JSONObject currentJson;
    protected JSONArray records;

    protected final List<File> dataFiles;

    // load from directory
    @SyncTask
    private Recorder(final File rootDirectory, byte dataType) throws Exception
    {
        this.rootDirectory = rootDirectory;
        this.manifestFile = new File(rootDirectory, MANIFEST_FILE_NAME);
        this.manifestJson = new JSONObject(FileHelper.read(this.manifestFile));

        this.dataType = (byte)(this.manifestJson.getInt(DATA_TYPE_KEY));

        if (this.dataType != dataType)
            throw new IOException();

        this.size = this.manifestJson.getInt(DATA_SIZE_KEY);
        this.groupSize = this.manifestJson.getInt(GROUP_SIZE_KEY);
        this.best = Data.load(this.manifestJson.getJSONObject(BEST_KEY), this.dataType);
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
            this.currentDataFile = newFile.getFirst();
            this.currentJson = newFile.getSecond();
        }
        else
            this.currentJson = new JSONObject(FileHelper.read(this.currentDataFile));

        this.records = this.currentJson.getJSONArray(RECORDS_KEY);
    }

    // create new instance
    @SyncTask
    private Recorder(final File rootDirectory, int groupSize, byte dataType) throws Exception
    {
        if (!rootDirectory.exists() && !FileHelper.createNewDirectories(rootDirectory))
            throw new IOException("Create directory false: " + rootDirectory.getAbsolutePath());

        this.dataType = dataType;
        this.size = 0;
        this.groupSize = groupSize;
        this.best = null;
        this.rootDirectory = rootDirectory;
        this.manifestFile = new File(rootDirectory, MANIFEST_FILE_NAME);
        this.dataFiles = new ArrayList<>();

        if (FileHelper.createNewFile(this.manifestFile) && !this.manifestFile.exists())
            throw new IOException("Create file false: " + this.manifestFile.getAbsolutePath());

        this.manifestJson = new JSONObject();
        this.manifestJson.put(VERSION_KEY, VERSION)
                         .put(DATA_TYPE_KEY, dataType)
                         .put(GROUP_SIZE_KEY, groupSize)
                         .put(DATA_SIZE_KEY, 0)
                         .put(GROUP_SIZE_KEY, groupSize);

        FileHelper.write(this.manifestFile, manifestJson.toString(), false);
        final Pair<File, JSONObject> newData = this.createNewDataFile(0);

        this.currentDataFile = newData.getFirst();
        this.currentJson = newData.getSecond();
        this.records = this.currentJson.getJSONArray(RECORDS_KEY);
    }

    /**
     * Adds a new data object to the record in ui thread.
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

        if (this.best == null || data.compare(this.best) == Data.BETTER)
        {
            this.best = data;
            this.manifestJson.put(BEST_KEY, data.toJson());
        }

        if (this.size % this.groupSize == 1 && this.size != 1)
        {
            final Pair<File, JSONObject> newData = this.createNewDataFile(this.size / groupSize);
            this.dataFiles.add(newData.getFirst());
            this.currentDataFile = newData.getFirst();
            this.currentJson = newData.getSecond();
            this.records = this.currentJson.getJSONArray(RECORDS_KEY);
        }

        this.records.put(data.toJson());

        this.updateDataFile();
        this.updateManifestFile();
    }

    /**
     * Adds a new data object to the record in specific thread.
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
     * Deletes the record in ui thread.
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
     * Deletes the record in a specific thread.
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
     * Returns all data objects in the record in ui thread.
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
     * Returns all data objects in the record in specific thread.
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
     * Returns the JSONObject representation of this Recorder object in ui thread.
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
     * Returns the JSONObject representation of this Recorder object in a specific thread.
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
    protected void updateManifestFile() throws Exception
    {
        FileHelper.write(this.manifestFile, this.manifestJson.toString(4), false);
    }

    /**
     * Writes the current data file content to the data file.
     * 
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    protected void updateDataFile() throws Exception
    {
        FileHelper.write(this.currentDataFile, this.currentJson.toString(4), false);
    }

    /**
     * Retrieves the data file corresponding to the specified index.
     *
     * @param index             the index of the data file to retrieve
     * @return                  the File object representing the data file
     */
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

    public static final class NumberResponseRecorder extends Recorder
    {
        public static String AVERAGE_KEY = "average";
        public static String CURRENT_MO3_KEY = "current_mo3";
        public static String BEST_MO3_KEY = "best_mo3";
        public static String CURRENT_MO5_KEY = "current_mo5";
        public static String BEST_MO5_KEY = "best_mo5";
        public static String CURRENT_MO12_KEY = "current_mo12";
        public static String BEST_MO12_KEY = "best_mo12";

        private Double bestMO3;
        private Double bestAO5;
        private Double bestAO12;
        private Double currentMO3;
        private Double currentAO5;
        private Double currentAO12;
        private Double average;

        private final LinkedList<Double> last12Data;

        // create new instance
        @SyncTask
        private NumberResponseRecorder(final File directory, int groupSize) throws Exception
        {
            super(directory, groupSize, Data.NUMBER_RESPONSE_TYPE);

            this.average = 0.0;
            this.last12Data = new LinkedList<>();
        }

        // load from file
        @SyncTask
        private NumberResponseRecorder(final File directory) throws Exception
        {
            super(directory, Data.NUMBER_RESPONSE_TYPE);

            this.average = this.manifestJson.getDouble(AVERAGE_KEY);
            this.last12Data = new LinkedList<>();

            final int endIndex = this.size - 1, startIndex = Math.max((endIndex - 11), 0);
            final int fileEndIndex = endIndex / this.groupSize, fileStartIndex = startIndex / this.groupSize;

            for (int i = fileStartIndex; i <= fileEndIndex; i++)
            {
                final JSONObject json = new JSONObject(FileHelper.read(this.getDataFile(i)));
                final JSONArray sessions = json.getJSONArray(RECORDS_KEY);

                for (int j = 0; j < sessions.length(); j++)
                {
                    final Data data = Data.load(sessions.getJSONObject(j), Data.NUMBER_RESPONSE_TYPE);
                    final int index = (int)(data.get(Data.NUMBER_RESPONSE_SERIAL));

                    if (index >= startIndex && index <= endIndex)
                        this.last12Data.add((double)(data.get(Data.NUMBER_RESPONSE_TIME)));
                }
            }
        }

        /**
         * Adds a new data object with the given time to the record.
         * 
         * @param time          the time to include in the data object
         * 
         * @throws Exception    if an error occurs while adding the data
         */
        public void addData(double time) throws Exception
        {
            this.addData(new Data.NumberResponse(this.size, time));
        }

        /**
         * Adds a new data object with the given time to the record.
         * 
         * @param data          the data object to include in the record
         * 
         * @throws Exception    if an error occurs while adding the data
         */
        @Override
        protected void addDataSync(final Data data) throws Exception
        {
            final double time = (double)(data.get(Data.NUMBER_RESPONSE_TIME));
            this.last12Data.add(time);
            final int size = this.last12Data.size();

            if (size >= 3)
            {
                final Iterator<Double> it = this.last12Data.descendingIterator();
                final double a = Math.abs(it.next()), b = Math.abs(it.next()), c = Math.abs(it.next());
                this.currentMO3 = (a + b + c) / 3.0;
                this.manifestJson.put(CURRENT_MO3_KEY, this.currentMO3);

                if (this.bestMO3 == null || this.currentMO3 < this.bestMO3)
                {
                    this.bestMO3 = this.currentMO3;
                    this.manifestJson.put(BEST_MO3_KEY, this.bestMO3);
                }

                if (size >= 5)
                {
                    final double[] temp = {a, b, c}; Arrays.sort(temp);
                    double minIn5 = temp[0], maxIn5 = temp[2];
                    double sumIn5 = a + b + c;

                    double popValue = Math.abs(it.next()); sumIn5 += popValue;
                    if (popValue < minIn5) minIn5 = popValue;
                    if (popValue > maxIn5) maxIn5 = popValue;

                    popValue = Math.abs(it.next()); sumIn5 += popValue;
                    if (popValue < minIn5) minIn5 = popValue;
                    if (popValue > maxIn5) maxIn5 = popValue;

                    this.currentAO5 = (sumIn5 - minIn5 - maxIn5) / 3.0;
                    this.manifestJson.put(CURRENT_MO5_KEY, this.currentAO5);

                    if (this.bestAO5 == null || this.currentAO5 < this.bestAO5)
                    {
                        this.bestAO5 = this.currentAO5;
                        this.manifestJson.put(BEST_MO5_KEY, this.bestAO5);
                    }

                    if (size >= 12)
                    {
                        double minIn12 = minIn5, maxIn12 = maxIn5;
                        double sumIn12 = sumIn5;

                        for (byte i = 0; i < 7; i++)
                        {
                            popValue = Math.abs(it.next()); sumIn12 += popValue;
                            if (popValue < minIn12) minIn12 = popValue;
                            if (popValue > maxIn12) maxIn12 = popValue;
                        }

                        this.currentAO12 = (sumIn12 - minIn12 - maxIn12) / 10.0;
                        this.manifestJson.put(CURRENT_MO12_KEY, this.currentAO12);

                        if (this.bestAO12 == null || this.currentAO12 < this.bestAO12)
                        {
                            this.bestAO12 = this.currentAO12;
                            this.manifestJson.put(BEST_MO12_KEY, this.bestAO12);
                        }

                        if (size > 12)
                            this.last12Data.remove(0);
                    }
                }
            }

            if (data.getDataType() != this.dataType)
                throw new RuntimeException(BasicUtils.combined("Try to add in compatible data type, expected: ",
                                            this.dataType, " but given: ", data.getDataType()));

            this.average = (this.average * this.size + Math.abs(time)) / (++this.size);
            this.manifestJson.put(DATA_SIZE_KEY, this.size);
            this.manifestJson.put(AVERAGE_KEY, this.average);

            if (this.best == null || data.compare(this.best) == Data.BETTER)
            {
                this.best = data;
                this.manifestJson.put(BEST_KEY, data.toJson());
            }

            if (this.size % this.groupSize == 1 && this.size != 1)
            {
                final Pair<File, JSONObject> newData = this.createNewDataFile(this.size / groupSize);
                this.dataFiles.add(newData.getFirst());
                this.currentDataFile = newData.getFirst();
                this.currentJson = newData.getSecond();
                this.records = this.currentJson.getJSONArray(RECORDS_KEY);
            }

            this.records.put(data.toJson());

            this.updateDataFile();
            this.updateManifestFile();
        }
    }
}