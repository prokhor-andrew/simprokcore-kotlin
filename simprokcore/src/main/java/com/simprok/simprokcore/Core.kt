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
interface Core<State, Event> : RootMachine<StateAction<State, Event>, StateAction<State, Event>> {

    /**
     * Application's layers that receive the latest state and handle it via their
     * mappers as well as emit events that are handled by their reducers.
     */
    val layers: Set<Layer<State, Event>>

    fun reduce(state: State?, event: Event): ReducerResult<State>

    override val child: Machine<StateAction<State, Event>, StateAction<State, Event>>
        get() {
            val reducer: Machine<StateAction<State, Event>, StateAction<State, Event>> =
                CoreReducerMachine<Event, State> { state, event ->
                    reduce(state, event)
                }.inward<State, StateAction<State, Event>, Event> {
                    when (it) {
                        is StateAction.WillUpdate<State, Event> -> Ward.set(it.event)
                        is StateAction.DidUpdate<State, Event> -> Ward.set()
                    }
                }.outward {
                    Ward.set(StateAction.DidUpdate(it))
                }

            return mergeList(layers.map { it.child }.plus(reducer)).redirect {
                Direction.Back(it)
            }
        }
}