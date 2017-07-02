package com.example.owner.mylibraryapiapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark on 15/10/2016.
 */
public class DisplayByIdActivity extends AppCompatActivity{
    private ImageView bookCover;
    private TextView bkTitle;
    private TextView bkAuthor;
    private TextView bkPublisher;
    private TextView bkPageCount;
    private TextView bkWeight;
    private TextView bkNotes;
    private TextView bkDate;
    private TextView bkKey;
    private BookAPIClient apiClient;
    private Button moreInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        // Fetch views
        bookCover = (ImageView) findViewById(R.id.bookCover);
        bkTitle = (TextView) findViewById(R.id.bkTitle);
        bkAuthor = (TextView) findViewById(R.id.bkAuthor);
        bkPublisher = (TextView) findViewById(R.id.bkPublisher);
        bkPageCount = (TextView) findViewById(R.id.bkPageCount);
        moreInfo = (Button) findViewById(R.id.btnMoreInfo);

        bkWeight = (TextView) findViewById(R.id.bkWeight);
        bkNotes = (TextView) findViewById(R.id.bkNotes);
        bkDate = (TextView) findViewById(R.id.bkDate);
        bkKey = (TextView) findViewById(R.id.bkKey);

        // Use the book to populate the data into our views
        final String bookId = (String) getIntent().getSerializableExtra(BookMainActivity.BOOK_ID_KEY);
        loadBookById(bookId);
        moreInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getMoreInfo(bookId);
            }
        });
        }

    // Populate data for the book
    private void loadBookById(final String bookId) {
        apiClient = new BookAPIClient();
        // fetch book data from Open Library API
        apiClient.getBookByID(bookId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject isbn = response.getJSONObject("ISBN:" + bookId);
                    // set up the view - optString for default value if no value exists
                    String title = isbn.optString("title", "");
                    String author = isbn.optString("by_statement", "");
                    String pages = getText(R.string.pages) + Integer.toString(isbn.optInt("number_of_pages", 0));
                    bkTitle.setText(title);
                    bkAuthor.setText(author);
                    bkPageCount.setText(pages);
                    JSONObject cover = isbn.getJSONObject("cover");
                    String coverUrl = cover.optString("medium", "");
                    // Implement Open Library API Call 4 - medium image
                    Picasso.with(DisplayByIdActivity.this).load(Uri.parse(coverUrl)).error(R.drawable.ic_nocover).into(bookCover);
                    // display comma separated list of publishers
                    final JSONArray publisher = isbn.getJSONArray("publishers");
                    final int numPublishers = publisher.length();
                    String[] pubNames = new String[numPublishers];
                    final JSONObject[] publishers = new JSONObject[numPublishers];
                    for (int i = 0; i < numPublishers; ++i) {
                        publishers[i] = publisher.getJSONObject(i);
                        pubNames[i] = publishers[i].getString("name");
                    }
                    bkPublisher.setText(TextUtils.join(", ", pubNames));
                    // Cache additional information to save making another network call later on
                    String weight = isbn.optString("weight", "");
                    String notes = isbn.optString("notes", "");
                    String date = isbn.optString("publish_date", "");
                    String key = isbn.optString("key", "");
                    // Small collection of key-values, so use the SharedPreferences API
                    SharedPreferences sp = getApplicationContext().getSharedPreferences("sharedPrefName", 0); // 0 for private mode
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("weight", weight);
                    editor.putString("notes", notes);
                    editor.putString("date", date);
                    editor.putString("key", key);
                    Log.i("weight", weight);
                    editor.commit();
                }   catch (JSONException e) {
                    Toast.makeText(DisplayByIdActivity.this,"Sorry - cannot get the book", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Replaced a network call with retrieving of cached information
    private void getMoreInfo(String bookId){
        Log.i("String", "before preferences");
        SharedPreferences sp = getApplicationContext().getSharedPreferences("sharedPrefName", 0); // 0 for private mode
        // get String or default value
        String weight = getText(R.string.weight) + sp.getString("weight", "");
        String notes = getText(R.string.notes) + sp.getString("notes", "");
        String date = getText(R.string.date) + sp.getString("date", "");
        String key = getText(R.string.key) + sp.getString("key", "");
        bkWeight.setText(weight);
        bkNotes.setText(notes);
        bkDate.setText(date);
        bkKey.setText(key);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            setShareIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setShareIntent() {
        ImageView ivImage = (ImageView) findViewById(R.id.bookCover);
        final TextView tvTitle = (TextView)findViewById(R.id.bkTitle);
        Uri bmpUri = getLocalBitmapUri(ivImage);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        if (tvTitle != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, (String) tvTitle.getText());
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

}
