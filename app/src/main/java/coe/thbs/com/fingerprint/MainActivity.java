package coe.thbs.com.fingerprint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private Button biometricLoginButton;
    private KeyboardView mKeyboardView;
    private Handler handler = new Handler();
    private Executor executor = new Executor() {
        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    };
    private static final int LOCK_REQUEST_CODE = 221;
    private static final int SECURITY_SETTING_REQUEST_CODE = 233;
    private TextView textView;
    private static final String TAG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authenticateApp();


        biometricLoginButton = findViewById(R.id.biometric_login);


        biometricLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBiometricPrompt();

            }
        });
    }


    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your Biometric credentials")
                .setNegativeButtonText("Cancel")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                BiometricPrompt.CryptoObject authenticatedCryptoObject =
                        result.getCryptoObject();



            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // Displays the "log in" prompt.
        biometricPrompt.authenticate(promptInfo);
    }


    private void authenticateApp() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        Intent i = keyguardManager.createConfirmDeviceCredentialIntent("Unlock", "Authenticate using PIN or FingerPrint");

        try{
            startActivityForResult(i, LOCK_REQUEST_CODE);
        }catch (Exception e){
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            try{
                startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE);
            }
            catch (Exception ex){
                textView.setText(getResources().getString((R.string.setting_label)));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCK_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    //If screen lock authentication is success update text
                    textView.setText(getResources().getString(R.string.unlock_success));
                } else {
                    //If screen lock authentication is failed update text
                    textView.setText(getResources().getString(R.string.unlock_failed));
                }
                break;
            case SECURITY_SETTING_REQUEST_CODE:
                //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    Toast.makeText(this, getResources().getString(R.string.device_is_secure), Toast.LENGTH_SHORT).show();
                    authenticateApp();
                } else {
                    //If screen lock is not enabled just update text
                    textView.setText(getResources().getString(R.string.security_device_cancelled));
                }

                break;
        }
    }


    private boolean isDeviceSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure();

        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23

    }

    //On Click of button do authentication again
    public void authenticateAgain(View view) {
        authenticateApp();
    }

}