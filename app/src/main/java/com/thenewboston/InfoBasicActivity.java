package com.thenewboston;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class InfoBasicActivity extends NBBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "basic";

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_basic);
        setNBToolbar();
        addNBNavigationMenu();

        List<String> years = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();
        final int curYear = calendar.get(Calendar.YEAR);
        years.add("-");
        for(int i=curYear - 100; i <= curYear; i++){
            years.add(Integer.toString(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner yearSpinner = (Spinner) findViewById(R.id.yearSpinner);

        yearSpinner.setAdapter(adapter);


        //Getting Initial Information
        NBRestAPIManager apiManager = new NBRestAPIManager("account", "getBasicInfo", nbUserApiToken, "post");

        NBRestAPIListener streamListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();
                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        JSONObject userInfo = pResponseData.getJSONObject("USER_INFO");

                        EditText firstName = (EditText)findViewById(R.id.firstName);
                        firstName.setText(userInfo.getString("firstName"));
                        
                        EditText lastName = (EditText)findViewById(R.id.lastName);
                        lastName.setText(userInfo.getString("lastName"));

                        //Birthday
                        Spinner yearSpinner = (Spinner)findViewById(R.id.yearSpinner);
                        if (userInfo.getInt("birthdate_year") > 0)
                            yearSpinner.setSelection(100 - (curYear - userInfo.getInt("birthdate_year")) + 1);
                        else
                            yearSpinner.setSelection(0);
                        
                        Spinner monthSpinner = (Spinner)findViewById(R.id.monthSpinner);
                        if (userInfo.getInt("birthdate_month") > 0)
                            monthSpinner.setSelection(userInfo.getInt("birthdate_month"));
                        else
                            monthSpinner.setSelection(0);
                        
                        Spinner daySpinner = (Spinner)findViewById(R.id.daySpinner);
                        if (userInfo.getInt("birthdate_day") > 0)
                            daySpinner.setSelection(userInfo.getInt("birthdate_day"));
                        else
                            daySpinner.setSelection(0);

                        //Birthday Toggle
                        ToggleButton birthdayToggle = (ToggleButton)findViewById(R.id.birthdayToggle);
                        birthdayToggle.setChecked(!userInfo.getString("birthdate_visibility").equals("1"));

                        //Gender
                        Spinner genderSpinner = (Spinner)findViewById(R.id.genderSpinner);
                        switch(userInfo.getString("gender"))
                        {
                            case "Male":
                                genderSpinner.setSelection(1);
                                break;
                            case "Female":
                                genderSpinner.setSelection(2);
                            default:
                                genderSpinner.setSelection(0);
                        }
                        //Gender Toggle
                        ToggleButton genderToggle = (ToggleButton)findViewById(R.id.genderToggle);
                        genderToggle.setChecked(!userInfo.getString("gender_visibility").equals("1"));

                        //Relationship Status
                        Spinner relationshipSpinner = (Spinner)findViewById(R.id.relationshipSpinner);
                        relationshipSpinner.setSelection(userInfo.getInt("relationship_status"));
                        //Relationship Status Toggle
                        ToggleButton relationshipToggle = (ToggleButton)findViewById(R.id.relationshipToggle);
                        relationshipToggle.setChecked(!userInfo.getString("relationship_status_visibility").equals("1"));

                        //Religion
                        EditText religionText = (EditText)findViewById(R.id.religion);
                        religionText.setText(userInfo.getString("religion"));
                        //Religion Toggle
                        ToggleButton religionToggle = (ToggleButton)findViewById(R.id.religionToggle);
                        religionToggle.setChecked(!userInfo.getString("religion_visibility").equals("1"));

                        //Political Views
                        EditText politicalText = (EditText)findViewById(R.id.political);
                        politicalText.setText(userInfo.getString("political_views"));
                        //Political Toggle
                        ToggleButton politicalToggle = (ToggleButton)findViewById(R.id.politicalToggle);
                        politicalToggle.setChecked(!userInfo.getString("political_views_visibility").equals("1"));

                        //Birthplace Views
                        EditText birthplaceText = (EditText)findViewById(R.id.birthplace);
                        birthplaceText.setText(userInfo.getString("birthplace"));
                        //Birthplace Toggle
                        ToggleButton birthplaceToggle = (ToggleButton)findViewById(R.id.birthplaceToggle);
                        birthplaceToggle.setChecked(!userInfo.getString("birthplace_visibility").equals("1"));

                        //Current City Views
                        EditText currentCityText = (EditText)findViewById(R.id.currentCity);
                        currentCityText.setText(userInfo.getString("current_city"));
                        //Current City Toggle
                        ToggleButton currentCityToggle = (ToggleButton)findViewById(R.id.currentCityToggle);
                        currentCityToggle.setChecked(!userInfo.getString("current_city_visibility").equals("1"));
                    }
                } catch (JSONException e) {
                    showPopupMessage(e.getMessage());
                }
            }

            @Override
            public void onFailure(String pError, int pStatusCode) {
                if(pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                    clearNBUserInfo();
                    gotoOtherActivity(LoginActivity.class, pError, true);
                }else {
                    hideOverlay();
                    showPopupMessage(pError);
                }
            }
        };

        apiManager.execute(streamListener);
    }

    public void saveInfo(View v)
    {
        Boolean tIsValid = true;

        cleanPopupMessages();

        EditText firstNameText = (EditText)findViewById(R.id.firstName);
        EditText lastNameText = (EditText)findViewById(R.id.lastName);

        String firstName = firstNameText.getText().toString().trim();
        String lastName = lastNameText.getText().toString().trim();
        System.out.print(firstName);
        System.out.print(lastName);
        if(TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName))
        {
            showPopupMessage("First Name and Last Name should not be empty.");
            return;
        }


        NBRestAPIManager apiManager = new NBRestAPIManager("account", "saveBasicInfo", nbUserApiToken, "post");

        apiManager.addField("firstName", firstName);
        apiManager.addField("lastName", lastName);

        Spinner birthYear = (Spinner)findViewById(R.id.yearSpinner);
        Spinner birthMonth = (Spinner)findViewById(R.id.monthSpinner);
        Spinner birthDay = (Spinner)findViewById(R.id.daySpinner);

        apiManager.addField("birthdate_year", birthYear.getSelectedItem().toString());
        apiManager.addField("birthdate_month", birthMonth.getSelectedItem().toString());
        apiManager.addField("birthdate_day", birthDay.getSelectedItem().toString());

        ToggleButton birthdateToggle = (ToggleButton)findViewById(R.id.birthdayToggle);
        apiManager.addField("birthdate_visibility", birthdateToggle.isChecked() ? "0" : "1");

        Spinner gender = (Spinner)findViewById(R.id.genderSpinner);
        apiManager.addField("gender", gender.getSelectedItem().toString());

        ToggleButton genderToggle = (ToggleButton)findViewById(R.id.genderToggle);
        apiManager.addField("gender_visibility", genderToggle.isChecked() ? "0" : "1");

        Spinner relationship = (Spinner)findViewById(R.id.relationshipSpinner);
        apiManager.addField("relationship_status", relationship.getSelectedItem().toString());

        ToggleButton relationshipToggle = (ToggleButton)findViewById(R.id.relationshipToggle);
        apiManager.addField("relationship_status_visibility", relationshipToggle.isChecked() ? "0" : "1");

        EditText religion = (EditText)findViewById(R.id.religion);
        apiManager.addField("religion", religion.getText().toString().trim());

        ToggleButton religionToggle = (ToggleButton)findViewById(R.id.religionToggle);
        apiManager.addField("religion_visibility", religionToggle.isChecked() ? "0" : "1");

        EditText political = (EditText)findViewById(R.id.political);
        apiManager.addField("political_views", political.getText().toString().trim());

        ToggleButton politicalToggle = (ToggleButton)findViewById(R.id.politicalToggle);
        apiManager.addField("political_views_visibility", politicalToggle.isChecked() ? "0" : "1");

        EditText birthplace = (EditText)findViewById(R.id.birthplace);
        apiManager.addField("birthplace", birthplace.getText().toString().trim());

        ToggleButton birthplaceToggle = (ToggleButton)findViewById(R.id.birthplaceToggle);
        apiManager.addField("birthplace_visibility", birthplaceToggle.isChecked() ? "0" : "1");

        EditText currentCity = (EditText)findViewById(R.id.currentCity);
        apiManager.addField("current_city", currentCity.getText().toString().trim());

        ToggleButton currentCityToggle = (ToggleButton)findViewById(R.id.currentCityToggle);
        apiManager.addField("current_city_visibility", currentCityToggle.isChecked() ? "0" : "1");

        showOverlay();

        NBRestAPIListener infoListener = new NBRestAPIListener(){
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        showPopupMessage("Your Basic Information has been updated successfully!");
                    }
                } catch (JSONException e) {
                    showPopupMessage(e.getMessage());
                }
            }

            @Override
            public void onFailure(String pError, int pStatusCode) {
                if(pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                    clearNBUserInfo();
                    gotoOtherActivity(LoginActivity.class, pError, true);
                }else {
                    hideOverlay();
                    showPopupMessage(pError);
                }
            }
        };

        apiManager.execute(infoListener);
    }

}
