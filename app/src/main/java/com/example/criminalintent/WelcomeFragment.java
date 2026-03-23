package com.example.criminalintent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WelcomeFragment extends Fragment {

    private static final String ARG_IS_EMPTY = "is_empty";

    public static WelcomeFragment newInstance(boolean isEmpty) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_EMPTY, isEmpty);
        WelcomeFragment fragment = new WelcomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        TextView textView = view.findViewById(R.id.welcome_text_view);
        
        boolean isEmpty = getArguments() != null && getArguments().getBoolean(ARG_IS_EMPTY);
        if (isEmpty) {
            textView.setText(R.string.welcome_add_crime);
        } else {
            textView.setText(R.string.welcome_select_crime);
        }

        return view;
    }
}
