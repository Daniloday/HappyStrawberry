package com.hyapp.sytrrraewb.mainfolder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hyapp.sytrrraewb.R
import com.hyapp.sytrrraewb.databinding.FragmentWebBinding
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebFragment : Fragment() {

    private val bind : FragmentWebBinding by viewBinding()

    var callbackUploadData : ValueCallback<Array<Uri>>? = null

    private val data by navArgs<WebFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web, container, false)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != 777 || callbackUploadData == null) {
            return
        }
        else {
            var results: Array<Uri>? = null
            if (resultCode == AppCompatActivity.RESULT_OK) {
                data?.let {
                    val dataString = data.dataString
                    val clipData = data.clipData
                    clipData?.let {
                        results = Array(clipData.itemCount) {
                            clipData.getItemAt(it).uri
                        }
                    }
                    dataString?.let {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
            callbackUploadData?.onReceiveValue(results)
            callbackUploadData = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind.web.settings.apply {
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
            setAppCacheEnabled(true)
            domStorageEnabled = true
            javaScriptEnabled = true
            mixedContentMode = 0
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            loadWithOverviewMode = true
            setEnableSmoothTransition(true)
            savePassword = true
            saveFormData = true
            databaseEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
            userAgentString = userAgentString.replace("; wv", "")
        }
        bind.web.apply {
            isSaveEnabled = true
            isFocusable = true
            isFocusableInTouchMode = true
            webViewClient = @Obfuscate object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    request?.url?.let {
                        if (it.toString().contains("http://localhost")) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                transferToPlay()
                            }

                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    if (request?.url?.toString()?.startsWith("https://t.me/joinchat") == true) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                    return if (request?.url == null ||
                        request.url?.toString()?.startsWith("http://")==true ||
                        request.url?.toString()?.startsWith("https://")==true)
                        false
                    else
                        try {
                            if (request.url?.toString()?.startsWith("mailto") == true) {
                                startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "plain/text"
                                            putExtra(
                                                Intent.EXTRA_EMAIL, request.url.toString().replace("mailto:", ""))
                                        },
                                        "Mail"))
                            } else if (request.url?.toString()?.startsWith("tel:")==true) {
                                startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse(url)
                                        },
                                        "Call"))
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            view!!.context.startActivity(intent)
                            true
                        } catch (e: java.lang.Exception) {
                            true
                        }
                }
            }
            webChromeClient = @Obfuscate object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback:
                    ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    callbackUploadData = filePathCallback
                    startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }, "Image Chooser"), 777)
                    return true
                }
            }
        }
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (bind.web.canGoBack()) {
                bind.web.goBack()
            }
        }
        bind.web.loadUrl(data.url)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        bind.web.saveState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            bind.web.restoreState(savedInstanceState)
        }
    }

    private fun transferToPlay(){
        findNavController().navigate(WebFragmentDirections.actionWebFragmentToPlayFragment())
    }


}