package hku.cs.hkutopia.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import hku.cs.hkutopia.model.CampusStory;

/**
 * Manager class for handling campus stories
 */
public class StoryManager {
    private static final String TAG = "StoryManager";
    private static final String PREF_NAME = "CampusStories";
    private static final String KEY_STORIES = "user_stories";

    private static StoryManager instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private StoryManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized StoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new StoryManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Add a new story to the list
     */
    public void addStory(CampusStory story) {
        List<CampusStory> stories = getUserStories();
        stories.add(0, story); // Add to the beginning of the list
        saveStories(stories);
    }

    /**
     * Get all user-created stories
     */
    public List<CampusStory> getUserStories() {
        String json = sharedPreferences.getString(KEY_STORIES, null);
        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<CampusStory>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "Error loading stories", e);
            return new ArrayList<>();
        }
    }

    /**
     * Save stories to SharedPreferences
     */
    private void saveStories(List<CampusStory> stories) {
        try {
            String json = gson.toJson(stories);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_STORIES, json);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving stories", e);
        }
    }

    /**
     * Delete a story by ID
     */
    public boolean deleteStory(int id) {
        List<CampusStory> stories = getUserStories();
        boolean removed = false;

        for (int i = 0; i < stories.size(); i++) {
            if (stories.get(i).getId() == id) {
                stories.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            saveStories(stories);
        }

        return removed;
    }

    /**
     * Update an existing story
     */
    public boolean updateStory(CampusStory updatedStory) {
        List<CampusStory> stories = getUserStories();
        boolean updated = false;

        for (int i = 0; i < stories.size(); i++) {
            if (stories.get(i).getId() == updatedStory.getId()) {
                stories.set(i, updatedStory);
                updated = true;
                break;
            }
        }

        if (updated) {
            saveStories(stories);
        }

        return updated;
    }

    /**
     * Get a story by ID
     */
    public CampusStory getStoryById(int id) {
        List<CampusStory> stories = getUserStories();

        for (CampusStory story : stories) {
            if (story.getId() == id) {
                return story;
            }
        }

        return null;
    }
}