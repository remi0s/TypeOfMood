/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package typeofmood.ime.latin;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import typeofmood.ime.ChooseMood;
import typeofmood.ime.accessibility.AccessibilityUtils;
import typeofmood.ime.annotations.UsedForTesting;
import typeofmood.ime.compat.BuildCompatUtils;
import typeofmood.ime.compat.EditorInfoCompatUtils;
import typeofmood.ime.compat.InputMethodServiceCompatUtils;
import typeofmood.ime.compat.ViewOutlineProviderCompatUtils;
import typeofmood.ime.compat.ViewOutlineProviderCompatUtils.InsetsUpdater;
import typeofmood.ime.datahandler.DatabaseHelper;
import typeofmood.ime.datahandler.KeyboardPayload;
import typeofmood.ime.dictionarypack.DictionaryPackConstants;
import typeofmood.ime.event.Event;
import typeofmood.ime.event.HardwareEventDecoder;
import typeofmood.ime.event.HardwareKeyboardEventDecoder;
import typeofmood.ime.event.InputTransaction;
import typeofmood.ime.keyboard.Keyboard;
import typeofmood.ime.keyboard.KeyboardActionListener;
import typeofmood.ime.keyboard.KeyboardId;
import typeofmood.ime.keyboard.KeyboardSwitcher;
import typeofmood.ime.keyboard.MainKeyboardView;
import typeofmood.ime.latin.common.Constants;
import typeofmood.ime.latin.common.CoordinateUtils;
import typeofmood.ime.latin.common.InputPointers;
import typeofmood.ime.latin.define.DebugFlags;
import typeofmood.ime.latin.define.ProductionFlags;
import typeofmood.ime.latin.inputlogic.InputLogic;
import typeofmood.ime.latin.permissions.PermissionsManager;
import typeofmood.ime.latin.personalization.PersonalizationHelper;
import typeofmood.ime.latin.settings.Settings;
import typeofmood.ime.latin.settings.SettingsActivity;
import typeofmood.ime.latin.settings.SettingsValues;
import typeofmood.ime.latin.suggestions.SuggestionStripView;
import typeofmood.ime.latin.suggestions.SuggestionStripViewAccessor;
import typeofmood.ime.latin.touchinputconsumer.GestureConsumer;
import typeofmood.ime.latin.utils.ApplicationUtils;
import typeofmood.ime.latin.utils.DialogUtils;
import typeofmood.ime.latin.utils.ImportantNoticeUtils;
import typeofmood.ime.latin.utils.JniUtils;
import typeofmood.ime.latin.utils.LeakGuardHandlerWrapper;
import typeofmood.ime.latin.utils.StatsUtils;
import typeofmood.ime.latin.utils.StatsUtilsManager;
import typeofmood.ime.latin.utils.SubtypeLocaleUtils;
import typeofmood.ime.latin.utils.UncachedInputMethodManagerUtils;
import typeofmood.ime.latin.utils.ViewLayoutUtils;
import typeofmood.ime.datahandler.KeyboardDynamics;
import typeofmood.ime.notificationhandler.NotificationHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import typeofmood.ime.R;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.*;


/**
 * Input method implementation for Qwerty'ish keyboard.
 */
