package com.aston.spendingtracker.pdf;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

//import android.os.FileUtils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aston.spendingtracker.R;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileSelectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileSelectorFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;

    private Button buttonBrowse;
    private TextView editTextPath;
    private Button buttonSubmitFile;
    private String selectedBank;

    private static final String LOG_TAG = "AndroidExample";

    private Uri uripath;

    public FileSelectorFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FileSelectorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileSelectorFragment newInstance(String param1, String param2) {
        FileSelectorFragment fragment = new FileSelectorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_file_selector, container, false);
        this.editTextPath = (TextView) rootView.findViewById(R.id.editText_path);
        this.buttonBrowse = (Button) rootView.findViewById(R.id.button_browse);


        Spinner spinner = (Spinner) rootView.findViewById(R.id.bank_group_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.banking_group_list, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);


        this.buttonBrowse.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                askPermissionAndBrowseFile();
            }

        });

        // Inflate the layout for this fragment
        return rootView;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.buttonSubmitFile = getView().findViewById(R.id.button_submit_file);
        this.buttonSubmitFile.setEnabled(false);

        this.buttonSubmitFile.setOnClickListener(v -> {
            try {
                stripText();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });


    }

    private void askPermissionAndBrowseFile()  {
        Log.d(getClass().toString(), "askPermissionAndBrowseFile------------------------------------");

        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23
            Log.d(getClass().toString(), "android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M------------------------------------");
            // Check if we have Call permission
            int permission = ActivityCompat.checkSelfPermission(this.getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.d(getClass().toString(), "don't have permission so prompt the user------------------------------------");
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_REQUEST_CODE_PERMISSION
                );
                return;
            }
        }

        this.doBrowseFile();
    }


    private void doBrowseFile()  {
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");
        //chooseFileIntent.setType("application/pdf");
        // Only return URIs that can be opened with ContentResolver
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
        startActivityForResult(chooseFileIntent, MY_RESULT_CODE_FILECHOOSER);
    }

    // When you have the request results

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_REQUEST_CODE_PERMISSION: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (CALL_PHONE).
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i( LOG_TAG,"Permission granted!");
                    Toast.makeText(this.getContext(), "Permission granted!", Toast.LENGTH_SHORT).show();

                    this.doBrowseFile();
                }
                // Cancelled or denied.
                else {
                    Log.i(LOG_TAG,"Permission denied!");
                    Toast.makeText(this.getContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case MY_RESULT_CODE_FILECHOOSER:
                if (resultCode == Activity.RESULT_OK ) {
                    if(data != null)  {
                        Uri fileUri = data.getData();
                        Log.i(LOG_TAG, "Uri: " + fileUri);

                        String filePath = null;
                        try {
                            filePath = FileUtils.getPath(this.getContext(),fileUri);
                            uripath = fileUri;
                            buttonSubmitFile.setEnabled(true);
                            //File file = new File(fileUri.getPath());
                        } catch (Exception e) {
                            Log.e(LOG_TAG,"Error: " + e);
                            Toast.makeText(this.getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                        this.editTextPath.setText(filePath);
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath()  {
        return this.editTextPath.getText().toString();
    }

    public Uri getPathURI()  {

        return uripath;
    }


    private void stripText() throws IOException, URISyntaxException, ParseException {
        String path = getPath();
        //Toast.makeText(this, "Path: " + path, Toast.LENGTH_LONG).show();

        if(selectedBank.equals("")){
            Toast.makeText(getActivity(), "Select a bank", Toast.LENGTH_SHORT).show();
        }
        else{
            try{


                Uri pathURI = getPathURI();
                PDFProcessor pdfProcessor;
                switch(selectedBank){
                    case "HSBC":
                        pdfProcessor = new HSBCPDFProcessor(getContext(), pathURI);
                        break;
                    default:
                        pdfProcessor = null;
                }

                if(pdfProcessor != null){
                    pdfProcessor.processPDF();

                    Toast.makeText(getActivity(), "Submitted", Toast.LENGTH_SHORT).show();
                    buttonSubmitFile.setEnabled(false);
                }
                else{
                    Toast.makeText(getActivity(), "Something went wrong, please upload appropriate statements.", Toast.LENGTH_SHORT).show();
                }

            }
            catch (IOException | ParseException ex){

                Toast.makeText(getActivity(), "Something went wrong, please upload appropriate statements.", Toast.LENGTH_SHORT).show();

            }
        }




        //pdftocsv.activateSequence(r);

//        // Create an adapter and supply the data to be displayed.
//        mAdapter = new RecyclerViewAdapter(this, pdfProcessor.getTransactionListItems());
//        // Connect the adapter with the RecyclerView.
//        mRecyclerView.setAdapter(mAdapter);
//        // Give the RecyclerView a default layout manager.
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String str = adapterView.getItemAtPosition(i).toString();

        selectedBank = str;

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
