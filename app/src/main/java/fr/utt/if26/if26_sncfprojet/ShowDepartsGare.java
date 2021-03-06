package fr.utt.if26.if26_sncfprojet;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ShowDepartsGare extends AppCompatActivity {
    List<DepartGareClass> departs = new ArrayList<>();
    ShowDepartsGare_RecycleView_Adapter adapter;
    Context context;
    String apikey;
    GareClasse garePref;
    DatabaseHelper db;
    Date lastDate = new Date();

    //Formatter date
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'kkmmss", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_departs_gare);

        context = getApplicationContext();
        db = new DatabaseHelper(context);
        Intent intent = getIntent();
        garePref = intent.getParcelableExtra("garepref");

        TextView nomGare = findViewById(R.id.showDepartsGare_NomGare);
        nomGare.setText(garePref.getName());
        apikey = APIKeyClasse.getKey(context);

        db = new DatabaseHelper(context);
        try {
            departs = db.getAllDepartGare(garePref.getId());
            System.out.println(departs);
            Iterator<DepartGareClass> iter = departs.iterator();
            while (iter.hasNext()) {
                DepartGareClass nd = iter.next();
                if(nd.getHeure_depart().before(new Date())) {
                    db.deleteDepartGare(nd.getId_depart());
                    iter.remove();
                }
            }
            if (departs.size() > 0) {
                DepartGareClass dernierTrajet = Collections.max(departs, new Comparator<DepartGareClass>() {
                    @Override
                    public int compare(DepartGareClass nextDepartureClass, DepartGareClass t1) {
                        if (nextDepartureClass.getHeure_depart().before(t1.getHeure_depart())) {
                            return -1;
                        } else if (nextDepartureClass.getHeure_depart().equals(t1.getHeure_depart())) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                });
                lastDate = new Date(dernierTrajet.getHeure_depart().getTime() + 60000);
            }
            if (departs.size() < 5) {
                searchInternetDeparture();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Recycler view
        RecyclerView rv = findViewById(R.id.showDepartsGare_recyclerView);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        rv.setLayoutManager(llm);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                llm.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        adapter = new ShowDepartsGare_RecycleView_Adapter(departs);
        rv.setAdapter(adapter);
    }

    IResult volleyCallback() {
        return new IResult() {
            @Override
            public void notifySuccess(JSONObject response) {
                System.out.println(response);
                List<DepartGareClass> newDeparts = ParseJSON.getDepartureGare(response, garePref);
                for (DepartGareClass newDepart: newDeparts) {
                    System.out.println(newDepart);
                    db.createDepartGare(newDepart);
                }
                departs.addAll(newDeparts);

                adapter.notifyDataSetChanged();
                System.out.println(departs);
                //Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void notifyError(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
            }
        };
    }
    void searchInternetDeparture() {
        Toast.makeText(context, "Recherche...", Toast.LENGTH_SHORT).show();
        String url = "https://api.sncf.com/v1/coverage/sncf/stop_areas/"
                + garePref.getId()
                + "/departures?data_freshness=realtime&from_datetime=" + formatter.format(lastDate);
        IResult callback = volleyCallback();
        VolleyService volleyService = new VolleyService(callback, context);
        volleyService.getData(url, apikey);
    }
}
