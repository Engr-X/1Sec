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

package com.wangdi.onesec.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.wangdi.onesec.data.Data;
import com.wangdi.onesec.interfaces.AsyncTask;
import com.wangdi.onesec.interfaces.SyncTask;
import com.wangdi.onesec.utils.BasicUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * I18n class (Internationalization) is aim to translate text to different languages,
 * this class need to read lang files in assets
 *
 * @author Di Wang
 * @version 1.0
 */

public final class I18n
{
    public static final byte EN_US = 0;
    public static final byte ES_ES = 1;
    @SuppressWarnings("unused")
    public static final byte ZH_CN = 2;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    private static final String LANG_DIRECTORY = "lang/";

    private static final String ES_ES_FILE = "es-ES.json";
    private static final String EN_US_FILE = "en_US.json";
    private static final String ZH_CN_FILE = "zh_CN.json";

    private static boolean isInit = false;
    private static AssetManager assetManager = null;
    private static Map<String, String> map = null;

    public static boolean isInit()
    {
        return isInit;
    }

    /**
     * Initializes the I18n class with the given language and context.
     * The method returns the map of translations. (Translate to system's first language)
     *
     * @param context           the context to be used to access the assets directory

     * @return                  the map of translations
     * @throws Exception        if an error occurs while reading the file
     */
    public static Map<String, String> init(final Context context) throws Exception
    {
        return init(context, getSystemLanguage() + ".json");
    }

    /**
     * Initializes the I18n class with the given language and context.
     * The method returns the map of translations.
     *
     * @param context           the context to be used to access the assets directory
     * @param language          the language to be used, ZH_CN or EN_US
     *
     * @return                  the map of translations
     * @throws Exception        if an error occurs while reading the file
     */
    public static Map<String, String> init(final Context context, byte language) throws Exception
    {
        return switch (language)
        {
            case EN_US -> init(context, EN_US_FILE);
            case ES_ES -> init(context, ES_ES_FILE);
            default -> init(context, ZH_CN_FILE);
        };
    }

    /**
     * Initializes the I18n class with the given language and context in the main thread.
     * The method returns the map of translations.
     *
     * @param context           the context to be used to access the assets directory
     * @param languageFile      the language file to be read, e.g., "en_us.json"
     *
     * @return                  the map of translations
     *
     * @throws Exception        if an error occurs while reading the file
     */
    public static Map<String, String> init(final Context context, String languageFile) throws Exception
    {
        isInit = true;
        assetManager = context.getAssets();
        map = getKeyMap(languageFile);
        return map;
    }

    /**
     * Formats the input string by replacing all occurrences of keys with
     * their corresponding values from a predefined map. The replacements
     * are performed using a StringBuilder for efficiency.
     *
     * @param str               the input string to be formatted
     * @return                  the formatted string with replacements applied
     */
    public static String format(String str)
    {
        final StringBuilder sb = new StringBuilder(str);

        for (Map.Entry<String, String> entry : map.entrySet())
            BasicUtils.replaceAll(sb, entry.getKey(), entry.getValue());

        return sb.toString();
    }

    /**
     * Retrieves a map of keys and values from a language-specific JSON file.
     * The file is read from the assets directory based on the provided language.
     * Each key-value pair in the JSON is added to the map.
     *
     * @param language          the language file to parse (e.g., "en_us.json")
     *
     * @return                  a map containing keys and their corresponding translations
     * @throws Exception        if an error occurs while reading the file or parsing JSON
     */
    private static Map<String, String> getKeyMap(String language) throws Exception
    {
        final String filePath = LANG_DIRECTORY + language;
        final JSONObject json = new JSONObject(read(filePath));
        final Iterator<String> it = json.keys();
        final Map<String, String> keyMap = new HashMap<>();

        while (it.hasNext())
        {
            final String key = it.next();
            keyMap.put(key, json.getString(key));
        }

        return keyMap;
    }

    /**
     * Reads the given file in assets and returns its content as a string.
     * This function should be called in a thread pool.
     *
     * @param path              the path to file in assets
     *
     * @return                  the content of the file as a string
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    private static String read(String path) throws Exception
    {
        InputStream inputStream = assetManager.open(path);
        final byte[] buffer = new byte[inputStream.available()];

        if (inputStream.read(buffer) == -1)
            throw new IOException("Failed to read file: " + path);
                
        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * Returns the language tag of the system's default locale, which is
     * the locale of the device's configuration.
     *
     * @return  the language tag, e.g. "en-US" or "zh-CN"
     */
    private static String getSystemLanguage()
    {
        final Configuration config = Resources.getSystem().getConfiguration();
        final Locale locale = config.getLocales().get(0);
        return locale.toLanguageTag();
    }

    private I18n()
    {

    }
}