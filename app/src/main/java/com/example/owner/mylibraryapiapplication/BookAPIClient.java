package com.example.owner.mylibraryapiapplication;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BookAPIClient {
    private static final String API_BASE_URL = "http://openlibrary.org/";
    private AsyncHttpClient httpClient;

    public BookAPIClient() {
        this.httpClient = new AsyncHttpClient();
    }

    private String getApiUrl(String relativeUrl) {
        return API_BASE_URL + relativeUrl;
    }

    // Open Library API Call 1 - Get book by ISBN
    public void getBookByID(String ISBNNo, JsonHttpResponseHandler handler) {
        String url = getApiUrl("api/books?bibkeys=ISBN:");
        httpClient.get(url + ISBNNo + "&jscmd=data&format=json", handler);
    }

    // Open Library API Call 2 - for accessing the Search API function
    public void getBooks(final String query, JsonHttpResponseHandler handler) {
        try {
            String url = getApiUrl("search.json?q=");
            httpClient.get(url + URLEncoder.encode(query, "utf-8"), handler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // Open Library API Call 3 - for accessing books API to get publisher and no. of pages in a book.
    public void getExtraBookDetails(String openLibraryId, JsonHttpResponseHandler handler) {
        String url = getApiUrl("books/");
        httpClient.get(url + openLibraryId + ".json", handler);
    }

   // Open Library API Call 4 - get book cover by ISBN
   // Implemented in BookDisplayActivity & DisplayByIdActivity using Piccaso

   //  Open Library API Call 5 - Get on-line readable book
    public void getReadableBook(String openLibraryId, JsonHttpResponseHandler handler) {
        String url = getApiUrl("api/volumes/brief/olid/");
        httpClient.get(url + openLibraryId + ".json", handler);
    }
}
