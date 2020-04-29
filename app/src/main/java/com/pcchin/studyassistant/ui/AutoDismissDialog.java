/*
 * Copyright 2020 PC Chin. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pcchin.studyassistant.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/** An AlertDialog that would br dismissed when rotated.
 * The default button values for the arrays are [BUTTON_POSITIVE, BUTTON_NEGATIVE or BUTTON_NEUTRAL].
 * An IllegalArgumentException will be thrown in the constructor when any of the arrays provided
 * does not have a length of 3.
 * The AlertDialog will be dismissed when it is rotated based on its tag in MainActivity. **/
public class AutoDismissDialog extends DialogFragment {
    private static final int DIALOG_CONTENT_MESSAGE = 1;
    private static final int DIALOG_CONTENT_VIEW = 2;
    private static final int DIALOG_CONTENT_LIST = 3;

    private static final String OK = "OK";
    private static final String CANCEL = "Cancel";
    private static final String ERROR_LENGTH_BTN_LIST = "Length of buttonList is not 3";
    private static final String ERROR_LENGTH_BTN_OR_LISTENER = "Length of buttonList is not 3 or " +
            "length of yListeners is not 3";

    private String title, message;
    private String[] buttonList;
    private int contentType, arrayRes;
    private boolean autoDismiss, cancellable = true;

    private View displayView;
    private DialogInterface.OnClickListener arrayListener;
    private DialogInterface.OnClickListener[] yListeners = new DialogInterface.OnClickListener[0];
    private DialogInterface.OnShowListener nListener;
    private DialogInterface.OnDismissListener dismissListener = null;

    /** Default constructor, used only when the app is rotated. **/
    public AutoDismissDialog() {
        autoDismiss = true;
        contentType = DIALOG_CONTENT_MESSAGE;
    }

    /** Constructor used for showing a view with auto dismiss after a button press.
     * @param title is the title of the AlertDialog.
     * @param displayView is the view that would be displayed in the AlertDialog.
     * @param buttonList is the display values for all 3 buttons. **/
    public AutoDismissDialog(String title, View displayView, String[] buttonList) {
        this(title, displayView, buttonList, new DialogInterface
                .OnClickListener[]{null, null, null});
    }

