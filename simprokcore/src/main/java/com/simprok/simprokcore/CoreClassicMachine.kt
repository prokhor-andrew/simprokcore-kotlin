//
//  CoreClassicMachine.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

import com.simprok.simprokmachine.api.BiMapper
import com.simprok.simprokmachine.api.Handler
import com.simprok.simprokmachine.machines.ChildMachine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO

internal class CoreClassicMachine<State, Input, Output>(
    initial: CoreClassicResult<State, Output>,
    override val dispatcher: CoroutineDispatcher = IO,
    private val reducer: BiMapper<State, Input, CoreClassicResult<State, Output>>
) : ChildMachine<Input, Output> {

    private var state: CoreClassicResult<State, Output> = initial

    override suspend fun process(input: Input?, callback: Handler<Output>) {
        if (input != null) {
            state = reducer(state.state, input)
        }
        state.outputs.forEach { callback(it) }
    }
}