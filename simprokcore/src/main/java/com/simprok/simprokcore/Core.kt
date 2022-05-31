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
interface Core<Event, State> : RootMachine<StateAction<Event, State>, StateAction<Event, State>> {

    /**
     * Application's layers that receive the latest state and handle it via their
     * mappers as well as emit events that are handled by their reducers.
     */
    val layers: Set<Layer<Event, State>>

    fun reduce(state: State?, event: Event): ReducerResult<State>

    override val child: Machine<StateAction<Event, State>, StateAction<Event, State>>
        get() {
            val reducer: Machine<StateAction<Event, State>, StateAction<Event, State>> =
                CoreReducerMachine<Event, State> { state, event ->
                    reduce(state, event)
                }.inward<State, StateAction<Event, State>, Event> {
                    when (it) {
                        is StateAction.WillUpdate<Event, State> -> Ward.set(it.event)
                        is StateAction.DidUpdate<Event, State> -> Ward.set()
                    }
                }.outward {
                    Ward.set(StateAction.DidUpdate(it))
                }

            return mergeList(layers.map { it.child }.plus(reducer)).redirect {
                Direction.Back(it)
            }
        }
}