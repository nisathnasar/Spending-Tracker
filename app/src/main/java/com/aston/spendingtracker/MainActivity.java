package com.aston.spendingtracker;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aston.spendingtracker.ui.login.LoginActivity;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.multipdf.Splitter;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;

    private FileSelectorFragment fragment;
    private Button buttonShowInfo;

    private File root;
    private AssetManager assetManager;
    private TextView tv;
    private LinearLayout linearLayout;

    private final LinkedList<String> mWordList = new LinkedList<>();

    private RecyclerView mRecyclerView;
    private WordListAdapter mAdapter;

//    PyObject pyobj;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            signOut();
        }


        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.fragment = (FileSelectorFragment) fragmentManager.findFragmentById(R.id.fragment_fileChooser);

        this.buttonShowInfo = this.findViewById(R.id.button_showInfo);

        this.buttonShowInfo.setOnClickListener(v -> {
            try {
                stripText();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });


        for(int i = 0; i < 20; i++){
            mWordList.addLast("Word " + i);
        }

        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.recyclerview);


        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }

//        Python py = Python.getInstance();
//        pyobj = py.getModule("listoftransactions");

    }

    @Override
    protected void onStart() {
        super.onStart();
        setup();
    }

    /**
     * Initializes variables used for convenience
     */
    private void setup() {
        // Enable Android asset loading
        PDFBoxResourceLoader.init(getApplicationContext());
        // Find the root of the external storage.

        root = getApplicationContext().getCacheDir();
        assetManager = getAssets();
        //tv = (TextView) findViewById(R.id.stripped_tv);
    }

    private void stripText() throws IOException, URISyntaxException {
        String path = this.fragment.getPath();
        Toast.makeText(this, "Path: " + path, Toast.LENGTH_LONG).show();

        PDFBoxResourceLoader.init(getApplicationContext());

        activateSequence();
    }


    public void activateSequence() throws IOException {
        PDFtoCSV pdftocsv = new PDFtoCSV();
        //int numOfPagesToExtractFrom = numOfPagesToExtract;
        int numOfPagesToExtractFrom = 1;
//        String readFilePath = sourceFilePath;
//        File oldFile = new File(readFilePath);



        ParcelFileDescriptor r = getApplicationContext().getContentResolver().openFileDescriptor(this.fragment.getPathURI(), "r");

        InputStream fileStream = new FileInputStream(r.getFileDescriptor());

        PDDocument document = PDDocument.load(fileStream);

        //PDDocument document = PDDocument.load(assetManager.open("sample_stmt.pdf"));


//        PyObject obj = pyobj.callAttr("extract_text");
//        addTextView(obj.toString());



        Splitter splitter = new Splitter();
        List<PDDocument> splitPages = splitter.split(document);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(splitPages.get(0));
        String[] lines = text.split("\\r?\\n");
        List<String> rows = pdftocsv.synthesiseList(lines);


        // Remove the first irrelevant lines
        for(int i=30; i > 1; i-- ){
            rows.remove(rows.remove(rows.size()-1));
        }

        // Remove the last useless lines
        for(int i=2; i > -1; i--){
            rows.remove(i);
        }

        rows = pdftocsv.processLines(rows);
        pdftocsv.printList(rows);

        FileWriter fw = new FileWriter(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/Created.csv");

        //FileWriter fw = new FileWriter(root.getAbsolutePath() + "/Created.csv");
        Log.d("MainActivity.java", root.getAbsolutePath() + "/Created.csv");

        //write header
        fw.write("Date, Type, Details, Pay Out, Pay In, Balance\n");

        //addTextView("Date, Type, Details, Pay Out, Pay In, Balance\n");

        LinkedList<String> listTransaction = new LinkedList<>();

        //write to file
        for(String str : rows){
            fw.write(str+"\n");
            //addTextView(str+"\n");
            listTransaction.addLast(str+"\n");
        }

        //Page 2
        if(numOfPagesToExtractFrom>1){
            String text2 = stripper.getText(splitPages.get(1));
            String[] lines2 = text2.split("\\r?\\n");
            List<String> rows2 = pdftocsv.synthesiseList(lines2);

            /*
             * Remove the last irrelevant lines
             */
            for(int i=16; i > 1; i-- ){
                rows2.remove(rows2.remove(rows2.size()-1));
            }
            /*
             * Remove the first useless lines
             */
            for(int i=1; i > -1; i--){
                rows2.remove(i);
            }

            pdftocsv.processLines(rows2);
            pdftocsv.printList(rows2);

            //write to file
            for(String str : rows2){
                fw.write(str+"\n");
                //addTextView(str + "\n");
                listTransaction.addLast(str+"\n");
            }
        }

        //Page 3

        if(numOfPagesToExtractFrom>2){
            String text3 = stripper.getText(splitPages.get(2));
            String[] lines3 = text3.split("\\r?\\n");
            List<String> rows3 = pdftocsv.synthesiseList(lines3);

            /*
             * Remove the last irrelevant lines
             */
            for(int i=16; i > 1; i-- ){
                rows3.remove(rows3.remove(rows3.size()-1));
            }
            /*
             * Remove the first useless lines
             */
            for(int i=1; i > -1; i--){
                rows3.remove(i);
            }

            pdftocsv.processLines(rows3);
            pdftocsv.printList(rows3);

            //write to file
            for(String str : rows3){
                fw.write(str+"\n");
                //addTextView(str + "\n");
            }
        }

        //Page 3

        if(numOfPagesToExtractFrom>3){
            String text3 = stripper.getText(splitPages.get(3));
            String[] lines3 = text3.split("\\r?\\n");
            List<String> rows3 = pdftocsv.synthesiseList(lines3);

            /*
             * Remove the last irrelevant lines
             */
            for(int i=16; i > 1; i-- ){
                rows3.remove(rows3.remove(rows3.size()-1));
            }

            /*
             * Remove the first useless lines
             */
            for(int i=1; i > -1; i--){
                rows3.remove(i);
            }

            pdftocsv.processLines(rows3);
            pdftocsv.printList(rows3);

            //write to file
            for(String str : rows3){
                fw.write(str+"\n");
                //addTextView(str + "\n");
            }
        }

        document.close();
        fw.close();

        // Create an adapter and supply the data to be displayed.
        mAdapter = new WordListAdapter(this, listTransaction);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    private void addTextView(String text){
        TextView valueTV = new TextView(this);
        valueTV.setText(text);
        valueTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(valueTV);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            signOut();
        }

    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_logout:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                signOut();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menufile, menu);
        return true;
    }

}