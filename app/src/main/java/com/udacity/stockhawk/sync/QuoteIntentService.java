package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;

import timber.log.Timber;


// IntentService runs an operation on a single background thread
// This allows it to handle long-running operations without affecting your user interface's responsiveness.
// IntentService needs to be defined in the manifest
public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {

        super(QuoteIntentService.class.getSimpleName());
    }

    // onHandleIntent starts a process on a worker thread
    @Override
    protected void onHandleIntent(Intent intent) {      // do background work here
        // μπορώ μέσα στην intent να περάσω έξτρα data..
        // e.g. String action = intent.getAction()
        Timber.d("Intent handled");
        QuoteSyncJob.getQuotes(getApplicationContext());
    }
}
