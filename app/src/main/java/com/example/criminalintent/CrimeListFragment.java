package com.example.criminalintent;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CrimeListFragment extends Fragment {
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private static final int CRIME_LIMIT = 10;

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }

        MenuItem newCrimeItem = menu.findItem(R.id.new_crime);
        int crimeCount = CrimeLab.get(requireActivity()).getCrimes().size();
        if (crimeCount >= CRIME_LIMIT) {
            newCrimeItem.setEnabled(false);
            if (newCrimeItem.getIcon() != null) {
                newCrimeItem.getIcon().setAlpha(128);
            }
        } else {
            newCrimeItem.setEnabled(true);
            if (newCrimeItem.getIcon() != null) {
                newCrimeItem.getIcon().setAlpha(255);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.new_crime) {
            int crimeCount = CrimeLab.get(requireActivity()).getCrimes().size();
            if (crimeCount >= CRIME_LIMIT) {
                Toast.makeText(requireActivity(), R.string.crime_limit_reached, Toast.LENGTH_SHORT).show();
                return true;
            }
            Crime crime = new Crime();
            CrimeLab.get(getActivity()).addCrime(crime);
            updateUI();
            mCallbacks.onCrimeSelected(crime);
            return true;
        }

        if (item.getItemId() == R.id.show_subtitle) {
            mSubtitleVisible = !mSubtitleVisible;
            requireActivity().invalidateOptionsMenu();
            updateSubtitle();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(requireActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(requireActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount,
                crimeCount);

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        if (mSubtitleVisible) {
            actionBar.setSubtitle(subtitle);
        } else {
            actionBar.setSubtitle(null);
        }
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Crime mCrime;
        private final TextView mTitleTextView;
        private final TextView mDateTextView;
        private final TextView mStatusTextView;
        private final ImageView mSolvedImageView;
        private final View mItemLayout;
        private final SimpleDateFormat mDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy hh:mm a", Locale.getDefault());

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));

            itemView.setOnClickListener(this);
            mItemLayout = itemView.findViewById(R.id.crime_item_layout);
            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mStatusTextView = itemView.findViewById(R.id.crime_status);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved_icon);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mDateFormat.format(mCrime.getDate()));
            
            if (mCrime.isSolved()) {
                mItemLayout.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green
                mStatusTextView.setText(R.string.crime_solved);
                mStatusTextView.setTextColor(Color.parseColor("#2E7D32")); // Dark Green
                mSolvedImageView.setVisibility(View.VISIBLE);
            } else {
                mItemLayout.setBackgroundColor(Color.TRANSPARENT);
                mStatusTextView.setText(R.string.crime_open);
                mStatusTextView.setTextColor(Color.GRAY);
                mSolvedImageView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }
}
