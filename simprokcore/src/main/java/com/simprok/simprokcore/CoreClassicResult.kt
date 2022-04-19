//
//  CoreClassicResult.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

internal class CoreClassicResult<State, Output> private constructor(
    val state: State,
    val outputs: List<Output>
) {
    companion object {

        fun <State, Output> set(
            state: State,
            vararg outputs: Output
        ): CoreClassicResult<State, Output> = CoreClassicResult(state, outputs.toList())

        fun <State, Output> set(
            state: State,
            outputs: List<Output>
        ): CoreClassicResult<State, Output> = CoreClassicResult(state, outputs)
    }
}