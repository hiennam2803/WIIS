package vhdt.wiis.data.local

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.serialization.json.Json
import vhdt.wiis.model.SchoolPlugin

object PluginLoader {
    private const val TAG = "PluginLoader"
    private const val PLUGINS_FOLDER = "plugins"
    private const val JSON_SUFFIX = ".json"
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Throws(Exception::class)
    fun loadBuiltinPlugins(context: Context): List<SchoolPlugin> {
        return try {
            val pluginFiles = context.assets.list(PLUGINS_FOLDER) ?: emptyArray()
            pluginFiles
                .filter { it.endsWith(JSON_SUFFIX) }
                .mapNotNull { fileName ->
                    try {
                        val inputStream = context.assets.open("$PLUGINS_FOLDER/$fileName")
                        val jsonString = inputStream.bufferedReader().use { it.readText() }
                        val plugin = json.decodeFromString<SchoolPlugin>(jsonString)
                        if (validatePlugin(plugin)) {
                            Log.d(TAG, "Successfully loaded plugin: ${plugin.id}")
                            plugin
                        } else {
                            Log.w(TAG, "Plugin validation failed: $fileName")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading plugin file: $fileName", e)
                        null
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading builtin plugins from assets", e)
            emptyList()
        }
    }
    
    @Throws(Exception::class)
    fun loadPluginFromUri(context: Context, uri: Uri): SchoolPlugin? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: run {
                    Log.e(TAG, "Cannot open input stream for URI: $uri")
                    return null
                }
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val plugin = json.decodeFromString<SchoolPlugin>(jsonString)
            if (validatePlugin(plugin)) {
                Log.d(TAG, "Successfully loaded plugin from URI: ${plugin.id}")
                plugin
            } else {
                Log.w(TAG, "Plugin validation failed for URI: $uri")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading plugin from URI: $uri", e)
            null
        }
    }
    
    fun validatePlugin(plugin: SchoolPlugin): Boolean {
        val isValid = plugin.id.isNotBlank() &&
            plugin.schoolName.isNotBlank() &&
            plugin.loginUrl.isNotBlank() &&
            plugin.scheduleEndpoint.isNotBlank()
        
        if (!isValid) {
            Log.w(TAG, "Plugin validation failed - missing required fields in: ${plugin.id}")
        }
        
        return isValid
    }
}
