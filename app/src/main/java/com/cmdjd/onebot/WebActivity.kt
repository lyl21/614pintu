package com.cmdjd.onebot

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener


class WebActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var permissionRequest: PermissionRequest? = null
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kdsk)
        webView = findViewById(R.id.webview)
        val settings = webView.settings
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        webView.clearCache(true)
        settings.mediaPlaybackRequiresUserGesture = false
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(JsInterface(this), "jsBridge")
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.setSupportMultipleWindows(true)
        webView.webChromeClient = object : WebChromeClient() {


            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                fileUploadCallback = filePathCallback
                showTheOptionsDialog()
                return true
            }
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
            override fun onPermissionRequest(request: PermissionRequest) {
                val permissions = request.resources
                for (resource in permissions) {
                    if (resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                        permissionRequest = request
                        showTheOptionsDialog()
                    }
                }
            }
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult): Boolean {
                result.confirm()
                return true
            }


        }
        val intent = intent
        val url = intent.getStringExtra("url")!!
        webView.loadUrl(url)
    }


    private fun theFile(){
        XXPermissions.with(this@WebActivity)
            .permission(Permission.READ_MEDIA_IMAGES)
            .permission(Permission.READ_MEDIA_VIDEO)
            .request(object : OnPermissionCallback {
                override fun onGranted(
                    permissions: MutableList<String>, allGranted: Boolean
                ) {
                    PictureSelector.create(this@WebActivity).openSystemGallery(SelectMimeType.ofImage())
                        .setSelectionMode( SelectModeConfig.SINGLE).forSystemResult(object : OnResultCallbackListener<LocalMedia?> {
                            override fun onResult(result: ArrayList<LocalMedia?>?) {
                                result?.let {
                                    val path =it[0]!!.availablePath
                                    var results: Array<Uri>? = null
                                    results = arrayOf(Uri.parse(path))
                                    if (fileUploadCallback == null) {
                                        return
                                    }
                                    fileUploadCallback!!.onReceiveValue(results)
                                    fileUploadCallback = null
                                }
                            }
                            override fun onCancel() {
                                fileUploadCallback!!.onReceiveValue(null)
                                fileUploadCallback = null
                            }
                        })
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    fileUploadCallback!!.onReceiveValue(null)
                    fileUploadCallback = null
                    if (doNotAskAgain) {
                        Toast.makeText(this@WebActivity, getString(R.string.use_of), Toast.LENGTH_SHORT).show()
                        XXPermissions.startPermissionActivity(this@WebActivity, permissions)
                    } else {
                        Toast.makeText(this@WebActivity, getString(R.string.use_of), Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
    private fun showTheOptionsDialog() {
        val options = arrayOf(getString(R.string.camera), getString(R.string.album))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_image_source))
        builder.setCancelable(false)
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    camera()
                }
                1 -> {
                    theFile()
                }
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
            fileUploadCallback!!.onReceiveValue(null)
            fileUploadCallback = null
        }
        builder.show()
    }
    private fun camera(){
        XXPermissions
            .with(this@WebActivity)
            .permission(Permission.CAMERA)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if(permissionRequest != null){
                        permissionRequest!!.grant(permissionRequest!!.resources)
                        permissionRequest!!.origin
                    }else{
                        PictureSelector.create(this@WebActivity).openCamera(SelectMimeType.ofImage())
                            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                                override fun onResult(result: ArrayList<LocalMedia?>?) {
                                    result?.let {
                                        val path =it[0]!!.availablePath
                                        var results: Array<Uri>? = null
                                        results = arrayOf(Uri.parse(path))
                                        if (fileUploadCallback == null) {
                                            return
                                        }
                                        fileUploadCallback!!.onReceiveValue(results)
                                        fileUploadCallback = null
                                    }
                                }
                                override fun onCancel() {
                                    fileUploadCallback!!.onReceiveValue(null)
                                    fileUploadCallback = null
                                }
                            })
                    }

                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    fileUploadCallback!!.onReceiveValue(null)
                    fileUploadCallback = null
                    if (doNotAskAgain) {
                        Toast.makeText(this@WebActivity, getString(R.string.normal), Toast.LENGTH_SHORT).show()
                        XXPermissions.startPermissionActivity(this@WebActivity, permissions)
                    } else {
                        Toast.makeText(this@WebActivity, getString(R.string.normal), Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === RESULT_OK) {
            if (requestCode === 1) {
                if (webView == null) {
                    return
                }
//                webView.loadUrl(loadUrl)
                webView.evaluateJavascript(
                    "j a v a s c r i p t :window.closeGame()"
                ) { }
            }
        }
    }


}