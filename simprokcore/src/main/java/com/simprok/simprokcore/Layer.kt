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
sealed interface Layer<GlobalState, GlobalEvent> {

    val child: Machine<StateAction<GlobalState, GlobalEvent>, StateAction<GlobalState, GlobalEvent>>
}

interface MachineLayerType<GlobalState, GlobalEvent, State, Event> :
    Layer<GlobalState, GlobalEvent> {

    override val child: Machine<StateAction<GlobalState, GlobalEvent>, StateAction<GlobalState, GlobalEvent>>
        get() = machine.outward<State, Event, StateAction<GlobalState, GlobalEvent>> {
            Ward.set(
                StateAction.WillUpdate(mapEvent(it))
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalState, GlobalEvent> -> Ward.set()
                is StateAction.DidUpdate<GlobalState, GlobalEvent> -> Ward.set(
                    mapState(it.state)
                )
            }
        }

    val machine: Machine<State, Event>

    fun mapState(state: GlobalState): State

    fun mapEvent(event: Event): GlobalEvent
}

data class MachineLayerObject<GlobalState, GlobalEvent, State, Event>(
    override val machine: Machine<State, Event>,
    private val stateMapper: Mapper<GlobalState, State>,
    private val eventMapper: Mapper<Event, GlobalEvent>
) : MachineLayerType<GlobalState, GlobalEvent, State, Event> {

    override fun mapState(state: GlobalState): State = stateMapper(state)

    override fun mapEvent(event: Event): GlobalEvent = eventMapper(event)
}

interface ConsumerLayerType<GlobalState, GlobalEvent, State, Event> :
    Layer<GlobalState, GlobalEvent> {

    override val child: Machine<StateAction<GlobalState, GlobalEvent>, StateAction<GlobalState, GlobalEvent>>
        get() = machine.outward<State, Event, StateAction<GlobalState, GlobalEvent>> {
            Ward.set()
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalState, GlobalEvent> -> Ward.set()
                is StateAction.DidUpdate<GlobalState, GlobalEvent> -> Ward.set(
                    map(it.state)
                )
            }
        }

    val machine: Machine<State, Event>

    fun map(state: GlobalState): State
}

data class ConsumerLayerObject<GlobalState, GlobalEvent, State, Event>(
    override val machine: Machine<State, Event>,
    private val mapper: Mapper<GlobalState, State>
) : ConsumerLayerType<GlobalState, GlobalEvent, State, Event> {

    override fun map(state: GlobalState): State = mapper(state)
}


interface ProducerLayerType<GlobalState, GlobalEvent, State, Event> :
    Layer<GlobalState, GlobalEvent> {

    override val child: Machine<StateAction<GlobalState, GlobalEvent>, StateAction<GlobalState, GlobalEvent>>
        get() = machine.outward<State, Event, StateAction<GlobalState, GlobalEvent>> {
            Ward.set(
                StateAction.WillUpdate(map(it))
            )
        }.inward {
            Ward.set()
        }

    val machine: Machine<State, Event>

    fun map(event: Event): GlobalEvent
}


data class ProducerLayerObject<GlobalState, GlobalEvent, State, Event>(
    override val machine: Machine<State, Event>,
    private val mapper: Mapper<Event, GlobalEvent>
) : ProducerLayerType<GlobalState, GlobalEvent, State, Event> {

    override fun map(event: Event): GlobalEvent = mapper(event)
}


interface MapEventLayerType<GlobalState, GlobalEvent, Event> : Layer<GlobalState, GlobalEvent> {

    override val child: Machine<StateAction<GlobalState, GlobalEvent>, StateAction<GlobalState, GlobalEvent>>
        get() = machine.outward<GlobalState, Event, StateAction<GlobalState, GlobalEvent>> {
            Ward.set(
                StateAction.WillUpdate(map(it))
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalState, GlobalEvent> -> Ward.set()
                is StateAction.DidUpdate<GlobalState, GlobalEvent> -> Ward.set(it.state)
            }
        }

    val machine: Machine<GlobalState, Event>

    fun map(event: Event): GlobalEvent
}

data class MapEventLayerObject<GlobalState, GlobalEvent, Event>(
    override val machine: Machine<GlobalState, Event>,
    private val mapper: Mapper<Event, GlobalEvent>
) : MapEventLayerType<GlobalState, GlobalEvent, Event> {

    override fun map(event: Event): GlobalEvent = mapper(event)
}

interface MapStateLayerType<GlobalState, GlobalEvent, State> : Layer<GlobalState, GlobalEvent> {

    override val child: Machine<StateAction<GlobalState, GlobalEvent>, StateAction<GlobalState, GlobalEvent>>
        get() = machine.outward<State, GlobalEvent, StateAction<GlobalState, GlobalEvent>> {
            Ward.set(
                StateAction.WillUpdate(it)
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalState, GlobalEvent> -> Ward.set()
                is StateAction.DidUpdate<GlobalState, GlobalEvent> -> Ward.set(
                    map(it.state)
                )
            }
        }

    val machine: Machine<State, GlobalEvent>

    fun map(state: GlobalState): State
}

data class MapStateLayerObject<GlobalState, GlobalEvent, State>(
    override val machine: Machine<State, GlobalEvent>,
    private val mapper: Mapper<GlobalState, State>
) : MapStateLayerType<GlobalState, GlobalEvent, State> {

    override fun map(state: GlobalState): State = mapper(state)
}

interface NoMapLayerType<GlobalState, GlobalEvent> : Layer<GlobalState, GlobalEvent> {

    override val child: Machine<StateAction<GlobalState, GlobalEvent>, StateAction<GlobalState, GlobalEvent>>
        get() = machine.outward<GlobalState, GlobalEvent, StateAction<GlobalState, GlobalEvent>> {
            Ward.set(
                StateAction.WillUpdate(it)
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<GlobalState, GlobalEvent> -> Ward.set()
                is StateAction.DidUpdate<GlobalState, GlobalEvent> -> Ward.set(it.state)
            }
        }

    val machine: Machine<GlobalState, GlobalEvent>
}

data class NoMapLayerObject<GlobalState, GlobalEvent>(
    override val machine: Machine<GlobalState, GlobalEvent>
) : NoMapLayerType<GlobalState, GlobalEvent>