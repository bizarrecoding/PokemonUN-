package com.example.herik21.pokemongo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.Random;

public class BattleActivity extends AppCompatActivity {

    public TeamPokemon pk;
    public Pokemon bpk;
    public Pokemon wild;
    public int HP;
    public int twHP,wHP,newAtk,newDef;
    public TextView battleLog;
    public ImageView wildpk;
    public ImageView pkmon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        Intent i = getIntent();

        pkmon  = (ImageView) findViewById(R.id.mypkmn);
        pk = (TeamPokemon) i.getSerializableExtra("pokemon");
        bpk = pk.basePokemon;
        HP = pk.hp;

        wildpk = (ImageView) findViewById(R.id.wild);
        wild = (Pokemon) i.getSerializableExtra("wild");
        Random r = new Random();
        wHP = wild.hp_max / 2 + r.nextInt(wild.hp_max / 2);
        twHP = wHP;
        newAtk = wild.hp_max / 2 + r.nextInt(wild.atk_max / 2);
        newDef = wild.hp_max / 2 + r.nextInt(wild.def_max / 2);

        battleLog = (TextView)findViewById(R.id.battleLog);
        new DownloadImageTask(0).execute(pk.basePokemon.imgBack);
        new DownloadImageTask(1).execute(wild.imgFront);

        append("wild "+wild.name+" appeared!");
    }

    public void append(String entry){
        String log = battleLog.getText().toString();
        battleLog.setText(log+"\n"+entry);
    }
    public void wildAction(){
        append(wild.name+" attacks!");
        String wWeakness = bpk.weakness;
        String wStrength = bpk.strength;
        float multiplier = 1f;
        if(wWeakness.equals(wild.type)){
            multiplier = 2f;
        }
        if(wStrength.equals(wild.type)){
            multiplier = 0.5f;
        }
        int dmg = (int)((newAtk-pk.def)*multiplier);
        if(dmg>0){
            append(bpk.name+" took "+dmg+" damage!");
            HP = HP - dmg;
        }else{
            append(bpk.name+" took 1 damage!");
            HP = HP-1;
        }
        if(HP<=0){
            HP=0;
            append(wild.name+" Defeated "+bpk.name+"!");
            pkmon.setImageBitmap(null);
            prepareToExit(2,wild.name+" Defeated "+bpk.name+"!");
        }else{
            append(bpk.name+" current hp: "+HP);
        }
    }
    public void onAttack(View view){
        append(bpk.name+" attacks!");
        String wWeakness = wild.weakness;
        String wStrength = wild.strength;
        float multiplier = 1f;
        if(wWeakness.equals(bpk.type)){
            multiplier = 2f;
        }
        if(wStrength.equals(bpk.type)){
            multiplier = 0.5f;
        }
        int dmg = (int)((pk.atk-newDef)*multiplier);
        if(dmg>0){
            append(wild.name+" took "+dmg+" damage!");
            wHP = wHP-dmg;
        }else{
            append(wild.name+" took 1 damage!");
            wHP = wHP-1;
        }
        if(wHP>0){
            append(wild.name+" current hp: "+wHP);
            wildAction();
        }else{
            wHP=0;
            append(bpk.name+" Defeated "+wild.name+"!");
            wildpk.setImageBitmap(null);
            prepareToExit(1,bpk.name+" Defeated "+wild.name+"!");
        }
    }
    public void onPokeball(View view){
        int percent = (int)(100 - (float)(70*wHP/wHP));
        Random r = new Random();
        int p = r.nextInt(100);
        Log.d("CAPTURE","chance "+p+" in "+percent);
        if(p<percent){
            append("wild "+wild.name+" captured!");
            wildpk.setImageBitmap(null);
            prepareToExit(3,"wild "+wild.name+" captured!");
        }else{
            append("pokeball failed");
            wildAction();
        }
    }
    public void onPotion(View view){
        append(bpk.name+" heals 20HP");
        if(pk.hp > (HP+20)){
            HP = HP+20;
        }else{
            HP = pk.hp;
        }
        wildAction();
    }
    public void onRun(View view){
        append("Got away safely");
        prepareToExit(0,"Got away safely");
    }

    public void prepareToExit(final int i, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       boolean lose = true;
                       Intent in = getIntent();
                       if(i==1 || i==3){
                           lose=false;
                           if(i==3){
                               in.putExtra("captured",wild);
                               in.putExtra("bwild",getIntent().getSerializableExtra("wild"));
                               in.putExtra("hp",twHP);
                               in.putExtra("atk",newAtk);
                               in.putExtra("def",newDef);
                           }
                       }
                       in.putExtra("lose",lose);
                       setResult(Activity.RESULT_OK,in);
                       finish();
                   }
               });
        builder.create().show();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView target;

        public DownloadImageTask(int i) {
            if(i==0) {
                target = (ImageView) findViewById(R.id.mypkmn);
            }else{
                target = (ImageView) findViewById(R.id.wild);
            }
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new URL(imageURL).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            target.setImageBitmap(result);
            Log.d("HTTPGET-IMG","set result on view");
        }
    }
}
