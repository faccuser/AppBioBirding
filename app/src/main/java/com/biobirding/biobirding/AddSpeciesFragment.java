package com.biobirding.biobirding;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.biobirding.biobirding.helper.CustomSimpleDialog;
import com.biobirding.biobirding.webservice.SpeciesCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class AddSpeciesFragment extends Fragment {

    private EditText scientificName;
    private EditText notes;
    private JSONObject json;
    private Spinner spinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_species, container, false);

        this.spinner = view.findViewById(R.id.conservationStateList);

        if (getContext() != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.items, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spinner.setAdapter(adapter);
        }

        Button addSpecies = view.findViewById(R.id.addSpecies);
        this.scientificName = view.findViewById(R.id.scientific_name);
        this.notes = view.findViewById(R.id.notes);

        addSpecies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(validateFields()) {

                    new Thread() {

                        SpeciesCall speciesCall = new SpeciesCall(getContext());

                        @Override
                        public void run() {
                            try {

                                String conservationState = null;
                                if(spinner.getSelectedItemId() != 0){
                                    conservationState = spinner.getSelectedItem().toString();
                                }

                                json = speciesCall.insert(scientificName.getText().toString(), notes.getText().toString(), conservationState);

                                if (getActivity() != null) {

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (json.has("exception")) {
                                                try {
                                                    CustomSimpleDialog alert = new CustomSimpleDialog(getContext(), json.getString("exception"));
                                                    alert.show();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }else if (!json.has("authorized")) {
                                                startActivity(new Intent(getActivity(), LogoffActivity.class));
                                            }else {
                                                try {
                                                    CustomSimpleDialog alert = new CustomSimpleDialog(getContext(), json.getString("response"));
                                                    alert.show();
                                                    redirectActivity();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                                }

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }.start();
                }

            }
        });

        return view;
    }

    public void redirectActivity(){
        if(getFragmentManager() != null) {
            ListOfSpeciesFragment speciesLIstFragment = new ListOfSpeciesFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, speciesLIstFragment);
            transaction.commit();
        }
    }

    public boolean validateFields(){
        if(TextUtils.isEmpty(scientificName.getText().toString())){
            scientificName.setError(getString(R.string.requiredText));
            return false;
        }

        return true;
    }

}
