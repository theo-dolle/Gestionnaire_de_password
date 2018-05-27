package com.programmation.safechest.ui.sampledata;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.programmation.safechest.R;
import com.programmation.safechest.sampledata.Compte;

import android.support.v7.widget.RecyclerView;
import android.widget.Toast;


import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

import static android.support.v4.content.ContextCompat.startActivity;


public class RecyclerCompte extends RealmRecyclerViewAdapter<Compte, RecyclerCompte.MyViewHolder> {

    String key;

    public RecyclerCompte(OrderedRealmCollection<Compte> data, String key) {
        super(data, true);
        this.key = key;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.compte_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Compte compte = getItem(position);
        holder.setCompte(compte, position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView contentView;
        Compte mCompte;
        Button mButton;

        MyViewHolder(View compteView) {
            super(compteView);
            contentView = compteView.findViewById(R.id.url);
            contentView.setOnClickListener(this);
        }

        void setCompte(Compte compte, int position) {
            this.mCompte = compte;
            mCompte.setKey(key);
            this.contentView.setText("URL : " + compte.getURL()+"\nIdentifiant : " + compte.getCompteLogin() + " \nMot de passe : " + compte.getUnencryptedPassword());
        }

        @Override
        public void onClick(View view) {
            View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_task, null);

            EditText loginText = dialogView.findViewById(R.id.login);
            EditText PasswordText = dialogView.findViewById(R.id.password);
            EditText UrlText = dialogView.findViewById(R.id.url);

            loginText.setText(mCompte.getCompteLogin());
            mCompte.setKey(key);PasswordText.setText(mCompte.getUnencryptedPassword());
            UrlText.setText(mCompte.getURL());

            new AlertDialog.Builder(view.getContext())
                .setTitle("Votre Mémo de compte")
                .setView(dialogView)
                .setNegativeButton("Fermer", null)
                    .setPositiveButton("Se rendre sur le site", (dialog, which) -> {

                        try {

                            String url = UrlText.getText().toString();
                            //On check que l'URL semble bien valide
                            if (!url.startsWith("http://www.") && !url.startsWith("https://www.")) {
                                url = "http://www." + url;
                            }
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(view.getContext(), i, null);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(view.getContext(),"Nous n'avons pas pu accéder au site, vérifiez que l'url est bien renseignée et valide",Toast.LENGTH_SHORT).show();
                        } catch (Exception e){
                            Toast.makeText(view.getContext(), "Essayez de vous connecter manuellement", Toast.LENGTH_SHORT).show();
                        }

                    })
                .create()
                .show();
        }
    }
}

