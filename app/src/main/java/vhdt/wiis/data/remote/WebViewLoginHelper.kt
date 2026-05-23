package vhdt.wiis.data.remote

import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import vhdt.wiis.model.SchoolPlugin

@Composable
fun WebViewLoginScreen(
    plugin: SchoolPlugin,
    onTokenReceived: (String) -> Unit,
    onDismiss: () -> Unit
)   {
    val isLoading = remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    val jwtBridge = JwtBridge { token ->
                        onTokenReceived(token)
                        onDismiss()
                    }
                    addJavascriptInterface(jwtBridge, "AndroidBridge")
                    webViewClient = LoginWebViewClient(
                        onLoadingStateChanged = { loading ->
                            isLoading.value = loading
                        }
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                    }
                    loadUrl(plugin.loginUrl) 
                }
            }
        )
        
        if (isLoading.value) {
            CircularProgressIndicator()
        }
    }
}

private class JwtBridge(private val onTokenReceived: (String) -> Unit) {
    @JavascriptInterface
    fun onLoginSuccess(token: String) {
        onTokenReceived(token)
    }
}

private class LoginWebViewClient(
    private val onLoadingStateChanged: (Boolean) -> Unit
) : WebViewClient() {
    
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onLoadingStateChanged(true)
    }
    
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onLoadingStateChanged(false)
        
        // Inject JavaScript to intercept fetch and XMLHttpRequest
        injectInterceptionScript(view)
    }
    
    private fun injectInterceptionScript(webView: WebView?) {
        if (webView == null) return
        
        val jsCode = """
            (function() {
                // Intercept fetch
                const originalFetch = window.fetch;
                window.fetch = function(...args) {
                    return originalFetch.apply(this, args)
                        .then(response => {
                            response.clone().json().then(data => {
                                if (data.success === true && data.token) {
                                    AndroidBridge.onLoginSuccess(data.token);
                                }
                            }).catch(() => {});
                            return response;
                        })
                        .catch(error => {
                            throw error;
                        });
                };
                
                // Intercept XMLHttpRequest
                const originalOpen = XMLHttpRequest.prototype.open;
                const originalSend = XMLHttpRequest.prototype.send;
                
                XMLHttpRequest.prototype.open = function(method, url, ...args) {
                    this._url = url;
                    this._method = method;
                    return originalOpen.apply(this, [method, url, ...args]);
                };
                
                XMLHttpRequest.prototype.send = function(...args) {
                    this.addEventListener('load', function() {
                        try {
                            const data = JSON.parse(this.responseText);
                            if (data.success === true && data.token) {
                                AndroidBridge.onLoginSuccess(data.token);
                            }
                        } catch (e) {}
                    });
                    return originalSend.apply(this, args);
                };
            })();
        """.trimIndent()
        
        webView.evaluateJavascript(jsCode, null)
    }
}
