package com.digilens.usbfileaccess;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
        private static final int PICK_USB_FOLDER_REQUEST = 42;
        private static final String TAG = "USB_ACCESS";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Assuming your layout file is named activity_main.xml
            setContentView(R.layout.activity_main);

            Button openUsbButton = findViewById(R.id.open_usb_button);
            openUsbButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openUsbFolderPicker();
                }
            });
        }

        /**
         * Launches the system's file picker to allow the user to select a directory.
         * This will grant the app access to the entire contents of the selected folder,
         * which can be on a USB drive.
         */
        private void openUsbFolderPicker() {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, PICK_USB_FOLDER_REQUEST);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_USB_FOLDER_REQUEST && resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    // Get the URI of the folder the user selected.
                    Uri uri = data.getData();
                    Log.d(TAG, "Selected URI: " + uri.toString());

                    // Persist the URI permission so the app can access it across restarts.
                    persistUriPermission(uri);

                    // Now you can work with the URI, for example, list its files.
                    listFilesFromUri(uri);
                }
            }
        }

        /**
         * Persists the permission to access the selected URI.
         * This is crucial to retain access to the folder after the app is closed or the device reboots.
         */
        private void persistUriPermission(Uri uri) {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            // Take persistable URI permission. This needs to be done once per URI.
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
            Log.d(TAG, "Persisted permission for URI: " + uri.toString());
        }

        /**
         * An example method to demonstrate how to use the URI to read file names.
         * This method queries the ContentResolver for a list of children documents in the selected tree.
         */
        private void listFilesFromUri(Uri treeUri) {
            Log.d(TAG, "Listing files from URI: " + treeUri.toString());
            ContentResolver resolver = getContentResolver();

            try {
                // Get the DocumentId from the tree URI
                String documentId = DocumentsContract.getTreeDocumentId(treeUri);
                Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId);

                // Query the ContentResolver to get a cursor of documents
                try (Cursor cursor = resolver.query(childrenUri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        Log.d(TAG, "Files found:");
                        do {
                            String documentName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                            Log.d(TAG, "- " + documentName);
                        } while (cursor.moveToNext());
                    } else {
                        Log.d(TAG, "No files found in the directory.");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error listing files: " + e.getMessage());
            }
        }
}