    /** Constructor used for showing a view with auto dismiss after a button pass.
     * The button list of new String[]{OK, CANCEL, ""} will be used as the default button list.
     * @param title is the title of the AlertDialog.
     * @param displayView is the view that would be displayed in the AlertDialog.
     * @param yListeners are the listeners that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, View displayView, DialogInterface.OnClickListener[] yListeners) {
        this(title, displayView, new String[]{OK, CANCEL, ""}, yListeners);
    }

    /** Constructor used for showing a view with auto dismiss after a button pass.
     * The button list of new String[]{OK, CANCEL, ""} will be used as the default button list.
     * @param title is the title of the AlertDialog.
     * @param message is the message of the AlertDialog.
     * @param yListeners are the listeners that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, String message, DialogInterface.OnClickListener[] yListeners) {
        this(title, message, new String[]{OK, CANCEL, ""}, yListeners);
    }

    /** Constructor used for showing a message with auto dismiss after a button press.
     * @param title is the title of the AlertDialog.
     * @param message is the message of the AlertDialog.
     * @param buttonList is the display values for all 3 buttons.
     * @param yListeners are the listeners that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, String message, @NonNull String[] buttonList,
                             DialogInterface.OnClickListener[] yListeners) {
        if (buttonList.length == 3 && yListeners.length == 3) {
            this.title = title;
            this.message = message;
            this.buttonList = buttonList;
            this.yListeners = yListeners;
            this.contentType = DIALOG_CONTENT_MESSAGE;
            this.autoDismiss = true;
        } else {
            throw new IllegalArgumentException(ERROR_LENGTH_BTN_OR_LISTENER);
        }
    }

    /** Constructor used for showing a view with auto dismiss after a button press.
     * @param title is the title of the AlertDialog.
     * @param displayView is the view that would be displayed in the AlertDialog.
     * @param buttonList is the display values for all 3 buttons.
     * @param yListeners are the listeners that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, View displayView, String[] buttonList,
                             DialogInterface.OnClickListener[] yListeners) {
        if (buttonList.length == 3 && yListeners.length == 3) {
            this.title = title;
            this.displayView = displayView;
            this.buttonList = buttonList;
            this.yListeners = yListeners;
            this.contentType = DIALOG_CONTENT_VIEW;
            this.autoDismiss = true;
        } else {
            throw new IllegalArgumentException(ERROR_LENGTH_BTN_OR_LISTENER);
        }
    }

    /** Constructor used for showing a view which would only dismiss after the
     * correct button is pressed.
     * @param title is the title of the AlertDialog.
     * @param arrayRes is the resource file pointing to a string array to be used in the AlertDialog
     * @param arrayListener is the OnClickListener for the elements in the array.
     * @param buttonList is the display values for all 3 buttons.
     * @param yListeners are the listeners that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, int arrayRes, DialogInterface.OnClickListener arrayListener,
                             String[] buttonList, DialogInterface.OnClickListener[] yListeners) {
        if (buttonList.length == 3 && yListeners.length == 3) {
            this.title = title;
            this.arrayRes = arrayRes;
            this.buttonList = buttonList;
            this.arrayListener = arrayListener;
            this.contentType = DIALOG_CONTENT_LIST;
            this.autoDismiss = true;
        } else {
            throw new IllegalArgumentException(ERROR_LENGTH_BTN_OR_LISTENER);
        }
    }

    /** Constructor used for showing a message which would only dismiss after the
     * correct button is pressed.
     * @param title is the title of the AlertDialog.
     * @param message is the message of the AlertDialog.
     * @param buttonList is the display values for all 3 buttons.
     * @param nListener is the OnShowListener that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, String message, @NonNull String[] buttonList,
                             DialogInterface.OnShowListener nListener) {
        if (buttonList.length == 3) {
            this.title = title;
            this.message = message;
            this.buttonList = buttonList;
            this.nListener = nListener;
            this.contentType = DIALOG_CONTENT_MESSAGE;
            this.autoDismiss = false;
        } else {
            throw new IllegalArgumentException(ERROR_LENGTH_BTN_LIST);
        }
    }

    /** Constructor used for showing a view with auto dismiss after a button pass.
     * The button list of new String[]{OK, CANCEL, ""} will be used as the default button list.
     * @param title is the title of the AlertDialog.
     * @param displayView is the view that would be displayed in the AlertDialog.
     * @param nListener is the OnShowListener that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, View displayView, DialogInterface.OnShowListener nListener) {
        this(title, displayView, new String[]{OK, CANCEL, ""}, nListener);
    }

    /** Constructor used for showing a view which would only dismiss after the
     * correct button is pressed.
     * @param title is the title of the AlertDialog.
     * @param displayView is the view that would be displayed in the AlertDialog.
     * @param buttonList is the display values for all 3 buttons.
     * @param nListener is the OnShowListener that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, View displayView, @NonNull String[] buttonList,
                             DialogInterface.OnShowListener nListener) {
        if (buttonList.length == 3) {
            this.title = title;
            this.displayView = displayView;
            this.buttonList = buttonList;
            this.nListener = nListener;
            this.contentType = DIALOG_CONTENT_VIEW;
            this.autoDismiss = false;
        } else {
            throw new IllegalArgumentException(ERROR_LENGTH_BTN_LIST);
        }
    }

    /** Sets the OnDismissListener for the AlertDialog. **/
    public void setDismissListener(DialogInterface.OnDismissListener listener) {
        dismissListener = listener;
    }

    /** Sets whether the AlertDialog can be cancelled **/
    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    /** Dismiss the dialog if the last one is still showing. **/
    @Override
    public void onStart() {
        if (getDialog() != null && getDialog().isShowing()) {
            getDialog().dismiss();
        }
        super.onStart();
    }

    /** Creates the actual AlertDialog based on the given parameters.
     * A NullPointerException may be thrown through requireContext.. **/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(title);
        if (contentType == DIALOG_CONTENT_VIEW) {
            builder.setView(displayView);
        } else if (contentType == DIALOG_CONTENT_MESSAGE) {
            builder.setMessage(message);
        } else if (contentType == DIALOG_CONTENT_LIST) {
            builder.setItems(arrayRes, arrayListener);
        }
        if (autoDismiss && buttonList != null && yListeners != null
                && contentType != DIALOG_CONTENT_LIST) {
            builder.setPositiveButton(buttonList[0], yListeners[0]);
            builder.setNegativeButton(buttonList[1], yListeners[1]);
            builder.setNeutralButton(buttonList[2], yListeners[2]);
        } else if (buttonList != null) {
            builder.setPositiveButton(buttonList[0], null)
                    .setNegativeButton(buttonList[1], null)
                    .setNeutralButton(buttonList[2], null);
        }
        setCancelable(cancellable);

        // Sets the dialog listeners if autoDismiss is false
        AlertDialog dialog = builder.create();
        if (!autoDismiss) {
            dialog.setOnShowListener(nListener);
        }
        if (dismissListener != null) {
            dialog.setOnDismissListener(dismissListener);
        }
        return dialog;
    }
}
