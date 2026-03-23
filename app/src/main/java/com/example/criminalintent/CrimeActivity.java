package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;

import java.util.UUID;

public class CrimeActivity extends SingleFragmentActivity 
        implements CrimeFragment.Callbacks {
    private static final String EXTRA_CRIME_ID = "com.example.criminalintent.crime_id";
    private static final String EXTRA_IS_NEW_CRIME = "com.example.criminalintent.is_new_crime";

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimeActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        intent.putExtra(EXTRA_IS_NEW_CRIME, false);
        return intent;
    }

    public static Intent newCrimeIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, CrimeActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, UUID.randomUUID());
        intent.putExtra(EXTRA_IS_NEW_CRIME, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Fragment createFragment() {
        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        boolean isNewCrime = getIntent().getBooleanExtra(EXTRA_IS_NEW_CRIME, false);
        return CrimeFragment.newInstance(crimeId, isNewCrime);
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        // Implementation not required for single-pane activity
        // as the list will be updated when it resumes.
    }
}
