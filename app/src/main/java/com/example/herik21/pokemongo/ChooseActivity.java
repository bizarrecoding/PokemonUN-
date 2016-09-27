package com.example.herik21.pokemongo;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.Random;

public class ChooseActivity extends AppCompatActivity {

    private ImageButton sq,ch,bu;
    private long selected = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        sq = (ImageButton)findViewById(R.id.Squirtle);
        ch = (ImageButton)findViewById(R.id.Charmander);
        bu = (ImageButton)findViewById(R.id.Bulbasaur);
    }

    public void onSquirtle(View view) {
        sq.setBackgroundResource(R.drawable.squirtle2);
        ch.setBackgroundResource(R.drawable.charmander);
        bu.setBackgroundResource(R.drawable.bulbasaur);
        selected = 5;
    }

    public void onCharmander(View view) {
        sq.setBackgroundResource(R.drawable.squirtle);
        ch.setBackgroundResource(R.drawable.charmander2);
        bu.setBackgroundResource(R.drawable.bulbasaur);
        selected = 3;
    }

    public void onBulbasaur(View view) {
        sq.setBackgroundResource(R.drawable.squirtle);
        ch.setBackgroundResource(R.drawable.charmander);
        bu.setBackgroundResource(R.drawable.bulbasaur2);
        selected = 1;
    }

    public void onSelect(View view) {
        if(selected!=0){
            new Delete().from(TeamPokemon.class);
            Log.d("Size"," = "+ new Select().from(TeamPokemon.class).count());
            Pokemon starter = new Select().from(Pokemon.class).where(Pokemon_Table.id.is(selected)).querySingle();
            Random r = new Random();
            int newHP = starter.hp_max / 2 + r.nextInt(starter.hp_max / 2);
            int newAtk = starter.hp_max / 2 + r.nextInt(starter.atk_max / 2);
            int newDef = starter.hp_max / 2 + r.nextInt(starter.def_max / 2);
            TeamPokemon tpk = new TeamPokemon(starter, newHP, newAtk, newDef);
            tpk.save();
            Intent i = new Intent(this, MapsActivity.class);
            startActivity(i);
        }

    }
}
