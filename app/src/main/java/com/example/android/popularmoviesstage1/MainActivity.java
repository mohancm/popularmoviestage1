package com.example.android.popularmoviesstage1;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmoviesstage1.Movie.MovieResult;
import com.example.android.popularmovies.R;
import com.example.android.popularmoviesstage1.utilities.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MovieGridAdapter.OnItemClickListener {
    private RecyclerView mRecyclerView;
    private MovieGridAdapter mAdapter;
    String baseBackDropUrl = "https://image.tmdb.org/t/p/w185";
    private View loading;
    private BottomNavigationView navigationView;

    private List<Movie> mMovieList;

   public static final String API_KEY = "Put your Api Key";
   private String getParameter;
   private static final String TOP_RATED = "top_rated";
   private static final String MOST_POPULAR = "popular";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getParameter = MOST_POPULAR;

        mMovieList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.rv_movie_posters);
        loading = findViewById(R.id.loading);
        navigationView = findViewById(R.id.navigation_view);

        fetchMovieList(getParameter);


        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.top_rated:
                        getParameter = TOP_RATED;
                        fetchMovieList(getParameter);
                        return true;

                    case R.id.most_popular:
                        getParameter = MOST_POPULAR;
                        fetchMovieList(getParameter);
                        return true;
                }
                return false;
            }
        };
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void fetchMovieList(String getParameter) {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        loading.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mAdapter = new MovieGridAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(MainActivity.this);
        List<Movie> movies = new ArrayList<>();

        for (int i=0; i< 100; i++) {
            movies.add(new Movie());
        }
        mAdapter.setmMovieList(movies);

        // Retrofit Library is used to pull the data base of Movies with the help of Api Interface
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.MOVIE_DB_BASE_MOVIE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
// connecting api interface to retrofit
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        retrofit2.Call<Movie.MovieResult> call = apiInterface.getMovieResults(getParameter, API_KEY);

        call.enqueue(new Callback<MovieResult>() {
            @Override
            public void onResponse(retrofit2.Call<Movie.MovieResult> call, Response<MovieResult> response) {
                Movie.MovieResult result = response.body();
                if (result !=null) {
                    mAdapter.setmMovieList(result.getResults());
                    mMovieList = result.getResults();
                    mRecyclerView.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Movie.MovieResult> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Fail to Load : ",t.getMessage());
            }
        });
    }
// adding intents access
    @Override
    public void onItemClick(int position) {
        Movie movie = mMovieList.get(position);
        Intent intent = new Intent(this, MovieDetailActivity.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(getApplicationContext(), android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        intent.putExtra("posterImage", movie.getPoster());
        intent.putExtra("backdrop", baseBackDropUrl + movie.getBackdrop());
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("description", movie.getDescription());
        intent.putExtra("userRating", movie.getUserRating());
        intent.putExtra("releaseDate", movie.getReleaseDate());
        startActivity(intent, options.toBundle());
    }

}