public class LatinIME extends InputMethodService implements KeyboardActionListener,
        SuggestionStripView.Listener, SuggestionStripViewAccessor,
        DictionaryFacilitator.DictionaryInitializationListener,
        PermissionsManager.PermissionsResultCallback {

    //azure info
    public static String storageContainer = "";
    public static String storageConnectionString = "";

    public static KeyboardDynamics sessionData=null; //remi0s
    private static NotificationHelper mNotificationHelper;
    private static NotificationCompat.Builder nb;
//    private static NotificationHelperPhysical mNotificationHelperPhysical ;
    public static String currentMood="undefined";
    public static String currentPhysicalState="undefined";
    public static long latestNotificationTime;
    public static Date latestNotificationTimeTemp;
    public static Date latestSendTimeTemp;
    public static Date StopDateTimeTemp;
    public static Boolean laterPressed=false;
    public static Boolean isLongPressedFlag=false;
    public static DatabaseHelper myDB;
    public static String userID;
    public static ArrayList<Double>rawX=new ArrayList<Double>();
    public static ArrayList<Double>rawY=new ArrayList<Double>();
    public static double densX;
    public static double densY;


    public static int user_sessions=1;
    public static long user_min_flight=3000;
    public static long user_max_flight=0;
    public static float user_mean_flight=0;
    public static long user_min_hold=3000;
    public static long user_max_hold=0;
    public static float user_mean_hold=0;
    public static int sessionsCounter=0;






    static final String TAG = LatinIME.class.getSimpleName();
    private static final boolean TRACE = false;

    private static final int EXTENDED_TOUCHABLE_REGION_HEIGHT = 100;
    private static final int PERIOD_FOR_AUDIO_AND_HAPTIC_FEEDBACK_IN_KEY_REPEAT = 2;
    private static final int PENDING_IMS_CALLBACK_DURATION_MILLIS = 800;
    static final long DELAY_WAIT_FOR_DICTIONARY_LOAD_MILLIS = TimeUnit.SECONDS.toMillis(2);
    static final long DELAY_DEALLOCATE_MEMORY_MILLIS = TimeUnit.SECONDS.toMillis(10);

    /**
     * The name of the scheme used by the Package Manager to warn of a new package installation,
     * replacement or removal.
     */
    private static final String SCHEME_PACKAGE = "package";

    final Settings mSettings;
    private final DictionaryFacilitator mDictionaryFacilitator =
            DictionaryFacilitatorProvider.getDictionaryFacilitator(
                    false /* isNeededForSpellChecking */);
    final InputLogic mInputLogic = new InputLogic(this /* LatinIME */,
            this /* SuggestionStripViewAccessor */, mDictionaryFacilitator);
    // We expect to have only one decoder in almost all cases, hence the default capacity of 1.
    // If it turns out we need several, it will get grown seamlessly.
    final SparseArray<HardwareEventDecoder> mHardwareEventDecoders = new SparseArray<>(1);

    // TODO: Move these {@link View}s to {@link KeyboardSwitcher}.
    private View mInputView;
    private InsetsUpdater mInsetsUpdater;
    private SuggestionStripView mSuggestionStripView;

    private RichInputMethodManager mRichImm;
    @UsedForTesting
    final KeyboardSwitcher mKeyboardSwitcher;
    private final SubtypeState mSubtypeState = new SubtypeState();
    private EmojiAltPhysicalKeyDetector mEmojiAltPhysicalKeyDetector;
    private StatsUtilsManager mStatsUtilsManager;
    // Working variable for {@link #startShowingInputView()} and
    // {@link #onEvaluateInputViewShown()}.
    private boolean mIsExecutingStartShowingInputView;

    // Object for reacting to adding/removing a dictionary pack.
    private final BroadcastReceiver mDictionaryPackInstallReceiver =
            new DictionaryPackInstallBroadcastReceiver(this);

    private final BroadcastReceiver mDictionaryDumpBroadcastReceiver =
            new DictionaryDumpBroadcastReceiver(this);

    private AlertDialog mOptionsDialog;

    private final boolean mIsHardwareAcceleratedDrawingEnabled;

    private GestureConsumer mGestureConsumer = GestureConsumer.NULL_GESTURE_CONSUMER;

    public final UIHandler mHandler = new UIHandler(this);

    public static final class UIHandler extends LeakGuardHandlerWrapper<LatinIME> {
        private static final int MSG_UPDATE_SHIFT_STATE = 0;
        private static final int MSG_PENDING_IMS_CALLBACK = 1;
        private static final int MSG_UPDATE_SUGGESTION_STRIP = 2;
        private static final int MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP = 3;
        private static final int MSG_RESUME_SUGGESTIONS = 4;
        private static final int MSG_REOPEN_DICTIONARIES = 5;
        private static final int MSG_UPDATE_TAIL_BATCH_INPUT_COMPLETED = 6;
        private static final int MSG_RESET_CACHES = 7;
        private static final int MSG_WAIT_FOR_DICTIONARY_LOAD = 8;
        private static final int MSG_DEALLOCATE_MEMORY = 9;
        private static final int MSG_RESUME_SUGGESTIONS_FOR_START_INPUT = 10;
        private static final int MSG_SWITCH_LANGUAGE_AUTOMATICALLY = 11;
        // Update this when adding new messages
        private static final int MSG_LAST = MSG_SWITCH_LANGUAGE_AUTOMATICALLY;

        private static final int ARG1_NOT_GESTURE_INPUT = 0;
        private static final int ARG1_DISMISS_GESTURE_FLOATING_PREVIEW_TEXT = 1;
        private static final int ARG1_SHOW_GESTURE_FLOATING_PREVIEW_TEXT = 2;
        private static final int ARG2_UNUSED = 0;
        private static final int ARG1_TRUE = 1;

        private int mDelayInMillisecondsToUpdateSuggestions;
        private int mDelayInMillisecondsToUpdateShiftState;



        public UIHandler(@Nonnull final LatinIME ownerInstance) {
            super(ownerInstance);
        }

        public void onCreate() {
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }


            final Resources res = latinIme.getResources();
            mDelayInMillisecondsToUpdateSuggestions = res.getInteger(
                    R.integer.config_delay_in_milliseconds_to_update_suggestions);
            mDelayInMillisecondsToUpdateShiftState = res.getInteger(
                    R.integer.config_delay_in_milliseconds_to_update_shift_state);

        }

        @Override
        public void handleMessage(final Message msg) {
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }
            final KeyboardSwitcher switcher = latinIme.mKeyboardSwitcher;
            switch (msg.what) {
            case MSG_UPDATE_SUGGESTION_STRIP:
                cancelUpdateSuggestionStrip();
                latinIme.mInputLogic.performUpdateSuggestionStripSync(
                        latinIme.mSettings.getCurrent(), msg.arg1 /* inputStyle */);
                break;
            case MSG_UPDATE_SHIFT_STATE:
                switcher.requestUpdatingShiftState(latinIme.getCurrentAutoCapsState(),
                        latinIme.getCurrentRecapitalizeState());
                break;
            case MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP:
                if (msg.arg1 == ARG1_NOT_GESTURE_INPUT) {
                    final SuggestedWords suggestedWords = (SuggestedWords) msg.obj;
                    latinIme.showSuggestionStrip(suggestedWords);
                } else {
                    latinIme.showGesturePreviewAndSuggestionStrip((SuggestedWords) msg.obj,
                            msg.arg1 == ARG1_DISMISS_GESTURE_FLOATING_PREVIEW_TEXT);
                }
                break;
            case MSG_RESUME_SUGGESTIONS:
                latinIme.mInputLogic.restartSuggestionsOnWordTouchedByCursor(
                        latinIme.mSettings.getCurrent(), false /* forStartInput */,
                        latinIme.mKeyboardSwitcher.getCurrentKeyboardScriptId());
                break;
            case MSG_RESUME_SUGGESTIONS_FOR_START_INPUT:
                latinIme.mInputLogic.restartSuggestionsOnWordTouchedByCursor(
                        latinIme.mSettings.getCurrent(), true /* forStartInput */,
                        latinIme.mKeyboardSwitcher.getCurrentKeyboardScriptId());
                break;
            case MSG_REOPEN_DICTIONARIES:
                // We need to re-evaluate the currently composing word in case the script has
                // changed.
                postWaitForDictionaryLoad();
                latinIme.resetDictionaryFacilitatorIfNecessary();
                break;
            case MSG_UPDATE_TAIL_BATCH_INPUT_COMPLETED:
                final SuggestedWords suggestedWords = (SuggestedWords) msg.obj;
                latinIme.mInputLogic.onUpdateTailBatchInputCompleted(
                        latinIme.mSettings.getCurrent(),
                        suggestedWords, latinIme.mKeyboardSwitcher);
                latinIme.onTailBatchInputResultShown(suggestedWords);
                break;
            case MSG_RESET_CACHES:
                final SettingsValues settingsValues = latinIme.mSettings.getCurrent();
                if (latinIme.mInputLogic.retryResetCachesAndReturnSuccess(
                        msg.arg1 == ARG1_TRUE /* tryResumeSuggestions */,
                        msg.arg2 /* remainingTries */, this /* handler */)) {
                    // If we were able to reset the caches, then we can reload the keyboard.
                    // Otherwise, we'll do it when we can.
                    latinIme.mKeyboardSwitcher.loadKeyboard(latinIme.getCurrentInputEditorInfo(),
                            settingsValues, latinIme.getCurrentAutoCapsState(),
                            latinIme.getCurrentRecapitalizeState());
                }
                break;
            case MSG_WAIT_FOR_DICTIONARY_LOAD:
                Log.i(TAG, "Timeout waiting for dictionary load");
                break;
            case MSG_DEALLOCATE_MEMORY:
                latinIme.deallocateMemory();
                break;
            case MSG_SWITCH_LANGUAGE_AUTOMATICALLY:
                latinIme.switchLanguage((InputMethodSubtype)msg.obj);
                break;
            }
        }

        public void postUpdateSuggestionStrip(final int inputStyle) {
            sendMessageDelayed(obtainMessage(MSG_UPDATE_SUGGESTION_STRIP, inputStyle,
                    0 /* ignored */), mDelayInMillisecondsToUpdateSuggestions);
        }

        public void postReopenDictionaries() {
            sendMessage(obtainMessage(MSG_REOPEN_DICTIONARIES));
        }

        private void postResumeSuggestionsInternal(final boolean shouldDelay,
                final boolean forStartInput) {
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }
            if (!latinIme.mSettings.getCurrent().isSuggestionsEnabledPerUserSettings()) {
                return;
            }
            removeMessages(MSG_RESUME_SUGGESTIONS);
            removeMessages(MSG_RESUME_SUGGESTIONS_FOR_START_INPUT);
            final int message = forStartInput ? MSG_RESUME_SUGGESTIONS_FOR_START_INPUT
                    : MSG_RESUME_SUGGESTIONS;
            if (shouldDelay) {
                sendMessageDelayed(obtainMessage(message),
                        mDelayInMillisecondsToUpdateSuggestions);
            } else {
                sendMessage(obtainMessage(message));
            }
        }

        public void postResumeSuggestions(final boolean shouldDelay) {
            postResumeSuggestionsInternal(shouldDelay, false /* forStartInput */);
        }

        public void postResumeSuggestionsForStartInput(final boolean shouldDelay) {
            postResumeSuggestionsInternal(shouldDelay, true /* forStartInput */);
        }

        public void postResetCaches(final boolean tryResumeSuggestions, final int remainingTries) {
            removeMessages(MSG_RESET_CACHES);
            sendMessage(obtainMessage(MSG_RESET_CACHES, tryResumeSuggestions ? 1 : 0,
                    remainingTries, null));
        }

        public void postWaitForDictionaryLoad() {
            sendMessageDelayed(obtainMessage(MSG_WAIT_FOR_DICTIONARY_LOAD),
                    DELAY_WAIT_FOR_DICTIONARY_LOAD_MILLIS);
        }

        public void cancelWaitForDictionaryLoad() {
            removeMessages(MSG_WAIT_FOR_DICTIONARY_LOAD);
        }

        public boolean hasPendingWaitForDictionaryLoad() {
            return hasMessages(MSG_WAIT_FOR_DICTIONARY_LOAD);
        }

        public void cancelUpdateSuggestionStrip() {
            removeMessages(MSG_UPDATE_SUGGESTION_STRIP);
        }

        public boolean hasPendingUpdateSuggestions() {
            return hasMessages(MSG_UPDATE_SUGGESTION_STRIP);
        }

        public boolean hasPendingReopenDictionaries() {
            return hasMessages(MSG_REOPEN_DICTIONARIES);
        }

        public void postUpdateShiftState() {
            removeMessages(MSG_UPDATE_SHIFT_STATE);
            sendMessageDelayed(obtainMessage(MSG_UPDATE_SHIFT_STATE),
                    mDelayInMillisecondsToUpdateShiftState);
        }

        public void postDeallocateMemory() {
            sendMessageDelayed(obtainMessage(MSG_DEALLOCATE_MEMORY),
                    DELAY_DEALLOCATE_MEMORY_MILLIS);
        }

        public void cancelDeallocateMemory() {
            removeMessages(MSG_DEALLOCATE_MEMORY);
        }

        public boolean hasPendingDeallocateMemory() {
            return hasMessages(MSG_DEALLOCATE_MEMORY);
        }

        @UsedForTesting
        public void removeAllMessages() {
            for (int i = 0; i <= MSG_LAST; ++i) {
                removeMessages(i);
            }
        }

        public void showGesturePreviewAndSuggestionStrip(final SuggestedWords suggestedWords,
                final boolean dismissGestureFloatingPreviewText) {
            removeMessages(MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP);
            final int arg1 = dismissGestureFloatingPreviewText
                    ? ARG1_DISMISS_GESTURE_FLOATING_PREVIEW_TEXT
                    : ARG1_SHOW_GESTURE_FLOATING_PREVIEW_TEXT;
            obtainMessage(MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP, arg1,
                    ARG2_UNUSED, suggestedWords).sendToTarget();
        }

        public void showSuggestionStrip(final SuggestedWords suggestedWords) {
            removeMessages(MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP);
            obtainMessage(MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP,
                    ARG1_NOT_GESTURE_INPUT, ARG2_UNUSED, suggestedWords).sendToTarget();
        }

        public void showTailBatchInputResult(final SuggestedWords suggestedWords) {
            obtainMessage(MSG_UPDATE_TAIL_BATCH_INPUT_COMPLETED, suggestedWords).sendToTarget();
        }

        public void postSwitchLanguage(final InputMethodSubtype subtype) {
            obtainMessage(MSG_SWITCH_LANGUAGE_AUTOMATICALLY, subtype).sendToTarget();
        }

        // Working variables for the following methods.
        private boolean mIsOrientationChanging;
        private boolean mPendingSuccessiveImsCallback;
        private boolean mHasPendingStartInput;
        private boolean mHasPendingFinishInputView;
        private boolean mHasPendingFinishInput;
        private EditorInfo mAppliedEditorInfo;

        public void startOrientationChanging() {
            removeMessages(MSG_PENDING_IMS_CALLBACK);
            resetPendingImsCallback();
            mIsOrientationChanging = true;
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }
            if (latinIme.isInputViewShown()) {
                latinIme.mKeyboardSwitcher.saveKeyboardState();
            }
        }

        private void resetPendingImsCallback() {
            mHasPendingFinishInputView = false;
            mHasPendingFinishInput = false;
            mHasPendingStartInput = false;
        }

        private void executePendingImsCallback(final LatinIME latinIme, final EditorInfo editorInfo,
                boolean restarting) {
            if (mHasPendingFinishInputView) {
                latinIme.onFinishInputViewInternal(mHasPendingFinishInput);
            }
            if (mHasPendingFinishInput) {
                latinIme.onFinishInputInternal();
            }
            if (mHasPendingStartInput) {
                latinIme.onStartInputInternal(editorInfo, restarting);
            }
            resetPendingImsCallback();
        }

        public void onStartInput(final EditorInfo editorInfo, final boolean restarting) {
            if (hasMessages(MSG_PENDING_IMS_CALLBACK)) {
                // Typically this is the second onStartInput after orientation changed.
                mHasPendingStartInput = true;
            } else {
                if (mIsOrientationChanging && restarting) {
                    // This is the first onStartInput after orientation changed.
                    mIsOrientationChanging = false;
                    mPendingSuccessiveImsCallback = true;
                }
                final LatinIME latinIme = getOwnerInstance();
                if (latinIme != null) {
                    executePendingImsCallback(latinIme, editorInfo, restarting);
                    latinIme.onStartInputInternal(editorInfo, restarting);
                }
            }
        }

        public void onStartInputView(final EditorInfo editorInfo, final boolean restarting) {
            if (hasMessages(MSG_PENDING_IMS_CALLBACK)
                    && KeyboardId.equivalentEditorInfoForKeyboard(editorInfo, mAppliedEditorInfo)) {
                // Typically this is the second onStartInputView after orientation changed.
                resetPendingImsCallback();
            } else {
                if (mPendingSuccessiveImsCallback) {
                    // This is the first onStartInputView after orientation changed.
                    mPendingSuccessiveImsCallback = false;
                    resetPendingImsCallback();
                    sendMessageDelayed(obtainMessage(MSG_PENDING_IMS_CALLBACK),
                            PENDING_IMS_CALLBACK_DURATION_MILLIS);
                }
                final LatinIME latinIme = getOwnerInstance();
                if (latinIme != null) {
                    executePendingImsCallback(latinIme, editorInfo, restarting);
                    latinIme.onStartInputViewInternal(editorInfo, restarting);
                    mAppliedEditorInfo = editorInfo;
                }
                cancelDeallocateMemory();
            }
        }

        public void onFinishInputView(final boolean finishingInput) {
            if (hasMessages(MSG_PENDING_IMS_CALLBACK)) {
                // Typically this is the first onFinishInputView after orientation changed.
                mHasPendingFinishInputView = true;
            } else {
                final LatinIME latinIme = getOwnerInstance();
                if (latinIme != null) {
                    latinIme.onFinishInputViewInternal(finishingInput);
                    mAppliedEditorInfo = null;
                }
                if (!hasPendingDeallocateMemory()) {
                    postDeallocateMemory();
                }
            }



        }

        public void onFinishInput() {
            if (hasMessages(MSG_PENDING_IMS_CALLBACK)) {
                // Typically this is the first onFinishInput after orientation changed.
                mHasPendingFinishInput = true;
            } else {
                final LatinIME latinIme = getOwnerInstance();
                if (latinIme != null) {
                    executePendingImsCallback(latinIme, null, false);
                    latinIme.onFinishInputInternal();
                }
            }
        }
    }

    static final class SubtypeState {
        private InputMethodSubtype mLastActiveSubtype;
        private boolean mCurrentSubtypeHasBeenUsed;

        public void setCurrentSubtypeHasBeenUsed() {
            mCurrentSubtypeHasBeenUsed = true;
        }

        public void switchSubtype(final IBinder token, final RichInputMethodManager richImm) {
            final InputMethodSubtype currentSubtype = richImm.getInputMethodManager()
                    .getCurrentInputMethodSubtype();
            final InputMethodSubtype lastActiveSubtype = mLastActiveSubtype;
            final boolean currentSubtypeHasBeenUsed = mCurrentSubtypeHasBeenUsed;
            if (currentSubtypeHasBeenUsed) {
                mLastActiveSubtype = currentSubtype;
                mCurrentSubtypeHasBeenUsed = false;
            }
            if (currentSubtypeHasBeenUsed
                    && richImm.checkIfSubtypeBelongsToThisImeAndEnabled(lastActiveSubtype)
                    && !currentSubtype.equals(lastActiveSubtype)) {
                richImm.setInputMethodAndSubtype(token, lastActiveSubtype);
                return;
            }
            richImm.switchToNextInputMethod(token, true /* onlyCurrentIme */);
        }
    }

    // Loading the native library eagerly to avoid unexpected UnsatisfiedLinkError at the initial
    // JNI call as much as possible.
    static {
        JniUtils.loadNativeLibrary();
    }

    public LatinIME() {
        super();
        mSettings = Settings.getInstance();
        mKeyboardSwitcher = KeyboardSwitcher.getInstance();
        mStatsUtilsManager = StatsUtilsManager.getInstance();
        mIsHardwareAcceleratedDrawingEnabled =
                InputMethodServiceCompatUtils.enableHardwareAcceleration(this);
        Log.i(TAG, "Hardware accelerated drawing: " + mIsHardwareAcceleratedDrawingEnabled);
    }

    @Override
    public void onCreate() {


        storageContainer=getConfigValue(this, "storageContainer");
        storageConnectionString=getConfigValue(this, "storageConnectionString");
        densX=getResources().getDisplayMetrics().xdpi;
        densY=getResources().getDisplayMetrics().ydpi;

        myDB = new DatabaseHelper(this); //remi0s
        mNotificationHelper=new NotificationHelper(this);
        nb=new NotificationCompat.Builder(getApplicationContext(),"TypeOfMoodChannelID");
        userID=android.provider.Settings.System.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);//remi0s

        Settings.init(this);
        DebugFlags.init(PreferenceManager.getDefaultSharedPreferences(this));
        RichInputMethodManager.init(this);
        mRichImm = RichInputMethodManager.getInstance();
        KeyboardSwitcher.init(this);
        AudioAndHapticFeedbackManager.init(this);
        AccessibilityUtils.init(this);
        mStatsUtilsManager.onCreate(this /* context */, mDictionaryFacilitator);
        super.onCreate();

        mHandler.onCreate();

        // TODO: Resolve mutual dependencies of {@link #loadSettings()} and
        // {@link #resetDictionaryFacilitatorIfNecessary()}.
        loadSettings();
        resetDictionaryFacilitatorIfNecessary();

        // Register to receive ringer mode change.
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(mRingerModeChangeReceiver, filter);

        // Register to receive installation and removal of a dictionary pack.
        final IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addDataScheme(SCHEME_PACKAGE);
        registerReceiver(mDictionaryPackInstallReceiver, packageFilter);

        final IntentFilter newDictFilter = new IntentFilter();
        newDictFilter.addAction(DictionaryPackConstants.NEW_DICTIONARY_INTENT_ACTION);
        registerReceiver(mDictionaryPackInstallReceiver, newDictFilter);

        final IntentFilter dictDumpFilter = new IntentFilter();
        dictDumpFilter.addAction(DictionaryDumpBroadcastReceiver.DICTIONARY_DUMP_INTENT_ACTION);
        registerReceiver(mDictionaryDumpBroadcastReceiver, dictDumpFilter);

        StatsUtils.onCreate(mSettings.getCurrent(), mRichImm);
    }

    // Has to be package-visible for unit tests
    @UsedForTesting
    void loadSettings() {
        final Locale locale = mRichImm.getCurrentSubtypeLocale();
        final EditorInfo editorInfo = getCurrentInputEditorInfo();
        final InputAttributes inputAttributes = new InputAttributes(
                editorInfo, isFullscreenMode(), getPackageName());
        mSettings.loadSettings(this, locale, inputAttributes);
        final SettingsValues currentSettingsValues = mSettings.getCurrent();
        AudioAndHapticFeedbackManager.getInstance().onSettingsChanged(currentSettingsValues);
        // This method is called on startup and language switch, before the new layout has
        // been displayed. Opening dictionaries never affects responsivity as dictionaries are
        // asynchronously loaded.
        if (!mHandler.hasPendingReopenDictionaries()) {
            resetDictionaryFacilitator(locale);
        }
        refreshPersonalizationDictionarySession(currentSettingsValues);
        resetDictionaryFacilitatorIfNecessary();
        mStatsUtilsManager.onLoadSettings(this /* context */, currentSettingsValues);
    }

    private void refreshPersonalizationDictionarySession(
            final SettingsValues currentSettingsValues) {
        if (!currentSettingsValues.mUsePersonalizedDicts) {
            // Remove user history dictionaries.
            PersonalizationHelper.removeAllUserHistoryDictionaries(this);
            mDictionaryFacilitator.clearUserHistoryDictionary(this);
        }
    }

    // Note that this method is called from a non-UI thread.
    @Override
    public void onUpdateMainDictionaryAvailability(final boolean isMainDictionaryAvailable) {
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (mainKeyboardView != null) {
            mainKeyboardView.setMainDictionaryAvailability(isMainDictionaryAvailable);
        }
        if (mHandler.hasPendingWaitForDictionaryLoad()) {
            mHandler.cancelWaitForDictionaryLoad();
            mHandler.postResumeSuggestions(false /* shouldDelay */);
        }
    }

    void resetDictionaryFacilitatorIfNecessary() {
        final Locale subtypeSwitcherLocale = mRichImm.getCurrentSubtypeLocale();
        final Locale subtypeLocale;
        if (subtypeSwitcherLocale == null) {
            // This happens in very rare corner cases - for example, immediately after a switch
            // to LatinIME has been requested, about a frame later another switch happens. In this
            // case, we are about to go down but we still don't know it, however the system tells
            // us there is no current subtype.
            Log.e(TAG, "System is reporting no current subtype.");
            subtypeLocale = getResources().getConfiguration().locale;
        } else {
            subtypeLocale = subtypeSwitcherLocale;
        }
        if (mDictionaryFacilitator.isForLocale(subtypeLocale)
                && mDictionaryFacilitator.isForAccount(mSettings.getCurrent().mAccount)) {
            return;
        }
        resetDictionaryFacilitator(subtypeLocale);
    }

    /**
     * Reset the facilitator by loading dictionaries for the given locale and
     * the current settings values.
     *
     * @param locale the locale
     */
    // TODO: make sure the current settings always have the right locales, and read from them.
    private void resetDictionaryFacilitator(final Locale locale) {
        final SettingsValues settingsValues = mSettings.getCurrent();
        mDictionaryFacilitator.resetDictionaries(this /* context */, locale,
                settingsValues.mUseContactsDict, settingsValues.mUsePersonalizedDicts,
                false /* forceReloadMainDictionary */,
                settingsValues.mAccount, "" /* dictNamePrefix */,
                this /* DictionaryInitializationListener */);
        if (settingsValues.mAutoCorrectionEnabledPerUserSettings) {
            mInputLogic.mSuggest.setAutoCorrectionThreshold(
                    settingsValues.mAutoCorrectionThreshold);
        }
        mInputLogic.mSuggest.setPlausibilityThreshold(settingsValues.mPlausibilityThreshold);
    }

    /**
     * Reset suggest by loading the main dictionary of the current locale.
     */
    /* package private */ void resetSuggestMainDict() {
        final SettingsValues settingsValues = mSettings.getCurrent();
        mDictionaryFacilitator.resetDictionaries(this /* context */,
                mDictionaryFacilitator.getLocale(), settingsValues.mUseContactsDict,
                settingsValues.mUsePersonalizedDicts,
                true /* forceReloadMainDictionary */,
                settingsValues.mAccount, "" /* dictNamePrefix */,
                this /* DictionaryInitializationListener */);
    }

    @Override
    public void onDestroy() {
        mDictionaryFacilitator.closeDictionaries();
        mSettings.onDestroy();
        unregisterReceiver(mRingerModeChangeReceiver);
        unregisterReceiver(mDictionaryPackInstallReceiver);
        unregisterReceiver(mDictionaryDumpBroadcastReceiver);
        mStatsUtilsManager.onDestroy(this /* context */);
        if(myDB!=null){
            myDB.close();
        }

        super.onDestroy();
    }

    @UsedForTesting
    public void recycle() {
        unregisterReceiver(mDictionaryPackInstallReceiver);
        unregisterReceiver(mDictionaryDumpBroadcastReceiver);
        unregisterReceiver(mRingerModeChangeReceiver);
        mInputLogic.recycle();
    }

    private boolean isImeSuppressedByHardwareKeyboard() {
        final KeyboardSwitcher switcher = KeyboardSwitcher.getInstance();
        return !onEvaluateInputViewShown() && switcher.isImeSuppressedByHardwareKeyboard(
                mSettings.getCurrent(), switcher.getKeyboardSwitchState());
    }

    @Override
    public void onConfigurationChanged(final Configuration conf) {
        SettingsValues settingsValues = mSettings.getCurrent();
        if (settingsValues.mDisplayOrientation != conf.orientation) {
            mHandler.startOrientationChanging();
            mInputLogic.onOrientationChange(mSettings.getCurrent());
        }
        if (settingsValues.mHasHardwareKeyboard != Settings.readHasHardwareKeyboard(conf)) {
            // If the state of having a hardware keyboard changed, then we want to reload the
            // settings to adjust for that.
            // TODO: we should probably do this unconditionally here, rather than only when we
            // have a change in hardware keyboard configuration.
            loadSettings();
            settingsValues = mSettings.getCurrent();
            if (isImeSuppressedByHardwareKeyboard()) {
                // We call cleanupInternalStateForFinishInput() because it's the right thing to do;
                // however, it seems at the moment the framework is passing us a seemingly valid
                // but actually non-functional InputConnection object. So if this bug ever gets
                // fixed we'll be able to remove the composition, but until it is this code is
                // actually not doing much.
                cleanupInternalStateForFinishInput();
            }
        }
        super.onConfigurationChanged(conf);
    }

    @Override
    public View onCreateInputView() {
        StatsUtils.onCreateInputView();
        return mKeyboardSwitcher.onCreateInputView(mIsHardwareAcceleratedDrawingEnabled);
    }

    @Override
    public void setInputView(final View view) {
        super.setInputView(view);
        mInputView = view;
        mInsetsUpdater = ViewOutlineProviderCompatUtils.setInsetsOutlineProvider(view);
        updateSoftInputWindowLayoutParameters();
        mSuggestionStripView = (SuggestionStripView)view.findViewById(R.id.suggestion_strip_view);
        if (hasSuggestionStripView()) {
            mSuggestionStripView.setListener(this, view);
        }
    }

    @Override
    public void setCandidatesView(final View view) {
        // To ensure that CandidatesView will never be set.
    }

    @Override
    public void onStartInput(final EditorInfo editorInfo, final boolean restarting) {
        mHandler.onStartInput(editorInfo, restarting);
    }

    @Override
    public void onStartInputView(final EditorInfo editorInfo, final boolean restarting) {

        mHandler.onStartInputView(editorInfo, restarting);
        mStatsUtilsManager.onStartInputView();
    }

    @Override
    public void onFinishInputView(final boolean finishingInput) {
        if (sessionData != null) {
            sessionData.StopDateTime = System.currentTimeMillis();

//            }

            //store data
            DataStoreAsyncTaskParams params = new DataStoreAsyncTaskParams(sessionData,rawX,rawY);
            DataStoreAsyncTask task = new DataStoreAsyncTask(getApplicationContext());
            task.execute(params);

            rawX.clear();
            rawY.clear();
            sessionData= null;

        }


        StatsUtils.onFinishInputView();
        mHandler.onFinishInputView(finishingInput);
        mStatsUtilsManager.onFinishInputView();
        mGestureConsumer = GestureConsumer.NULL_GESTURE_CONSUMER;





    }




    @Override
    public void onFinishInput() {
        mHandler.onFinishInput();
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(final InputMethodSubtype subtype) {
        // Note that the calling sequence of onCreate() and onCurrentInputMethodSubtypeChanged()
        // is not guaranteed. It may even be called at the same time on a different thread.
        InputMethodSubtype oldSubtype = mRichImm.getCurrentSubtype().getRawSubtype();
        StatsUtils.onSubtypeChanged(oldSubtype, subtype);
        mRichImm.onSubtypeChanged(subtype);
        mInputLogic.onSubtypeChanged(SubtypeLocaleUtils.getCombiningRulesExtraValue(subtype),
                mSettings.getCurrent());
        loadKeyboard();
    }

    void onStartInputInternal(final EditorInfo editorInfo, final boolean restarting) {
        super.onStartInput(editorInfo, restarting);

        // If the primary hint language does not match the current subtype language, then try
        // to switch to the primary hint language.
        // TODO: Support all the locales in EditorInfo#hintLocales.
        final Locale primaryHintLocale = EditorInfoCompatUtils.getPrimaryHintLocale(editorInfo);
        if (primaryHintLocale == null) {
            return;
        }
        final InputMethodSubtype newSubtype = mRichImm.findSubtypeByLocale(primaryHintLocale);
        if (newSubtype == null || newSubtype.equals(mRichImm.getCurrentSubtype().getRawSubtype())) {
            return;
        }
        mHandler.postSwitchLanguage(newSubtype);
    }

    @SuppressWarnings("deprecation")
    void onStartInputViewInternal(final EditorInfo editorInfo, final boolean restarting) {
        super.onStartInputView(editorInfo, restarting);

        mDictionaryFacilitator.onStartInput();
        // Switch to the null consumer to handle cases leading to early exit below, for which we
        // also wouldn't be consuming gesture data.
        mGestureConsumer = GestureConsumer.NULL_GESTURE_CONSUMER;
        mRichImm.refreshSubtypeCaches();
        final KeyboardSwitcher switcher = mKeyboardSwitcher;
        switcher.updateKeyboardTheme();
        final MainKeyboardView mainKeyboardView = switcher.getMainKeyboardView();
        // If we are starting input in a different text field from before, we'll have to reload
        // settings, so currentSettingsValues can't be final.
        SettingsValues currentSettingsValues = mSettings.getCurrent();

        if (editorInfo == null) {
            Log.e(TAG, "Null EditorInfo in onStartInputView()");
            if (DebugFlags.DEBUG_ENABLED) {
                throw new NullPointerException("Null EditorInfo in onStartInputView()");
            }
            return;
        }
        if (DebugFlags.DEBUG_ENABLED) {
            Log.d(TAG, "onStartInputView: editorInfo:"
                    + String.format("inputType=0x%08x imeOptions=0x%08x",
                            editorInfo.inputType, editorInfo.imeOptions));
            Log.d(TAG, "All caps = "
                    + ((editorInfo.inputType & InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0)
                    + ", sentence caps = "
                    + ((editorInfo.inputType & InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0)
                    + ", word caps = "
                    + ((editorInfo.inputType & InputType.TYPE_TEXT_FLAG_CAP_WORDS) != 0));
        }
        Log.i(TAG, "Starting input. Cursor position = "
                + editorInfo.initialSelStart + "," + editorInfo.initialSelEnd);
        // TODO: Consolidate these checks with {@link InputAttributes}.
        if (InputAttributes.inPrivateImeOptions(null, Constants.ImeOption.NO_MICROPHONE_COMPAT, editorInfo)) {
            Log.w(TAG, "Deprecated private IME option specified: " + editorInfo.privateImeOptions);
            Log.w(TAG, "Use " + getPackageName() + "." + Constants.ImeOption.NO_MICROPHONE + " instead");
        }
        if (InputAttributes.inPrivateImeOptions(getPackageName(), Constants.ImeOption.FORCE_ASCII, editorInfo)) {
            Log.w(TAG, "Deprecated private IME option specified: " + editorInfo.privateImeOptions);
            Log.w(TAG, "Use EditorInfo.IME_FLAG_FORCE_ASCII flag instead");
        }

        // In landscape mode, this method gets called without the input view being created.
        if (mainKeyboardView == null) {
            return;
        }

        // Update to a gesture consumer with the current editor and IME state.
        mGestureConsumer = GestureConsumer.newInstance(editorInfo,
                mInputLogic.getPrivateCommandPerformer(),
                mRichImm.getCurrentSubtypeLocale(),
                switcher.getKeyboard());

        // Forward this event to the accessibility utilities, if enabled.
        final AccessibilityUtils accessUtils = AccessibilityUtils.getInstance();
        if (accessUtils.isTouchExplorationEnabled()) {
            accessUtils.onStartInputViewInternal(mainKeyboardView, editorInfo, restarting);
        }

        final boolean inputTypeChanged = !currentSettingsValues.isSameInputType(editorInfo);
        final boolean isDifferentTextField = !restarting || inputTypeChanged;

        StatsUtils.onStartInputView(editorInfo.inputType,
                Settings.getInstance().getCurrent().mDisplayOrientation,
                !isDifferentTextField);

        // The EditorInfo might have a flag that affects fullscreen mode.
        // Note: This call should be done by InputMethodService?
        updateFullscreenMode();

        // ALERT: settings have not been reloaded and there is a chance they may be stale.
        // In the practice, if it is, we should have gotten onConfigurationChanged so it should
        // be fine, but this is horribly confusing and must be fixed AS SOON AS POSSIBLE.

        // In some cases the input connection has not been reset yet and we can't access it. In
        // this case we will need to call loadKeyboard() later, when it's accessible, so that we
        // can go into the correct mode, so we need to do some housekeeping here.
        final boolean needToCallLoadKeyboardLater;
        final Suggest suggest = mInputLogic.mSuggest;
        if (!isImeSuppressedByHardwareKeyboard()) {
            // The app calling setText() has the effect of clearing the composing
            // span, so we should reset our state unconditionally, even if restarting is true.
            // We also tell the input logic about the combining rules for the current subtype, so
            // it can adjust its combiners if needed.
            mInputLogic.startInput(mRichImm.getCombiningRulesExtraValueOfCurrentSubtype(),
                    currentSettingsValues);

            resetDictionaryFacilitatorIfNecessary();

            // TODO[IL]: Can the following be moved to InputLogic#startInput?
            if (!mInputLogic.mConnection.resetCachesUponCursorMoveAndReturnSuccess(
                    editorInfo.initialSelStart, editorInfo.initialSelEnd,
                    false /* shouldFinishComposition */)) {
                // Sometimes, while rotating, for some reason the framework tells the app we are not
                // connected to it and that means we can't refresh the cache. In this case, schedule
                // a refresh later.
                // We try resetting the caches up to 5 times before giving up.
                mHandler.postResetCaches(isDifferentTextField, 5 /* remainingTries */);
                // mLastSelection{Start,End} are reset later in this method, no need to do it here
                needToCallLoadKeyboardLater = true;
            } else {
                // When rotating, and when input is starting again in a field from where the focus
                // didn't move (the keyboard having been closed with the back key),
                // initialSelStart and initialSelEnd sometimes are lying. Make a best effort to
                // work around this bug.
                mInputLogic.mConnection.tryFixLyingCursorPosition();
                mHandler.postResumeSuggestionsForStartInput(true /* shouldDelay */);
                needToCallLoadKeyboardLater = false;
            }
        } else {
            // If we have a hardware keyboard we don't need to call loadKeyboard later anyway.
            needToCallLoadKeyboardLater = false;
        }

        if (isDifferentTextField ||
                !currentSettingsValues.hasSameOrientation(getResources().getConfiguration())) {
            loadSettings();
        }
        if (isDifferentTextField) {
            mainKeyboardView.closing();
            currentSettingsValues = mSettings.getCurrent();

            if (currentSettingsValues.mAutoCorrectionEnabledPerUserSettings) {
                suggest.setAutoCorrectionThreshold(
                        currentSettingsValues.mAutoCorrectionThreshold);
            }
            suggest.setPlausibilityThreshold(currentSettingsValues.mPlausibilityThreshold);

            switcher.loadKeyboard(editorInfo, currentSettingsValues, getCurrentAutoCapsState(),
                    getCurrentRecapitalizeState());
            if (needToCallLoadKeyboardLater) {
                // If we need to call loadKeyboard again later, we need to save its state now. The
                // later call will be done in #retryResetCaches.
                switcher.saveKeyboardState();
            }
        } else if (restarting) {
            // TODO: Come up with a more comprehensive way to reset the keyboard layout when
            // a keyboard layout set doesn't get reloaded in this method.
            switcher.resetKeyboardStateToAlphabet(getCurrentAutoCapsState(),
                    getCurrentRecapitalizeState());
            // In apps like Talk, we come here when the text is sent and the field gets emptied and
            // we need to re-evaluate the shift state, but not the whole layout which would be
            // disruptive.
            // Space state must be updated before calling updateShiftState
            switcher.requestUpdatingShiftState(getCurrentAutoCapsState(),
                    getCurrentRecapitalizeState());
        }
        // This will set the punctuation suggestions if next word suggestion is off;
        // otherwise it will clear the suggestion strip.
        setNeutralSuggestionStrip();

        mHandler.cancelUpdateSuggestionStrip();

        mainKeyboardView.setMainDictionaryAvailability(
                mDictionaryFacilitator.hasAtLeastOneInitializedMainDictionary());
        mainKeyboardView.setKeyPreviewPopupEnabled(currentSettingsValues.mKeyPreviewPopupOn,
                currentSettingsValues.mKeyPreviewPopupDismissDelay);
        mainKeyboardView.setSlidingKeyInputPreviewEnabled(
                currentSettingsValues.mSlidingKeyInputPreviewEnabled);
        mainKeyboardView.setGestureHandlingEnabledByUser(
                currentSettingsValues.mGestureInputEnabled,
                currentSettingsValues.mGestureTrailEnabled,
                currentSettingsValues.mGestureFloatingPreviewTextEnabled);

        if (TRACE) Debug.startMethodTracing("/data/trace/latinime");
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        setNavigationBarVisibility(isInputViewShown());
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (mainKeyboardView != null) {
            mainKeyboardView.closing();
        }
        setNavigationBarVisibility(false);
    }

    void onFinishInputInternal() {
        super.onFinishInput();

        mDictionaryFacilitator.onFinishInput(this);
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (mainKeyboardView != null) {
            mainKeyboardView.closing();
        }
    }

    void onFinishInputViewInternal(final boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        cleanupInternalStateForFinishInput();
    }

    private void cleanupInternalStateForFinishInput() {
        // Remove pending messages related to update suggestions
        mHandler.cancelUpdateSuggestionStrip();
        // Should do the following in onFinishInputInternal but until JB MR2 it's not called :(
        mInputLogic.finishInput();
    }

    protected void deallocateMemory() {
        mKeyboardSwitcher.deallocateMemory();
    }

    @Override
    public void onUpdateSelection(final int oldSelStart, final int oldSelEnd,
            final int newSelStart, final int newSelEnd,
            final int composingSpanStart, final int composingSpanEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                composingSpanStart, composingSpanEnd);
        if (DebugFlags.DEBUG_ENABLED) {
            Log.i(TAG, "onUpdateSelection: oss=" + oldSelStart + ", ose=" + oldSelEnd
                    + ", nss=" + newSelStart + ", nse=" + newSelEnd
                    + ", cs=" + composingSpanStart + ", ce=" + composingSpanEnd);
        }

        // This call happens whether our view is displayed or not, but if it's not then we should
        // not attempt recorrection. This is true even with a hardware keyboard connected: if the
        // view is not displayed we have no means of showing suggestions anyway, and if it is then
        // we want to show suggestions anyway.
        final SettingsValues settingsValues = mSettings.getCurrent();
        if (isInputViewShown()
                && mInputLogic.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                        settingsValues)) {
            mKeyboardSwitcher.requestUpdatingShiftState(getCurrentAutoCapsState(),
                    getCurrentRecapitalizeState());
        }
    }

    /**
     * This is called when the user has clicked on the extracted text view,
     * when running in fullscreen mode.  The default implementation hides
     * the suggestions view when this happens, but only if the extracted text
     * editor has a vertical scroll bar because its text doesn't fit.
     * Here we override the behavior due to the possibility that a re-correction could
     * cause the suggestions strip to disappear and re-appear.
     */
    @Override
    public void onExtractedTextClicked() {
        if (mSettings.getCurrent().needsToLookupSuggestions()) {
            return;
        }

        super.onExtractedTextClicked();
    }

    /**
     * This is called when the user has performed a cursor movement in the
     * extracted text view, when it is running in fullscreen mode.  The default
     * implementation hides the suggestions view when a vertical movement
     * happens, but only if the extracted text editor has a vertical scroll bar
     * because its text doesn't fit.
     * Here we override the behavior due to the possibility that a re-correction could
     * cause the suggestions strip to disappear and re-appear.
     */
    @Override
    public void onExtractedCursorMovement(final int dx, final int dy) {
        if (mSettings.getCurrent().needsToLookupSuggestions()) {
            return;
        }

        super.onExtractedCursorMovement(dx, dy);
    }

    @Override
    public void hideWindow() {
        mKeyboardSwitcher.onHideWindow();

        if (TRACE) Debug.stopMethodTracing();
        if (isShowingOptionDialog()) {
            mOptionsDialog.dismiss();
            mOptionsDialog = null;
        }
        super.hideWindow();
    }

    @Override
    public void onDisplayCompletions(final CompletionInfo[] applicationSpecifiedCompletions) {
        if (DebugFlags.DEBUG_ENABLED) {
            Log.i(TAG, "Received completions:");
            if (applicationSpecifiedCompletions != null) {
                for (int i = 0; i < applicationSpecifiedCompletions.length; i++) {
                    Log.i(TAG, "  #" + i + ": " + applicationSpecifiedCompletions[i]);
                }
            }
        }
        if (!mSettings.getCurrent().isApplicationSpecifiedCompletionsOn()) {
            return;
        }
        // If we have an update request in flight, we need to cancel it so it does not override
        // these completions.
        mHandler.cancelUpdateSuggestionStrip();
        if (applicationSpecifiedCompletions == null) {
            setNeutralSuggestionStrip();
            return;
        }

        final ArrayList<SuggestedWords.SuggestedWordInfo> applicationSuggestedWords =
                SuggestedWords.getFromApplicationSpecifiedCompletions(
                        applicationSpecifiedCompletions);
        final SuggestedWords suggestedWords = new SuggestedWords(applicationSuggestedWords,
                null /* rawSuggestions */,
                null /* typedWord */,
                false /* typedWordValid */,
                false /* willAutoCorrect */,
                false /* isObsoleteSuggestions */,
                SuggestedWords.INPUT_STYLE_APPLICATION_SPECIFIED /* inputStyle */,
                SuggestedWords.NOT_A_SEQUENCE_NUMBER);
        // When in fullscreen mode, show completions generated by the application forcibly
        setSuggestedWords(suggestedWords);
    }

    @Override
    public void onComputeInsets(final InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
        // This method may be called before {@link #setInputView(View)}.
        if (mInputView == null) {
            return;
        }
        final SettingsValues settingsValues = mSettings.getCurrent();
        final View visibleKeyboardView = mKeyboardSwitcher.getVisibleKeyboardView();
        if (visibleKeyboardView == null || !hasSuggestionStripView()) {
            return;
        }
        final int inputHeight = mInputView.getHeight();
        if (isImeSuppressedByHardwareKeyboard() && !visibleKeyboardView.isShown()) {
            // If there is a hardware keyboard and a visible software keyboard view has been hidden,
            // no visual element will be shown on the screen.
            outInsets.contentTopInsets = inputHeight;
            outInsets.visibleTopInsets = inputHeight;
            mInsetsUpdater.setInsets(outInsets);
            return;
        }
        final int suggestionsHeight = (!mKeyboardSwitcher.isShowingEmojiPalettes()
                && mSuggestionStripView.getVisibility() == View.VISIBLE)
                ? mSuggestionStripView.getHeight() : 0;
        final int visibleTopY = inputHeight - visibleKeyboardView.getHeight() - suggestionsHeight;
        mSuggestionStripView.setMoreSuggestionsHeight(visibleTopY);
        // Need to set expanded touchable region only if a keyboard view is being shown.
        if (visibleKeyboardView.isShown()) {
            final int touchLeft = 0;
            final int touchTop = mKeyboardSwitcher.isShowingMoreKeysPanel() ? 0 : visibleTopY;
            final int touchRight = visibleKeyboardView.getWidth();
            final int touchBottom = inputHeight
                    // Extend touchable region below the keyboard.
                    + EXTENDED_TOUCHABLE_REGION_HEIGHT;
            outInsets.touchableInsets = InputMethodService.Insets.TOUCHABLE_INSETS_REGION;
            outInsets.touchableRegion.set(touchLeft, touchTop, touchRight, touchBottom);
        }
        outInsets.contentTopInsets = visibleTopY;
        outInsets.visibleTopInsets = visibleTopY;
        mInsetsUpdater.setInsets(outInsets);
    }

    public void startShowingInputView(final boolean needsToLoadKeyboard) {
        mIsExecutingStartShowingInputView = true;
        // This {@link #showWindow(boolean)} will eventually call back
        // {@link #onEvaluateInputViewShown()}.
        showWindow(true /* showInput */);
        mIsExecutingStartShowingInputView = false;
        if (needsToLoadKeyboard) {
            loadKeyboard();
        }
    }

    public void stopShowingInputView() {
        showWindow(false /* showInput */);
    }

    @Override
    public boolean onShowInputRequested(final int flags, final boolean configChange) {
        //Collect Data remi0s

        if (sessionData == null && userHaveAgreed())
        {

            sessionData = new KeyboardDynamics();

            final SettingsValues settingsValues = mSettings.getCurrent();

            final String packageName = getCurrentInputEditorInfo().packageName;
            String appName;
            PackageManager packageManager= getApplicationContext().getPackageManager();
            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            }catch (Exception e){
                appName="undefined";
            }
            sessionData.StartDateTime = System.currentTimeMillis();
            sessionData.CurrentAppName=appName;
            sessionData.IsSoundOn=settingsValues.mSoundOn;
            sessionData.IsVibrationOn=settingsValues.mVibrateOn;
            sessionData.IsShowPopupOn=settingsValues.mKeyPreviewPopupOn;


            //notification

            onShowNotificationAsync task = new onShowNotificationAsync(getApplicationContext());
            task.execute();


        }

        if (isImeSuppressedByHardwareKeyboard()) {
            return true;
        }

        return super.onShowInputRequested(flags, configChange);
    }

    @Override
    public boolean onEvaluateInputViewShown() {
        if (mIsExecutingStartShowingInputView) {
            return true;
        }
        return super.onEvaluateInputViewShown();
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        final SettingsValues settingsValues = mSettings.getCurrent();
        if (isImeSuppressedByHardwareKeyboard()) {
            // If there is a hardware keyboard, disable full screen mode.
            return false;
        }
        // Reread resource value here, because this method is called by the framework as needed.
        final boolean isFullscreenModeAllowed = Settings.readUseFullscreenMode(getResources());
        if (super.onEvaluateFullscreenMode() && isFullscreenModeAllowed) {
            // TODO: Remove this hack. Actually we should not really assume NO_EXTRACT_UI
            // implies NO_FULLSCREEN. However, the framework mistakenly does.  i.e. NO_EXTRACT_UI
            // without NO_FULLSCREEN doesn't work as expected. Because of this we need this
            // hack for now.  Let's get rid of this once the framework gets fixed.
            final EditorInfo ei = getCurrentInputEditorInfo();
            return !(ei != null && ((ei.imeOptions & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0));
        }
        return false;
    }

    @Override
    public void updateFullscreenMode() {
        super.updateFullscreenMode();
        updateSoftInputWindowLayoutParameters();
    }

    private void updateSoftInputWindowLayoutParameters() {
        // Override layout parameters to expand {@link SoftInputWindow} to the entire screen.
        // See {@link InputMethodService#setinputView(View)} and
        // {@link SoftInputWindow#updateWidthHeight(WindowManager.LayoutParams)}.
        final Window window = getWindow().getWindow();
        ViewLayoutUtils.updateLayoutHeightOf(window, LayoutParams.MATCH_PARENT);
        // This method may be called before {@link #setInputView(View)}.
        if (mInputView != null) {
            // In non-fullscreen mode, {@link InputView} and its parent inputArea should expand to
            // the entire screen and be placed at the bottom of {@link SoftInputWindow}.
            // In fullscreen mode, these shouldn't expand to the entire screen and should be
            // coexistent with {@link #mExtractedArea} above.
            // See {@link InputMethodService#setInputView(View) and
            // com.ime.internal.R.layout.input_method.xml.
            final int layoutHeight = isFullscreenMode()
                    ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT;
            final View inputArea = window.findViewById(android.R.id.inputArea);
            ViewLayoutUtils.updateLayoutHeightOf(inputArea, layoutHeight);
            ViewLayoutUtils.updateLayoutGravityOf(inputArea, Gravity.BOTTOM);
            ViewLayoutUtils.updateLayoutHeightOf(mInputView, layoutHeight);
        }
    }

    int getCurrentAutoCapsState() {
        return mInputLogic.getCurrentAutoCapsState(mSettings.getCurrent());
    }

    int getCurrentRecapitalizeState() {
        return mInputLogic.getCurrentRecapitalizeState();
    }

    /**
     * @param codePoints code points to get coordinates for.
     * @return x,y coordinates for this keyboard, as a flattened array.
     */
    public int[] getCoordinatesForCurrentKeyboard(final int[] codePoints) {
        final Keyboard keyboard = mKeyboardSwitcher.getKeyboard();
        if (null == keyboard) {
            return CoordinateUtils.newCoordinateArray(codePoints.length,
                    Constants.NOT_A_COORDINATE, Constants.NOT_A_COORDINATE);
        }
        return keyboard.getCoordinates(codePoints);
    }

    // Callback for the {@link SuggestionStripView}, to call when the important notice strip is
    // pressed.
    @Override
    public void showImportantNoticeContents() {
        PermissionsManager.get(this).requestPermissions(
                this /* PermissionsResultCallback */,
                null /* activity */, permission.READ_CONTACTS);
    }

    @Override
    public void onRequestPermissionsResult(boolean allGranted) {
        ImportantNoticeUtils.updateContactsNoticeShown(this /* context */);
        setNeutralSuggestionStrip();
    }

    public void displaySettingsDialog() {
        if (isShowingOptionDialog()) {
            return;
        }
        showSubtypeSelectorAndSettings();
    }

    @Override
    public boolean onCustomRequest(final int requestCode) {
        if (isShowingOptionDialog()) return false;
        switch (requestCode) {
        case Constants.CUSTOM_CODE_SHOW_INPUT_METHOD_PICKER:
            if (mRichImm.hasMultipleEnabledIMEsOrSubtypes(false /* include aux subtypes */)) {
                mRichImm.getInputMethodManager().showInputMethodPicker();
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean isShowingOptionDialog() {
        return mOptionsDialog != null && mOptionsDialog.isShowing();
    }

    public void switchLanguage(final InputMethodSubtype subtype) {
        final IBinder token = getWindow().getWindow().getAttributes().token;
        mRichImm.setInputMethodAndSubtype(token, subtype);
    }

    // TODO: Revise the language switch key behavior to make it much smarter and more reasonable.
    public void switchToNextSubtype() {
        final IBinder token = getWindow().getWindow().getAttributes().token;
        if (shouldSwitchToOtherInputMethods()) {
            mRichImm.switchToNextInputMethod(token, true /* onlyCurrentIme */); //remi0s
            return;
        }
        mSubtypeState.switchSubtype(token, mRichImm);
    }

    // TODO: Instead of checking for alphabetic keyboard here, separate keycodes for
    // alphabetic shift and shift while in symbol layout and get rid of this method.
    private int getCodePointForKeyboard(final int codePoint) {
        if (Constants.CODE_SHIFT == codePoint) {
            final Keyboard currentKeyboard = mKeyboardSwitcher.getKeyboard();
            if (null != currentKeyboard && currentKeyboard.mId.isAlphabetKeyboard()) {
                return codePoint;
            }
            return Constants.CODE_SYMBOL_SHIFT;
        }
        return codePoint;
    }

    // Implementation of {@link KeyboardActionListener}.
    @Override
    public void onCodeInput(final int codePoint, final int x, final int y,
            final boolean isKeyRepeat) {
        // TODO: this processing does not belong inside LatinIME, the caller should be doing this.
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        // x and y include some padding, but everything down the line (especially native
        // code) needs the coordinates in the keyboard frame.
        // TODO: We should reconsider which coordinate system should be used to represent
        // keyboard event. Also we should pull this up -- LatinIME has no business doing
        // this transformation, it should be done already before calling onEvent.
        final int keyX = mainKeyboardView.getKeyX(x);
        final int keyY = mainKeyboardView.getKeyY(y);
        final Event event = createSoftwareKeypressEvent(getCodePointForKeyboard(codePoint),
                keyX, keyY, isKeyRepeat);
        onEvent(event);
    }

    // This method is public for testability of LatinIME, but also in the future it should
    // completely replace #onCodeInput.
    public void onEvent(@Nonnull final Event event) {
        if (Constants.CODE_SHORTCUT == event.mKeyCode) {
            mRichImm.switchToShortcutIme(this);
        }
        final InputTransaction completeInputTransaction =
                mInputLogic.onCodeInput(mSettings.getCurrent(), event,
                        mKeyboardSwitcher.getKeyboardShiftMode(),
                        mKeyboardSwitcher.getCurrentKeyboardScriptId(), mHandler);
        updateStateAfterInputTransaction(completeInputTransaction);
        mKeyboardSwitcher.onEvent(event, getCurrentAutoCapsState(), getCurrentRecapitalizeState());
    }

    // A helper method to split the code point and the key code. Ultimately, they should not be
    // squashed into the same variable, and this method should be removed.
    // public for testing, as we don't want to copy the same logic into test code
    @Nonnull
    public static Event createSoftwareKeypressEvent(final int keyCodeOrCodePoint, final int keyX,
             final int keyY, final boolean isKeyRepeat) {
        final int keyCode;
        final int codePoint;
        if (keyCodeOrCodePoint <= 0) {
            keyCode = keyCodeOrCodePoint;
            codePoint = Event.NOT_A_CODE_POINT;
        } else {
            keyCode = Event.NOT_A_KEY_CODE;
            codePoint = keyCodeOrCodePoint;
        }
        return Event.createSoftwareKeypressEvent(codePoint, keyCode, keyX, keyY, isKeyRepeat);
    }

    // Called from PointerTracker through the KeyboardActionListener interface
    @Override
    public void onTextInput(final String rawText) {
        // TODO: have the keyboard pass the correct key code when we need it.
        final Event event = Event.createSoftwareTextEvent(rawText, Constants.CODE_OUTPUT_TEXT);
        final InputTransaction completeInputTransaction =
                mInputLogic.onTextInput(mSettings.getCurrent(), event,
                        mKeyboardSwitcher.getKeyboardShiftMode(), mHandler);
        updateStateAfterInputTransaction(completeInputTransaction);
        mKeyboardSwitcher.onEvent(event, getCurrentAutoCapsState(), getCurrentRecapitalizeState());
    }

    @Override
    public void onStartBatchInput() {
        mInputLogic.onStartBatchInput(mSettings.getCurrent(), mKeyboardSwitcher, mHandler);
        mGestureConsumer.onGestureStarted(
                mRichImm.getCurrentSubtypeLocale(),
                mKeyboardSwitcher.getKeyboard());
    }

    @Override
    public void onUpdateBatchInput(final InputPointers batchPointers) {
        mInputLogic.onUpdateBatchInput(batchPointers);
    }

    @Override
    public void onEndBatchInput(final InputPointers batchPointers) {
        mInputLogic.onEndBatchInput(batchPointers);
        mGestureConsumer.onGestureCompleted(batchPointers);
    }

    @Override
    public void onCancelBatchInput() {
        mInputLogic.onCancelBatchInput(mHandler);
        mGestureConsumer.onGestureCanceled();
    }

    /**
     * To be called after the InputLogic has gotten a chance to act on the suggested words by the
     * IME for the full gesture, possibly updating the TextView to reflect the first suggestion.
     * <p>
     * This method must be run on the UI Thread.
     * @param suggestedWords suggested words by the IME for the full gesture.
     */
    public void onTailBatchInputResultShown(final SuggestedWords suggestedWords) {
        mGestureConsumer.onImeSuggestionsProcessed(suggestedWords,
                mInputLogic.getComposingStart(), mInputLogic.getComposingLength(),
                mDictionaryFacilitator);
    }

    // This method must run on the UI Thread.
    void showGesturePreviewAndSuggestionStrip(@Nonnull final SuggestedWords suggestedWords,
            final boolean dismissGestureFloatingPreviewText) {
        showSuggestionStrip(suggestedWords);
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        mainKeyboardView.showGestureFloatingPreviewText(suggestedWords,
                dismissGestureFloatingPreviewText /* dismissDelayed */);
    }

    // Called from PointerTracker through the KeyboardActionListener interface
    @Override
    public void onFinishSlidingInput() {
        // User finished sliding input.
        mKeyboardSwitcher.onFinishSlidingInput(getCurrentAutoCapsState(),
                getCurrentRecapitalizeState());
    }

    // Called from PointerTracker through the KeyboardActionListener interface
    @Override
    public void onCancelInput() {
        // User released a finger outside any key
        // Nothing to do so far.
    }

    public boolean hasSuggestionStripView() {
        return null != mSuggestionStripView;
    }

    private void setSuggestedWords(final SuggestedWords suggestedWords) {
        final SettingsValues currentSettingsValues = mSettings.getCurrent();
        mInputLogic.setSuggestedWords(suggestedWords);
        // TODO: Modify this when we support suggestions with hard keyboard
        if (!hasSuggestionStripView()) {
            return;
        }
        if (!onEvaluateInputViewShown()) {
            return;
        }

        final boolean shouldShowImportantNotice =
                ImportantNoticeUtils.shouldShowImportantNotice(this, currentSettingsValues);
        final boolean shouldShowSuggestionCandidates =
                currentSettingsValues.mInputAttributes.mShouldShowSuggestions
                && currentSettingsValues.isSuggestionsEnabledPerUserSettings();
        final boolean shouldShowSuggestionsStripUnlessPassword = shouldShowImportantNotice
                || currentSettingsValues.mShowsVoiceInputKey
                || shouldShowSuggestionCandidates
                || currentSettingsValues.isApplicationSpecifiedCompletionsOn();
        final boolean shouldShowSuggestionsStrip = shouldShowSuggestionsStripUnlessPassword
                && !currentSettingsValues.mInputAttributes.mIsPasswordField;
        mSuggestionStripView.updateVisibility(shouldShowSuggestionsStrip, isFullscreenMode());
        if (!shouldShowSuggestionsStrip) {
            return;
        }

        final boolean isEmptyApplicationSpecifiedCompletions =
                currentSettingsValues.isApplicationSpecifiedCompletionsOn()
                && suggestedWords.isEmpty();
        final boolean noSuggestionsFromDictionaries = suggestedWords.isEmpty()
                || suggestedWords.isPunctuationSuggestions()
                || isEmptyApplicationSpecifiedCompletions;
        final boolean isBeginningOfSentencePrediction = (suggestedWords.mInputStyle
                == SuggestedWords.INPUT_STYLE_BEGINNING_OF_SENTENCE_PREDICTION);
        final boolean noSuggestionsToOverrideImportantNotice = noSuggestionsFromDictionaries
                || isBeginningOfSentencePrediction;
        if (shouldShowImportantNotice && noSuggestionsToOverrideImportantNotice) {
            if (mSuggestionStripView.maybeShowImportantNoticeTitle()) {
                return;
            }
        }

        if (currentSettingsValues.isSuggestionsEnabledPerUserSettings()
                || currentSettingsValues.isApplicationSpecifiedCompletionsOn()
                // We should clear the contextual strip if there is no suggestion from dictionaries.
                || noSuggestionsFromDictionaries) {
            mSuggestionStripView.setSuggestions(suggestedWords,
                    mRichImm.getCurrentSubtype().isRtlSubtype());
        }
    }

    // TODO[IL]: Move this out of LatinIME.
    public void getSuggestedWords(final int inputStyle, final int sequenceNumber,
            final Suggest.OnGetSuggestedWordsCallback callback) {
        final Keyboard keyboard = mKeyboardSwitcher.getKeyboard();
        if (keyboard == null) {
            callback.onGetSuggestedWords(SuggestedWords.getEmptyInstance());
            return;
        }
        mInputLogic.getSuggestedWords(mSettings.getCurrent(), keyboard,
                mKeyboardSwitcher.getKeyboardShiftMode(), inputStyle, sequenceNumber, callback);
    }

    @Override
    public void showSuggestionStrip(final SuggestedWords suggestedWords) {
        if (suggestedWords.isEmpty()) {
            setNeutralSuggestionStrip();
        } else {
            setSuggestedWords(suggestedWords);
        }
        // Cache the auto-correction in accessibility code so we can speak it if the user
        // touches a key that will insert it.
        AccessibilityUtils.getInstance().setAutoCorrection(suggestedWords);
    }

    // Called from {@link SuggestionStripView} through the {@link SuggestionStripView#Listener}
    // interface
    @Override
    public void pickSuggestionManually(final SuggestedWords.SuggestedWordInfo suggestionInfo) {
        final InputTransaction completeInputTransaction = mInputLogic.onPickSuggestionManually(
                mSettings.getCurrent(), suggestionInfo,
                mKeyboardSwitcher.getKeyboardShiftMode(),
                mKeyboardSwitcher.getCurrentKeyboardScriptId(),
                mHandler);
        updateStateAfterInputTransaction(completeInputTransaction);
    }

    // This will show either an empty suggestion strip (if prediction is enabled) or
    // punctuation suggestions (if it's disabled).
    @Override
    public void setNeutralSuggestionStrip() {
        final SettingsValues currentSettings = mSettings.getCurrent();
        final SuggestedWords neutralSuggestions = currentSettings.mBigramPredictionEnabled
                ? SuggestedWords.getEmptyInstance()
                : currentSettings.mSpacingAndPunctuations.mSuggestPuncList;
        setSuggestedWords(neutralSuggestions);
    }

    // Outside LatinIME, only used by the {@link InputTestsBase} test suite.
    @UsedForTesting
    void loadKeyboard() {
        // Since we are switching languages, the most urgent thing is to let the keyboard graphics
        // update. LoadKeyboard does that, but we need to wait for buffer flip for it to be on
        // the screen. Anything we do right now will delay this, so wait until the next frame
        // before we do the rest, like reopening dictionaries and updating suggestions. So we
        // post a message.
        mHandler.postReopenDictionaries();
        loadSettings();
        if (mKeyboardSwitcher.getMainKeyboardView() != null) {
            // Reload keyboard because the current language has been changed.
            mKeyboardSwitcher.loadKeyboard(getCurrentInputEditorInfo(), mSettings.getCurrent(),
                    getCurrentAutoCapsState(), getCurrentRecapitalizeState());
        }
    }

    /**
     * After an input transaction has been executed, some state must be updated. This includes
     * the shift state of the keyboard and suggestions. This method looks at the finished
     * inputTransaction to find out what is necessary and updates the state accordingly.
     * @param inputTransaction The transaction that has been executed.
     */
    private void updateStateAfterInputTransaction(final InputTransaction inputTransaction) {
        switch (inputTransaction.getRequiredShiftUpdate()) {
        case InputTransaction.SHIFT_UPDATE_LATER:
            mHandler.postUpdateShiftState();
            break;
        case InputTransaction.SHIFT_UPDATE_NOW:
            mKeyboardSwitcher.requestUpdatingShiftState(getCurrentAutoCapsState(),
                    getCurrentRecapitalizeState());
            break;
        default: // SHIFT_NO_UPDATE
        }
        if (inputTransaction.requiresUpdateSuggestions()) {
            final int inputStyle;
            if (inputTransaction.mEvent.isSuggestionStripPress()) {
                // Suggestion strip press: no input.
                inputStyle = SuggestedWords.INPUT_STYLE_NONE;
            } else if (inputTransaction.mEvent.isGesture()) {
                inputStyle = SuggestedWords.INPUT_STYLE_TAIL_BATCH;
            } else {
                inputStyle = SuggestedWords.INPUT_STYLE_TYPING;
            }
            mHandler.postUpdateSuggestionStrip(inputStyle);
        }
        if (inputTransaction.didAffectContents()) {
            mSubtypeState.setCurrentSubtypeHasBeenUsed();
        }
    }

    private void hapticAndAudioFeedback(final int code, final int repeatCount) {
        final MainKeyboardView keyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (keyboardView != null && keyboardView.isInDraggingFinger()) {
            // No need to feedback while finger is dragging.
            return;
        }
        if (repeatCount > 0) {
            if (code == Constants.CODE_DELETE && !mInputLogic.mConnection.canDeleteCharacters()) {
                // No need to feedback when repeat delete key will have no effect.
                return;
            }
            // TODO: Use event time that the last feedback has been generated instead of relying on
            // a repeat count to thin out feedback.
            if (repeatCount % PERIOD_FOR_AUDIO_AND_HAPTIC_FEEDBACK_IN_KEY_REPEAT == 0) {
                return;
            }
        }
        final AudioAndHapticFeedbackManager feedbackManager =
                AudioAndHapticFeedbackManager.getInstance();
        if (repeatCount == 0) {
            // TODO: Reconsider how to perform haptic feedback when repeating key.
            feedbackManager.performHapticFeedback(keyboardView);
        }
        feedbackManager.performAudioFeedback(code);
    }

    // Callback of the {@link KeyboardActionListener}. This is called when a key is depressed;
    // release matching call is {@link #onReleaseKey(int,boolean)} below.
    @Override
    public void onPressKey(final int primaryCode, final int repeatCount,
            final boolean isSinglePointer) {

        if(primaryCode==Constants.CODE_DELETE && sessionData!=null){
            sessionData.NumDels+=1;
        }
        mKeyboardSwitcher.onPressKey(primaryCode, isSinglePointer, getCurrentAutoCapsState(),
                getCurrentRecapitalizeState());
        hapticAndAudioFeedback(primaryCode, repeatCount);
    }

    // Callback of the {@link KeyboardActionListener}. This is called when a key is released;
    // press matching call is {@link #onPressKey(int,int,boolean)} above.
    @Override
    public void onReleaseKey(final int primaryCode, final boolean withSliding) {
        mKeyboardSwitcher.onReleaseKey(primaryCode, withSliding, getCurrentAutoCapsState(),
                getCurrentRecapitalizeState());
    }

    private HardwareEventDecoder getHardwareKeyEventDecoder(final int deviceId) {
        final HardwareEventDecoder decoder = mHardwareEventDecoders.get(deviceId);
        if (null != decoder) return decoder;
        // TODO: create the decoder according to the specification
        final HardwareEventDecoder newDecoder = new HardwareKeyboardEventDecoder(deviceId);
        mHardwareEventDecoders.put(deviceId, newDecoder);
        return newDecoder;
    }

    // Hooks for hardware keyboard
    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent keyEvent) {

        if (mEmojiAltPhysicalKeyDetector == null) {
            mEmojiAltPhysicalKeyDetector = new EmojiAltPhysicalKeyDetector(
                    getApplicationContext().getResources());
        }
        mEmojiAltPhysicalKeyDetector.onKeyDown(keyEvent);
        if (!ProductionFlags.IS_HARDWARE_KEYBOARD_SUPPORTED) {
            return super.onKeyDown(keyCode, keyEvent);
        }
        final Event event = getHardwareKeyEventDecoder(
                keyEvent.getDeviceId()).decodeHardwareKey(keyEvent);
        // If the event is not handled by LatinIME, we just pass it to the parent implementation.
        // If it's handled, we return true because we did handle it.
        if (event.isHandled()) {
            mInputLogic.onCodeInput(mSettings.getCurrent(), event,
                    mKeyboardSwitcher.getKeyboardShiftMode(),
                    // TODO: this is not necessarily correct for a hardware keyboard right now
                    mKeyboardSwitcher.getCurrentKeyboardScriptId(),
                    mHandler);
            return true;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent keyEvent) {
        if (mEmojiAltPhysicalKeyDetector == null) {
            mEmojiAltPhysicalKeyDetector = new EmojiAltPhysicalKeyDetector(
                    getApplicationContext().getResources());
        }
        mEmojiAltPhysicalKeyDetector.onKeyUp(keyEvent);
        if (!ProductionFlags.IS_HARDWARE_KEYBOARD_SUPPORTED) {
            return super.onKeyUp(keyCode, keyEvent);
        }
        final long keyIdentifier = keyEvent.getDeviceId() << 32 + keyEvent.getKeyCode();
        if (mInputLogic.mCurrentlyPressedHardwareKeys.remove(keyIdentifier)) {
            return true;
        }
        return super.onKeyUp(keyCode, keyEvent);
    }

    // onKeyDown and onKeyUp are the main events we are interested in. There are two more events
    // related to handling of hardware key events that we may want to implement in the future:
    // boolean onKeyLongPress(final int keyCode, final KeyEvent event);
    // boolean onKeyMultiple(final int keyCode, final int count, final KeyEvent event);

    // receive ringer mode change.
    private final BroadcastReceiver mRingerModeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                AudioAndHapticFeedbackManager.getInstance().onRingerModeChanged();
            }
        }
    };

    void launchSettings(final String extraEntryValue) {
        mInputLogic.commitTyped(mSettings.getCurrent(), LastComposedWord.NOT_A_SEPARATOR);
        requestHideSelf(0);
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (mainKeyboardView != null) {
            mainKeyboardView.closing();
        }
        final Intent intent = new Intent();
        intent.setClass(LatinIME.this, SettingsActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_HOME_AS_UP, false);
        intent.putExtra(SettingsActivity.EXTRA_ENTRY_KEY, extraEntryValue);
        startActivity(intent);
    }

    private void showSubtypeSelectorAndSettings() {
        final CharSequence title = getString(R.string.english_ime_input_options);
        // TODO: Should use new string "Select active input modes".
        final CharSequence languageSelectionTitle = getString(R.string.language_selection_title);
        final CharSequence[] items = new CharSequence[] {
                languageSelectionTitle,
                getString(ApplicationUtils.getActivityTitleResId(this, SettingsActivity.class))
        };
        final String imeId = mRichImm.getInputMethodIdOfThisIme();
        final OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface di, int position) {
                di.dismiss();
                switch (position) {
                case 0:
//                    final Intent intent = IntentUtils.getInputLanguageSelectionIntent(
//                            imeId,
//                            FLAG_ACTIVITY_NEW_TASK
//                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED  //remi0s commented
//                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.putExtra(Intent.EXTRA_TITLE, languageSelectionTitle);
//                    startActivity(intent);

                    final InputMethodInfo imi =
                            UncachedInputMethodManagerUtils.getInputMethodInfoOf(getPackageName(), mRichImm.getInputMethodManager()); //remi0s fixed bug
                    if (imi == null) {
                        return;
                    }
                    final Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.putExtra(android.provider.Settings.EXTRA_INPUT_METHOD_ID, imi.getId());
                    startActivity(intent);
                    break;
                case 1:
                    launchSettings(SettingsActivity.EXTRA_ENTRY_VALUE_LONG_PRESS_COMMA);
                    break;
                }
            }
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                DialogUtils.getPlatformDialogThemeContext(this));
        builder.setItems(items, listener).setTitle(title);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(true /* cancelable */);
        dialog.setCanceledOnTouchOutside(true /* cancelable */);
        showOptionDialog(dialog);
    }

    // TODO: Move this method out of {@link LatinIME}.
    private void showOptionDialog(final AlertDialog dialog) {
        final IBinder windowToken = mKeyboardSwitcher.getMainKeyboardView().getWindowToken();
        if (windowToken == null) {
            return;
        }

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = windowToken;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        mOptionsDialog = dialog;
        dialog.show();
    }

    @UsedForTesting
    SuggestedWords getSuggestedWordsForTest() {
        // You may not use this method for anything else than debug
        return DebugFlags.DEBUG_ENABLED ? mInputLogic.mSuggestedWords : null;
    }

    // DO NOT USE THIS for any other purpose than testing. This is information private to LatinIME.
    @UsedForTesting
    void waitForLoadingDictionaries(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        mDictionaryFacilitator.waitForLoadingDictionariesForTesting(timeout, unit);
    }

    // DO NOT USE THIS for any other purpose than testing. This can break the keyboard badly.
    @UsedForTesting
    void replaceDictionariesForTest(final Locale locale) {
        final SettingsValues settingsValues = mSettings.getCurrent();
        mDictionaryFacilitator.resetDictionaries(this, locale,
            settingsValues.mUseContactsDict, settingsValues.mUsePersonalizedDicts,
            false /* forceReloadMainDictionary */,
            settingsValues.mAccount, "", /* dictionaryNamePrefix */
            this /* DictionaryInitializationListener */);
    }

    // DO NOT USE THIS for any other purpose than testing.
    @UsedForTesting
    void clearPersonalizedDictionariesForTest() {
        mDictionaryFacilitator.clearUserHistoryDictionary(this);
    }

    @UsedForTesting
    List<InputMethodSubtype> getEnabledSubtypesForTest() {
        return (mRichImm != null) ? mRichImm.getMyEnabledInputMethodSubtypeList(
                true /* allowsImplicitlySelectedSubtypes */) : new ArrayList<InputMethodSubtype>();
    }

    public void dumpDictionaryForDebug(final String dictName) {
        if (!mDictionaryFacilitator.isActive()) {
            resetDictionaryFacilitatorIfNecessary();
        }
        mDictionaryFacilitator.dumpDictionaryForDebug(dictName);
    }

    public void debugDumpStateAndCrashWithException(final String context) {
        final SettingsValues settingsValues = mSettings.getCurrent();
        final StringBuilder s = new StringBuilder(settingsValues.toString());
        s.append("\nAttributes : ").append(settingsValues.mInputAttributes)
                .append("\nContext : ").append(context);
        throw new RuntimeException(s.toString());
    }

    @Override
    protected void dump(final FileDescriptor fd, final PrintWriter fout, final String[] args) {
        super.dump(fd, fout, args);

        final Printer p = new PrintWriterPrinter(fout);
        p.println("LatinIME state :");
        p.println("  VersionCode = " + ApplicationUtils.getVersionCode(this));
        p.println("  VersionName = " + ApplicationUtils.getVersionName(this));
        final Keyboard keyboard = mKeyboardSwitcher.getKeyboard();
        final int keyboardMode = keyboard != null ? keyboard.mId.mMode : -1;
        p.println("  Keyboard mode = " + keyboardMode);
        final SettingsValues settingsValues = mSettings.getCurrent();
        p.println(settingsValues.dump());
        p.println(mDictionaryFacilitator.dump(this /* context */));
        // TODO: Dump all settings values
    }

    public boolean shouldSwitchToOtherInputMethods() {
        // TODO: Revisit here to reorganize the settings. Probably we can/should use different
        // strategy once the implementation of
        // {@link InputMethodManager#shouldOfferSwitchingToNextInputMethod} is defined well.
        final boolean fallbackValue = mSettings.getCurrent().mIncludesOtherImesInLanguageSwitchList;
        final IBinder token = getWindow().getWindow().getAttributes().token;
        if (token == null) {
            return fallbackValue;
        }
       // return mRichImm.shouldOfferSwitchingToNextInputMethod(token, fallbackValue);
        return false; //remi0s to block changing to other keyboards
    }

    public boolean shouldShowLanguageSwitchKey() {
        // TODO: Revisit here to reorganize the settings. Probably we can/should use different
        // strategy once the implementation of
        // {@link InputMethodManager#shouldOfferSwitchingToNextInputMethod} is defined well.
        final boolean fallbackValue = mSettings.getCurrent().isLanguageSwitchKeyEnabled();
        final IBinder token = getWindow().getWindow().getAttributes().token;
        if (token == null) {
            return fallbackValue;
        }
        return mRichImm.shouldOfferSwitchingToNextInputMethod(token, fallbackValue);
    }

    private void setNavigationBarVisibility(final boolean visible) {
        if (BuildCompatUtils.EFFECTIVE_SDK_INT > Build.VERSION_CODES.M) {
            // For N and later, IMEs can specify Color.TRANSPARENT to make the navigation bar
            // transparent.  For other colors the system uses the default color.
            getWindow().getWindow().setNavigationBarColor(
                    visible ? Color.BLACK : Color.TRANSPARENT);
        }
    }



    private void CreateAlertDialogWithRadioButtonGroup() {
        Intent intent = new Intent(getApplicationContext(), ChooseMood.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private static boolean AddData(String sessionDataString,Context context) {
        boolean insertData=myDB.addData(sessionDataString);
        if(insertData==false) {
            Toast.makeText(context, "Something went wrong in database", Toast.LENGTH_LONG).show();
        }
        return insertData;
    }




    public static boolean isConnected(Context context){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo networkInfo = null;
            if (connMgr != null) {
                networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            }
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;

        }catch (Exception e){
            return false;
        }

    }

//    public static class HttpAsyncTask extends AsyncTask<String, Void, String> {
//        String result="";
//        @Override
//        protected String doInBackground(String... params) {
//            String prefs_id=params[0];
//            String prefs_age=params[1];
//            String prefs_gender=params[2];
//            String prefs_health=params[3];
//            ArrayList<KeyboardPayload> notSendData = new ArrayList<>();
//            Cursor data = myDB.getNotSendContents();
//            try {
//                if (data.getCount() != 0) {
//                    while (data.moveToNext()) {
//                        KeyboardPayload payload=new KeyboardPayload();
//                        payload.DocID=data.getString(0);//DocID
//                        payload.DateData=data.getString(1); //DateTime
////                    payload.UserID=data.getString(2); //UserID
//                        payload.SessionData= data.getString(2); //SessionData
//                        notSendData.add(payload);
//                    }
//                    result=upload(notSendData,prefs_id,prefs_age,prefs_gender,prefs_health, storageConnectionString, storageContainer);//POST(urls[0],notSendData);
//                }else{
//                    Log.d("SQL","I'm in do in background 0 data");
//                }
//            } finally {
//                data.close();
//            }
//
//
//
//            return result;
//        }
//        // onPostExecute displays the results of the AsyncTask.
//        @Override
//        protected void onPostExecute(String result) {
//            Log.d("SQL","Tried to send data with result: "+result);
////            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
//        }
//    }


//    public static String oldPOST(String url,ArrayList<KeyboardPayload> payload){
//        InputStream inputStream = null;
//        String result = "";
//
//
//        try {
//            // 1. create HttpClient
//            HttpClient httpclient = new DefaultHttpClient();
//
//            // 2. make POST request to the given URL
//            HttpPost httpPost = new HttpPost(url);
//
//            String json = "";
//
//            if((!pref_ID.isEmpty() && !pref_age.isEmpty() && !pref_gender.isEmpty() && !pref_health.isEmpty())){
//
//                for(int i = 0; i < payload.size(); i++) { //payload.size()
//
//                    // 3. build jsonObject
//
//
//
//
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.accumulate("DOC_ID", payload.get(i).DocID);
//                    jsonObject.accumulate("USER_ID",pref_ID);
//                    jsonObject.accumulate("USER_AGE", pref_age);
//                    jsonObject.accumulate("USER_GENDER", pref_gender);
//                    jsonObject.accumulate("USER_PHQ9", pref_health);
//                    jsonObject.accumulate("DATE_DATA", payload.get(i).DateData);
//                    jsonObject.accumulate("SESSION_DATA", payload.get(i).SessionData);
//
//
//                    // 4. convert JSONObject to JSON to String
//                    json = jsonObject.toString();
//
//                    Log.d("server","Json to be sent:\n"+json);
//
//
//                    // ** Alternative way to convert Person object to JSON string usin Jackson Lib
//                    // ObjectMapper mapper = new ObjectMapper();
//                    // json = mapper.writeValueAsString(person);
////                Gson gson = new Gson();
////                json = gson.toJson(payload.get(i), KeyboardPayload.class);
//
//                    // 5. set json to StringEntity
//                    StringEntity se = new StringEntity(json);
//
//                    // 6. set httpPost Entity
//                    httpPost.setEntity(se);
//
//                    // 7. Set some headers to inform server about the type of the content
//                    httpPost.setHeader("Accept", "application/json");
//                    httpPost.setHeader("Content-type", "application/json");
//
//                    // 8. Execute POST request to the given URL
//                    HttpResponse httpResponse = httpclient.execute(httpPost);
//
//                    // 9. receive response as inputStream
//                    inputStream = httpResponse.getEntity().getContent();
//
//
//
//
//                    // 10. convert inputstream to string
//                    if(inputStream != null) {
//                        result = convertInputStreamToString(inputStream);
//                        Log.d("SQL","I'm at inputstream");
//                        myDB.setSend(payload.get(i).DocID);
//                        Log.d("SQL","Data Sent!");
//                    }
//                    else {
//                        result = "Did not work!";
//                    }
//
//                }
//            }
//
//
//
//
//        } catch (Exception e) {
//            Log.d("InputStream", e.getLocalizedMessage());
//        }
//
//        // 11. return result
//        return result;
//    }

    protected static String upload(ArrayList<KeyboardPayload> payload,Context context){
        String result = "";
        SharedPreferences pref =context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String prefs_age= pref.getString("Age", "");
        String prefs_id= pref.getString("ID", "");
        String prefs_gender= pref.getString("Gender", "");
        String prefs_health= pref.getString("Health", "");

        String pref_phq9_item_1=pref.getString("PHQ9_item_1", "");
        String pref_phq9_item_2=pref.getString("PHQ9_item_2", "");
        String pref_phq9_item_3=pref.getString("PHQ9_item_3", "");
        String pref_phq9_item_4=pref.getString("PHQ9_item_4", "");
        String pref_phq9_item_5=pref.getString("PHQ9_item_5", "");
        String pref_phq9_item_6=pref.getString("PHQ9_item_6", "");
        String pref_phq9_item_7=pref.getString("PHQ9_item_7", "");
        String pref_phq9_item_8=pref.getString("PHQ9_item_8", "");
        String pref_phq9_item_9=pref.getString("PHQ9_item_9", "");

        String pref_education = pref.getString("Education", "");
        String pref_usage = pref.getString("Usage", "");
        String pref_income = pref.getString("Income", "");
        String pref_medication= pref.getString("Medication", "");







        long users_max_flight= pref.getLong("MaxFlight", 0);
        long users_min_flight= pref.getLong("MinFlight", 3000);
        float users_mean_flight= pref.getFloat("MeanFlight", 0);
        long users_max_hold= pref.getLong("MaxHold", 0);
        long users_min_hold= pref.getLong("MinHold", 3000);
        float users_mean_hold= pref.getFloat("MeanHold", 0);
        editor.apply();
        String storagesContainer=getConfigValue(context, "storageContainer");
        String storagesConnectionString=getConfigValue(context, "storageConnectionString");
        String country =getUserCountry(context);

        try
        {
            if((!prefs_id.isEmpty() && !prefs_age.isEmpty() && !prefs_gender.isEmpty() && !prefs_health.isEmpty())){
                for(int i = 0; i < payload.size(); i++) { //payload.size()

                    // 3. build jsonObject


                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("DOC_ID", payload.get(i).DocID);
                    jsonObject.accumulate("USER_ID", prefs_id);
                    jsonObject.accumulate("USER_COUNTRY", country);
                    jsonObject.accumulate("USER_AGE", prefs_age);
                    jsonObject.accumulate("USER_GENDER", prefs_gender);

                    jsonObject.accumulate("USER_EDUCATION", pref_education);
                    jsonObject.accumulate("USER_PHONE_USAGE", pref_usage);
                    jsonObject.accumulate("USER_INCOME", pref_income);
                    jsonObject.accumulate("USER_MEDICATION", pref_medication);

                    jsonObject.accumulate("USER_PHQ9", prefs_health);
                    jsonObject.accumulate("USER_PHQ9_ITEM_1", pref_phq9_item_1);
                    jsonObject.accumulate("USER_PHQ9_ITEM_2", pref_phq9_item_2);
                    jsonObject.accumulate("USER_PHQ9_ITEM_3", pref_phq9_item_3);
                    jsonObject.accumulate("USER_PHQ9_ITEM_4", pref_phq9_item_4);
                    jsonObject.accumulate("USER_PHQ9_ITEM_5", pref_phq9_item_5);
                    jsonObject.accumulate("USER_PHQ9_ITEM_6", pref_phq9_item_6);
                    jsonObject.accumulate("USER_PHQ9_ITEM_7", pref_phq9_item_7);
                    jsonObject.accumulate("USER_PHQ9_ITEM_8", pref_phq9_item_8);
                    jsonObject.accumulate("USER_PHQ9_ITEM_9", pref_phq9_item_9);
                    jsonObject.accumulate("USER_MAX_FLIGHT", users_max_flight);
                    jsonObject.accumulate("USER_MIN_FLIGHT", users_min_flight);
                    jsonObject.accumulate("USER_MEAN_FLIGHT", users_mean_flight);
                    jsonObject.accumulate("USER_MAX_HOLD", users_max_hold);
                    jsonObject.accumulate("USER_MIN_HOLD", users_min_hold);
                    jsonObject.accumulate("USER_MEAN_HOLD", users_mean_hold);
//                jsonObject.accumulate("DATE_DATA", payload.get(i).DateData);
//                jsonObject.accumulate("SESSION_DATA", payload.get(i).SessionData);


                    // 4. convert JSONObject to JSON to String


                    // Retrieve storage account from connection-string.
                    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storagesConnectionString);

                    // Create the blob client.
                    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                    // Retrieve reference to a previously created container.
                    CloudBlobContainer container = blobClient.getContainerReference(storagesContainer);

                    // Create or overwrite the blob (with the name "example.jpeg") with contents from a local file.

                    String formattedDate=new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    String formattedDateData="";

                    try{
                        Date date =new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss", Locale.US).parse(payload.get(i).DateData);
                        jsonObject.accumulate("DATE_DATA", payload.get(i).DateData);
                        formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
                    }catch (Exception e)
                    {
                        try {
                            Date tempDate = new Date(payload.get(i).DateData);
                            formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tempDate);
                            formattedDateData= new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss", Locale.US).format(tempDate);
                            jsonObject.accumulate("DATE_DATA", formattedDateData);
                        }catch (Exception a){
                            Log.d("dateError", "Even that failed");
                        }
                    }
                    jsonObject.accumulate("DATE_SEND", new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss", Locale.US).format(new Date()));
                    jsonObject.accumulate("SESSION_DATA", payload.get(i).SessionData);

                    String blobName=prefs_id+"/"+formattedDate+"/"+payload.get(i).DocID+".json";
                    CloudBlockBlob blob = container.getBlockBlobReference(blobName);



                    blob.uploadText(jsonObject.toString());
                    result="success";

                    myDB.setSend(payload.get(i).DocID);
                }
            }


        }
        catch (Exception e)
        {
            // Output the stack trace.
            result="failed";
            e.printStackTrace();
            Log.d("Upload", e.getLocalizedMessage());
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

//    private Boolean isPasswordGivenCorrect () {
////        SharedPreferences pref =getSharedPreferences("user_info", Context.MODE_PRIVATE);
////        SharedPreferences.Editor editor = pref.edit();
////        String pref_password= pref.getString("Password", "");
////        editor.apply();
////        //return (pref_password.equals(getConfigValue(getApplicationContext(), "password")));
////        return true;
////    }

    private Boolean userHaveAgreed(){
        SharedPreferences pref =getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        boolean pref_terms= pref.getBoolean("TermsAgreement", false);
        editor.apply();
        return pref_terms;
    }

    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open config file.");
        }

        return null;
    }

    private static class DataStoreAsyncTask extends AsyncTask<DataStoreAsyncTaskParams, Void, String> {
        WeakReference<Context> weakContext;

        private DataStoreAsyncTask (Context context){
            weakContext = new WeakReference<Context>(context);
        }

        @Override
        protected String doInBackground(DataStoreAsyncTaskParams... args) {
            Boolean result=false;
            KeyboardDynamics sessionDataTemp;
            sessionDataTemp=args[0].data;
            ArrayList<Double> xTemp=new ArrayList<Double>(args[0].x);
            ArrayList<Double> yTemp=new ArrayList<Double>(args[0].y);

            if (sessionDataTemp != null) {
//                sessionDataTemp.StopDateTime = System.currentTimeMillis();
                StopDateTimeTemp=new Date(sessionDataTemp.StopDateTime);
                sessionDataTemp.CurrentMood = currentMood;
                sessionDataTemp.CurrentPhysicalState=currentPhysicalState;
                sessionDataTemp.LatestNotification=latestNotificationTime;



                int notificationFlag = 0;

                if (latestNotificationTime != 0) {
                    long minutesPassed = TimeUnit.MINUTES.convert(StopDateTimeTemp.getTime() - latestNotificationTimeTemp.getTime(), TimeUnit.MILLISECONDS);

                    if(laterPressed && minutesPassed>=120){
                        sessionDataTemp.CurrentMood =currentMood+" TIMEOUT";
                        sessionDataTemp.CurrentPhysicalState=currentPhysicalState+" TIMEOUT";
                        notificationFlag = 1;
                        laterPressed=false;
                    }else if (minutesPassed >= 120 || (sessionsCounter>=30 && minutesPassed>=90)) {
                        notificationFlag = 1;
                        sessionDataTemp.CurrentMood = currentMood + " TIMEOUT";
                        sessionDataTemp.CurrentPhysicalState = currentPhysicalState + " TIMEOUT";
                    }else{
                        notificationFlag = 0;
                    }
                } else {
                    notificationFlag = 1;
                    sessionDataTemp.CurrentMood = "undefined";
                    sessionDataTemp.CurrentPhysicalState="undefined";

                }

                if (notificationFlag == 1 && sessionDataTemp.DownTime.size() > 5) {
                    String title = "TypeOfMood";
                    String message = "Please Expand to describe your mood!";
                    sessionsCounter=0;


//                    mNotificationHelperPhysical = new NotificationHelperPhysical(weakContext.get());
//                    NotificationCompat.Builder nbPhysical = mNotificationHelperPhysical.getTypeOfMoodNotification(title, message);
//                    mNotificationHelperPhysical.getManager().notify(mNotificationHelperPhysical.notification_id, nbPhysical.build());

//                    mNotificationHelper = new NotificationHelper(weakContext.get());

                    nb = mNotificationHelper.getTypeOfMoodNotification(title, message,nb);
                    mNotificationHelper.getManager().notify(mNotificationHelper.notification_id, nb.build());
//                CreateAlertDialogWithRadioButtonGroup();



                }

                if (sessionDataTemp.DownTime.size() >= 5){
                    long min_flight=3000;
                    long max_flight=0;
                    float average_flight=0;
                    int num_flight=0;
                    ArrayList<Long>flight_time=new ArrayList<Long>();
                    long min_hold=3000;
                    long max_hold=0;
                    float average_hold=0;
                    int num_hold=0;
                    ArrayList<Long>hold_time=new ArrayList<Long>();
                    double dist=0;
                    double dx=0,dy=0;


                    for(int i=0;i<xTemp.size()-1;i++){
                        dx=(xTemp.get(i)-xTemp.get(i+1));
                        dy=(yTemp.get(i)-yTemp.get(i+1));
                        dx=dx/densX * 25.4f;
                        dy=dy/densY * 25.4f;

                        dist=Math.hypot(dx,dy);
                        sessionDataTemp.Distance.add(dist);
                        flight_time.add(sessionDataTemp.DownTime.get(i+1)-sessionDataTemp.UpTime.get(i));

                        //flight time
                        long temp=flight_time.get(flight_time.size()-1);
                        if (temp<min_flight && temp>0){
                            min_flight=temp;
                        }
                        if (temp>max_flight && temp<3000){
                            max_flight=temp;
                        }
                        if (temp>0 && temp<3000){
                            num_flight=num_flight+1;
                            average_flight=(average_flight+temp);
                        }
                        //hold time
                        if(sessionDataTemp.IsLongPress.get(i)!=1){
                            hold_time.add(sessionDataTemp.UpTime.get(i)-sessionDataTemp.DownTime.get(i));
                            long temp2=hold_time.get(hold_time.size()-1);
                            if (temp2<min_hold){
                                min_hold=temp2;
                            }
                            if (temp2>max_hold){
                                max_hold=temp2;
                            }
                            num_hold=num_hold+1;
                            average_hold=(average_hold+temp2);

                        }


                    }//end for i size downtime

                    average_hold=average_hold/num_hold;
                    average_flight=average_flight/num_flight;



                    SharedPreferences pref =weakContext.get().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    user_sessions= pref.getInt("Sessions_counter", 1);
                    user_max_flight= pref.getLong("MaxFlight", 0);
                    user_min_flight= pref.getLong("MinFlight", 3000);
                    user_mean_flight= pref.getFloat("MeanFlight", 0);
                    user_max_hold= pref.getLong("MaxHold", 0);
                    user_min_hold= pref.getLong("MinHold", 3000);
                    user_mean_hold= pref.getFloat("MeanHold", 0);
                    //flight time
                    if (min_flight<user_min_flight){
                        editor.putLong("MinFlight",min_flight );
                    }

                    if (max_flight>user_max_flight){
                        editor.putLong("MaxFlight",max_flight );
                    }
                    float temp_average=(user_mean_flight*user_sessions+average_flight)/(user_sessions+1);
                    editor.putFloat("MeanFlight", temp_average);
                    //hold time
                    if (min_hold<user_min_hold){
                        editor.putLong("MinHold",min_hold );
                    }

                    if (max_hold>user_max_hold){
                        editor.putLong("MaxHold",max_hold );
                    }
                    //if not all was longpressed
                    if(average_hold>0){
                        temp_average=(user_mean_hold*user_sessions+average_hold)/(user_sessions+1);
                        editor.putFloat("MeanHold",temp_average );
                    }
                    int temp_sessions=user_sessions+1;
                    editor.putInt("Sessions_counter",temp_sessions );
                    editor.apply();

                    //save data

                    Gson gson = new GsonBuilder().serializeNulls().create();
                    String sessionDataString = gson.toJson(sessionDataTemp, KeyboardDynamics.class);
//                Log.d("Json", "Json string: " + sessionDataString);
                    result=AddData(sessionDataString,weakContext.get());
                    sessionsCounter=sessionsCounter+1;




                    //send data

                    long sendMinutesPassed;
                    if(latestSendTimeTemp!=null){
                        sendMinutesPassed= TimeUnit.MINUTES.convert((new Date(System.currentTimeMillis())).getTime()-latestSendTimeTemp.getTime() , TimeUnit.MILLISECONDS);
                    }else{
                        sendMinutesPassed=61;
                    }
                    String send_result="not yet "+sendMinutesPassed;
                    if(isConnected(weakContext.get() ) && (sendMinutesPassed>=15)){
                        latestSendTimeTemp=new Date(System.currentTimeMillis());
                        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
                        editor = pref.edit();
                        editor.putString("latest_send_date",dateFormat.format(latestSendTimeTemp));
                        editor.apply();

                        ArrayList<KeyboardPayload> notSendData = new ArrayList<>();
                        Cursor data = myDB.getNotSendContents();
                        try {
                            if (data.getCount() != 0) {
                                while (data.moveToNext()) {
                                    KeyboardPayload payload=new KeyboardPayload();
                                    payload.DocID=data.getString(0);//DocID
                                    payload.DateData=data.getString(1); //DateTime
//                    payload.UserID=data.getString(2); //UserID
                                    payload.SessionData= data.getString(2); //SessionData
                                    notSendData.add(payload);
                                }
                                String locale =getUserCountry(weakContext.get());
                                Log.d("country",locale);
                                send_result=upload(notSendData,weakContext.get());//POST(urls[0],notSendData);
                            }else{
                                Log.d("Upload","I'm in do in background 0 data");
                            }
                        } finally {
                            data.close();
                        }
                    }
                    Log.w("Upload","Tried to send data with result: "+send_result);





                }



            }
            if(result){
                return "success";
            }else{
                return "failure";
            }

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

//            Log.d("SQL","Tried to save data with result: "+result);
//            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    private static class DataStoreAsyncTaskParams {
        KeyboardDynamics data;
        ArrayList<Double> x;
        ArrayList<Double> y;
//        String prefs_id;
//        String prefs_age;
//        String prefs_gender;
//        String prefs_health;
//        String storageConnectionString;
//        String storageContainer;

        DataStoreAsyncTaskParams(KeyboardDynamics data,  ArrayList<Double> x, ArrayList<Double> y) {
            this.data = data;
            this.x = new ArrayList<Double>(x);
            this.y = new ArrayList<Double>(y);
//            this.prefs_id=prefs_id;
//            this.prefs_age=prefs_age;
//            this.prefs_gender=prefs_gender;
//            this.prefs_health=prefs_health;
//            this.storageConnectionString=storageConnectionString;
//            this.storageContainer=storageContainer;
        }
    }


    private static class onShowNotificationAsync extends AsyncTask<Void, Void, String> {
        WeakReference<Context> weakContext;

        private onShowNotificationAsync (Context context){
            weakContext = new WeakReference<Context>(context);
        }

        @Override
        protected String doInBackground(Void... params) {

            //notification
            int notificationFlag = 0;
            Date StartDateTimeTemp=new Date(System.currentTimeMillis());


            if (latestNotificationTime != 0) {
                long minutesPassed = TimeUnit.MINUTES.convert(StartDateTimeTemp.getTime() - latestNotificationTimeTemp.getTime(), TimeUnit.MILLISECONDS);

                if(laterPressed && minutesPassed>=120){
//                    sessionData.CurrentMood =currentMood+" TIMEOUT";
//                    sessionData.CurrentPhysicalState=currentPhysicalState+" TIMEOUT";
                    notificationFlag = 1;
                    laterPressed=false;
                }else if(minutesPassed >= 120 || (sessionsCounter>=30 && minutesPassed>=90)){
                    notificationFlag = 1;
//                    sessionData.CurrentMood = currentMood + " TIMEOUT";
//                    sessionData.CurrentPhysicalState = currentPhysicalState + " TIMEOUT";
                }else{
                    notificationFlag = 0;
                }
            } else {
                notificationFlag = 1;
//                sessionData.CurrentMood = "undefined";
//                sessionData.CurrentPhysicalState="undefined";

            }

            if (notificationFlag == 1 ) {
                String title = "TypeOfMood";
                String message = "Please Expand to describe your mood!";
                sessionsCounter=0;


//                mNotificationHelperPhysical = new NotificationHelperPhysical(weakContext.get());
//                NotificationCompat.Builder nbPhysical = mNotificationHelperPhysical.getTypeOfMoodNotification(title, message);
//                mNotificationHelperPhysical.getManager().notify(mNotificationHelperPhysical.notification_id, nbPhysical.build());


                nb = mNotificationHelper.getTypeOfMoodNotification(title, message,nb);
                mNotificationHelper.getManager().notify(mNotificationHelper.notification_id, nb.build());
//                CreateAlertDialogWithRadioButtonGroup();



            }
            return "success";


        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
//            Log.d("SQL","Tried to save data with result: "+result);
//            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toUpperCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toUpperCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }
        return "undefined";
    }




}
