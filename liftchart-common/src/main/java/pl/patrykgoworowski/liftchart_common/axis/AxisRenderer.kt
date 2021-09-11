/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.dimensions.DataSetInsetter

interface AxisRenderer<Position : AxisPosition> : BoundsAware, DataSetInsetter {

    val position: Position
    val dataSetBounds: RectF
    val axisThickness: Float
    val tickThickness: Float
    val guidelineThickness: Float
    val tickLength: Float
    val restrictedBounds: List<RectF>

    public val maxAnyAxisLineThickness: Float
        get() = maxOf(axisThickness, tickThickness, guidelineThickness)

    public val labelLineHeight: Int
        get() = label?.lineHeight ?: 0

    public val labelAllLinesHeight: Int
        get() = label?.allLinesHeight ?: 0

    var label: TextComponent?
    var axis: LineComponent?
    var tick: TickComponent?
    var guideline: LineComponent?
    var isLTR: Boolean
    var valueFormatter: AxisValueFormatter

    fun draw(
        canvas: Canvas,
        model: EntryModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    ) {
        drawBehindDataSet(canvas, model, dataSetModel, segmentProperties, rendererViewState)
    }

    fun drawBehindDataSet(
        canvas: Canvas,
        model: EntryModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    )

    fun drawAboveDataSet(
        canvas: Canvas,
        model: EntryModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    )

    fun setDataSetBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    )

    fun setDataSetBounds(bounds: RectF) =
        setDataSetBounds(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        )

    fun getDesiredWidth(
        labels: List<String>,
    ): Float

    fun setRestrictedBounds(vararg bounds: RectF?)

    fun getDesiredHeight(): Int

}