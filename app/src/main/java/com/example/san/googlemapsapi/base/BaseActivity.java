package com.example.san.googlemapsapi.base;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.san.googlemapsapi.R;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.registerable.connection.Connectable;
import com.novoda.merlin.registerable.disconnection.Disconnectable;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

/**
 * Created by San on 1/13/2018.
 */

public class BaseActivity extends AppCompatActivity /*implements NetworkChangeReceiver.NetworkChangeListener*/ {
    private ACProgressFlower mDialog;
    private Dialog mDialogMessengeNetwork = null;
    private Merlin mMerlin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mDialog = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Loading...")
                .fadeColor(Color.DKGRAY).build();
        super.onCreate(savedInstanceState);
        mMerlin = new Merlin.Builder().withAllCallbacks().build(getApplicationContext());
        mMerlin.registerConnectable(mRegisterConnectable);
        mMerlin.registerDisconnectable(mDisconnectable);
    }

    private Connectable mRegisterConnectable = new Connectable() {
        @Override
        public void onConnect() {
            if(mDialogMessengeNetwork != null && mDialogMessengeNetwork.isShowing()) mDialogMessengeNetwork.dismiss();
        }
    };

    private Disconnectable mDisconnectable = new Disconnectable() {
        @Override
        public void onDisconnect() {
            innitDialog();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMerlin.bind();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMerlin.unbind();
    }

    protected void showDialog() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    protected void hideDialog() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void innitDialog() {
        if (mDialogMessengeNetwork == null) {
            mDialogMessengeNetwork = new Dialog(this, R.style.FullHeightDialog);

            mDialogMessengeNetwork.setContentView(R.layout.yes_no_messenge_dialog);
            mDialogMessengeNetwork.setCancelable(false);

            TextView message = mDialogMessengeNetwork.findViewById(R.id.text_message_dialog);
            message.setText("Vui lòng kiểm tra lại kết nối internet!");

            Button yes = mDialogMessengeNetwork.findViewById(R.id.button_dialog_yes);
            yes.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    mDialogMessengeNetwork.dismiss();
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                }
            });

            Button no = mDialogMessengeNetwork.findViewById(R.id.button_dialog_no);
            no.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mDialogMessengeNetwork.dismiss();
                    finishAffinity();
                }
            });
        }
        mDialogMessengeNetwork.show();
    }

}
