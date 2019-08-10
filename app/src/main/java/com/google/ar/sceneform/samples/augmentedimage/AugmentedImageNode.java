/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

  private static final String TAG = "AugmentedImageNode";

  // The augmented image represented by this node.
  private AugmentedImage image;

  // Add a member variable to hold the maze model.
  private Node middleNode;
  private Node rightNode;
  private Node leftNode;
  // Add a variable called mazeRenderable for use with loading
  // GreenMaze.sfb.
  private CompletableFuture<ModelRenderable> redRenderable;
  private CompletableFuture<ModelRenderable> greenRenderable;

  private OnTapListener leftListen;
  private OnTapListener midListen;
  private OnTapListener rightListen;

  private boolean leftSwitch = false;
  private boolean midSwitch = false;
  private boolean rightSwitch = false;


//  private float middle_scale = 0.1;



  public AugmentedImageNode(Context context) {
    String green = "green_tick.sfb";
    greenRenderable =
            ModelRenderable.builder()
                    .setSource(context, Uri.parse("green_tick.sfb"))
                    .build();
    redRenderable =
            ModelRenderable.builder()
                    .setSource(context, Uri.parse("color_red.sfb"))
                    .build();

    //Setting up on tap listener to do whatever we want when we click it
    leftListen = new OnTapListener() {
      @Override
      public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if(leftSwitch) {
          leftSwitch = false;
        }
        else {
          leftSwitch = true;
        }
        if(leftSwitch) {
          leftNode.setRenderable(redRenderable.getNow(null));
        }
        else {
          leftNode.setRenderable(greenRenderable.getNow(null));
        }
      }
    };
    midListen = new OnTapListener() {
      @Override
      public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if(midSwitch) {
          midSwitch = false;
        }
        else {
          midSwitch = true;
        }
        if(midSwitch) {
          middleNode.setRenderable(redRenderable.getNow(null));
        }
        else {
          middleNode.setRenderable(greenRenderable.getNow(null));
        }
      }
    };
    rightListen = new OnTapListener() {
      @Override
      public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if(rightSwitch) {
          rightSwitch = false;
        }
        else {
          rightSwitch = true;
        }
        if(rightSwitch) {
          rightNode.setRenderable(redRenderable.getNow(null));
        }
        else {
          rightNode.setRenderable(greenRenderable.getNow(null));
        }
      }
    };
  }

  /**
   * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
   * created based on an Anchor created from the image. The corners are then positioned based on the
   * extents of the image. There is no need to worry about world coordinates since everything is
   * relative to the center of the image, which is the parent node of the corners.
   */
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})

  public void setImage(AugmentedImage image) {
    this.image = image;

    // Initialize mazeNode and set its parents and the Renderable.
    // If any of the models are not loaded, process this function
    // until they all are loaded.
    if (!greenRenderable.isDone() || !redRenderable.isDone()) {
      CompletableFuture.allOf(greenRenderable, redRenderable)
              .thenAccept((Void aVoid) -> setImage(image))
              .exceptionally(
                      throwable -> {
                        Log.e(TAG, "Exception loading", throwable);
                        return null;
                      });
      return;
    }
    // Set the anchor based on the center of the image.
    setAnchor(image.createAnchor(image.getCenterPose()));



    middleNode = new Node();
    middleNode.setParent(this);
    middleNode.setOnTapListener(midListen);
    if(midSwitch) {
      middleNode.setRenderable(redRenderable.getNow(null));
    }
    else {
      middleNode.setRenderable(greenRenderable.getNow(null));
    }

    //activating listener in the middle one


    rightNode = new Node();
    rightNode.setParent(this);
    rightNode.setLocalPosition(new Vector3(0.0f,0.0f,-0.3f*image.getExtentZ()));
    rightNode.setOnTapListener(rightListen);
    if(rightSwitch) {
      rightNode.setRenderable(redRenderable.getNow(null));
    }
    else {
      rightNode.setRenderable(greenRenderable.getNow(null));
    }

    leftNode = new Node();
    leftNode.setParent(this);
    leftNode.setLocalPosition(new Vector3(0.0f,0.0f,0.3f*image.getExtentZ()));
    leftNode.setOnTapListener(leftListen);
    if(leftSwitch) {
      leftNode.setRenderable(redRenderable.getNow(null));
    }
    else {
      leftNode.setRenderable(greenRenderable.getNow(null));
    }
  }

  public AugmentedImage getImage() {
    return image;
  }
}
