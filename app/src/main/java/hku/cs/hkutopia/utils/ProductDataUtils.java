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

import hku.cs.hkutopia.model.ProductItem;

public class ProductDataUtils {
    private static final String TAG = "ProductDataUtils";

    public static List<ProductItem> loadProductsFromJson(Context context, int resourceId) {
        List<ProductItem> products = new ArrayList<>();

        try {
            // Read JSON file
            InputStream is = context.getResources().openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // Parse JSON data
            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray productsArray = jsonObject.getJSONArray("products");

            Log.d(TAG, "Found " + productsArray.length() + " products in JSON");

            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject productObject = productsArray.getJSONObject(i);

                int id = productObject.getInt("id");
                String name = productObject.getString("name");
                double price = productObject.getDouble("price");

                // Get image resource ID list
                JSONArray imagesArray = productObject.getJSONArray("images");
                List<Integer> imageResIds = new ArrayList<>();

                for (int j = 0; j < imagesArray.length(); j++) {
                    String imageName = imagesArray.getString(j);
                    int imageResId = context.getResources().getIdentifier(
                            imageName, "drawable", context.getPackageName());
                    if (imageResId != 0) {
                        imageResIds.add(imageResId);
                    } else {
                        Log.w(TAG, "Could not find resource for image: " + imageName);
                        // If resource not found, use default image
                        imageResId = context.getResources().getIdentifier(
                                "cultural_product1", "drawable", context.getPackageName());
                        if (imageResId != 0) {
                            imageResIds.add(imageResId);
                        }
                    }
                }

                // Ensure at least one image
                if (imageResIds.isEmpty()) {
                    int defaultImageResId = context.getResources().getIdentifier(
                            "cultural_product1", "drawable", context.getPackageName());
                    if (defaultImageResId != 0) {
                        imageResIds.add(defaultImageResId);
                    }
                }

                // Create product object and add to list
                ProductItem product = new ProductItem(id, name, price, imageResIds);
                products.add(product);
                Log.d(TAG, "Added product: " + name + " (ID: " + id + ")");
            }

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading products from JSON", e);
        }

        Log.d(TAG, "Returning " + products.size() + " products");
        return products;
    }
}
