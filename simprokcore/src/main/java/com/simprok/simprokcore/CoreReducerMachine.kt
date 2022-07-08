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

internal class CoreReducerMachine<State, Event>(
    private val reducer: BiMapper<State?, Event, ReducerResult<State>>
) : ParentMachine<Event, Event> {

    override val child: Machine<Event, Event>
        get() = CoreClassicMachine(
            CoreClassicResult.set<State?, Event>(null)
        ) { state, event ->
            when (val result = reducer(state, event)) {
                is ReducerResult.Set<State> -> CoreClassicResult.set(result.value, event)
                is ReducerResult.Skip<State> -> CoreClassicResult.set(state)
            }
        }
}