package se.pp.mc.android.Gerberoid.tasks

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Looper
import se.pp.mc.android.Gerberoid.gerber.Layers
import se.pp.mc.android.Gerberoid.model.FileType
import se.pp.mc.android.Gerberoid.utils.FileUtils.toInternalLayerFile
import java.io.File
import java.io.Serializable
import java.lang.ref.WeakReference

class LayerLoadTask(applicationContext: Context, private val layers: Layers, callback: LayerLoadCallback) : AsyncTask<LoadRequest<out SourceDescriptor>, Void, Pair<Boolean, List<GerberFile<out SourceDescriptor>>>>() {

    private val weakContext = WeakReference(applicationContext)
    private val weakCallback = WeakReference(callback)

    override fun onPreExecute() {
        weakCallback.get()?.onStarted()
        super.onPreExecute()
    }

    override fun doInBackground(vararg request: LoadRequest<out SourceDescriptor>): Pair<Boolean, List<GerberFile<out SourceDescriptor>>> {
        Looper.prepare()

        var success = true
        val firstLayer = this.layers.activeLayer

        request.mapIndexed { index, r -> load(r, firstLayer + index) }.forEach { r ->

            if(r?.file != null) {

                try {

                    success = success && if (r.gfile.type == FileType.GERBER) {
                        layers.LoadGerber(r.file)
                    } else {
                        layers.LoadDrill(r.file)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                success = false
            }

        }

        return Pair(success, request.map { it.gfile }.toList())

    }

    private fun load(request: LoadRequest<out SourceDescriptor>, layer: Int) : LoadResult<out SourceDescriptor>? {

        val context = weakContext.get() ?: return null

        val result = when(request.gfile.source){

            is UriSourceDescriptor -> toInternalLayerFile(context, request.gfile.source.uri, layer)
            is FileSourceDescriptor -> toInternalLayerFile(context, request.gfile.source.file, layer)
            else -> null

        }

        return LoadResult(request.gfile, result)

    }

    override fun onPostExecute(result: Pair<Boolean, List<GerberFile<out SourceDescriptor>>>) {
        weakCallback.get()?.onFinished(result.first, result.second.toList() )
    }

}

interface SourceDescriptor {

    fun asString() : String
    fun type() : String

}

data class FileSourceDescriptor(val file : File) : SourceDescriptor, Serializable {

    override fun asString() : String {
        return file.absolutePath
    }

    override fun type() : String {
        return "file"
    }

}

data class UriSourceDescriptor(val uri : Uri) : SourceDescriptor, Serializable {

    override fun asString(): String {
        return uri.toString()
    }

    override fun type(): String {
        return "uri"
    }

}

data class GerberFile<T : SourceDescriptor>(val source : T, val type : FileType)

private data class LoadResult<T : SourceDescriptor>(val gfile : GerberFile<T>, val file : File?)
data class LoadRequest<T : SourceDescriptor>(val gfile : GerberFile<T>)


interface LayerLoadCallback {
    fun onStarted()
    fun onFinished(success: Boolean, files: List<GerberFile<out SourceDescriptor>>)
}