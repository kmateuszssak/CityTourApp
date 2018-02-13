package com.example.mateusz.citytourapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mateusz.citytourapp.scheduler.JobSchedulerService;

import com.example.mateusz.citytourapp.tweeter.DataStoreClass;
import com.example.mateusz.citytourapp.tweeter.TwitterHelper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.auth.api.credentials.IdToken;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final int RC_SAVE = 1;
    private static final int RC_HINT = 2;
    private static final int RC_READ = 3;

    private CredentialsClient mCredentialsClient;
    private Credential mCurrentCredential;
    private boolean mIsResolving = false;

    private TwitterLoginButton m_aLoginButton;
    private TwitterHelper m_aTwitterObject;
    private FirebaseAuth mAuth;

    private static final String JOB_TAG = "scheduled_job";
    private boolean m_bFlag = false;
    private JobScheduler m_aScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_aTwitterObject = DataStoreClass.getGlobalTwitterHelper();

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
        }

        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();
        mCredentialsClient = Credentials.getClient(this, options);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Attempt auto-sign in.
        if (!mIsResolving) {
            requestCredentials();

            //Tylko raz pytamy o credentiale. Jak się nie udało to trzeba ręcznie się logować. Nie wiem dlaczego ale wszystko odpala się dwa razy.
            mIsResolving = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {

                //zamknięcie obecnego activity z FirebaseUI - ekran logowania
                finish();

                // TODO (opcjonalne) zapisanie Credential'i

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);

                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    return;
                }
            }
        }
    }

    private void requestCredentials() {

        // TODO naprawić/zrobić dobry request o Credential'e
        CredentialRequest mCredentialRequest = new CredentialRequest.Builder()
                //.setPasswordLoginSupported(true)
                .setAccountTypes(IdentityProviders.TWITTER)
                .build();

        mCredentialsClient.request(mCredentialRequest).addOnCompleteListener(
                new OnCompleteListener<CredentialRequestResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<CredentialRequestResponse> task) {

                        if (task.isSuccessful()) {
                            // Successfully read the credential without any user interaction, this
                            // means there was only a single credential and the user has auto
                            // sign-in enabled.
                            processRetrievedCredential(task.getResult().getCredential(), false);
                            return;
                        }

                        //Tylko raz pytamy o credentiale. Jak się nie udało to trzeba ręcznie się logować.
                        mIsResolving = true;

                        // Pierwsze logowanie
                        startFirebaseLogInScreen();
                    }
                });
    }

    private void processRetrievedCredential(Credential credential, boolean isHint) {
        Log.d(TAG, "Credential Retrieved: " + credential.getId());

        //TODO (opcjonalne) Jak uda się otrzymać automatycznie credentiale to tutaj po prostu trzeba się zalogować/przejść do następnego ekranu.

        // If the Credential is not a hint, we should store it an enable the delete button.
        // If it is a hint, skip this because a hint cannot be deleted.
        if (!isHint) {
            mCurrentCredential = credential;
        } else {
        }

        if (!credential.getIdTokens().isEmpty()) {
            IdToken idToken = credential.getIdTokens().get(0);

            TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
            m_aTwitterObject.setM_aSession(twitterSession);

            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        } else {
            // This state is reached if non-Google accounts are added to Gmail:
            // https://support.google.com/mail/answer/6078445
            Log.d(TAG, "Credential does not contain ID Tokens.");
        }
    }

    private void startFirebaseLogInScreen() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
/*                                new AuthUI.IdpConfig.PhoneBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.FacebookBuilder().build(),*/
                                new AuthUI.IdpConfig.TwitterBuilder().build()))
                        .setTheme(R.style.NoActionBarTheme)
                        .setIsSmartLockEnabled(true)
                        .setLogo(R.drawable.city_tour_app_logo)
                        .build(),
                RC_SIGN_IN);
    }

    public void schedule(View aView){
        if(!m_bFlag)
        {
            //Tworzymy Joba, który będzie wykonywany w tle przez serwis
            ComponentName aServiceName = new ComponentName(this, JobSchedulerService.class);
            JobInfo aJobInfo = new JobInfo.Builder(1, aServiceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(10000)
                    .setPersisted(true)
                    .build();

            this.m_aScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int nResult = m_aScheduler.schedule(aJobInfo);
            if (nResult == JobScheduler.RESULT_SUCCESS) {
                Toast.makeText(this, "Job został schedulowany...", Toast.LENGTH_SHORT).show();
            }

            m_bFlag = true;
        }
        else
        {
            this.m_aScheduler.cancelAll();
            Toast.makeText(this, "Job został zatrzymany...", Toast.LENGTH_SHORT).show();
            m_bFlag = false;
        }
    }
}
