/*
 * Copyright (C) The Android Open Source Project
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
package com.yash.ocr;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.yash.ocr.camera.GraphicOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
    private GraphicOverlay<OcrGraphic> graphicOverlay;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        graphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();

        synchronized (sb) {

            sb = new StringBuffer();
            list = new ArrayList<>();

            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                List<Line> lines = (List<Line>) item.getComponents();

            /*if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                graphicOverlay.add(graphic);
            }*/
                for (Line line : lines) {
                    String lineStr = line.getValue();
                    Log.e("Line : ", "" + lineStr);
                    OcrGraphic graphic = new OcrGraphic(graphicOverlay, line);
                    list.add(line);
                    graphicOverlay.add(graphic);
                    sb.append(lineStr);
                    sb.append('\n');
                }
            }
        }
    }

    ArrayList<Line> list = new ArrayList<>();
    StringBuffer sb = new StringBuffer();

    class LineSort implements Comparator<Line> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Line a, Line b) {
            return a.getBoundingBox().top - b.getBoundingBox().top;
        }
    }

    public String getData() {
        return sb.toString();
    }

    public ArrayList<Line> getListData() {
        Collections.sort(list, new LineSort());
        return list;
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        graphicOverlay.clear();
    }
}
