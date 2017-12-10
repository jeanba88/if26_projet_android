package fr.utt.if26.if26_sncfprojet;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;

public class SearchPlace extends AppCompatActivity {
    String apikey;
    EditText searchPlaceInput;
    ListView searchPlaceListView;
    Context context;
    ArrayList<GareClasse> gares = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);

        Intent mainIntent = getIntent();
        String type = mainIntent.getStringExtra("type");


        context = getApplicationContext();

        ImageButton searchPlaceButton = findViewById(R.id.searchplace_button);
        searchPlaceInput = findViewById(R.id.searchplace_input);
        searchPlaceListView = findViewById(R.id.searchplace_listView);
        searchPlaceButton.setOnClickListener(onClickListener);
        apikey = APIKeyClass.getKey(context);

        switch (type) {
            case "depart":
                searchPlaceInput.setHint(R.string.addDestination_textDepart_default_input);
                break;
            case "arrive":
                searchPlaceInput.setHint(R.string.addDestination_textDestination_default_input);
                break;
        }

        final GareArrayAdapter gareArrayAdapter = new GareArrayAdapter(context, gares);

        searchPlaceListView.setAdapter(gareArrayAdapter);

        searchPlaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GareClasse gare = (GareClasse) adapterView.getItemAtPosition(i);
                System.out.println(gare.toString());
            }
        });



    }
    IResult volleyCallback() {
        return new IResult() {
            @Override
            public void notifySuccess(JSONObject response) {
                gares.clear();
                gares.addAll(ParseJSON.getGares(response));
                System.out.println(gares);
                ((BaseAdapter) searchPlaceListView.getAdapter()).notifyDataSetChanged();
                //Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void notifyError(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
            }
        };
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = "https://api.sncf.com/v1/coverage/sncf/places?type[]=stop_area&q=" + searchPlaceInput.getText().toString();
            IResult callback = volleyCallback();
            VolleyService volleyService = new VolleyService(callback, context);
            volleyService.getData(url, apikey);

        }
    };
}
