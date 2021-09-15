package com.example.arcorebasic

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
//import com.google.ar.sceneform.assets.RenderableSource
//import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class MainActivity : AppCompatActivity() {

    private lateinit var arFrag: ArFragment
    private var viewRenderable: ViewRenderable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //onCreate()...
        arFrag = supportFragmentManager.findFragmentById(
                R.id.sceneform_fragment) as ArFragment
        ViewRenderable.builder()
            .setView(this, R.layout.view_renderable)
            .build()
            .thenAccept{viewRenderable = it}

        /*ViewRenderable.builder()
        .setSource(this, Uri.parse("plaque.glb"))
            .setIsFilamentGltf(true)
            .setAsyncLoadEnabled(true)
            .setRegistryId("CesiumMan")
            .build()
            .thenAccept { viewRenderable = it }
            .exceptionally {
                Log.e("ERR", "something went wrong ${it.localizedMessage}")
                null
                }*/

        arFrag.setOnTapArPlaneListener { hitResult: HitResult?, _, _ ->
            viewRenderable ?: return@setOnTapArPlaneListener
            //Creates a new anchor at the hit location
            val anchor = hitResult!!.createAnchor()
            //Creates a new anchorNode attaching it to anchor
            val anchorNode = AnchorNode(anchor)
            // Add anchorNode as root scene node's child
            anchorNode.setParent(arFrag.arSceneView.scene)
            // Can be selected, rotated...
            val viewNode = TransformableNode(arFrag.transformationSystem)
            viewNode.renderable = viewRenderable
                    // Add viewNode as anchorNode's child
            viewNode.setParent(anchorNode)
            // Sets this as the selected node in the TransformationSystem
            viewNode.select()
        }
    }
}