package com.example.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

public class PhotoViewFragment extends DialogFragment {
    private static final String ARG_PHOTO_FILE = "photo_file";

    public static PhotoViewFragment newInstance(File photoFile) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHOTO_FILE, photoFile);

        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        File photoFile = (File) getArguments().getSerializable(ARG_PHOTO_FILE);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_photo, null);

        ImageView imageView = v.findViewById(R.id.crime_photo_large);

        if (photoFile == null || !photoFile.exists()) {
            imageView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            imageView.setImageBitmap(bitmap);
        }

        return new AlertDialog.Builder(requireActivity())
                .setView(v)
                .create();
    }
}
