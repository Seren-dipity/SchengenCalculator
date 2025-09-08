package com.example.schengenapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.JsResult
import android.content.Context
import android.app.Activity
import android.content.Intent
import android.util.Base64
import java.io.File
import java.nio.charset.StandardCharsets
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // JavaScript接口类，用于处理WebView中的JavaScript调用
    inner class WebAppInterface(private val context: Context) {

        @JavascriptInterface
        fun saveDataToFile(fileName: String, data: String) {
            try {
                val file = File(context.filesDir, fileName)
                file.writeText(data)
                runOnUiThread {
                    android.widget.Toast.makeText(context, "数据导出成功！", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    android.widget.Toast.makeText(context, "导出失败：${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun openFilePicker() {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            startActivityForResult(intent, 2)
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        webView = findViewById(R.id.webView)
        
        // 启用JavaScript
        webView.settings.javaScriptEnabled = true

        // 启用DOM存储
        webView.settings.domStorageEnabled = true

        // 启用数据库存储
        webView.settings.databaseEnabled = true

        // WebView性能优化设置
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT // 开启缓存
        webView.settings.allowFileAccess = true // 允许访问文件
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH) // 高渲染优先级
        webView.settings.setEnableSmoothTransition(true) // 启用平滑过渡

        // 将WebAppInterface实例添加到WebView
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        
        // 设置WebViewClient以处理页面导航
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // 保持链接在WebView内打开
                return false
            }
        }
                // 设置WebChromeClient以支持 JavaScript 的 alert/confirm 弹窗
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult
            ): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                    .setCancelable(false)
                    .create()
                    .show()
                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult
            ): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
                    .create()
                    .show()
                return true
            }
        }
        // 加载本地HTML文件
        webView.loadUrl("file:///android_asset/index.html")
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    // 处理文件选择器的返回结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                try {
                    val jsonString = contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }

                    if (!jsonString.isNullOrEmpty()) {
                        // 将JSON字符串进行Base64编码
                        val base64Encoded = Base64.encodeToString(jsonString.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

                        // 将编码后的字符串传递给JavaScript
                        webView.evaluateJavascript("javascript:handleImportedData('$base64Encoded')", null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        android.widget.Toast.makeText(this, "导入失败：${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
