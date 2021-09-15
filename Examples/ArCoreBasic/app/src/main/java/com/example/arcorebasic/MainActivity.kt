package com.example.arcorebasic

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class MainActivity : AppCompatActivity() {

    private lateinit var arFrag: ArFragment
    private var renderable: ModelRenderable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        arFrag = supportFragmentManager.findFragmentById(
                R.id.sceneform_fragment) as ArFragment

        ModelRenderable.builder()
        .setSource(this, Uri.parse("https://github.com/Cyroxin/AndroidStudioExamples/raw/main/Examples/ArCoreBasic/app/sampledata/plaque.glb"))
            .setIsFilamentGltf(true)
            .setAsyncLoadEnabled(true)
            .setRegistryId("CesiumMan")
            .build()
            .thenAccept { renderable = it }
            .exceptionally {
                Log.e("ERR", "something went wrong ${it.localizedMessage}")
                null
                }

        arFrag.setOnTapArPlaneListener { hitResult: HitResult?, _, _ ->
            renderable ?: return@setOnTapArPlaneListener
            //Creates a new anchor at the hit location
            val anchor = hitResult!!.createAnchor()
            //Creates a new anchorNode attaching it to anchor
            val anchorNode = AnchorNode(anchor)
            // Add anchorNode as root scene node's child
            anchorNode.setParent(arFrag.arSceneView.scene)
            // Can be selected, rotated...
            val viewNode = TransformableNode(arFrag.transformationSystem)
            viewNode.renderable = renderable
            // Add viewNode as anchorNode's child
            viewNode.setParent(anchorNode)
            // Sets this as the selected node in the TransformationSystem
            viewNode.select()
        }
    }
}