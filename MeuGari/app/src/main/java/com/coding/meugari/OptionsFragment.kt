package com.coding.meugari

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView

class OptionsFragment : Fragment() {

    //private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_options, container, false)
//        try {
//            val pi: PackageInfo = activity?.packageManager!!.getPackageInfo(activity?.packageName!!, 0)
//            val lbVersion = view?.findViewById<TextView>(R.id.lbVersion)
//            lbVersion?.text = getString(R.string.app_label_version, pi.versionName);
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//        }

        //webView = view?.findViewById<WebView>(R.id.webView)!!


        val webView = view?.findViewById<WebView>(R.id.webView) ?: return view
        // WebViewClient allows you to handle
        // onPageFinished and override Url loading.

        webView.webViewClient = WebViewClient()

        // this will load the url of the website
        webView.loadUrl("https://www.reciclasampa.com.br/conteudos/noticias")

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        webView.settings.javaScriptEnabled = true

        // if you want to enable zoom feature
        webView.settings.setSupportZoom(true)

        return view
    }

}