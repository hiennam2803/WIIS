package vhdt.wiis.data.remote

import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import vhdt.wiis.model.SchoolPlugin
import vhdt.wiis.R

@Composable
fun WebViewLoginScreen(
    plugin: SchoolPlugin,
    onTokenReceived: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isLoading = remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Part 1 — Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1856BB))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = plugin.schoolName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_24px),
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Part 2 — Loading bar
                if (isLoading.value) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1856BB)
                    )
                }

                // Part 3 — WebView
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
                                databaseEnabled = true
                            }
                            loadUrl(plugin.loginUrl)
                        }
                    }
                )
            }
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
