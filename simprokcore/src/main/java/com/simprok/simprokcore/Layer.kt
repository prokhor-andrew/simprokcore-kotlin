//
//  Layer.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

import com.simprok.simprokmachine.api.Mapper
import com.simprok.simprokmachine.api.Ward
import com.simprok.simprokmachine.api.inward
import com.simprok.simprokmachine.api.outward
import com.simprok.simprokmachine.machines.Machine

/**
 * A general interface that describes a type that represents a layer object.
 */
sealed interface Layer<GlobalEvent, GlobalState> {

    val child: Machine<StateAction<GlobalEvent, GlobalState>, StateAction<GlobalEvent, GlobalState>>
}

interface MachineLayerType<GlobalEvent, GlobalState, Event, State> :
    Layer<GlobalEvent, GlobalState> {

    override val child: Machine<StateAction<GlobalEvent, GlobalState>, StateAction<GlobalEvent, GlobalState>>
        get() = machine.outward<State, Event, StateAction<GlobalEvent, GlobalState>> {
            Ward.set(
                StateAction.WillUpdate(mapEvent(it))
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalEvent, GlobalState> -> Ward.set()
                is StateAction.DidUpdate<GlobalEvent, GlobalState> -> Ward.set(
                    mapState(it.state)
                )
            }
        }

    val machine: Machine<State, Event>

    fun mapState(state: GlobalState): State

    fun mapEvent(event: Event): GlobalEvent
}

data class MachineLayerObject<GlobalEvent, GlobalState, Event, State>(
    override val machine: Machine<State, Event>,
    private val stateMapper: Mapper<GlobalState, State>,
    private val eventMapper: Mapper<Event, GlobalEvent>
) : MachineLayerType<GlobalEvent, GlobalState, Event, State> {

    override fun mapState(state: GlobalState): State = stateMapper(state)

    override fun mapEvent(event: Event): GlobalEvent = eventMapper(event)
}

interface ConsumerLayerType<GlobalEvent, GlobalState, Event, State> :
    Layer<GlobalEvent, GlobalState> {

    override val child: Machine<StateAction<GlobalEvent, GlobalState>, StateAction<GlobalEvent, GlobalState>>
        get() = machine.outward<State, Event, StateAction<GlobalEvent, GlobalState>> {
            Ward.set()
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalEvent, GlobalState> -> Ward.set()
                is StateAction.DidUpdate<GlobalEvent, GlobalState> -> Ward.set(
                    map(it.state)
                )
            }
        }

    val machine: Machine<State, Event>

    fun map(state: GlobalState): State
}

data class ConsumerLayerObject<GlobalEvent, GlobalState, Event, State>(
    override val machine: Machine<State, Event>,
    private val mapper: Mapper<GlobalState, State>
) : ConsumerLayerType<GlobalEvent, GlobalState, Event, State> {

    override fun map(state: GlobalState): State = mapper(state)
}


interface ProducerLayerType<GlobalEvent, GlobalState, Event, State> :
    Layer<GlobalEvent, GlobalState> {

    override val child: Machine<StateAction<GlobalEvent, GlobalState>, StateAction<GlobalEvent, GlobalState>>
        get() = machine.outward<State, Event, StateAction<GlobalEvent, GlobalState>> {
            Ward.set(
                StateAction.WillUpdate(map(it))
            )
        }.inward {
            Ward.set()
        }

    val machine: Machine<State, Event>

    fun map(event: Event): GlobalEvent
}


data class ProducerLayerObject<GlobalEvent, GlobalState, Event, State>(
    override val machine: Machine<State, Event>,
    private val mapper: Mapper<Event, GlobalEvent>
) : ProducerLayerType<GlobalEvent, GlobalState, Event, State> {

    override fun map(event: Event): GlobalEvent = mapper(event)
}


interface MapEventLayerType<GlobalEvent, GlobalState, Event> : Layer<GlobalEvent, GlobalState> {

    override val child: Machine<StateAction<GlobalEvent, GlobalState>, StateAction<GlobalEvent, GlobalState>>
        get() = machine.outward<GlobalState, Event, StateAction<GlobalEvent, GlobalState>> {
            Ward.set(
                StateAction.WillUpdate(map(it))
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalEvent, GlobalState> -> Ward.set()
                is StateAction.DidUpdate<GlobalEvent, GlobalState> -> Ward.set(it.state)
            }
        }

    val machine: Machine<GlobalState, Event>

    fun map(event: Event): GlobalEvent
}

data class MapEventLayerObject<GlobalEvent, GlobalState, Event>(
    override val machine: Machine<GlobalState, Event>,
    private val mapper: Mapper<Event, GlobalEvent>
) : MapEventLayerType<GlobalEvent, GlobalState, Event> {

    override fun map(event: Event): GlobalEvent = mapper(event)
}

interface MapStateLayerType<GlobalEvent, GlobalState, State> : Layer<GlobalEvent, GlobalState> {

    override val child: Machine<StateAction<GlobalEvent, GlobalState>, StateAction<GlobalEvent, GlobalState>>
        get() = machine.outward<State, GlobalEvent, StateAction<GlobalEvent, GlobalState>> {
            Ward.set(
                StateAction.WillUpdate(it)
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalEvent, GlobalState> -> Ward.set()
                is StateAction.DidUpdate<GlobalEvent, GlobalState> -> Ward.set(
                    map(it.state)
                )
            }
        }

    val machine: Machine<State, GlobalEvent>

    fun map(state: GlobalState): State
}

data class MapStateLayerObject<GlobalEvent, GlobalState, State>(
    override val machine: Machine<State, GlobalEvent>,
    private val mapper: Mapper<GlobalState, State>
) : MapStateLayerType<GlobalEvent, GlobalState, State> {

    override fun map(state: GlobalState): State = mapper(state)
}

interface NoMapLayerType<GlobalEvent, GlobalState> : Layer<GlobalEvent, GlobalState> {

    override val child: Machine<StateAction<GlobalEvent, GlobalState>, StateAction<GlobalEvent, GlobalState>>
        get() = machine.outward<GlobalState, GlobalEvent, StateAction<GlobalEvent, GlobalState>> {
            Ward.set(
                StateAction.WillUpdate(it)
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalEvent, GlobalState> -> Ward.set()
                is StateAction.DidUpdate<GlobalEvent, GlobalState> -> Ward.set(it.state)
            }
        }

    val machine: Machine<GlobalState, GlobalEvent>
}

data class NoMapLayerObject<GlobalEvent, GlobalState>(
    override val machine: Machine<GlobalState, GlobalEvent>
) : NoMapLayerType<GlobalEvent, GlobalState>