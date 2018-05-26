package com.programmation.safechest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;
import com.programmation.safechest.sampledata.Compte;
import com.programmation.safechest.ui.sampledata.RecyclerCompte;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;


public class ListeComptesActivity extends AppCompatActivity {

    private Realm realm;
    private String password;

    public final static String PASSWORD = "com.programmation.safechest.Password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listecompte_activity);

        password = getIntent().getStringExtra(PASSWORD);

        setSupportActionBar(findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.fab).setOnClickListener(view -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task, null);
            EditText loginText = dialogView.findViewById(R.id.login);
            EditText PasswordText = dialogView.findViewById(R.id.password);
            EditText UrlText = dialogView.findViewById(R.id.url);
            new AlertDialog.Builder(ListeComptesActivity.this)
                .setTitle("Add a new site")
                .setMessage("Set url, id and passord !")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    realm.executeTransactionAsync(realm -> {
                        try {
                            Compte compte = new Compte();
                            compte.setLogin(loginText.getText().toString());
                            compte.setURL(UrlText.getText().toString());
                            compte.setPassword(PasswordText.getText().toString());
                            compte.setOwner(SyncUser.current().getIdentity());
                            realm.insert(compte);
                        } catch (Exception e){
                            setError("Un compte avec ce login existe déjà !");
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
        });

        OrderedRealmCollection<Compte> comptes = setUpRealm();

        final RecyclerCompte compteRecycler = new RecyclerCompte(comptes);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(compteRecycler);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                String id = compteRecycler.getItem(position).getCompteId();
                realm.executeTransactionAsync(realm -> {
                    Compte compte = realm.where(Compte.class)
                            .equalTo("CompteId", id)
                            .findFirst();
                    if (compte != null) {
                        compte.deleteFromRealm();
                    }
                });
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setError(String errorText){
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
    }

    private OrderedRealmCollection<Compte> setUpRealm() {
        try{
            Realm.setDefaultConfiguration(SyncConfiguration.automatic());
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        realm = Realm.getDefaultInstance();

        OrderedRealmCollection<Compte> comptes =  realm
                .where(Compte.class)
                .equalTo("owner", SyncUser.current().getIdentity())
                .sort("timestamp", Sort.DESCENDING)
                .findAllAsync();
        for(Compte compte: comptes)
            compte.setKey(password);

        return comptes;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_logout:

                SyncUser syncUser = SyncUser.current();
                if (syncUser != null) {
                    syncUser.logOut();
                    Intent intent = new Intent(this, EcranAccueil.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                return true;
            case R.id.home:
                // La ligne de code ci-dessous permet d'activité le bouton retour
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
