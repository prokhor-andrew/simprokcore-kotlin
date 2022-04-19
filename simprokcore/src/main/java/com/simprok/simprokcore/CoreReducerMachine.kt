//
//  CoreReducerMachine.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

import com.simprok.simprokmachine.api.BiMapper
import com.simprok.simprokmachine.machines.Machine
import com.simprok.simprokmachine.machines.ParentMachine

internal class CoreReducerMachine<Event, State>(
    private val reducer: BiMapper<State?, Event, ReducerResult<State>>
) : ParentMachine<Event, State> {

    override val child: Machine<Event, State>
        get() = CoreClassicMachine(
            CoreClassicResult.set<State?, State>(null)
        ) { state, event ->
            when (val result = reducer(state, event)) {
                is ReducerResult.Set<State> -> CoreClassicResult.set(result.value, result.value)
                is ReducerResult.Skip<State> -> CoreClassicResult.set(state)
            }
        }
}