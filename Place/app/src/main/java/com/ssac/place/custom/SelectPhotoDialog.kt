package com.ssac.place.custom

import android.content.Context
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.ssac.place.R

class SelectPhotoDialog(private val context: Context) {
    private val builder: AlertDialog.Builder by lazy { AlertDialog.Builder(context, R.style.TransparentDialog).setView(view) }
    private val view: View by lazy { View.inflate(context, R.layout.alert_select_picture, null) }
    private val cameraButton: Button by lazy { view.findViewById(R.id.cameraButton) }
    private val albumButton: Button by lazy { view.findViewById(R.id.albumButton) }
    private var dialog: AlertDialog? = null

    fun setOnCameraButton(listener: (view: View) -> (Unit)): SelectPhotoDialog {
        cameraButton.setOnClickListener{
            listener(it)
            dismiss()
        }
        return this
    }

    fun setOnAlbumButton(listener: (view: View) -> (Unit)): SelectPhotoDialog {
        albumButton.setOnClickListener {
            listener(it)
            dismiss()
        }
        return this
    }

    fun create() {
        dialog = builder.create()
    }

    fun show() {
        dialog = builder.create()
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}