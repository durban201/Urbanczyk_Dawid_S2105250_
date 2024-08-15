package com.example.urbanczyk_dawid_s2105250;

/*  Starter project for Mobile Platform Development in main diet 2023/2024
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 Dawid Urbanczyk
// Student ID           S2105250
// Programme of Study   Computing
//



import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String DRIVER_STANDINGS_URL = "http://ergast.com/api/f1/current/driverStandings";
    private static final String RACE_SCHEDULE_URL = "http://ergast.com/api/f1/current";
    private ListView listView;
    private TextView detailTextView;
    private List<String> driverStandingsList = new ArrayList<>();
    private List<String> raceScheduleList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private boolean isDriverStandings = true; // To toggle between standings and schedule

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        detailTextView = findViewById(R.id.detailTextView);

        fetchData(DRIVER_STANDINGS_URL); // Start with driver standings

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isDriverStandings) {
                    // Display detailed info for driver standings
                    displayDriverDetails(position);
                } else {
                    // Display detailed info for race schedule
                    displayRaceDetails(position);
                }
            }
        });

        // Toggle data display on button click (not included in starter code but recommended)
        findViewById(R.id.toggleButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDriverStandings) {
                    fetchData(RACE_SCHEDULE_URL);
                } else {
                    fetchData(DRIVER_STANDINGS_URL);
                }
                isDriverStandings = !isDriverStandings;
            }
        });
    }

    private void fetchData(final String urlString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    Log.d("FETCH_DATA", "Data fetched: " + response.toString());

                    if (urlString.equals(DRIVER_STANDINGS_URL)) {
                        parseDriverStandings(response.toString());
                    } else {
                        parseRaceSchedule(response.toString());
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateListView();
                        }
                    });

                } catch (Exception e) {
                    Log.e("MainActivity", "Error fetching data", e);
                }
            }
        }).start();
    }


    private void parseDriverStandings(String xmlData) {
        try {
            driverStandingsList.clear();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlData));

            int eventType = parser.getEventType();
            String driver = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase("DriverStanding")) {
                            driver = "";
                        }
                        break;

                    case XmlPullParser.TEXT:
                        driver += parser.getText() + " ";
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagName.equalsIgnoreCase("DriverStanding")) {
                            driverStandingsList.add(driver);
                            Log.d("PARSER", "Driver added: " + driver);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing driver standings", e);
        }
    }


    private void parseRaceSchedule(String xmlData) {
        try {
            raceScheduleList.clear();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlData));

            int eventType = parser.getEventType();
            String race = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase("Race")) {
                            race = "";
                        }
                        break;

                    case XmlPullParser.TEXT:
                        race += parser.getText() + " ";
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagName.equalsIgnoreCase("Race")) {
                            raceScheduleList.add(race);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing race schedule", e);
        }
    }

    private void updateListView() {
        if (isDriverStandings) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, driverStandingsList);
        } else {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, raceScheduleList);
        }
        listView.setAdapter(adapter);
    }

    private void displayDriverDetails(int position) {
        // This method should be customized based on actual XML tags for driver details
        String details = "Driver Details: " + driverStandingsList.get(position);
        detailTextView.setText(details);
    }

    private void displayRaceDetails(int position) {
        // This method should be customized based on actual XML tags for race details
        String details = "Race Details: " + raceScheduleList.get(position);
        detailTextView.setText(details);
    }


}
