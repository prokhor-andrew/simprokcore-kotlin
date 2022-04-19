//
//  Layer.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

import com.simprok.simprokmachine.api.*
import com.simprok.simprokmachine.machines.Machine

/**
 * A general interface that describes a type that represents a layer object.
 */
sealed interface Layer<GlobalState> {

    val child: Machine<StateAction<GlobalState>, StateAction<GlobalState>>
}

/**
 * A Layer interface.
 * Contains a machine that receives mapped layer state as
 * input and emits output that is reduced into application's state.
 */
interface MachineLayerType<GlobalState, State, Event> : Layer<GlobalState> {

    /**
     * A machine that receives mapped state as input and
     * emits output that is reduced into application's state.
     */
    val machine: Machine<State, Event>

    /**
     * A mapper that maps application's state into layer state and sends it into machine as input.
     */
    fun map(state: GlobalState): State

    /**
     * A reducer that receives machine's event as output and reduces it into application's state.
     */
    fun reduce(state: GlobalState?, event: Event): ReducerResult<GlobalState>

    override val child: Machine<StateAction<GlobalState>, StateAction<GlobalState>>
        get() = getChild(
            machine,
            false,
            { map(it) },
            { state, event -> reduce(state, event) }
        )
}

/**
 * A Layer class.
 * Contains a machine that receives mapped layer state as
 * input and emits output that is reduced into application's state.
 */
class MachineLayerObject<GlobalState, State, Event>(
    private val machine: Machine<State, Event>,
    private val mapper: Mapper<GlobalState, State>,
    private val reducer: BiMapper<GlobalState?, Event, ReducerResult<GlobalState>>
) : Layer<GlobalState> {

    override val child: Machine<StateAction<GlobalState>, StateAction<GlobalState>>
        get() = getChild(machine, false, mapper, reducer)
}

/**
 * A Layer interface.
 * Contains a machine that receives mapped layer state as input and
 * *does not* emit output that is reduced into application's state.
 */
interface ConsumerLayerType<GlobalState, State, Output> : Layer<GlobalState> {

    /**
     * A machine that receives mapped state as input and
     * *does not* emit output that is reduced into application's state.
     */
    val machine: Machine<State, Output>

    /**
     * A mapper that maps application's state into layer state and sends it into machine as input.
     */
    fun map(state: GlobalState): State

    override val child: Machine<StateAction<GlobalState>, StateAction<GlobalState>>
        get() = getChild(
            machine,
            false,
            { map(it) },
            { _, _ -> ReducerResult.Skip() }
        )
}

/**
 * A Layer class.
 * Contains a machine that receives mapped layer state as input and
 * *does not* emit output that is reduced into application's state.
 */
class ConsumerLayerObject<GlobalState, State, Output>(
    override val machine: Machine<State, Output>,
    private val mapper: Mapper<GlobalState, State>
) : ConsumerLayerType<GlobalState, State, Output> {

    override fun map(state: GlobalState): State = mapper(state)
}

/**
 * A Layer interface.
 * Contains a machine that *does not* receive mapped layer state
 * as input and emits output that is reduced into application's state.
 */
interface ProducerLayerType<GlobalState, Event, Input> : Layer<GlobalState> {

    /**
     * A machine that *does not* receive mapped state as input
     * and emits output that is reduced into application's state.
     */
    val machine: Machine<Input, Event>

    /**
     * A reducer that receives machine's event as output and reduces it into application's state.
     */
    fun reduce(state: GlobalState?, event: Event): ReducerResult<GlobalState>

    override val child: Machine<StateAction<GlobalState>, StateAction<GlobalState>>
        get() = getChild(
            machine,
            true,
            { throw Exception("Must never be reached") },
            { state, event -> reduce(state, event) }
        )
}

/**
 * A Layer class.
 * Contains a machine that *does not* receive mapped layer state
 * as input and emits output that is reduced into application's state.
 */
class ProducerLayerObject<GlobalState, Event, Input>(
    override val machine: Machine<Input, Event>,
    private val reducer: BiMapper<GlobalState?, Event, ReducerResult<GlobalState>>
) : ProducerLayerType<GlobalState, Event, Input> {

    override fun reduce(state: GlobalState?, event: Event): ReducerResult<GlobalState> =
        reducer(state, event)
}

private fun <GlobalState, State, Event> getChild(
    machine: Machine<State, Event>,
    shouldIgnoreNewState: Boolean,
    mapper: Mapper<GlobalState, State>,
    reducer: BiMapper<GlobalState?, Event, ReducerResult<GlobalState>>
): Machine<StateAction<GlobalState>, StateAction<GlobalState>> =
    machine.inward<Event, StateAction<GlobalState>, State> {
        if (shouldIgnoreNewState) {
            Ward.set()
        } else {
            when (it) {
                is StateAction.WillUpdate<GlobalState> -> Ward.set()
                is StateAction.DidUpdate<GlobalState> -> Ward.set(mapper(it.state))
            }
        }
    }.outward { event ->
        Ward.set(StateAction.WillUpdate { state -> reducer(state, event) })
    }