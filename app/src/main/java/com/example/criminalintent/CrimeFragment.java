package com.example.criminalintent;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String ARG_IS_NEW_CRIME = "is_new_crime";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_PHOTO = "DialogPhoto";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT_PERMISSION = 2;

    private Crime mCrime;
    private File mPhotoFile;
    private Uri mPhotoUri;
    private boolean mIsNewCrime;
    private boolean mWasAdded;
    private Callbacks mCallbacks;

    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mContactPoliceButton;
    private Button mAddCrimeButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    private final ActivityResultLauncher<Uri> mTakePhoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onPhotoCaptured);

    private final ActivityResultLauncher<Intent> mPickContact =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    loadContactData(contactUri);
                }
            });

    public static CrimeFragment newInstance(UUID crimeId) {
        return newInstance(crimeId, false);
    }

    public static CrimeFragment newInstance(UUID crimeId, boolean isNewCrime) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        args.putBoolean(ARG_IS_NEW_CRIME, isNewCrime);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
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
        UUID crimeId = (UUID) requireArguments().getSerializable(ARG_CRIME_ID);
        mIsNewCrime = requireArguments().getBoolean(ARG_IS_NEW_CRIME, false);

        if (mIsNewCrime) {
            mCrime = new Crime(crimeId);
        } else {
            mCrime = CrimeLab.get(requireActivity()).getCrime(crimeId);
        }

        if (mCrime == null) {
            mCrime = new Crime(crimeId);
            mIsNewCrime = true;
        }

        if (savedInstanceState != null) {
            mWasAdded = savedInstanceState.getBoolean("was_added", false);
        }

        mPhotoFile = CrimeLab.get(requireActivity()).getPhotoFile(mCrime);
        mPhotoUri = FileProvider.getUriForFile(
                requireActivity(),
                requireActivity().getPackageName() + ".fileprovider",
                mPhotoFile
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = view.findViewById(R.id.crime_title);
        mDateButton = view.findViewById(R.id.crime_date);
        mTimeButton = view.findViewById(R.id.crime_time);
        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mReportButton = view.findViewById(R.id.crime_report);
        mSuspectButton = view.findViewById(R.id.crime_suspect_btn);
        mContactPoliceButton = view.findViewById(R.id.crime_contact_police);
        mAddCrimeButton = view.findViewById(R.id.crime_add);
        mPhotoButton = view.findViewById(R.id.crime_camera);
        mPhotoView = view.findViewById(R.id.crime_photo);

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile != null && mPhotoFile.exists()) {
                    FragmentManager manager = getParentFragmentManager();
                    PhotoViewFragment dialog = PhotoViewFragment.newInstance(mPhotoFile);
                    dialog.show(manager, DIALOG_PHOTO);
                }
            }
        });

        mTitleField.setText(mCrime.getTitle());
        mSolvedCheckBox.setChecked(mCrime.isSolved());

        if (mCrime.getSuspect() != null && !mCrime.getSuspect().isEmpty()) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        updateDateAndTime();
        updatePhotoView();

        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mCrime.setTitle(charSequence.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                startActivity(Intent.createChooser(intent, getString(R.string.send_report)));
            }
        });

        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    mPickContact.launch(intent);
                }
            }
        });

        mContactPoliceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = mCrime.getSuspectPhone();
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                } else {
                    String suspect = mCrime.getSuspect();
                    if (suspect == null || suspect.isEmpty()) {
                        Toast.makeText(requireActivity(), R.string.no_suspect_selected, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireActivity(), getString(R.string.no_phone_number_for, suspect), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mTakePhoto.launch(mPhotoUri);
                } catch (ActivityNotFoundException exception) {
                    Toast.makeText(requireActivity(), R.string.no_camera_app, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (mIsNewCrime && !mWasAdded) {
            mAddCrimeButton.setVisibility(View.VISIBLE);
            mAddCrimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CrimeLab.get(requireActivity()).addCrime(mCrime);
                    mWasAdded = true;
                    updateCrime();
                    requireActivity().finish();
                }
            });
        } else {
            mAddCrimeButton.setVisibility(View.GONE);
        }

        return view;
    }

    private void loadContactData(Uri contactUri) {
        String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};
        try (Cursor c = requireActivity().getContentResolver().query(contactUri, queryFields, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                String suspect = c.getString(0);
                String contactId = c.getString(1);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);

                // Query for phone number using ID
                try (Cursor p = requireActivity().getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId}, null)) {
                    if (p != null && p.moveToFirst()) {
                        String phone = p.getString(0);
                        mCrime.setSuspectPhone(phone);
                    } else {
                        mCrime.setSuspectPhone("");
                    }
                }
                updateCrime();
            }
        } catch (Exception e) {
            Toast.makeText(requireActivity(), R.string.error_loading_contact, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                mPickContact.launch(intent);
            } else {
                Toast.makeText(requireActivity(), R.string.permission_denied_contacts, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_crime) {
            CrimeLab.get(requireActivity()).deleteCrime(mCrime);
            updateCrime();
            requireActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDateAndTime();
        } else if (requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateCrime();
            updateDateAndTime();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mIsNewCrime || mWasAdded) {
            CrimeLab.get(requireActivity()).updateCrime(mCrime);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("was_added", mWasAdded);
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePhotoView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPhotoView.setImageDrawable(null);
    }

    private void updateDateAndTime() {
        mDateButton.setText(DateFormat.getLongDateFormat(requireContext()).format(mCrime.getDate()));
        mTimeButton.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = DateFormat.getLongDateFormat(requireContext()).format(mCrime.getDate());

        String suspect = mCrime.getSuspect();
        String suspectString;
        if (suspect == null || suspect.isEmpty()) {
            suspectString = getString(R.string.crime_report_no_suspect);
        } else {
            suspectString = getString(R.string.crime_report_suspect, suspect);
        }

        return getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspectString);
    }

    private void onPhotoCaptured(Boolean didTakePhoto) {
        if (Boolean.TRUE.equals(didTakePhoto)) {
            updateCrime();
            updatePhotoView();
        }
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
            return;
        }

        int width = mPhotoView.getWidth();
        int height = mPhotoView.getHeight();

        if (width <= 0 || height <= 0) {
            mPhotoView.post(new Runnable() {
                @Override
                public void run() {
                    updatePhotoView();
                }
            });
            return;
        }

        Bitmap scaledBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), width, height);
        mPhotoView.setImageBitmap(scaledBitmap);
        mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
    }

    private void updateCrime() {
        CrimeLab.get(requireActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }
}
