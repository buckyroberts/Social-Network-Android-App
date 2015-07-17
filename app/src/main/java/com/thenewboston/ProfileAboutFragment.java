package com.thenewboston;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileAboutFragment extends Fragment {
    JSONObject profileData;
    NBBaseActivity mActivity;

    private ImageView editBasicInfo;
    private ImageView editEducationInfo;
    private ImageView editEmployeeInfo;
    private ImageView editLinksInfo;
    private ImageView editContactInfo;

    private String mProfileId;
    private Boolean mIsEditable = false;

    public ProfileAboutFragment(){
        super();
    }

    public void setProfileData(JSONObject pProfileData) {
        this.profileData = pProfileData;
    }

    public JSONObject getProfileData() {
        return profileData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tab_profile_about,container,false);

        profileData = getProfileData();

        Bundle extras = getActivity().getIntent().getExtras();
        mProfileId = extras.getString("profileId");

        mActivity =(ProfileActivity) getActivity();
        try {
            if(mActivity.nbUserID.equals(mProfileId)) {
                mIsEditable = true;
            }

            editBasicInfo = (ImageView) view.findViewById(R.id.editBasicInfo);
            editEducationInfo = (ImageView) view.findViewById(R.id.editEducation);
            editEmployeeInfo = (ImageView) view.findViewById(R.id.editEmployment);
            editLinksInfo = (ImageView) view.findViewById(R.id.editLinks);
            editContactInfo = (ImageView) view.findViewById(R.id.editContact);

            if (!mIsEditable)
            {
                editBasicInfo.setVisibility(ImageView.GONE);
                editEducationInfo.setVisibility(ImageView.GONE);
                editEmployeeInfo.setVisibility(ImageView.GONE);
                editLinksInfo.setVisibility(ImageView.GONE);
                editContactInfo.setVisibility(ImageView.GONE);
            }

            editBasicInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mActivity.getApplicationContext(), InfoBasicActivity.class));
                }
            });

            editEducationInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mActivity.getApplicationContext(), InfoEducationActivity.class));
                }
            });

            editEmployeeInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mActivity.getApplicationContext(), InfoEmploymentActivity.class));
                }
            });

            editLinksInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mActivity.getApplicationContext(), InfoLinksActivity.class));
                }
            });

            editContactInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mActivity.getApplicationContext(), InfoContactActivity.class));
                }
            });

            //Basic Info
            RelativeLayout basicInfoLayout = (RelativeLayout)view.findViewById(R.id.profile_basic_info_layout);
            TableLayout basicInfoTable = (TableLayout)view.findViewById(R.id.profile_basic_info_table);
            if (profileData.has("BASIC_INFO")) {
                JSONArray basicInfo = profileData.getJSONArray("BASIC_INFO");

                for(int i=0; i < basicInfo.length(); i++) {
                    JSONObject info = (JSONObject) basicInfo.get(i);
                    TableRow genderRow = new TableRow(getActivity());

                    TextView textView1 = new TextView(getActivity());
                    textView1.setText(info.getString("label") + ":");

                    textView1.setPadding(0, 2, 24, 2);
                    textView1.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView1);

                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(info.getString("value"));
                    textView2.setPadding(2, 2, 2, 2);
                    textView2.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView2);

                    basicInfoTable.addView(genderRow, new TableLayout.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                }
                basicInfoLayout.setVisibility(View.VISIBLE);
            } else if(mIsEditable == false) {
                basicInfoLayout.setVisibility(View.GONE);
            }

            //Education
            RelativeLayout educationLayout = (RelativeLayout)view.findViewById(R.id.profile_education_layout);
            TableLayout educationTable = (TableLayout)view.findViewById(R.id.profile_education_table);
            if (profileData.has("EDUCATIONS")) {
                JSONArray education = profileData.getJSONArray("EDUCATIONS");

                for(int i=0; i < education.length(); i++) {
                    JSONObject info = (JSONObject) education.get(i);
                    TableRow genderRow = new TableRow(getActivity());

                    TextView textView1 = new TextView(getActivity());
                    textView1.setText(info.getString("school") + ":");

                    textView1.setPadding(0, 2, 24, 2);
                    textView1.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView1);

                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(info.getString("date"));
                    textView2.setPadding(2, 2, 2, 2);
                    textView2.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView2);

                    educationTable.addView(genderRow, new TableLayout.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                }
                educationLayout.setVisibility(View.VISIBLE);
            } else if(mIsEditable == false) {
                educationLayout.setVisibility(View.GONE);
            }

            //Employment
            RelativeLayout employmentLayout = (RelativeLayout)view.findViewById(R.id.profile_employment_layout);
            TableLayout employmentTable = (TableLayout)view.findViewById(R.id.profile_employment_table);
            if (profileData.has("EMPLOYMENTS")) {
                JSONArray employment = profileData.getJSONArray("EMPLOYMENTS");

                for(int i=0; i < employment.length(); i++) {
                    JSONObject info = (JSONObject) employment.get(i);
                    TableRow genderRow = new TableRow(getActivity());

                    TextView textView1 = new TextView(getActivity());
                    textView1.setText(info.getString("employer") + ":");

                    textView1.setPadding(0, 2, 24, 2);
                    textView1.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView1);

                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(info.getString("date"));
                    textView2.setPadding(2, 2, 2, 2);
                    textView2.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView2);

                    employmentTable.addView(genderRow, new TableLayout.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                }
                employmentLayout.setVisibility(View.VISIBLE);
            } else if(mIsEditable == false) {
                employmentLayout.setVisibility(View.GONE);
            }

            //Links
            RelativeLayout linksLayout = (RelativeLayout)view.findViewById(R.id.profile_links_layout);
            TableLayout linksTable = (TableLayout)view.findViewById(R.id.profile_links_table);
            if (profileData.has("LINKS")) {
                JSONArray links = profileData.getJSONArray("LINKS");

                for(int i=0; i < links.length(); i++) {
                    JSONObject info = (JSONObject) links.get(i);
                    TableRow genderRow = new TableRow(getActivity());

                    TextView textView1 = new TextView(getActivity());
                    textView1.setText(info.getString("title") + ":");

                    textView1.setPadding(0, 2, 24, 2);
                    textView1.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView1);

                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(info.getString("link"));
                    textView2.setPadding(2, 2, 2, 2);
                    textView2.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView2);

                    linksTable.addView(genderRow, new TableLayout.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                }
                linksLayout.setVisibility(View.VISIBLE);
            } else if(mIsEditable == false) {
                linksLayout.setVisibility(View.GONE);
            }

            //Contact
            RelativeLayout contactLayout = (RelativeLayout)view.findViewById(R.id.profile_contact_layout);
            TableLayout contactTable = (TableLayout)view.findViewById(R.id.profile_contact_table);

            Boolean hasContactInfo = false;
            if (profileData.has("EMAIL")) {
                String email = profileData.getString("EMAIL");
                TableRow genderRow = new TableRow(getActivity());

                TextView textView1 = new TextView(getActivity());
                textView1.setText("Email:");

                textView1.setPadding(0, 2, 24, 2);
                textView1.setTextColor(Color.parseColor("#555555"));
                genderRow.addView(textView1);

                TextView textView2 = new TextView(getActivity());
                textView2.setText(email);
                textView2.setPadding(2, 2, 2, 2);
                textView2.setTextColor(Color.parseColor("#555555"));
                genderRow.addView(textView2);

                contactTable.addView(genderRow, new TableLayout.LayoutParams(
                        TableRow.LayoutParams.FILL_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                hasContactInfo = true;
            }

            if (profileData.has("PHONES")) {
                JSONArray phones = profileData.getJSONArray("PHONES");

                for(int i=0; i < phones.length(); i++) {
                    JSONObject info = (JSONObject) phones.get(i);
                    TableRow genderRow = new TableRow(getActivity());

                    TextView textView1 = new TextView(getActivity());
                    textView1.setText(info.getString("label") + ":");

                    textView1.setPadding( 0, i == 0 && hasContactInfo ? 22 : 2, 24, 2);
                    textView1.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView1);

                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(info.getString("value"));
                    textView2.setPadding( 2, i == 0 && hasContactInfo ? 22 : 2, 2, 2);
                    textView2.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView2);

                    contactTable.addView(genderRow, new TableLayout.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                }

                hasContactInfo = true;
            }

            if (profileData.has("CONTACTS")) {
                JSONArray contacts = profileData.getJSONArray("CONTACTS");

                for(int i=0; i < contacts.length(); i++) {
                    JSONObject info = (JSONObject) contacts.get(i);
                    TableRow genderRow = new TableRow(getActivity());

                    TextView textView1 = new TextView(getActivity());
                    textView1.setText(info.getString("contact_type") + ":");

                    textView1.setPadding(0, i == 0 && hasContactInfo ? 22 : 2, 24, 2);
                    textView1.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView1);

                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(info.getString("contact_name"));
                    textView2.setPadding(2, i == 0 && hasContactInfo ? 22 : 2, 2, 2);
                    textView2.setTextColor(Color.parseColor("#555555"));
                    genderRow.addView(textView2);

                    contactTable.addView(genderRow, new TableLayout.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                }

                hasContactInfo = true;
            }

            if (hasContactInfo) {
                contactLayout.setVisibility(View.VISIBLE);
            }else if(mIsEditable == false){
                contactLayout.setVisibility(View.GONE);
            }

            ImageView editBasicInfo = (ImageView)view.findViewById(R.id.editBasicInfo);
            ImageView editEducation = (ImageView)view.findViewById(R.id.editEducation);
            ImageView editEmployment = (ImageView)view.findViewById(R.id.editEmployment);
            ImageView editLinks = (ImageView)view.findViewById(R.id.editLinks);
            ImageView editContact = (ImageView)view.findViewById(R.id.editContact);

            if (mIsEditable) {
                editBasicInfo.setVisibility(View.VISIBLE);
                editEducation.setVisibility(View.VISIBLE);
                editEmployment.setVisibility(View.VISIBLE);
                editLinks.setVisibility(View.VISIBLE);
                editContact.setVisibility(View.VISIBLE);
                editBasicInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.gotoOtherActivity(InfoBasicActivity.class, "", true);
                    }
                });
            } else {
                editBasicInfo.setVisibility(View.GONE);
                editEducation.setVisibility(View.GONE);
                editEmployment.setVisibility(View.GONE);
                editLinks.setVisibility(View.GONE);
                editContact.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

}