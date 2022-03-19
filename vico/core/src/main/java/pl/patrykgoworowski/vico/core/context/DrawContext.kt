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

package pl.patrykgoworowski.vico.core.context

import android.graphics.Canvas

/**
 * [DrawContext] is an extension of [MeasureContext] storing [Canvas] and other properties.
 * It also defines helpful drawing functions.
 */
public interface DrawContext : MeasureContext {

    /**
     * The color of elevation overlay, used with shadow layer in
     * [pl.patrykgoworowski.vico.core.component.shape.ShapeComponent].
     */
    public val elevationOverlayColor: Long

    /**
     * The canvas to draw the chart on.
     */
    public val canvas: Canvas

    /**
     * Saves the [Canvas] state.
     *
     * @see Canvas.save
     */
    public fun saveCanvas(): Int = canvas.save()

    /**
     * Temporarily swaps the [Canvas] and yields [DrawContext] as [block]’s receiver.
     */
    public fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit)

    /**
     * Clips the [Canvas] to the specified rectangle.
     *
     * @see Canvas.clipRect
     */
    public fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        canvas.clipRect(left, top, right, bottom)
    }

    /**
     * Restores the [Canvas] state.
     *
     * @see Canvas.restore
     */
    public fun restoreCanvas() {
        canvas.restore()
    }

    /**
     * Restores the [Canvas] state to given [count].
     *
     * @see Canvas.restoreToCount
     */
    public fun restoreCanvasToCount(count: Int) {
        canvas.restoreToCount(count)
    }
}
