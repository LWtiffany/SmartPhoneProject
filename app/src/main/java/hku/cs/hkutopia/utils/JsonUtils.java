package hku.cs.hkutopia.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import hku.cs.hkutopia.R;
import hku.cs.hkutopia.model.CampusStory;

public class JsonUtils {
    private static final String TAG = "JsonUtils";

    public static List<CampusStory> loadCampusStories(Context context) {
        List<CampusStory> stories = new ArrayList<>();
        try {
            // 读取JSON文件
            InputStream is = context.getResources().openRawResource(R.raw.campus_stories);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // 解析JSON
            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray storiesArray = jsonObject.getJSONArray("stories");

            for (int i = 0; i < storiesArray.length(); i++) {
                JSONObject storyObject = storiesArray.getJSONObject(i);

                CampusStory story = new CampusStory();
                story.setId(storyObject.getInt("id"));
                story.setTitle(storyObject.getString("title"));
                story.setContent(storyObject.getString("content"));
                story.setAuthor(storyObject.getString("author"));
                story.setDate(storyObject.getString("date"));
                // 获取单图片字段，保证兼容性
                String imageUrl = storyObject.optString("imageUrl", "");
                story.setImageUrl(imageUrl);

                List<String> imageUrls = new ArrayList<>();
                if (storyObject.has("imageUrls")) {
                    JSONArray imageUrlsArray = storyObject.getJSONArray("imageUrls");
                    for (int j = 0; j < imageUrlsArray.length(); j++) {
                        imageUrls.add(imageUrlsArray.getString(j));
                    }
                } else {
                    // 如果没有imageUrls字段，添加imageUrl作为默认值
                    if (!imageUrl.isEmpty()) {
                        imageUrls.add(imageUrl);
                    } else {
                        imageUrls.add("news_image1"); // 添加默认图
                    }
                }
                story.setImageUrls(imageUrls);  //确保imageUrls不为空

                // 解析标签
                JSONArray tagsArray = storyObject.getJSONArray("tags");
                List<String> tags = new ArrayList<>();
                for (int j = 0; j < tagsArray.length(); j++) {
                    tags.add(tagsArray.getString(j));
                }
                story.setTags(tags);


                story.setLikes(storyObject.getInt("likes"));
                story.setComments(storyObject.getInt("comments"));
                story.setLatest(storyObject.getBoolean("isLatest"));

                stories.add(story);
            }

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading campus stories: " + e.getMessage());
        }
        return stories;
    }
}
