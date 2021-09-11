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

package pl.patrykgoworowski.liftchart_common.axis.horizontal

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.DEF_AXIS_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_GUIDELINE_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_LABEL_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_TICK_COMPONENT
import pl.patrykgoworowski.liftchart_common.axis.AxisPosition
import pl.patrykgoworowski.liftchart_common.axis.BaseLabeledAxisRenderer
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.component.text.VerticalPosition
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero
import kotlin.math.ceil

class HorizontalAxis<Position : AxisPosition.Horizontal>(
    override val position: Position,
    label: TextComponent?,
    axis: LineComponent?,
    tick: TickComponent?,
    guideline: LineComponent?,
    override var valueFormatter: AxisValueFormatter,
) : BaseLabeledAxisRenderer<Position>(label, axis, tick, guideline) {

    private val AxisPosition.Horizontal.textVerticalPosition: VerticalPosition
        get() = if (isBottom) VerticalPosition.Top else VerticalPosition.Bottom

    var tickType: TickType = TickType.Minor

    override fun drawBehindDataSet(
        canvas: Canvas,
        model: EntryModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    ) {
        val scrollX = rendererViewState.horizontalScroll
        val clipRestoreCount = canvas.save()

        canvas.clipRect(
            bounds.left - if (tickType == TickType.Minor) tickThickness.half else 0f,
            minOf(bounds.top, dataSetBounds.top),
            bounds.right + if (tickType == TickType.Minor) tickThickness.half else 0f,
            maxOf(bounds.bottom, dataSetBounds.bottom)
        )

        val entryLength = getEntryLength(segmentProperties.segmentWidth)
        val tickCount = tickType.getTickCount(entryLength)
        val tickDrawStep = segmentProperties.segmentWidth
        val scrollAdjustment = (scrollX / tickDrawStep).toInt()
        var textDrawCenter =
            bounds.left + tickDrawStep.half - scrollX + (tickDrawStep * scrollAdjustment)
        var tickDrawCenter =
            tickType.getTickDrawCenter(scrollX, tickDrawStep, scrollAdjustment, textDrawCenter)

        val guidelineTop = dataSetBounds.top
        val guidelineBottom = dataSetBounds.bottom

        for (index in 0 until tickCount) {
            guideline?.run {
                setParentBounds(bounds)
                takeIf {
                    it.fitsInVertical(
                        guidelineTop,
                        guidelineBottom,
                        tickDrawCenter,
                        dataSetBounds
                    )
                }?.drawVertical(
                    canvas = canvas,
                    top = guidelineTop,
                    bottom = guidelineBottom,
                    centerX = tickDrawCenter
                )
            }

            tickDrawCenter += tickDrawStep
            textDrawCenter += tickDrawStep
        }

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

    override fun drawAboveDataSet(
        canvas: Canvas,
        model: EntryModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState
    ) {
        val tickMarkTop = if (position.isBottom) bounds.top else bounds.bottom - tickLength
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength
        val scrollX = rendererViewState.horizontalScroll
        val clipRestoreCount = canvas.save()

        canvas.clipRect(
            bounds.left - if (tickType == TickType.Minor) tickThickness.half else 0f,
            minOf(bounds.top, dataSetBounds.top),
            bounds.right + if (tickType == TickType.Minor) tickThickness.half else 0f,
            maxOf(bounds.bottom, dataSetBounds.bottom)
        )

        val entryLength = getEntryLength(segmentProperties.segmentWidth)
        val tickCount = tickType.getTickCount(entryLength)
        val tickDrawStep = segmentProperties.segmentWidth
        val scrollAdjustment = (scrollX / tickDrawStep).toInt()
        var textDrawCenter =
            bounds.left + tickDrawStep.half - scrollX + (tickDrawStep * scrollAdjustment)
        var tickDrawCenter =
            tickType.getTickDrawCenter(scrollX, tickDrawStep, scrollAdjustment, textDrawCenter)

        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop

        var valueIndex: Float = dataSetModel.minX + scrollAdjustment * model.step

        for (index in 0 until tickCount) {
            tick?.run {
                setParentBounds(bounds)
                drawVertical(
                    canvas = canvas,
                    top = tickMarkTop,
                    bottom = tickMarkBottom,
                    centerX = tickDrawCenter
                )
            }

            if (index < entryLength) {
                label?.run {
                    background?.setParentBounds(bounds)
                    drawText(
                        canvas,
                        valueFormatter.formatValue(valueIndex, index, model, dataSetModel),
                        textDrawCenter,
                        textY,
                        verticalPosition = position.textVerticalPosition,
                        width = tickDrawStep.toInt(),
                    )
                }

                valueIndex += model.step
            }
            tickDrawCenter += tickDrawStep
            textDrawCenter += tickDrawStep
        }

        axis?.run {
            setParentBounds(bounds)
            drawHorizontal(
                canvas = canvas,
                left = dataSetBounds.left,
                right = dataSetBounds.right,
                centerY = if (position is AxisPosition.Horizontal.Bottom) {
                    bounds.top + axis?.thickness?.half.orZero
                } else {
                    bounds.bottom + axis?.thickness?.half.orZero
                }
            )
        }

        label?.clearLayoutCache()

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

    private fun getEntryLength(segmentWidth: Float) =
        ceil(bounds.width() / segmentWidth).toInt() + 1

    private fun TickType.getTickCount(entryLength: Int) = when (this) {
        TickType.Minor -> entryLength + 1
        TickType.Major -> entryLength
    }

    private fun TickType.getTickDrawCenter(
        scrollX: Float,
        tickDrawStep: Float,
        scrollAdjustment: Int,
        textDrawCenter: Float,
    ) = when (this) {
        TickType.Minor -> bounds.left - scrollX + (tickDrawStep * scrollAdjustment)
        TickType.Major -> textDrawCenter
    }

    override fun getVerticalInsets(
        outDimensions: MutableDimensions,
        model: EntryModel,
        dataSetModel: DataSetModel
    ): Dimensions =
        outDimensions.apply {
            setHorizontal(
                if (tickType == TickType.Minor) tick?.thickness?.half.orZero
                else 0f
            )
            top = if (position.isTop) getDesiredHeight().toFloat() else 0f
            bottom = if (position.isBottom) getDesiredHeight().toFloat() else 0f
        }

    override fun getHorizontalInsets(
        outDimensions: MutableDimensions,
        availableHeight: Float,
        model: EntryModel,
        dataSetModel: DataSetModel
    ): Dimensions = outDimensions

    override fun getDesiredHeight() =
        ((if (position.isBottom) axisThickness else 0f)
                + tickLength
                + label?.getHeight().orZero
                ).toInt()

    override fun getDesiredWidth(
        labels: List<String>
    ): Float = 0f

    enum class TickType {
        Minor, Major
    }
}

fun topAxis(
    label: TextComponent? = DEF_LABEL_COMPONENT,
    axis: LineComponent? = DEF_AXIS_COMPONENT,
    tick: TickComponent? = DEF_TICK_COMPONENT,
    guideline: LineComponent? = DEF_GUIDELINE_COMPONENT,
    valueFormatter: AxisValueFormatter = DecimalFormatAxisValueFormatter(),
): HorizontalAxis<AxisPosition.Horizontal.Top> = HorizontalAxis(
    position = AxisPosition.Horizontal.Top,
    label = label,
    axis = axis,
    tick = tick,
    guideline = guideline,
    valueFormatter = valueFormatter,
)

fun bottomAxis(
    label: TextComponent? = DEF_LABEL_COMPONENT,
    axis: LineComponent? = DEF_AXIS_COMPONENT,
    tick: TickComponent? = DEF_TICK_COMPONENT,
    guideline: LineComponent? = DEF_GUIDELINE_COMPONENT,
    valueFormatter: AxisValueFormatter = DecimalFormatAxisValueFormatter(),
): HorizontalAxis<AxisPosition.Horizontal.Bottom> = HorizontalAxis(
    position = AxisPosition.Horizontal.Bottom,
    label = label,
    axis = axis,
    tick = tick,
    guideline = guideline,
    valueFormatter = valueFormatter,
)