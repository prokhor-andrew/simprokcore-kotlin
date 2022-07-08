//
//  Core.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

import com.simprok.simprokmachine.api.*
import com.simprok.simprokmachine.machines.Machine

/**
 * A RootMachine interface that describes all the layers of the application.
 */
interface Core<State, Event> : RootMachine<Event, Event> {

    /**
     * Application's layers that receive the latest state and handle it via their
     * mappers as well as emit events that are handled by their reducers.
     */
    val layers: Set<Layer<Event>>

    fun reduce(state: State?, event: Event): ReducerResult<State>

    override val child: Machine<Event, Event>
        get() {
            val reducer: Machine<StateAction<Event>, StateAction<Event>> =
                CoreReducerMachine<State, Event> { state, event ->
                    reduce(state, event)
                }.outward {
                    Ward.set<StateAction<Event>>(StateAction.DidUpdate(it))
                }.inward {
                    when (it) {
                        is StateAction.WillUpdate<Event> -> Ward.set(it.event)
                        is StateAction.DidUpdate<Event> -> Ward.set()
                    }
                }

            val merged = mergeList(layers.map { it.child }.plus(reducer)).redirect {
                Direction.Back(it)
            }

            return merged.outward { Ward.set<Event>() }.inward { Ward.set() }
        }
}