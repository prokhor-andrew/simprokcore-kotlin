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
interface Core<State> : RootMachine<StateAction<State>, StateAction<State>> {

    /**
     * Application's layers that receive the latest state and handle it via their
     * mappers as well as emit events that are handled by their reducers.
     */
    val layers: Set<Layer<State>>

    override val child: Machine<StateAction<State>, StateAction<State>>
        get() {
            val reducer: Machine<StateAction<State>, StateAction<State>> =
                CoreReducerMachine<Mapper<State?, ReducerResult<State>>, State> { state, event ->
                    event(state)
                }.inward<State, StateAction<State>, Mapper<State?, ReducerResult<State>>> {
                    when (it) {
                        is StateAction.WillUpdate<State> -> Ward.set(it.mapper)
                        is StateAction.DidUpdate<State> -> Ward.set()
                    }
                }.outward {
                    Ward.set(StateAction.DidUpdate(it))
                }

            return mergeList(layers.map { it.child }.plus(reducer)).redirect {
                Direction.Back(it)
            }
        }
}