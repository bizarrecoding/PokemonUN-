package com.example.herik21.pokemongo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;
import java.util.Objects;

public class TeamActivity extends AppCompatActivity {

    public ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv = (ListView)findViewById(R.id.listView);
        List<TeamPokemon> myTeam = new Select().from(TeamPokemon.class).queryList();
        CustomAdapter cAdapter = new CustomAdapter(this,myTeam);
        Log.d("size",""+cAdapter.getCount());
        lv.setAdapter(cAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
