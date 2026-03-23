package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (findViewById(R.id.detail_fragment_container) != null) {
            if (savedInstanceState == null) {
                setupWelcomeMessage();
            }
        }
    }

    private void setupWelcomeMessage() {
        int crimeCount = CrimeLab.get(this).getCrimes().size();
        boolean isEmpty = crimeCount == 0;

        Fragment welcome = WelcomeFragment.newInstance(isEmpty);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_fragment_container, welcome)
                .commit();
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.updateUI();
        }

        // If in two-pane mode, check if we need to show the welcome message (e.g., if crime was deleted)
        if (findViewById(R.id.detail_fragment_container) != null) {
            if (CrimeLab.get(this).getCrime(crime.getId()) == null) {
                setupWelcomeMessage();
            }
        }
    }
}
