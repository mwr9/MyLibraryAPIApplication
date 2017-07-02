package com.example.owner.mylibraryapiapplication;

import android.content.Intent;
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

public class BookDisplayActivity extends AppCompatActivity {
    public static final String BOOK_DETAIL_URL = "book_url";
    private static final String Default_URL = "http://www.archive.org/stream/thissideparadis01fitzgoog#page/n7/mode/2up";
    private ImageView bookCover;
    private TextView bkTitle;
    private TextView bkAuthor;
    private TextView bkPublisher;
    private TextView bkPageCount;
    private BookAPIClient apiClient;
    private Button readable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Fetch views
        bookCover = (ImageView) findViewById(R.id.bookCover);
        bkTitle = (TextView) findViewById(R.id.bkTitle);
        bkAuthor = (TextView) findViewById(R.id.bkAuthor);
        bkPublisher = (TextView) findViewById(R.id.bkPublisher);
        bkPageCount = (TextView) findViewById(R.id.bkPageCount);
        readable = (Button) findViewById(R.id.btnReadable);
        // Use the book to populate the data into our views
        final Book book = (Book) getIntent().getSerializableExtra(BookListActivity.BOOK_DETAIL_KEY);
        loadBook(book);
        readable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                readBook(book);
            }
        });
    }

    // Load data for the book
    private void loadBook(Book book) {
        //change activity title
        this.setTitle(book.getTitle());
        // Implement Open Library API Call 4 - using Picasso to get Book Cover - Large Size
        Picasso.with(this).load(Uri.parse(book.getLargeCoverUrl())).error(R.drawable.ic_nocover).into(bookCover);
        bkTitle.setText(book.getTitle());
        bkAuthor.setText(book.getAuthor());
        apiClient = new BookAPIClient();

        // fetch extra book data from books API
        apiClient.getExtraBookDetails(book.getOpenLibraryId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("publishers")) {
                        // display comma separated list of publishers
                        final JSONArray publisher = response.getJSONArray("publishers");
                        final int numPublishers = publisher.length();
                        final String[] publishers = new String[numPublishers];
                        for (int i = 0; i < numPublishers; ++i) {
                            publishers[i] = publisher.getString(i);
                        }
                        bkPublisher.setText(TextUtils.join(", ", publishers));
                    }
                    if (response.has("number_of_pages")) {
                        String pages = getText(R.string.pages) + Integer.toString(response.getInt("number_of_pages"));
                        bkPageCount.setText(pages);
                    }
                } catch (JSONException e) {
                    Toast.makeText(BookDisplayActivity.this,"Sorry - cannot get info", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void readBook(Book book) {
        apiClient = new BookAPIClient();
        apiClient.getReadableBook(book.getOpenLibraryId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonItems = response.getJSONObject("items");
                    String status = jsonItems.optString("status", "");
                        if (status.equals("full_access")) {
                        String readLink = jsonItems.getString("itemURL");
                        // Launch the detail view passing book as an extra
                        Intent intent = new Intent(BookDisplayActivity.this, BookReadActivity.class);
                        intent.putExtra(BOOK_DETAIL_URL, readLink);
                        startActivity(intent);
                    } else {
                        Toast.makeText(BookDisplayActivity.this,"The book cannot be accessed for reading", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(BookDisplayActivity.this,"Sorry - cannot get that book. Getting another ....", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(BookDisplayActivity.this, BookReadActivity.class);
                    intent.putExtra(BOOK_DETAIL_URL, Default_URL);
                    startActivity(intent);
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.action_share) {
            setShareIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Implement a share intent - building on work in earlier assignment submissions
    private void setShareIntent() {
        ImageView ivImage = (ImageView) findViewById(R.id.bookCover);
        final TextView tvTitle = (TextView)findViewById(R.id.bkTitle);
        // Get URI for the bitmap
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
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
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
