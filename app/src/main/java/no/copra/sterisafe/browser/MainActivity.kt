package no.copra.sterisafe.browser

import android.R.attr
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.R.attr.description
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ProgressBar
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar

    private var tag = "MainActivity"
    private var wifiAvailable = false
    private var sterisafeUrl = "http://192.168.111.1/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = super.getBaseContext()
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback()
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        progressBar = findViewById(R.id.progressBar)


        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (Uri.parse(url).host == Uri.parse(sterisafeUrl).host) {
                    return false
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.INVISIBLE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                progressBar.visibility = View.INVISIBLE
                Log.e("error", "ReceivedError on WebView. ERROR CODE is $error")
                Log.e("error", "description is " + description)
                Log.e("error", "failingUrl is ${request?.url}")
                try {
                    view!!.loadUrl("file:///android_asset/www/error.html?errorCode=" + error.toString() + "&errorDescription=" + description)
                } catch (e: Exception) {
                    Log.e("error", e.toString())
                }
            }


        }
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.loadUrl(sterisafeUrl)

        updateWifiIcon(wifiAvailable)


        imageView.setOnClickListener {
            Log.i(tag, "click!")
            webView.loadUrl(sterisafeUrl)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun updateWifiIcon(available: Boolean) {
        wifiAvailable = available
        if (this::imageView.isInitialized) {
            if (available) {
                imageView.setImageResource(android.R.drawable.presence_online)
            } else {
                imageView.setImageResource(android.R.drawable.presence_offline)

            }
        }
        if (this::textView.isInitialized) {
            if (available) {
                textView.setText("WIFI connected")
            } else {
                textView.setText("WIFI not connected!")
            }
        }
    }

    fun networkCallback() {
        val builder = NetworkRequest.Builder()
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

        val networkRequest = builder.build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.i(tag, network.toString())
                    updateWifiIcon(true)
                    connectivityManager.bindProcessToNetwork(network)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    updateWifiIcon(false)
                    connectivityManager.bindProcessToNetwork(null)
                }
            })
    }
}